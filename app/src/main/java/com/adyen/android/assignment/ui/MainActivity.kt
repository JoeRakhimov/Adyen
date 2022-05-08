package com.adyen.android.assignment.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.adyen.android.assignment.R
import com.adyen.android.assignment.ui.places.PlacesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PlacesFragment.newInstance())
                .commitNow()
        }
    }

}

