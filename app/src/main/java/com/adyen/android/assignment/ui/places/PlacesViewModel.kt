package com.adyen.android.assignment.ui.places

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.Place
import com.adyen.android.assignment.ui.base.ErrorState
import com.adyen.android.assignment.ui.base.LoadingState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

const val PERMISSION_TYPE_GRANTED = 1
const val PERMISSION_TYPE_DENIED = 0
const val PERMISSION_TYPE_DENIED_NEVER_ASK_AGAIN = -1

const val LOADING_TYPE_LOCATION = 1
const val LOADING_TYPE_PLACES = 2

const val ERROR_TYPE_LOCATION = 1
const val ERROR_TYPE_PLACES = 2
const val ERROR_TYPE_AIRPLANE_MODE_ON = 3

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesService: PlacesService
) : ViewModel() {

    private val _placesList = MutableStateFlow<List<Place>>(emptyList())
    val placesList: StateFlow<List<Place>> = _placesList

    private val _loading = MutableStateFlow(LoadingState(false))
    val loading: StateFlow<LoadingState> = _loading

    private val _error = MutableStateFlow(ErrorState(false))
    val error: StateFlow<ErrorState> = _error

    private val _locationPermissionState = MutableStateFlow(PERMISSION_TYPE_GRANTED)
    val locationPermissionState: StateFlow<Int> = _locationPermissionState

    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient?) {
        viewModelScope.launch {
            _loading.value = LoadingState(true, LOADING_TYPE_LOCATION)
            val cts = CancellationTokenSource()
            fusedLocationProviderClient?.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.token
            )?.addOnSuccessListener { location ->
                if(location!=null){
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getPlaces(latitude, longitude)
                } else {
                    _loading.value = LoadingState(false)
                    _error.value = ErrorState(true, ERROR_TYPE_LOCATION)
                }
            }?.addOnFailureListener {
                _loading.value = LoadingState(false)
                _error.value = ErrorState(true, ERROR_TYPE_LOCATION)
            }
        }
    }

    private fun getPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _error.value = ErrorState(false)
                _loading.value = LoadingState(true, LOADING_TYPE_PLACES)
                val queryParams = VenueRecommendationsQueryBuilder()
                    .setLatitudeLongitude(latitude, longitude)
                    .build()
                val response = placesService.getVenueRecommendations(queryParams)
                _loading.value = LoadingState(false)
                _placesList.value = response.results
            } catch (e: Exception) {
                _loading.value = LoadingState(false)
                _error.value = ErrorState(true, ERROR_TYPE_PLACES)
            }
        }
    }

    fun onLocationPermissionChange(type: Int) {
        _locationPermissionState.value = type
    }

    fun sortPlacesByDistance() {
        _placesList.value = placesList.value.sortedBy { it.distance }
    }

    fun sortPlacesByName() {
        _placesList.value = placesList.value.sortedBy { it.name }
    }

    fun onAirplaneMode() {
        _error.value = ErrorState(false) // this is needed to show the error again, if airplane mode is not turned on
        _error.value = ErrorState(true, ERROR_TYPE_AIRPLANE_MODE_ON)
    }

}