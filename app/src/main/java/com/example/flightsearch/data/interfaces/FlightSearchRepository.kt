package com.example.flightsearch.data.interfaces

import com.example.flightsearch.data.favorites.FavoriteAirport
import kotlinx.coroutines.flow.Flow

interface FlightSearchRepository {

    fun queryAirports(searchTerm: String): Flow<List<AirportSearchQuery>>

    fun getPossibleFlights(searchTerm: String): Flow<List<AirportSearchQuery>>

    suspend fun addAirportToFavorite(favoriteAirport: FavoriteAirport)

    suspend fun removeAirportFromFavorite(favoriteAirport: FavoriteAirport)

    suspend fun fetchAirportName(iataCode: String): String

    suspend fun isFlightInFavoriteList(depatureCode: String, destinationCode: String): Boolean

    fun getAllFavorites(): Flow<List<FavoriteAirport>>
}