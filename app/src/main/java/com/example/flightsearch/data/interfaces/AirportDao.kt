package com.example.flightsearch.data.interfaces

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import com.example.flightsearch.data.airport.Airport
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    // Ensure searchQuery has % enclosing it, eg: %sample text%
    @Query("SELECT name, iata_code FROM airport WHERE name LIKE :searchTerm OR iata_code LIKE :searchTerm")
    fun queryAirports(searchTerm: String): Flow<List<AirportSearchQuery>>

    @Query("SELECT * FROM airport WHERE name != :searchTerm OR iata_code ORDER BY passengers DESC")
    fun getPossibleFlights(searchTerm: String): Flow<List<AirportSearchQuery>>

    @Query("SELECT name FROM airport WHERE iata_code = :iataCode LIMIT 1")
    suspend fun fetchAirportName(iataCode: String): String
}

data class AirportSearchQuery(
    val name: String,
    @ColumnInfo(name = "iata_code")
    val iataCode: String,
)
