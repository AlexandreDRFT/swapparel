package fr.swapparel.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [SavedApparel::class], version = 1)
abstract class SavedApparelDB : RoomDatabase() {

    abstract fun apparelDataDao(): SavedApparelDAO

    companion object {
        private var INSTANCE: SavedApparelDB? = null

        fun getInstance(context: Context): SavedApparelDB? {
            if (INSTANCE == null) {
                synchronized(SavedApparelDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            SavedApparelDB::class.java, "apparel.db")
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