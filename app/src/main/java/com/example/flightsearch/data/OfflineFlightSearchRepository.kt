package com.example.flightsearch.data

import com.example.flightsearch.data.favorites.FavoriteAirport
import com.example.flightsearch.data.interfaces.AirportDao
import com.example.flightsearch.data.interfaces.AirportSearchQuery
import com.example.flightsearch.data.interfaces.FavoriteDao
import com.example.flightsearch.data.interfaces.FlightSearchRepository
import kotlinx.coroutines.flow.Flow

class OfflineFlightSearchRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
): FlightSearchRepository {

    // Airport Operations
    override fun queryAirports(searchTerm: String): Flow<List<AirportSearchQuery>> = airportDao.queryAirports(searchTerm)

    override fun getPossibleFlights(searchTerm: String): Flow<List<AirportSearchQuery>> = airportDao.getPossibleFlights(searchTerm)

    override suspend fun fetchAirportName(iataCode: String): String = airportDao.fetchAirportName(iataCode)

    // Favorite Airport Operations
    override suspend fun addAirportToFavorite(favoriteAirport: FavoriteAirport) = favoriteDao.addAirportToFavorite(favoriteAirport)

    override suspend fun removeAirportFromFavorite(favoriteAirport: FavoriteAirport) = favoriteDao.removeAirportFromFavorite(favoriteAirport.depatureCode, favoriteAirport.destinationCode)

    override suspend fun isFlightInFavoriteList(
        depatureCode: String,
        destinationCode: String
    ): Boolean = favoriteDao.isFlightInFavoriteList(depatureCode, destinationCode)

    override fun getAllFavorites(): Flow<List<FavoriteAirport>> = favoriteDao.getAllFavorites()
}