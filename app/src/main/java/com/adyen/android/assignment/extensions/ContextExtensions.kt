package com.adyen.android.assignment.extensions

import android.content.Context
import android.provider.Settings

fun Context.isAirplaneModeOn(): Boolean =
    Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0