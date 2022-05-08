package com.adyen.android.assignment.api

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val PLACES_LIMIT_PER_REQUEST = "50"

abstract class PlacesQueryBuilder {

    fun build(): Map<String, String> {
        val queryParams = hashMapOf(
            "v" to dateFormat.format(Date()),
            "limit" to PLACES_LIMIT_PER_REQUEST
        )
        putQueryParams(queryParams)
        return queryParams
    }

    abstract fun putQueryParams(queryParams: MutableMap<String, String>)

    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ROOT)
    }

}
