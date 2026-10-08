package com.vezhny.cookdiary.data

import kotlinx.coroutines.flow.Flow

class CookRepository(
    private val dishDao: DishDao,
    private val cookEventDao: CookEventDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeDishes(): Flow<List<DishWithLastCooked>> = dishDao.observeAllWithLastCooked()

    fun observeHistory(): Flow<List<CookEventWithDish>> = cookEventDao.observeHistory()

    suspend fun getDishesForPicking(): List<DishWithLastCooked> = dishDao.getAllWithLastCooked()

    suspend fun getDish(id: Long): Dish? = dishDao.getById(id)

    /** Inserts a new dish when [Dish.id] is 0, otherwise updates it. */
    suspend fun saveDish(dish: Dish): Long =
        if (dish.id == 0L) dishDao.insert(dish) else dish.id.also { dishDao.update(dish) }

    suspend fun deleteDish(dish: Dish) = dishDao.delete(dish)

    suspend fun markCooked(dishId: Long, at: Long = clock()): Long =
        cookEventDao.insert(CookEvent(dishId = dishId, cookedAt = at))

    suspend fun deleteCookEvent(eventId: Long) = cookEventDao.deleteById(eventId)
}
