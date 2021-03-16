package fr.swapparel.data

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE


@Dao
interface SavedApparelDAO {
    @Query("SELECT * from savedApparel")
    fun getAll(): List<SavedApparel>

    @Insert(onConflict = REPLACE)
    fun insert(apparel: SavedApparel)

    @Query("DELETE from savedApparel")
    fun deleteAll()

    @Delete
    fun delete(model: SavedApparel)

    @Update(onConflict = REPLACE)
    fun updateApparel(apparel: SavedApparel)
}