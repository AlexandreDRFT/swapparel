package fr.swapparel.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class City(@PrimaryKey(autoGenerate = true) var id: Long?,
                @ColumnInfo(name = "name") var name: String,
                @ColumnInfo(name = "latitude") var latitude: Double,
                @ColumnInfo(name = "longitude") var longitude: Double

){
    constructor():this(null,"",0.0,0.0)
}
