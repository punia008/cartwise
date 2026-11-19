package com.harshvardhan.cartwise.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(
    entities = [ProductEntity::class, RemoteKeyEntity::class, CartItemEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CartwiseDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun cartDao(): CartDao
}

class Converters {
    // Image URLs never contain a newline, so a joined string is enough; no JSON needed.
    @TypeConverter
    fun fromList(value: List<String>): String = value.joinToString("\n")

    @TypeConverter
    fun toList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split("\n")
}
