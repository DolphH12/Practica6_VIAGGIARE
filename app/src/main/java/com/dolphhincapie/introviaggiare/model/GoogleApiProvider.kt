package com.dolphhincapie.introviaggiare.model

import android.content.Context
import com.dolphhincapie.introviaggiare.retrofit.RetrofitClient
import com.dolphhincapie.introviaggiare.retrofit.iGoogleApi
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call


class GoogleApiProvider(private var context: Context) {

    private var retrofitClient: RetrofitClient = RetrofitClient()

    fun getDirections(originLatLng: LatLng, destinationLatLng: LatLng): Call<String>? {
        val baseUrl = "https://maps.googleapis.com"
        val query =
            ("/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                    + "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&"
                    + "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&"
                    + "key=" + context.resources.getString(com.dolphhincapie.introviaggiare.R.string.google_maps_key))
        return retrofitClient.getClient(baseUrl)?.create(iGoogleApi::class.java)
            ?.getDirections(baseUrl + query)
    }

}