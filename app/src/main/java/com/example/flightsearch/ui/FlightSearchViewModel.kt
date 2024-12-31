package com.example.flightsearch.ui

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.favorites.FavoriteAirport
import com.example.flightsearch.data.interfaces.AirportSearchQuery
import com.example.flightsearch.data.interfaces.FlightSearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FlightSearchHomeUiState {

    data class DisplayFavoriteFlights(val listOfFavoriteFlights: StateFlow<List<DetailedFlight>> = MutableStateFlow<List<DetailedFlight>>(emptyList())) : FlightSearchHomeUiState
    data class SearchForFlight(val searchQueryList: StateFlow<List<AirportSearchQuery>> = MutableStateFlow<List<AirportSearchQuery>>(emptyList())) : FlightSearchHomeUiState
    data class DisplayAvailableFlights(val listOfAvailableFlights: StateFlow<List<DetailedFlight>> = MutableStateFlow<List<DetailedFlight>>(emptyList())) : FlightSearchHomeUiState
}

class FlightSearchViewModel(
    private val flightSearchRepository: FlightSearchRepository
): ViewModel() {

    // Toast Mesasages
    private val _viewModelToastMessage = MutableStateFlow("")
    val viewModelToastMessage: StateFlow<String> = _viewModelToastMessage.asStateFlow()

    // List of Favorite Flights
    private val favoriteAirportList: StateFlow<List<DetailedFlight>> =
        flightSearchRepository.getAllFavorites().map { FavoriteFlightList ->

            val buildingFavoriteList: MutableList<DetailedFlight> = mutableListOf()
            for(favoriteFlight in FavoriteFlightList) {
                val departureName = flightSearchRepository.fetchAirportName(favoriteFlight.depatureCode) ?: ""
                val arrivalName = flightSearchRepository.fetchAirportName(favoriteFlight.destinationCode) ?: ""

                buildingFavoriteList.add(
                    DetailedFlight(
                        depatureCode = favoriteFlight.depatureCode,
                        depatureName = departureName,
                        arrivalCode = favoriteFlight.destinationCode,
                        arrivalName = arrivalName,
                        isFavorite = true
                    )
                )
            }

            buildingFavoriteList.toList()
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    // Home Ui State
    // Initialized displaying the favorite
    var homeUiState: FlightSearchHomeUiState by mutableStateOf(FlightSearchHomeUiState.DisplayFavoriteFlights(favoriteAirportList))
        private set

    // Flight Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var selectedSearchQueryAirportName = MutableStateFlow("")
    private var selectedSearchQueryAirportIataCode = MutableStateFlow("")

    fun updateSearchQuery(text: String) {

        _searchQuery.value = text
        homeUiState = if (text.isBlank()) {
            FlightSearchHomeUiState.DisplayFavoriteFlights(favoriteAirportList)
        } else {
            FlightSearchHomeUiState.SearchForFlight(querySearchResultList)
        }
    }

    fun searchQueryAirportSelected(airport: AirportSearchQuery) {

        selectedSearchQueryAirportIataCode.value = airport.iataCode
        selectedSearchQueryAirportName.value = airport.name
        homeUiState = FlightSearchHomeUiState.DisplayAvailableFlights(availableFlightList)
    }

    fun getSelectedAirportQueryName(): String {

        return selectedSearchQueryAirportName.value
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val querySearchResultList: StateFlow<List<AirportSearchQuery>> = searchQuery
        .debounce(DEBOUNCE_MILLIS)
        .flatMapLatest { query->
            // Adds %% before sending the search query to repository
            flightSearchRepository.queryAirports("%${query}%").map { it }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    // List of Available Flights
    @OptIn(ExperimentalCoroutinesApi::class)
    private val availableFlightList: StateFlow<List<DetailedFlight>> =
        selectedSearchQueryAirportName.flatMapLatest { airportName ->
            flightSearchRepository.getPossibleFlights(airportName).map { PossibleFlightList ->

                val buildingAvailableFlightList: MutableList<DetailedFlight> = mutableListOf()
                for(flight in PossibleFlightList) {
                    val depatureName = airportName
                    val arrivalName = flight.name
                    buildingAvailableFlightList.add(
                        DetailedFlight(
                            depatureCode = selectedSearchQueryAirportIataCode.value,
                            depatureName = depatureName,
                            arrivalCode = flight.iataCode,
                            arrivalName = arrivalName,
                            isFavorite = flightSearchRepository.isFlightInFavoriteList(selectedSearchQueryAirportIataCode.value, flight.iataCode)
                        )
                    )
                }

                buildingAvailableFlightList.toList()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun tweakFavoritePressed(flight: DetailedFlight) {
        viewModelScope.launch {

            // If flight is in favorite list, remove it
            if (flightSearchRepository.isFlightInFavoriteList(flight.depatureCode, flight.arrivalCode)) {
                flightSearchRepository.removeAirportFromFavorite(
                    FavoriteAirport(
                        depatureCode = flight.depatureCode,
                        destinationCode = flight.arrivalCode
                    )
                )
                _viewModelToastMessage.value = "Flight from ${flight.depatureCode} to ${flight.arrivalCode} removed from favorites."

                // Else add it to favorite list
            } else {
                flightSearchRepository.addAirportToFavorite(
                    FavoriteAirport(
                        depatureCode = flight.depatureCode,
                        destinationCode = flight.arrivalCode
                    )
                )
                _viewModelToastMessage.value = "Flight from ${flight.depatureCode} to ${flight.arrivalCode} added to favorites!"
            }
        }
    }

    fun resetToastMessage() {
        _viewModelToastMessage.value = ""
    }


    companion object {
        private const val DEBOUNCE_MILLIS = 300L
        private const val TIMEOUT_MILLIS = 5_000L;

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightSearchViewModel(application.container.flightSearchRepository)
            }
        }
    }
}

data class DetailedFlight(
    val depatureCode: String,
    val depatureName: String,
    val arrivalCode: String,
    val arrivalName: String,
    val isFavorite: Boolean
)