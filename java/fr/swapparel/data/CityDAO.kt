package fr.swapparel.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface CityDAO {
    @Query("SELECT * from cities")
    fun getAll(): List<City>

    @Insert(onConflict = REPLACE)
    fun insert(city: City)

    @Query("DELETE from cities")
    fun deleteAll()
}