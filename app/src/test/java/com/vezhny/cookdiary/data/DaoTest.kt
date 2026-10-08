package com.vezhny.cookdiary.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
// Plain Application: the real one starts Koin, which can't be started twice across tests.
@Config(sdk = [35], application = Application::class)
class DaoTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: CookRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        repository = CookRepository(db.dishDao(), db.cookEventDao(), clock = { 0L })
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `insert, update and delete dish`() = runTest {
        val id = repository.saveDish(Dish(name = "Борщ", tags = "суп"))
        assertEquals("Борщ", repository.getDish(id)?.name)

        repository.saveDish(Dish(id = id, name = "Борщ красный", tags = "суп"))
        assertEquals("Борщ красный", repository.getDish(id)?.name)

        repository.deleteDish(repository.getDish(id)!!)
        assertNull(repository.getDish(id))
    }

    @Test
    fun `last cooked is the max event time, null if never cooked`() = runTest {
        val soup = repository.saveDish(Dish(name = "Суп"))
        val omelette = repository.saveDish(Dish(name = "Омлет"))
        repository.markCooked(soup, at = 100)
        repository.markCooked(soup, at = 300)
        repository.markCooked(soup, at = 200)

        val byId = repository.observeDishesWithLastCooked().first().associateBy { it.dish.id }
        assertEquals(300L, byId[soup]?.lastCookedAt)
        assertNull(byId[omelette]?.lastCookedAt)
    }

    @Test
    fun `dishes with last cooked are coldest first`() = runTest {
        val hot = repository.saveDish(Dish(name = "Горячее"))
        val warm = repository.saveDish(Dish(name = "Тёплое"))
        repository.saveDish(Dish(name = "b никогда"))
        repository.saveDish(Dish(name = "A никогда"))
        repository.markCooked(hot, at = 300)
        repository.markCooked(warm, at = 100)

        assertEquals(
            listOf("A никогда", "b никогда", "Тёплое", "Горячее"),
            repository.observeDishesWithLastCooked().first().map { it.dish.name },
        )
    }

    @Test
    fun `dishes are sorted by name ignoring case`() = runTest {
        repository.saveDish(Dish(name = "b"))
        repository.saveDish(Dish(name = "A"))
        repository.saveDish(Dish(name = "c"))

        assertEquals(listOf("A", "b", "c"), repository.observeDishes().first().map { it.name })
    }

    @Test
    fun `history is newest first with dish names`() = runTest {
        val soup = repository.saveDish(Dish(name = "Суп"))
        val pasta = repository.saveDish(Dish(name = "Паста"))
        repository.markCooked(soup, at = 100)
        repository.markCooked(pasta, at = 200)

        val history = repository.observeHistory().first()
        assertEquals(listOf("Паста", "Суп"), history.map { it.dishName })
        assertEquals(listOf(200L, 100L), history.map { it.cookedAt })
    }

    @Test
    fun `deleting a dish removes its history`() = runTest {
        val soup = repository.saveDish(Dish(name = "Суп"))
        repository.markCooked(soup, at = 100)

        repository.deleteDish(repository.getDish(soup)!!)

        assertEquals(emptyList<CookEventWithDish>(), repository.observeHistory().first())
    }

    @Test
    fun `deleting a cook event keeps the dish`() = runTest {
        val soup = repository.saveDish(Dish(name = "Суп"))
        val event = repository.markCooked(soup, at = 100)

        repository.deleteCookEvent(event)

        assertEquals(emptyList<CookEventWithDish>(), repository.observeHistory().first())
        assertNull(repository.observeDishesWithLastCooked().first().single().lastCookedAt)
    }
}
