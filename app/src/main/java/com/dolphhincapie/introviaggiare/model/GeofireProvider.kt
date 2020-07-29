package com.dolphhincapie.introviaggiare.model

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase.getInstance


class GeofireProvider {

    private var mDatabase: DatabaseReference = getInstance().reference.child("active_drivers")
    private var mGeofire: GeoFire = GeoFire(mDatabase)

    fun saveLocation(idDriver: String?, latLng: LatLng) {
        mGeofire.setLocation(idDriver, GeoLocation(latLng.latitude, latLng.longitude))
    }

    fun removeLocation(idDriver: String?) {
        mGeofire.removeLocation(idDriver)
    }

    fun getActiveDrivers(latLng: LatLng, radio: Double): GeoQuery {
        val geoQuery: GeoQuery =
            mGeofire.queryAtLocation(GeoLocation(latLng.latitude, latLng.longitude), radio)
        geoQuery.removeAllListeners()
        return geoQuery
    }

}