package com.dolphhincapie.introviaggiare.driver

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dolphhincapie.introviaggiare.R
import com.dolphhincapie.introviaggiare.activities.ChooseActivity
import com.dolphhincapie.introviaggiare.provider.DriverProvider
import com.dolphhincapie.introviaggiare.provider.ImageProvider
import com.dolphhincapie.introviaggiare.utils.FileUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_driver.*
import java.io.File

class UserDriverFragment : Fragment() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mDriverProvider: DriverProvider =
        DriverProvider()
    private var mImageProvider: ImageProvider = ImageProvider("Driver_image")
    private var mImageFile: File? = null
    private var urlImage: String = ""
    private val GALLERY_REQUEST = 1
    private var name: String = ""
    private var auto: String = ""
    private var placa: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_user_driver, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_cargaDriver.visibility = View.VISIBLE
        getDriverInfo()

        iv_perfilDriver.setOnClickListener {
            openGallery()
        }

        bt_actualizarDriver.setOnClickListener {
            updateProfile()
        }

        bt_historialDriver.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_user_driver_to_navigation_historyBookingDriver)
        }

        bt_cerrarSesionDriver.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            goToChooseActivity()
        }


    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                mImageFile = FileUtil.from(requireContext(), data?.data!!)
                iv_perfilDriver.setImageBitmap(BitmapFactory.decodeFile(mImageFile?.absolutePath))
            } catch (e: Exception) {
            }
        }
    }

    private fun getDriverInfo() {
        mDriverProvider.getDriver(mAuth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name: String = snapshot.child("nombre").value.toString()
                        val auto: String = snapshot.child("auto").value.toString()
                        val placa: String = snapshot.child("placa").value.toString()
                        var image: String = ""
                        if (snapshot.hasChild("image")) {
                            image = snapshot.child("image").value.toString()
                            if (image.isNotEmpty()) {
                                Picasso.get().load(image).into(iv_perfilDriver)
                            }
                        }
                        te_nameDriver.setText(name)
                        te_carDriver.setText(auto)
                        te_plateDriver.setText(placa)
                        pb_cargaDriver.visibility = View.GONE
                    }
                }

            })
    }

    private fun updateProfile() {
        name = te_nameDriver.text.toString()
        auto = te_carDriver.text.toString()
        placa = te_plateDriver.text.toString()
        if (name.isNotEmpty() && auto.isNotEmpty() && placa.isNotEmpty() && mImageFile != null) {
            pb_cargaDriver.visibility = View.VISIBLE
            saveImage()
        } else {
            Toast.makeText(requireContext(), "Complete los campos y la imagen", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveImage() {
        mImageProvider.saveImage(requireContext(), mImageFile!!, mAuth.currentUser?.uid.toString())
            .addOnCompleteListener(OnCompleteListener<UploadTask.TaskSnapshot> { task ->
                if (task.isSuccessful) {
                    mImageProvider.getStorage().downloadUrl.addOnSuccessListener { uri ->
                        val image: String = uri.toString()
                        mDriverProvider.update(
                            mAuth.currentUser?.uid.toString(),
                            name,
                            image,
                            auto,
                            placa
                        ).addOnSuccessListener {
                            pb_cargaDriver.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Su informacion se actualizo correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Hubo un error al subir imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })


    }

    private fun goToChooseActivity() {
        val intent = Intent(requireContext(), ChooseActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}
