package com.adyen.android.assignment.extensions

import android.content.Context
import android.provider.Settings
import android.widget.Toast
import com.adyen.android.assignment.R

fun Context.isAirplaneModeOn(): Boolean =
    Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

fun Context.showToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}