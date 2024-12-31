package com.example.flightsearch.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.R
import com.example.flightsearch.data.interfaces.AirportSearchQuery
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.security.AccessController.getContext

@Composable
fun FlightSearchApp(
    viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModel.Factory)
) {

    val context = LocalContext.current
    val toastMessage by viewModel.viewModelToastMessage.collectAsState()
    val contentPadding = PaddingValues(horizontal = 20.dp)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val homeScreenUiState = viewModel.homeUiState

    // If The ViewModel Has A Toast Message For The User
    if (toastMessage.isNotBlank()) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        viewModel.resetToastMessage()
    }


    Scaffold { innerPadding ->
        // Home Page for Searching flights & seeing favorites
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Search Bar
            FlightSearch(
                searchBarValue = searchQuery,
                onSearchBarValueChange = {viewModel.updateSearchQuery(it)}
            )

            when(homeScreenUiState) {

                // Display favorite flights, when the user is not typing
                is FlightSearchHomeUiState.DisplayFavoriteFlights -> {

                    val listOfFlight by homeScreenUiState.listOfFavoriteFlights.collectAsState()
                    DisplayListOfFlights(
                        listHeading = "Favorite Flight",
                        listOfFlights = listOfFlight,
                        onTweakFavoritePressed = {
                            viewModel.tweakFavoritePressed(it)
                                                 },
                        modifier = Modifier.padding(top = 12.dp),
                        contentPaddingValues = contentPadding
                    )
                }


                // Display result of search query when user is typing
                is FlightSearchHomeUiState.SearchForFlight -> {

                    val airportSearchQueryList by homeScreenUiState.searchQueryList.collectAsState()
                    SearchQueryListResult(
                        airportSearchQueryList = airportSearchQueryList,
                        onAirportSearchQuerySelected = { viewModel.searchQueryAirportSelected(it) },
                        modifier = Modifier.padding(top = 10.dp),
                        contentPaddingValues = contentPadding
                    )
                }

                // Display List of possible flights, if a search query is selected
                is FlightSearchHomeUiState.DisplayAvailableFlights -> {

                    val listOfFlights by homeScreenUiState.listOfAvailableFlights.collectAsState()
                    DisplayListOfFlights(
                        listHeading = "Flights From ${viewModel.getSelectedAirportQueryName()}",
                        listOfFlights = listOfFlights,
                        onTweakFavoritePressed = { viewModel.tweakFavoritePressed(it) },
                        modifier = Modifier.padding(top = 12.dp),
                        contentPaddingValues = contentPadding
                    )
                }

            }

        }
    }
}

@Composable
fun FlightSearch(
    searchBarValue: String,
    onSearchBarValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier
        .fillMaxWidth()
        .height(200.dp)) {
        Image(
            painter = painterResource(id = R.drawable.homepage_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
                .graphicsLayer { // Flip The Image
                    scaleX = -1f
                },
            contentScale = ContentScale.Crop,
        )
        TextField(
            value = searchBarValue,
            placeholder = { Text(text = "Enter Departure Airport") },
            onValueChange = onSearchBarValueChange,
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            colors = TextFieldDefaults.colors().copy(
                unfocusedPlaceholderColor = Color.Gray
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun DisplayListOfFlights(
    listHeading: String,
    listOfFlights: List<DetailedFlight>,
    onTweakFavoritePressed: (DetailedFlight) -> Unit,
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(0.dp)
) {

    LazyColumn(modifier = modifier.padding(contentPaddingValues)) {

        item {
            Text(
                text = listHeading,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(listOfFlights) { flight ->
            FlightDetails(
                departingCode = flight.depatureCode,
                departingAirport = flight.depatureName,
                arrivalCode = flight.arrivalCode,
                arrivingAirport = flight.arrivalName,
                isFavorite = flight.isFavorite,
                onTweakFavoritePressed = { onTweakFavoritePressed(flight) }
            )
        }
    }
}

@Composable
fun SearchQueryListResult(
    airportSearchQueryList: List<AirportSearchQuery>,
    onAirportSearchQuerySelected: (AirportSearchQuery) -> Unit,
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(0.dp)
) {

    LazyColumn(modifier = modifier.padding(contentPaddingValues)) {
        items(airportSearchQueryList) { airport ->
            
            Box(modifier = Modifier.clickable { onAirportSearchQuerySelected(airport) }) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = airport.iataCode,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer(alpha = 0.5f)
                    )
                    Text(text = airport.name)
                }
            }
            HorizontalDivider(thickness = 0.5.dp)
            
        }
    }
}

@Composable
fun FlightDetails(
    departingCode: String,
    departingAirport: String,
    arrivalCode: String,
    arrivingAirport: String,
    isFavorite: Boolean,
    onTweakFavoritePressed: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Flight Information
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .weight(0.8f),
                verticalArrangement = Arrangement.SpaceAround
            ) {

                // Departure
                Column {
                    FlightDetailsText(
                        header = "DEPART",
                        iataCode = departingCode,
                        airportName = departingAirport
                    )
                }

                // Arrival
                Column {
                    FlightDetailsText(
                        header = "ARRIVE",
                        iataCode = arrivalCode,
                        airportName = arrivingAirport
                    )
                }
            }

            // Add to favorite button
            var isFlightFavorite by remember {
                mutableStateOf(isFavorite)
            }
            IconButton(
                onClick = {
                    isFlightFavorite = !isFlightFavorite
                    onTweakFavoritePressed()
                          },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Add to/Remove from favorites",
                    modifier = Modifier.size(49.dp),
                    tint = if(isFlightFavorite) {colorResource(R.color.dark_yellow)} else Color.Gray
                )
            }
        }

    }
}

@Composable
fun FlightDetailsText(
    header: String,
    iataCode: String,
    airportName: String
) {

    Text(
        text = header,
        fontWeight = FontWeight.Light
    )
    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = iataCode, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = airportName, fontWeight = FontWeight.Light, fontSize = 18.sp)
    }
}

@Preview
@Composable
fun PreviewFlightSearchApp() {
    FlightDetails(
        "FCO", "Leornado", "CPH", "Copem", true, {}
    )
}

