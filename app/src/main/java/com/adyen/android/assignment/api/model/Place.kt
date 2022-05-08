package com.adyen.android.assignment.api.model

data class Place(
    val categories: List<Category>,
    val distance: Int,
    val geocodes: GeoCode,
    val location: Location,
    val name: String,
    val timezone: String,
)