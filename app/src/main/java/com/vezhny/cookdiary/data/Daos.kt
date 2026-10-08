package com.vezhny.cookdiary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Insert
    suspend fun insert(dish: Dish): Long

    @Update
    suspend fun update(dish: Dish)

    @Delete
    suspend fun delete(dish: Dish)

    @Query("SELECT * FROM dish WHERE id = :id")
    suspend fun getById(id: Long): Dish?

    @Query("SELECT * FROM dish ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Dish>>

    /** Coldest first: never cooked, then longest ago; ties by name. */
    @Query(
        """
        SELECT dish.*, MAX(cook_event.cookedAt) AS lastCookedAt
        FROM dish LEFT JOIN cook_event ON cook_event.dishId = dish.id
        GROUP BY dish.id
        ORDER BY lastCookedAt IS NOT NULL, lastCookedAt, dish.name COLLATE NOCASE
        """,
    )
    fun observeAllWithLastCooked(): Flow<List<DishWithLastCooked>>
}

@Dao
interface CookEventDao {
    @Insert
    suspend fun insert(event: CookEvent): Long

    @Query("DELETE FROM cook_event WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT cook_event.id AS eventId, dish.id AS dishId, dish.name AS dishName, cook_event.cookedAt
        FROM cook_event JOIN dish ON dish.id = cook_event.dishId
        ORDER BY cook_event.cookedAt DESC
        """,
    )
    fun observeHistory(): Flow<List<CookEventWithDish>>
}
