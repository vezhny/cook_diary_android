package com.vezhny.cookdiary.data

import kotlinx.coroutines.flow.Flow

class CookRepository(
    private val dishDao: DishDao,
    private val cookEventDao: CookEventDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeDishes(): Flow<List<Dish>> = dishDao.observeAll()

    fun observeHistory(): Flow<List<CookEventWithDish>> = cookEventDao.observeHistory()

    fun observeDishesWithLastCooked(): Flow<List<DishWithLastCooked>> = dishDao.observeAllWithLastCooked()

    suspend fun getDish(id: Long): Dish? = dishDao.getById(id)

    /** Inserts a new dish when [Dish.id] is 0, otherwise updates it. */
    suspend fun saveDish(dish: Dish): Long =
        if (dish.id == 0L) dishDao.insert(dish) else dish.id.also { dishDao.update(dish) }

    suspend fun deleteDish(dish: Dish) = dishDao.delete(dish)

    suspend fun getAllDishes(): List<Dish> = dishDao.getAll()

    /**
     * Adds [dishes] as new rows, skipping those whose name (ignoring case) already exists
     * or repeats within [dishes]. Returns how many were added.
     */
    suspend fun importDishes(dishes: List<Dish>): Int {
        val seen = dishDao.getAll().mapTo(HashSet()) { it.name.trim().lowercase() }
        val fresh = dishes.filter { seen.add(it.name.trim().lowercase()) }.map { it.copy(id = 0) }
        dishDao.insertAll(fresh)
        return fresh.size
    }

    suspend fun markCooked(dishId: Long, at: Long = clock()): Long =
        cookEventDao.insert(CookEvent(dishId = dishId, cookedAt = at))

    suspend fun deleteCookEvent(eventId: Long) = cookEventDao.deleteById(eventId)
}
