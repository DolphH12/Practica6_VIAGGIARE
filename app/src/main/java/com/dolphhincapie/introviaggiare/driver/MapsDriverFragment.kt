package com.dolphhincapie.introviaggiare.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.provider.GeofireProvider
import com.dolphhincapie.introviaggiare.provider.TokenProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_maps_driver.*
import java.io.IOException


class MapsDriverFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnowLocation: Location? = null
    private var medellin = LatLng(6.2442876, -75.616231)
    private var mMarker: Marker? = null
    lateinit var mainHandler: Handler
    private var mIsConnection = false
    private var ban = false
    private var mGeofireProvider: GeofireProvider =
        GeofireProvider("active_drivers")
    private lateinit var mCurrentLatLng: LatLng
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mTokenProvider: TokenProvider =
        TokenProvider()
    private var mListener: ValueEventListener? = null

    private val callback = OnMapReadyCallback { googleMap ->

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        //mMap.setOnPoiClickListener(this)

        bt_conexion.setOnClickListener {
            if (mIsConnection) {
                ban = false
                disconnect()
            } else {
                activarMyLocation()
                ban = true
                ejecutar()
            }

        }

        generateToken()
        isDriverWorking()

    }

    override fun onPause() {
        super.onPause()
        ban = false
    }

    override fun onResume() {
        super.onResume()
        ban = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mListener != null) {
            mGeofireProvider.isDriverWorking(mAuth.currentUser?.uid.toString())
                .removeEventListener(mListener!!)
        }
    }

    private fun isDriverWorking() {
        mListener = mGeofireProvider.isDriverWorking(mAuth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        disconnect()
                    }
                }

            })
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

    fun setBan() {
        mIsConnection = false
        ban = false
        //mainHandler.removeCallbacksAndMessages(null)
    }


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
        return inflater.inflate(R.layout.fragment_maps_driver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_driver) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    private fun disconnect() {
        mIsConnection = false
        bt_conexion.text = getString(R.string.conectarse_driver)
        val user = mAuth.currentUser
        if (user != null)
            mGeofireProvider.removeLocation(mAuth.uid)
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
        mIsConnection = true
        bt_conexion.text = getString(R.string.desconectarse_driver)
        val locationResult = fusedLocationClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
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
                                mCurrentLatLng
                            ).title("Usuario")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        )
                        updateLocation()
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
            mGeofireProvider.saveLocation(mAuth.currentUser?.uid, mCurrentLatLng)
        }

    }

    @SuppressLint("SetTextI18n")
/*    override fun onPoiClick(poi: PointOfInterest?) {
        *//*Toast.makeText(
            context,
            "Nombre ${poi?.name}, latitud ${poi?.latLng?.latitude}, longitud ${poi?.latLng?.longitude}", Toast.LENGTH_SHORT
        ).show()*//*
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

    override fun onDestroy() {
        super.onDestroy()
        ban = false
        val user = mAuth.currentUser
        if (user != null) {
            mGeofireProvider.removeLocation(user.uid)
        }
    }

    private fun generateToken() {
        mTokenProvider.create(mAuth.currentUser?.uid)
    }

}