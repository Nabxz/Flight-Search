package com.example.flightsearch.data.airport

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airport")
data class Airport (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val passengers: Int,

    @ColumnInfo(name = "iata_code")
    val iataCode: String
)
