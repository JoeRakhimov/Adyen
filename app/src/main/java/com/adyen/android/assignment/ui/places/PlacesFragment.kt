package com.adyen.android.assignment.ui.places

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.Place
import com.adyen.android.assignment.ui.base.RecyclerClickListener
import com.adyen.android.assignment.extensions.hide
import com.adyen.android.assignment.extensions.show
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.places_fragment.*

@AndroidEntryPoint
class PlacesFragment : Fragment() {

    companion object {
        fun newInstance() = PlacesFragment()
    }

    private val viewModel: PlacesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        if(savedInstanceState==null) checkLocationPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.places_fragment, container, false)
    }

    private fun init() {
        lifecycleScope.launchWhenStarted {
            viewModel.loading.collect { loading ->
                if (loading) {
                    progress_loading.show()
                    text_message.show()
                    text_message.text = getString(R.string.loading_data)
                } else {
                    progress_loading.hide()
                    text_message.hide()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.placesList.collect {
                recycler_places.layoutManager = LinearLayoutManager(context)
                val adapter = PlacesAdapter(it)
                recycler_places.adapter = adapter
                adapter.setClickListener(object : RecyclerClickListener {
                    override fun onClick(item: Any) {
                        val place = item as Place
                        openMaps(place)
                    }
                })
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.error.collect { error ->
                if (error) {
                    progress_loading.hide()
                    text_message.show()
                    text_message.text = getString(R.string.something_went_wrong)
                    button_try_again.show()
                    button_try_again.setOnClickListener {
                        text_message.hide()
                        button_try_again.hide()
                        getLocation()
                    }
                }
            }
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                progress_loading.hide()
                text_message.hide()
                button_location_permission.hide()
                getLocation()
            } else {
                recycler_places.hide()
                progress_loading.hide()
                text_message.show()
                text_message.text = getString(R.string.location_permission_is_not_granted)
                button_location_permission.show()
                button_location_permission.setOnClickListener {
                    checkLocationPermission()
                }
            }
        }

    private fun checkLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        lifecycleScope.launchWhenStarted {
            progress_loading.show()
            text_message.show()
            text_message.text = getString(R.string.retrieving_location)
            button_location_permission.hide()
            val fusedLocationProviderClient = activity?.let {
                LocationServices.getFusedLocationProviderClient(it)
            }
            val cts = CancellationTokenSource()
            fusedLocationProviderClient?.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cts.token
            )?.addOnSuccessListener {
                progress_loading.hide()
                text_message.hide()
                val latitude = it.latitude
                val longitude = it.longitude
                getPlaces(latitude, longitude)
            }
        }
    }

    private fun getPlaces(latitude: Double, longitude: Double) {
        viewModel.getPlaces(latitude, longitude)
    }

    private fun openMaps(place: Place) {
        val latitude = place.geocodes.main.latitude
        val longitude = place.geocodes.main.longitude
        val label = place.name
        val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

}