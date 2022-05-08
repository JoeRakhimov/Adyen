package com.adyen.android.assignment.ui.places

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.GeoCode
import com.adyen.android.assignment.api.model.Location
import com.adyen.android.assignment.api.model.Main
import com.adyen.android.assignment.api.model.Place
import com.google.android.gms.location.LocationRequest
import com.jintin.fancylocation.LocationData
import com.jintin.fancylocation.LocationFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class PlacesViewModel @Inject constructor(
    application: Application,
    private val placesService: PlacesService
) : ViewModel() {

    private val _placesList = MutableStateFlow<List<Place>>(emptyList())
    val placesList: StateFlow<List<Place>> = _placesList

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error

    private val _locationPermissionGranted = MutableStateFlow(true)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    private val locationRequest =
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    private val locationFlow = LocationFlow(application, locationRequest)

    @SuppressLint("MissingPermission")
    fun getLocation(){
        viewModelScope.launch {
            _loading.value = true
            locationFlow.get().collect {
                when (it) {
                    is LocationData.Success -> {
                        val latitude = it.location.latitude
                        val longitude = it.location.longitude
                        getPlaces(latitude, longitude)
                    }
                    is LocationData.Fail -> {
                        _loading.value = false
                        _error.value = true
                    }
                }
            }
        }
    }

    private fun getPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _error.value = false
                _loading.value = true
                val queryParams = VenueRecommendationsQueryBuilder()
                    .setLatitudeLongitude(latitude, longitude)
                    .build()
                val response = placesService.getVenueRecommendations(queryParams)
                _loading.value = false
                _placesList.value = response.results
            } catch (e: Exception) {
                _loading.value = false
                _error.value = true
            }

        }
    }

    fun onLocationPermissionChange(isGranted: Boolean) {
        _locationPermissionGranted.value = isGranted
    }

}