package com.dolphhincapie.introviaggiare.provider

import android.content.Context
import com.dolphhincapie.introviaggiare.utils.CompressorBitmapImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

class ImageProvider(ref: String) {

    private var mStorage: StorageReference = FirebaseStorage.getInstance().reference.child(ref)

    fun saveImage(context: Context, image: File, id: String): UploadTask {
        val imageByte: ByteArray = CompressorBitmapImage.getImage(context, image.path, 500, 500)
        val storage: StorageReference = mStorage.child("$id.jpg")
        mStorage = storage
        return storage.putBytes(imageByte)
    }

    fun getStorage(): StorageReference {
        return mStorage
    }


}