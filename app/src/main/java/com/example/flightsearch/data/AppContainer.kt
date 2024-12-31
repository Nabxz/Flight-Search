package com.example.flightsearch.data

import android.content.Context
import com.example.flightsearch.data.interfaces.FlightSearchRepository

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val flightSearchRepository: FlightSearchRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val flightSearchRepository: FlightSearchRepository by lazy {
        OfflineFlightSearchRepository(
            airportDao = FlightDatabase.getDatabase(context).airportDao(),
            favoriteDao = FlightDatabase.getDatabase(context).favoriteDao()
        )
    }
}