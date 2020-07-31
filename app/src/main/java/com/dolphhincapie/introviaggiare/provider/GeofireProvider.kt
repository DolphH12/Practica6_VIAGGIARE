package com.dolphhincapie.introviaggiare.provider

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance


class GeofireProvider(reference: String) {

    private var mDatabase: DatabaseReference = getInstance().reference.child(reference)
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

    fun isDriverWorking(idDriver: String): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child("driver_working").child(idDriver)
    }

    fun getDriverLocation(idDriver: String): DatabaseReference {
        return mDatabase.child(idDriver).child("l")
    }

}