package com.dolphhincapie.introviaggiare.client

import android.app.Activity.RESULT_OK
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
import com.dolphhincapie.introviaggiare.provider.ClientProvider
import com.dolphhincapie.introviaggiare.provider.ImageProvider
import com.dolphhincapie.introviaggiare.utils.FileUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user.*
import java.io.File

class UserFragment : Fragment() {

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mClientProvider: ClientProvider =
        ClientProvider()
    private var mImageProvider: ImageProvider = ImageProvider("client_image")
    private var mImageFile: File? = null
    private var urlImage: String = ""
    private val GALLERY_REQUEST = 1
    private var name: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_user, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_carga.visibility = View.VISIBLE
        getClientInfo()

        iv_perfilClient.setOnClickListener {
            openGallery()
        }

        bt_actualizar.setOnClickListener {
            updateProfile()
        }

        bt_historial.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_user_to_navigation_historyBookingClient)
        }

        bt_cerrarSesion.setOnClickListener {
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
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {

                mImageFile = FileUtil.from(requireContext(), data?.data!!)
                iv_perfilClient.setImageBitmap(BitmapFactory.decodeFile(mImageFile?.absolutePath))
            } catch (e: Exception) {
            }
        }
    }

    private fun getClientInfo() {
        mClientProvider.getClient(mAuth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name: String = snapshot.child("nombre").value.toString()
                        var image: String = ""
                        if (snapshot.hasChild("image")) {
                            image = snapshot.child("image").value.toString()
                            if (image.isNotEmpty()) {
                                Picasso.get().load(image).into(iv_perfilClient)
                            }
                        }
                        te_name.setText(name)
                        pb_carga.visibility = View.GONE
                    }
                }

            })
    }

    private fun updateProfile() {
        name = te_name.text.toString()
        if (name.isNotEmpty() && mImageFile != null) {
            pb_carga.visibility = View.VISIBLE
            saveImage()
        } else {
            Toast.makeText(requireContext(), "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveImage() {
        mImageProvider.saveImage(requireContext(), mImageFile!!, mAuth.currentUser?.uid.toString())
            .addOnCompleteListener(OnCompleteListener<UploadTask.TaskSnapshot> { task ->
                if (task.isSuccessful) {
                    mImageProvider.getStorage().downloadUrl.addOnSuccessListener { uri ->
                        val image: String = uri.toString()
                        mClientProvider.update(mAuth.currentUser?.uid.toString(), name, image)
                            .addOnSuccessListener {
                                pb_carga.visibility = View.GONE
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