package com.example.flightsearch.data.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flightsearch.data.favorites.FavoriteAirport
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAirportToFavorite(favoriteAirport: FavoriteAirport)

    @Query("DELETE from favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun removeAirportFromFavorite(departureCode: String, destinationCode: String)

    @Query("SELECT * from favorite")
    fun getAllFavorites(): Flow<List<FavoriteAirport>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE departure_code = :depatureCode AND destination_code = :destinationCode)")
    suspend fun isFlightInFavoriteList(depatureCode: String, destinationCode: String): Boolean

}