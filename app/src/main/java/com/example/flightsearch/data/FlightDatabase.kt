package com.example.flightsearch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flightsearch.data.airport.Airport
import com.example.flightsearch.data.favorites.FavoriteAirport
import com.example.flightsearch.data.interfaces.AirportDao
import com.example.flightsearch.data.interfaces.FavoriteDao

@Database(entities = [Airport::class, FavoriteAirport::class], version = 1, exportSchema = false)
abstract class FlightDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var Instance: FlightDatabase? = null

        fun getDatabase(context: Context): FlightDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlightDatabase::class.java, "flight_search.db")
                    .createFromAsset("flight_search.db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
