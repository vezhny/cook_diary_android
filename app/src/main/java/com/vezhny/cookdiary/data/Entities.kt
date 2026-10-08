package com.vezhny.cookdiary.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dish")
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Comma-separated tags, e.g. "завтрак,быстро". Will move to its own table later. */
    val tags: String = "",
    val note: String? = null,
) {
    val tagList: List<String>
        get() = tags.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(
    tableName = "cook_event",
    foreignKeys = [
        ForeignKey(
            entity = Dish::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dishId"), Index("cookedAt")],
)
data class CookEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    /** Epoch millis. */
    val cookedAt: Long,
)

/** A dish together with the moment it was last cooked (null if never). */
data class DishWithLastCooked(
    @Embedded val dish: Dish,
    @ColumnInfo(name = "lastCookedAt") val lastCookedAt: Long?,
)

/** A history row: the event joined with its dish name. */
data class CookEventWithDish(
    val eventId: Long,
    val dishId: Long,
    val dishName: String,
    val cookedAt: Long,
)
