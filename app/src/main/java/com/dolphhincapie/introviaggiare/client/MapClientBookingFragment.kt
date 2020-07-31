package com.dolphhincapie.introviaggiare.client

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.provider.*
import com.dolphhincapie.introviaggiare.utils.DecodePoints
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_map_client_booking.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class MapClientBookingFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnowLocation: Location? = null
    private var medellin = LatLng(6.2442876, -75.616231)
    private var mMarker: Marker? = null
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("drivers_working")
    private var mTokenProvider: TokenProvider =
        TokenProvider()
    private var mIsFirstTime = true
    lateinit var mainHandler: Handler
    private var mOrigin: String = ""
    private var mDestination: String = ""
    private var mTime: String = ""
    private var mDistance: String = ""
    private lateinit var mOriginLatLng: LatLng
    private lateinit var mDestinationLatLng: LatLng
    private var mDriverLatLng: LatLng? = null
    private lateinit var mGoogleApiProvider: GoogleApiProvider
    private var mPolylineList: MutableList<LatLng> = mutableListOf()
    private var mPolylineOptions: PolylineOptions = PolylineOptions()
    private var mDecodePoints: DecodePoints = DecodePoints()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    private var mDriverProvider: DriverProvider =
        DriverProvider()
    private var mListener: ValueEventListener? = null
    private var mListenerStatus: ValueEventListener? = null
    private var mIdDriver: String = ""
    private var puntoDestino = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_client_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_client_booking) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private val callback = OnMapReadyCallback { googleMap ->

        mGoogleApiProvider =
            GoogleApiProvider(
                requireContext()
            )
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (!Places.isInitialized()) {
            Places.initialize(
                requireActivity().applicationContext,
                resources.getString(R.string.google_maps_key)
            )
        }
        getStatus()
        getClientBooking()
    }

    private fun getStatus() {
        mListenerStatus = mClientBookingProvider.getStatus(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.value.toString()
                        if (status == "accept") {
                            tv_statusBooking.text = "Estado: Aceptado"
                        } else if (status == "start") {
                            tv_statusBooking.text = "Estado: Iniciado"
                            startBooking()
                        } else if (status == "finish") {
                            tv_statusBooking.text = "Estado: Finalizado"
                            finishBooking()
                        }
                    }
                }

            })
    }

    private fun finishBooking() {
        findNavController().navigate(R.id.action_navigation_mapClientBooking_to_navigation_calificationDriver)
    }

    private fun startBooking() {
        mMap.clear()
        puntoDestino = true
        drawRoute(mDestinationLatLng)
    }

    private fun getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val destination: String = snapshot.child("destination").value.toString()
                        val origin: String = snapshot.child("origin").value.toString()
                        val idDriver: String = snapshot.child("idDriver").value.toString()
                        mIdDriver = idDriver
                        val destinationLat: Double =
                            (snapshot.child("destinationLat").value.toString()).toDouble()
                        val destinationLng: Double =
                            (snapshot.child("destinationLng").value.toString()).toDouble()
                        val originLat: Double =
                            (snapshot.child("originLat").value.toString()).toDouble()
                        val originLng: Double =
                            (snapshot.child("originLng").value.toString()).toDouble()
                        mOriginLatLng = LatLng(originLat, originLng)
                        mDestinationLatLng = LatLng(destinationLat, destinationLng)
                        tv_clientbookingorigin.text = "Recoger en: " + origin
                        tv_clientbookingdestination.text = "Destino: " + destination
                        getDriver(idDriver)
                        getDriverLocation(idDriver)

                    } else {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
                }

            })
    }

    private fun getDriver(idDriver: String) {
        mDriverProvider.getDriver(idDriver)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("nombre").value.toString()
                        val email = snapshot.child("correo").value.toString()
                        var image: String = ""
                        if (snapshot.hasChild("image")) {
                            image = snapshot.child("image").value.toString()
                            Picasso.get().load(image).into(iv_DriverBooking)
                        }
                        tv_namedriverbooking.text = name
                        tv_emaildriverbooking.text = email
                    }

                }

            })
    }

    private fun getDriverLocation(idDriver: String) {
        mListener = mGeofireProvider.getDriverLocation(idDriver)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lat: Double = (snapshot.child("0").value.toString()).toDouble()
                        val lng: Double = (snapshot.child("1").value.toString()).toDouble()
                        mDriverLatLng = LatLng(lat, lng)
                        if (mMarker != null) {
                            mMarker!!.remove()
                        }
                        mMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(mDriverLatLng!!)
                                .title("Tu conductor")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        )
                        if (mIsFirstTime) {
                            mIsFirstTime = false
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    mDriverLatLng,
                                    14F
                                )
                            )
                            drawRoute(mOriginLatLng)
                        }
                    }
                }

            })
    }


    private fun drawRoute(latLng: LatLng) {

        if (!puntoDestino) {
            val mMarkerO = mMap.addMarker(
                MarkerOptions().position(
                    latLng
                ).title("Aqui estas")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pino))
            )
        } else {
            val mMarkerD = mMap.addMarker(
                MarkerOptions().position(
                    latLng
                ).title("Destino")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pind))
            )
        }

        mGoogleApiProvider.getDirections(mDriverLatLng!!, latLng)!!
            .enqueue(object : retrofit2.Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {

                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try {
                        mPolylineOptions = PolylineOptions()
                        val jsonObject: JSONObject = JSONObject(response.body())
                        val jsonArray: JSONArray = jsonObject.getJSONArray("routes")
                        val routes: JSONObject = jsonArray.getJSONObject(0)
                        val polylines = routes.getJSONObject("overview_polyline")
                        val points = polylines.getString("points")
                        mPolylineList = mDecodePoints.decodePoly(points)!!
                        mPolylineOptions.color(Color.GREEN)
                        mPolylineOptions.width(10F)
                        mPolylineOptions.startCap(SquareCap())
                        mPolylineOptions.jointType(JointType.ROUND)
                        mPolylineOptions.addAll(mPolylineList)
                        mMap.addPolyline(mPolylineOptions)
                        Log.d("Ruta", mPolylineList.toString())

                        val legs: JSONArray = routes.getJSONArray("legs")
                        val leg: JSONObject = legs.getJSONObject(0)
                        val distance: JSONObject = leg.getJSONObject("distance")
                        val time: JSONObject = leg.getJSONObject("duration")
                        mDistance = distance.getString("text").toString()
                        mTime = time.getString("text").toString()

                    } catch (e: Exception) {
                        Log.d("Error", "Error encontrado" + e.message)
                    }
                }

            })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mListener != null) {
            mGeofireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener!!)
        }
        if (mListenerStatus != null) {
            mClientBookingProvider.getStatus(mAuth.currentUser?.uid.toString())
                .removeEventListener(mListenerStatus!!)
        }
    }


}