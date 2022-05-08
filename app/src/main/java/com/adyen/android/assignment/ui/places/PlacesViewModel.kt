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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(private val placesService: PlacesService) : ViewModel() {

    private val _placesList = MutableLiveData<List<Place>>()
    val placesList: LiveData<List<Place>> = _placesList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun getPlaces(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _loading.value = true
            val queryParams = VenueRecommendationsQueryBuilder()
                .setLatitudeLongitude(latitude, longitude)
                .build()
            val response = placesService.getVenueRecommendations(queryParams)
            _loading.value = false
            _placesList.value = response.results
        }
    }

}