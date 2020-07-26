package com.dolphhincapie.introviaggiare.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_layout.view.*

class MapsDriverFragment : Fragment(), GoogleMap.OnPoiClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnowLocation: Location
    private var medellin = LatLng(6.2442876, -75.616231)
    private lateinit var mMarker: Marker
    private var mLocationRequest: LocationRequest? = null
    private var LOCATION_REQUEST_CODE = 1


    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        mLocationRequest?.interval = 1000
        mLocationRequest?.fastestInterval = 1000
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest?.smallestDisplacement = 5F

        startLocation()

        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnPoiClickListener(this)

    }


    /*mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (activity?.applicationContext != null) {
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            15F
                        )

                    )
                }
            }
        }
    }*/

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15F
                    )

                )
            }
        }
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

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        Looper.myLooper()
                    )
                } else {
                    checkLocationPermission()
                }
            } else {
                checkLocationPermission()
            }
        }
    }


    private fun startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )
            } else {
                checkLocationPermission()
            }
        } else {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireContext() as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                messageActivePermisos()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            }
        }
    }

/*    private fun activarMyLocation() {
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
            return
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest,  )
        val locationResult = fusedLocationClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                try {
                    lastKnowLocation = task.result!!
                    if (lastKnowLocation != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnowLocation.latitude, lastKnowLocation.longitude),
                                13F
                            )
                        )
                        mMarker = mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    lastKnowLocation.latitude,
                                    lastKnowLocation.longitude
                                )
                            ).title("Conductor")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        )
                    }
                }catch (e: IOException){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 13F))
                }
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 13F))
            }
        }
        mMap.isMyLocationEnabled = true

    }*/

    @SuppressLint("SetTextI18n")
    override fun onPoiClick(poi: PointOfInterest?) {
        /*Toast.makeText(
            context,
            "Nombre ${poi?.name}, latitud ${poi?.latLng?.latitude}, longitud ${poi?.latLng?.longitude}", Toast.LENGTH_SHORT
        ).show()*/
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

    private fun messageActivePermisos() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Esta aplicacion requiere permisos de ubicacion")
                setPositiveButton("Ok") { dialog, id ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        LOCATION_REQUEST_CODE
                    )
                }
            }
            builder.create()
        }
        alertDialog?.show()
    }
}