package com.dolphhincapie.introviaggiare.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.*
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_request_driver.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RequestDriverFragment : Fragment() {

    private var mGeofireProvider: GeofireProvider = GeofireProvider()
    private var mOriginLatLng: LatLng = LatLng(0.0, 0.0)
    private var mDestinationLatLng: LatLng = LatLng(0.0, 0.0)
    private var origen_lat: String = ""
    private var origen_lng: String = ""
    private var destino_lat: String = ""
    private var origen: String = ""
    private var destino: String = ""
    private var mDistance: String = ""
    private var mTime: String = ""
    private var destino_lng: String = ""
    private var mRadius: Double = 0.1
    private var mDriverFound: Boolean = false
    private var mIdDriverFound: String = ""
    private lateinit var mDriverFoundLatLng: LatLng
    private var mNotificationProvider: NotificationProvider = NotificationProvider()
    private var mTokenProvider: TokenProvider = TokenProvider()
    private var mClientBookingProvider: ClientBookingProvider = ClientBookingProvider()
    private var mAuth = FirebaseAuth.getInstance()
    private lateinit var mGoogleApiProvider: GoogleApiProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_driver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animation_driver.playAnimation()

        mGoogleApiProvider = GoogleApiProvider(requireContext())

        origen_lat = arguments?.getString("origin_lat").toString()
        origen_lng = arguments?.getString("origin_lng").toString()
        destino_lat = arguments?.getString("destination_lat").toString()
        destino_lng = arguments?.getString("destination_lng").toString()
        origen = arguments?.getString("origin").toString()
        destino = arguments?.getString("destination").toString()
        Log.d("Error_aqui", "$origen_lat , $origen_lng")

        mOriginLatLng = LatLng(origen_lat.toDouble(), origen_lng.toDouble())
        mDestinationLatLng = LatLng(destino_lat.toDouble(), destino_lng.toDouble())

        getClosestDriver()

    }

    private fun getClosestDriver() {
        mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius)
            .addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onGeoQueryReady() {
                    if (!mDriverFound) {
                        mRadius += 0.1f

                        if (mRadius > 5.0) {
                            tv_buscando.text = "CONDUCTOR NO ENCONTRADO\n INTENTE MAS TARDE"
                            return
                        } else {
                            getClosestDriver()
                        }

                    }
                }

                override fun onKeyEntered(key: String?, location: GeoLocation?) {

                    if (!mDriverFound) {
                        mDriverFound = true
                        mIdDriverFound = key.toString()
                        mDriverFoundLatLng = LatLng(location!!.latitude, location.longitude)
                        tv_buscando.text = "CONDUCTOR ENCONTRADO\n ESPERANDO RESPUESTA"
                        createClientBooking()
                    }

                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {

                }

                override fun onKeyExited(key: String?) {

                }

                override fun onGeoQueryError(error: DatabaseError?) {
                }

            })
    }

    private fun createClientBooking() {

        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng)!!
            .enqueue(object : retrofit2.Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {

                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try {
                        val jsonObject: JSONObject = JSONObject(response.body())
                        val jsonArray: JSONArray = jsonObject.getJSONArray("routes")
                        val routes: JSONObject = jsonArray.getJSONObject(0)
                        val polylines = routes.getJSONObject("overview_polyline")
                        val points = polylines.getString("points")
                        val legs: JSONArray = routes.getJSONArray("legs")
                        val leg: JSONObject = legs.getJSONObject(0)
                        val distance: JSONObject = leg.getJSONObject("distance")
                        val time: JSONObject = leg.getJSONObject("duration")
                        mDistance = distance.getString("text").toString()
                        mTime = time.getString("text").toString()

                        sendNotification(mTime, mDistance)

                    } catch (e: Exception) {
                        Log.d("Error", "Error encontrado" + e.message)
                    }
                }

            })
    }

    private fun sendNotification(time: String, km: String) {

        mTokenProvider.getToken(mIdDriverFound)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val token: String = snapshot.child("token").value.toString()
                        val map: MutableMap<String, String> = HashMap()
                        map["title"] = "SOLICITUD DE SERVICIO A " + time + " DE TU POSICION"
                        map["body"] =
                            "Un cliente esta solicitando un servicio  a una distancia de $km"
                        val fcmBody: FCMBody = FCMBody(token, "high", map)
                        mNotificationProvider.sendNotification(fcmBody)
                            ?.enqueue(object : Callback<FCMResponse?> {
                                override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                                    Log.d("Error_notifi", "Error" + t.message)
                                }

                                override fun onResponse(
                                    call: Call<FCMResponse?>,
                                    response: Response<FCMResponse?>
                                ) {
                                    if (response.body() != null) {
                                        if (response.body()!!.success == 1) {
                                            val clientBooking: ClientBooking = ClientBooking(
                                                mAuth.currentUser?.uid.toString(),
                                                mIdDriverFound,
                                                destino,
                                                origen,
                                                mTime,
                                                mDistance,
                                                "create",
                                                origen_lat.toDouble(),
                                                origen_lng.toDouble(),
                                                destino_lat.toDouble(),
                                                destino_lng.toDouble()
                                            )
                                            mClientBookingProvider.create(clientBooking)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "La peticion fue exitosa",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "La notificacion no se envio correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "La notificacion no se envio correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            })
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "La notificacion no se envio correctamente, dado que no tiene Token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
    }
}