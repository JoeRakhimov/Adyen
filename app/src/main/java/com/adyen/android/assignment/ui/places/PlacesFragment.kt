package com.adyen.android.assignment.ui.places

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.places_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlacesFragment : Fragment() {

    companion object {
        fun newInstance() = PlacesFragment()
    }

    private val viewModel: PlacesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) checkLocationPermission()
        observeData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.places_fragment, container, false)
    }

    private fun observeData() {
        lifecycleScope.launchWhenStarted {
            viewModel.loading.collect { loading ->
                if (loading) {
                    progress_loading.show()
                    text_loading.show()
                    text_loading.text = getString(R.string.loading)
                    button_try_again.hide()
                } else {
                    progress_loading.hide()
                    text_loading.hide()
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
                    text_loading.show()
                    text_loading.text = getString(R.string.something_went_wrong)
                    button_try_again.show()
                    button_try_again.setOnClickListener {
                        text_loading.hide()
                        button_try_again.hide()
                        checkLocationPermission()
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.locationPermissionGranted.collect { isGranted ->
                if (!isGranted) {
                    recycler_places.hide()
                    progress_loading.hide()
                    text_loading.show()
                    text_loading.text = getString(R.string.location_permission_is_not_granted)
                    button_try_again.show()
                    button_try_again.setOnClickListener {
                        checkLocationPermission()
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
                viewModel.onLocationPermissionChange(true)
                viewModel.getLocation()
            } else {
                viewModel.onLocationPermissionChange(false)
            }
        }

    private fun checkLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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