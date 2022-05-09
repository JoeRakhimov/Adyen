package com.adyen.android.assignment.ui.places

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.Place
import com.adyen.android.assignment.extensions.hide
import com.adyen.android.assignment.extensions.isAirplaneModeOn
import com.adyen.android.assignment.extensions.show
import com.adyen.android.assignment.ui.base.RecyclerClickListener
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.places_fragment.*

@AndroidEntryPoint
class PlacesFragment : Fragment() {

    companion object {
        fun newInstance() = PlacesFragment()
    }

    private val viewModel: PlacesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        if (savedInstanceState == null) checkLocationPermission()
        observeData()
        return inflater.inflate(R.layout.places_fragment, container, false)
    }

    private fun observeData() {

        lifecycleScope.launchWhenStarted {
            viewModel.loading.collect { loading ->
                if (loading.loading) {
                    progress_loading.show()
                    text_loading.show()
                    when (loading.type) {
                        LOADING_TYPE_LOCATION -> text_loading.text =
                            getString(R.string.retrieving_location)
                        LOADING_TYPE_PLACES -> text_loading.text = getString(R.string.loading_data)
                    }

                    button_try_again.hide()
                } else {
                    progress_loading.hide()
                    text_loading.hide()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.placesList.collect {
                recycler_places.show()
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
                if (error.error) {
                    recycler_places.hide()
                    progress_loading.hide()
                    text_loading.show()
                    text_loading.text = when(error.type){
                        ERROR_TYPE_LOCATION -> getString(R.string.error_on_retrieving_location)
                        ERROR_TYPE_PLACES -> getString(R.string.error_on_loading_data)
                        ERROR_TYPE_AIRPLANE_MODE_ON -> getString(R.string.device_in_airplane_mode)
                        else -> getString(R.string.something_went_wrong)
                    }
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
            viewModel.locationPermissionState.collect { state ->
                when (state) {
                    PERMISSION_TYPE_DENIED -> {
                        recycler_places.hide()
                        progress_loading.hide()
                        text_loading.show()
                        text_loading.text = getString(R.string.location_permission_is_not_granted)
                        button_try_again.show()
                        button_try_again.setOnClickListener {
                            checkLocationPermission()
                        }
                    }
                    PERMISSION_TYPE_DENIED_NEVER_ASK_AGAIN -> {
                        recycler_places.hide()
                        progress_loading.hide()
                        text_loading.show()
                        text_loading.text = getString(R.string.location_permission_is_not_granted)
                        button_open_settings.show()
                        button_open_settings.setOnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
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
                button_open_settings.hide()
                viewModel.onLocationPermissionChange(PERMISSION_TYPE_GRANTED)
                getLocation()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    viewModel.onLocationPermissionChange(PERMISSION_TYPE_DENIED)
                } else {
                    viewModel.onLocationPermissionChange(PERMISSION_TYPE_DENIED_NEVER_ASK_AGAIN)
                }
            }
        }

    private fun getLocation() {
        if(requireContext().isAirplaneModeOn()){
            viewModel.onAirplaneMode()
        } else {
            val fusedLocationProviderClient = activity?.let {
                LocationServices.getFusedLocationProviderClient(it)
            }
            viewModel.getLocation(fusedLocationProviderClient)
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
        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.google_maps_not_installed), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_places, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_distance -> {
                viewModel.sortPlacesByDistance()
                true
            }
            R.id.sort_by_name -> {
                viewModel.sortPlacesByName()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}