package com.adyen.android.assignment.ui.places

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(private val placesService: PlacesService) : ViewModel() {

    private val _placesList = MutableStateFlow<List<Place>>(emptyList())
    val placesList: StateFlow<List<Place>> = _placesList

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<Boolean>(false)
    val error: StateFlow<Boolean> = _error

    fun getPlaces(latitude: Double, longitude: Double) {
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
            } catch (e: Exception){
                _loading.value = false
                _error.value = true
            }

        }
    }

}