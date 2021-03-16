package fr.swapparel.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savedApparel")
data class SavedApparel(
    @PrimaryKey(autoGenerate = true) var uid: Long?,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "drawablePath") var drawablePath: String,
    @ColumnInfo(name = "color") var color: String,
    @ColumnInfo(name = "heaviness") var heaviness: Int
) {
    constructor() : this(null, "", "", "", 0)
}


