package com.vezhny.cookdiary.data

import com.vezhny.cookdiary.domain.HistoryEntry
import kotlinx.coroutines.flow.Flow

data class ImportResult(
    val dishesAdded: Int,
    val dishesSkipped: Int,
    val historyAdded: Int = 0,
    val historySkipped: Int = 0,
)

class CookRepository(
    private val dishDao: DishDao,
    private val cookEventDao: CookEventDao,
    private val transactor: Transactor,
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

    /** The whole cooking history, oldest first, with dishes referenced by name. */
    suspend fun getHistoryForExport(): List<HistoryEntry> =
        cookEventDao.getHistory().map { HistoryEntry(it.dishName, it.cookedAt) }

    /**
     * Imports dishes and, if given, history, all in one transaction:
     * - a dish is skipped if its name (ignoring case) already exists or repeats within [dishes];
     * - a history entry is matched to a dish by name (existing or just imported) and skipped
     *   if that dish already has an entry at the same moment, or if no dish has that name.
     */
    suspend fun importData(dishes: List<Dish>, history: List<HistoryEntry>? = null): ImportResult =
        transactor.inTransaction {
            val seen = dishDao.getAll().mapTo(HashSet()) { it.key }
            val freshDishes = dishes.filter { seen.add(it.key) }.map { it.copy(id = 0) }
            dishDao.insertAll(freshDishes)

            var historyAdded = 0
            if (history != null) {
                val idByName = dishDao.getAll().associate { it.key to it.id }
                val existing = cookEventDao.getAll().mapTo(HashSet()) { it.dishId to it.cookedAt }
                val freshEvents = history.mapNotNull { entry ->
                    val dishId = idByName[entry.dishName.trim().lowercase()] ?: return@mapNotNull null
                    CookEvent(dishId = dishId, cookedAt = entry.cookedAt).takeIf { existing.add(dishId to entry.cookedAt) }
                }
                cookEventDao.insertAll(freshEvents)
                historyAdded = freshEvents.size
            }

            ImportResult(
                dishesAdded = freshDishes.size,
                dishesSkipped = dishes.size - freshDishes.size,
                historyAdded = historyAdded,
                historySkipped = (history?.size ?: 0) - historyAdded,
            )
        }

    suspend fun markCooked(dishId: Long, at: Long = clock()): Long =
        cookEventDao.insert(CookEvent(dishId = dishId, cookedAt = at))

    suspend fun deleteCookEvent(eventId: Long) = cookEventDao.deleteById(eventId)

    /** Name key for matching dishes: trimmed, case-insensitive. */
    private val Dish.key: String get() = name.trim().lowercase()
}
