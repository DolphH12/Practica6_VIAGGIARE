package com.dolphhincapie.introviaggiare.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dolphhincapie.introviaggiare.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_layout.view.*
import java.io.IOException


class MapsFragment : Fragment(), GoogleMap.OnPoiClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastKnowLocation: Location
    private var medellin = LatLng(6.2442876, -75.616231)
    private lateinit var mMarker: Marker


    private val callback = OnMapReadyCallback { googleMap ->

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mMap = googleMap

        activarMyLocation()
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnPoiClickListener(this)

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
                    lastKnowLocation = task.result!!
                    if (lastKnowLocation != null) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnowLocation.latitude, lastKnowLocation.longitude),
                                13F
                            )
                        )
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    lastKnowLocation.latitude,
                                    lastKnowLocation.longitude
                                )
                            ).title("Usuario")
                        )
                    }
                } catch (e: IOException) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 13F))
                    if (!gpsActived()) {
                        messageActiveGPS()
                    }
                }

            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medellin, 13F))
                if (!gpsActived()) {
                    messageActiveGPS()
                }
            }
        }
        mMap.isMyLocationEnabled = true

    }

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

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager: LocationManager? =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

}