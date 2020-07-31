package com.dolphhincapie.introviaggiare.driver

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.model.FCMBody
import com.dolphhincapie.introviaggiare.model.FCMResponse
import com.dolphhincapie.introviaggiare.provider.*
import com.dolphhincapie.introviaggiare.utils.DecodePoints
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_map_driver_booking.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MapDriverBookingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnowLocation: Location? = null
    private var medellin = LatLng(6.2442876, -75.616231)
    private var mMarker: Marker? = null
    lateinit var mainHandler: Handler
    private var mIsConnection = false
    private var ban = true
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("drivers_working")
    private var mCurrentLatLng: LatLng? = null
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var clientID: String = ""
    private var mClientProvider: ClientProvider =
        ClientProvider()
    private var mClientBookingProvider: ClientBookingProvider =
        ClientBookingProvider()
    private var mOrigin: String = ""
    private var mDestination: String = ""
    private var mTime: String = ""
    private var mDistance: String = ""
    private var mExtraPrice: String = ""
    private var mOriginLatLng: LatLng? = null
    private var mDestinationLatLng: LatLng? = null
    private lateinit var mGoogleApiProvider: GoogleApiProvider
    private var mPolylineList: MutableList<LatLng> = mutableListOf()
    private var mPolylineOptions: PolylineOptions = PolylineOptions()
    private var mDecodePoints: DecodePoints = DecodePoints()
    private var mIsFirstTime = true
    private var mIsCloseToClient = false
    private var puntoDestino = false
    private var mNotificationProvider: NotificationProvider =
        NotificationProvider()
    private var mTokenProvider: TokenProvider =
        TokenProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_driver_booking)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_driver_booking) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mGoogleApiProvider =
            GoogleApiProvider(this)

        clientID = intent.getStringExtra("idClient").toString()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        activarMyLocation()
        getClient()
        //getClientBooking()

        bt_startBooking.setOnClickListener {
            startBooking()
        }

        bt_finishBooking.setOnClickListener {
            finishBooking()
        }

    }

    private fun finishBooking() {
        mClientBookingProvider.updateStatus(clientID, "finish")
        mClientBookingProvider.updateIdHistoryBook(clientID)
        sendNotification("Viaje Finalizado")
        ban = false
        mGeofireProvider.removeLocation(mAuth.currentUser?.uid.toString())
        val intent: Intent = Intent(this, CalificationClientActivity::class.java)
        intent.putExtra("idClient", clientID)
        startActivity(intent)
        finish()
    }

    private fun startBooking() {
        mClientBookingProvider.updateStatus(clientID, "start")
        bt_finishBooking.visibility = View.VISIBLE
        bt_startBooking.visibility = View.GONE
        mMap.clear()
        puntoDestino = true
        drawRoute(mDestinationLatLng!!)
        sendNotification("Viaje Iniciado")

    }

    private fun getDistanceReturn(clientLatLng: LatLng, driverLatLng: LatLng): Double {
        val clientLocation: Location = Location("")
        val driverLocation: Location = Location("")
        clientLocation.latitude = clientLatLng.latitude
        clientLocation.longitude = clientLatLng.longitude
        driverLocation.latitude = driverLatLng.latitude
        driverLocation.longitude = driverLatLng.longitude
        return clientLocation.distanceTo(driverLocation).toDouble()
    }

    private fun getClientBooking() {
        mClientBookingProvider.getClientBooking(clientID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val destination: String = snapshot.child("destination").value.toString()
                        val origin: String = snapshot.child("origin").value.toString()
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
                        tv_originclientbooking.text = "Recoger en: " + origin
                        tv_destinationclientbooking.text = "Destino: " + destination
                        drawRoute(mOriginLatLng!!)
                        ejecutar()

                    }
                }

            })
    }

    private fun drawRoute(latLng: LatLng) {

        if (!puntoDestino) {
            val mMarkerO = mMap.addMarker(
                MarkerOptions().position(
                    latLng
                ).title("Recoger Aqui")
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

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 14F))

        mGoogleApiProvider.getDirections(mCurrentLatLng!!, latLng)!!
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

    private fun getClient() {
        mClientProvider.getClient(clientID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val email: String = snapshot.child("correo").value.toString()
                        val name: String = snapshot.child("nombre").value.toString()
                        var image: String = ""
                        if (snapshot.hasChild("image")) {
                            image = snapshot.child("image").value.toString()
                            if (image.isNotEmpty()) {
                                Picasso.get().load(image).into(iv_clientBooking)
                            }
                        }
                        tv_nameclientbooking.text = name
                        tv_emailclientbooking.text = email
                    }
                }

            })
    }

    private fun activarMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
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
        mIsConnection = true
        val locationResult = fusedLocationClient.lastLocation
        locationResult.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                try {
                    lastKnowLocation = task.result
                    if (lastKnowLocation != null) {
                        mCurrentLatLng =
                            LatLng(lastKnowLocation!!.latitude, lastKnowLocation!!.longitude)
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                mCurrentLatLng,
                                16F
                            )
                        )
                        mMarker?.remove()
                        mMarker = mMap.addMarker(
                            MarkerOptions().position(
                                mCurrentLatLng!!
                            ).title("Usuario")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        )
                        updateLocation()
                        if (mIsFirstTime) {
                            mIsFirstTime = false
                            getClientBooking()
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
        mMap.isMyLocationEnabled = false

    }

    private fun updateLocation() {
        val user = mAuth.currentUser
        if (user != null && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuth.currentUser?.uid, mCurrentLatLng!!)
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    val distance: Double = getDistanceReturn(mOriginLatLng!!, mCurrentLatLng!!)
                    if (distance <= 200) {
                        bt_startBooking.visibility = View.VISIBLE
                        mIsCloseToClient = true
                        Toast.makeText(
                            this,
                            "Esta cerca de la posicion de recogida",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

        }


    }

    private fun ejecutar() {
        mainHandler = Handler()
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                if (ban) {
                    activarMyLocation() //llamamos nuestro metodo
                    mainHandler.postDelayed(this, 1000) //se ejecutara cada 10 segundos
                } else {
                    mainHandler.removeCallbacks(this)
                }
            }
        }, 5000) //empezara a ejecutarse después de 5 milisegundos
    }

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager: LocationManager? =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

    private fun messageActiveGPS() {
        val alertDialog: AlertDialog? = this.let {
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

    private fun disconnect() {
        ban = false
        val user = mAuth.currentUser
        if (user != null)
            mGeofireProvider.removeLocation(mAuth.uid)
    }

    private fun sendNotification(status: String) {

        mTokenProvider.getToken(clientID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val token: String = snapshot.child("token").value.toString()
                        val map: MutableMap<String, String> = HashMap()
                        map["title"] = "ESTADO DE TU VIAJE"
                        map["body"] =
                            "Tu estado de viaje es: $status"
                        Log.d("idCliente", mAuth.currentUser?.uid.toString())
                        val fcmBody: FCMBody = FCMBody(token, "high", "4500s", map)
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
                                        if (response.body()!!.success != 1) {
                                            Toast.makeText(
                                                this@MapDriverBookingActivity,
                                                "La notificacion no se envio correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@MapDriverBookingActivity,
                                            "La notificacion no se envio correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            })
                    } else {
                        Toast.makeText(
                            this@MapDriverBookingActivity,
                            "La notificacion no se envio correctamente, dado que no tiene Token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            })
    }


}