package com.dolphhincapie.introviaggiare.client

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.Info
import com.dolphhincapie.introviaggiare.provider.GeofireProvider
import com.dolphhincapie.introviaggiare.provider.GoogleApiProvider
import com.dolphhincapie.introviaggiare.provider.InfoProvider
import com.dolphhincapie.introviaggiare.provider.TokenProvider
import com.dolphhincapie.introviaggiare.utils.DecodePoints
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_maps.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException


class MapsFragment : Fragment(), GoogleMap.OnPoiClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnowLocation: Location? = null
    private var medellin = LatLng(6.2442876, -75.616231)
    private var mMarker: Marker? = null
    private lateinit var mCurrentLatLng: LatLng
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("active_drivers")
    private var mTokenProvider: TokenProvider =
        TokenProvider()
    private var mDriversMarkers: MutableList<Marker> = mutableListOf()
    private var mIsFirstTime = true
    lateinit var mainHandler: Handler
    private lateinit var mPlaces: PlacesClient
    private var mAutoComplete: AutocompleteSupportFragment = AutocompleteSupportFragment()
    private var mAutoCompleteDestination: AutocompleteSupportFragment =
        AutocompleteSupportFragment()
    private var mOrigin: String = ""
    private var mDestination: String = ""
    private var mTime: String = ""
    private var mDistance: String = ""
    private var total: Double = 0.0
    private lateinit var mOriginLatLng: LatLng
    private lateinit var mDestinationLatLng: LatLng
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var mGoogleApiProvider: GoogleApiProvider
    private var mPolylineList: MutableList<LatLng> = mutableListOf()
    private var mPolylineOptions: PolylineOptions = PolylineOptions()
    private var mDecodePoints: DecodePoints = DecodePoints()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mInfoProvider: InfoProvider = InfoProvider()


    private val callback = OnMapReadyCallback { googleMap ->

        mGoogleApiProvider =
            GoogleApiProvider(
                requireContext()
            )

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        //mMap.setOnPoiClickListener(this)

        activarMyLocation()
        //ejecutar()

        if (!Places.isInitialized()) {
            Places.initialize(
                requireActivity().applicationContext,
                resources.getString(R.string.google_maps_key)
            )
        }
        mPlaces = Places.createClient(requireContext())
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()

        onCameraMove()

        bt_RequestDiver.setOnClickListener {
            if (mDestination.isEmpty()) {
                Toast.makeText(requireContext(), "Complete el lugar de destino", Toast.LENGTH_SHORT)
                    .show()
            } else {
                progressBar2.visibility = View.VISIBLE
                drawRoute()
            }
        }

        generateToken()

    }

    private fun goToRequestDriver() {
        val bundle: Bundle = Bundle()
        bundle.putString("origin_lat", mOriginLatLng.latitude.toString())
        bundle.putString("origin_lng", mOriginLatLng.longitude.toString())
        bundle.putString("destination_lat", mDestinationLatLng.latitude.toString())
        bundle.putString("destination_lng", mDestinationLatLng.longitude.toString())
        bundle.putString("origin", mOrigin)
        bundle.putString("destination", mDestination)
        bundle.putString("precio", total.toString())
        findNavController().navigate(R.id.action_navigation_maps_to_detailRequestFragment, bundle)
    }

    private fun drawRoute() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_layout, null)

        iv_markerClient.visibility = View.GONE
        linearLayout_search.visibility = View.GONE
        bt_RequestDiver.visibility = View.GONE

        val mMarkerO = mMap.addMarker(
            MarkerOptions().position(
                mOriginLatLng
            ).title("Usuario")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pino))
        )

        val mMarkerD = mMap.addMarker(
            MarkerOptions().position(
                mDestinationLatLng
            ).title("Usuario")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pind))
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                mOriginLatLng,
                5F
            )
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mOriginLatLng, 14F))

        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng)!!
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

                        val distanceAndKm: List<String> = mDistance.split(" ")
                        val distanceValue: Double = distanceAndKm[0].toDouble()

                        val timeAndMin: List<String> = mTime.split(" ")
                        val timeValue: Double = timeAndMin[0].toDouble()

                        calculatePrice(distanceValue, timeValue, view.tv_precioValue)

                        view.tv_latitud.text = "DESTINO: "
                        view.tv_longitud.text = mDestination
                        view.tv_servicio.text = "ORIGEN: "
                        view.tv_distancia.text = mOrigin
                        view.tv_distance.text = "DISTANCIA: $mDistance"
                        view.tv_time.text = "TIEMPO: $mTime"
                        view.tv_precio.text = "PRECIO"
                        progressBar2.visibility = View.GONE

                    } catch (e: Exception) {
                        Log.d("Error", "Error encontrado" + e.message)
                    }
                }

            })

        view.bt_solicitar.setOnClickListener {
            goToRequestDriver()
            dialog.dismiss()
        }
        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            mMap.clear()
            mPolylineOptions = PolylineOptions()
            iv_markerClient.visibility = View.VISIBLE
            linearLayout_search.visibility = View.VISIBLE
            bt_RequestDiver.visibility = View.VISIBLE
            mMarkerO?.remove()
            mMarkerD?.remove()
            getActiveDrivers()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

    }

    private fun calculatePrice(
        distanceValue: Double,
        timeValue: Double,
        tvPreciovalue: TextView
    ) {
        mInfoProvider.getInfo().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val info: Info? = snapshot.getValue(Info::class.java)
                    val totalDistance: Double = distanceValue * info!!.km
                    val totalTime: Double = timeValue * info.min
                    total = totalDistance + totalTime
                    if (total < 5200.0) {
                        total = 5200.0
                        tvPreciovalue.text = "$total$ COP"
                    } else {
                        tvPreciovalue.text = "$total$ COP"
                    }
                    Log.d("precio1", "$total")
                }
            }

        })
    }

    private fun limitSearch() {
        val northSide: LatLng = SphericalUtil.computeOffset(mCurrentLatLng, 5000.0, 0.0)
        val southSide: LatLng = SphericalUtil.computeOffset(mCurrentLatLng, 5000.0, 0.0)
        mAutoComplete.setCountries("COL")
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        mAutoCompleteDestination.setCountries("COL")
        mAutoCompleteDestination.setLocationBias(
            RectangularBounds.newInstance(
                southSide,
                northSide
            )
        )


    }

    private fun onCameraMove() {
        val mCameraListener = GoogleMap.OnCameraIdleListener {
            try {
                val geoCoder: Geocoder = Geocoder(requireContext())
                mOriginLatLng = mMap.cameraPosition.target
                val addresList: List<Address> =
                    geoCoder.getFromLocation(mOriginLatLng.latitude, mOriginLatLng.longitude, 1)
                val city = addresList[0].locality.toString()
                val country = addresList[0].countryName.toString()
                val address = addresList[0].getAddressLine(0)
                mOrigin = "$address $city"
                mAutoComplete.setText("$address $city")

            } catch (e: Exception) {
                Log.d("Error", "Mensaje de error ${e.message}")
            }
        }

        mMap.setOnCameraIdleListener(mCameraListener)
    }

/*    private fun ejecutar() {
        mainHandler = Handler()
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                activarMyLocation() //llamamos nuestro metodo
                mainHandler.postDelayed(this, 1000) //se ejecutara cada 10 segundos
            }
        }, 5000) //empezara a ejecutarse después de 5 milisegundos
    }*/

    private fun messageActiveGPS() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Por favor active la ubicacion.")
                setPositiveButton("Configuraciones") { dialog, id ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
            builder.create()
        }
        alertDialog?.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun activarMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1234
            )
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val locationResult = fusedLocationClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                try {
                    lastKnowLocation = task.result
                    if (lastKnowLocation != null) {
                        mCurrentLatLng =
                            LatLng(lastKnowLocation!!.latitude, lastKnowLocation!!.longitude)

                        if (mIsFirstTime) {
                            mIsFirstTime = false
                            getActiveDrivers()
                            limitSearch()
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    mCurrentLatLng,
                                    16F
                                )
                            )
                        }
                    }
                } catch (e: IOException) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 16F))
                    if (!gpsActived()) {
                        messageActiveGPS()
                    }
                }

            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 16F))
                if (!gpsActived()) {
                    messageActiveGPS()
                }
            }
        }
        mMap.isMyLocationEnabled = true

    }

    private fun getActiveDrivers() {
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10.0)
            .addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onGeoQueryReady() {
                }

                override fun onKeyEntered(key: String?, location: GeoLocation) {
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag!! == key) {
                                return
                            }
                        }
                    }

                    val driverLatLng: LatLng = LatLng(location.latitude, location.longitude)
                    val marker = mMap.addMarker(
                        MarkerOptions().position(driverLatLng).title("Conductor disponible")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    )
                    marker.tag = key
                    mDriversMarkers.add(marker)
                }

                override fun onKeyMoved(key: String?, location: GeoLocation) {
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag!! == key) {
                                marker.position = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }
                }

                override fun onKeyExited(key: String?) {
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag!! == key) {
                                marker.remove()
                                mDriversMarkers.remove(marker)
                                return
                            }
                        }
                    }
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                }

            })
    }

    private fun instanceAutocompleteOrigin() {
        mAutoComplete =
            childFragmentManager.findFragmentById(R.id.placesAutocompleteOrigin)
                    as AutocompleteSupportFragment
        mAutoComplete.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        mAutoComplete.setHint("Lugar de Recogida")
        mAutoComplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                mOrigin = p0.name.toString()
                mOriginLatLng = LatLng(p0.latLng!!.latitude, p0.latLng!!.longitude)
                Log.d("Place", p0.name.toString())
            }

            override fun onError(p0: Status) {
            }

        })

    }

    private fun instanceAutocompleteDestination() {
        mAutoCompleteDestination =
            childFragmentManager.findFragmentById(R.id.placesAutocompleteDestination)
                    as AutocompleteSupportFragment
        mAutoCompleteDestination.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        mAutoCompleteDestination.setHint("Lugar de destino")
        mAutoCompleteDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                mDestination = p0.name.toString()
                mDestinationLatLng = LatLng(p0.latLng!!.latitude, p0.latLng!!.longitude)
                Log.d("Place", p0.name.toString())
            }

            override fun onError(p0: Status) {
            }

        })
    }

/*    @SuppressLint("SetTextI18n")
    override fun onPoiClick(poi: PointOfInterest?) {
        Toast.makeText(
            context,
            "Nombre ${poi?.name}, latitud ${poi?.latLng?.latitude}, longitud ${poi?.latLng?.longitude}", Toast.LENGTH_SHORT
        ).show()
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_layout, null)

        view.tv_nombre.text = poi?.name
        view.tv_latitud.text = poi?.latLng?.latitude.toString()
        view.tv_longitud.text = poi?.latLng?.longitude.toString()
        view.tv_servicio.text = "Servicio\n UBER"
        view.tv_distancia.text = "Varios Km por medir"

        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

    }*/

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager: LocationManager? =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        status.statusMessage?.let { it1 -> Log.i(TAG, it1) }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPoiClick(p0: PointOfInterest?) {
    }

    private fun generateToken() {
        mTokenProvider.create(mAuth.currentUser?.uid)
    }

}