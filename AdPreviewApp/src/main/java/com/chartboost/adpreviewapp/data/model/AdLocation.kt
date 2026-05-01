/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdLocation(
    val uuid: String,
    val name: String = "",
    @SerialName("country_targeting") val countryTargeting: List<CountryTargeting> = emptyList(),
    val response: String = "",
    @SerialName("template_url") val templateUrl: String = "",
    @SerialName("ad_type") val adType: String = "",
    val type: String = "",
)

@Serializable
data class CountryTargeting(
    val country: String,
    val price: Double,
    @SerialName("exchange_floor_multiplier") val exchangeFloorMultiplier: Int = 0,
)

@Serializable
data class AdLocationsResponse(val items: List<AdLocation> = emptyList())

fun AdLocation.containsQuery(
    query: String,
    ignoreCase: Boolean = true,
): Boolean {
    if (query.isBlank()) return true

    return name.contains(query, ignoreCase = ignoreCase) ||
        response.contains(query, ignoreCase = ignoreCase) ||
        templateUrl.contains(query, ignoreCase = ignoreCase) ||
        adType.contains(query, ignoreCase = ignoreCase) ||
        type.contains(query, ignoreCase = ignoreCase) ||
        countryTargeting.any { country ->
            country.country.contains(query, ignoreCase = ignoreCase)
        }
}
