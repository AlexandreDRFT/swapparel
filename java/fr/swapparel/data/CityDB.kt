package fr.swapparel.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [City::class], version = 1)
abstract class CityDB : RoomDatabase() {

    abstract fun cityDao(): CityDAO

    companion object {
        private var INSTANCE: CityDB? = null

        fun getInstance(context: Context): CityDB? {
            if (INSTANCE == null) {
                synchronized(CityDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        CityDB::class.java, "cities.db")
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}