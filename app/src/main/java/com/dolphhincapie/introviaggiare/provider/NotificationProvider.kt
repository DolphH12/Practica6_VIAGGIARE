package com.dolphhincapie.introviaggiare.provider

import com.dolphhincapie.introviaggiare.model.FCMBody
import com.dolphhincapie.introviaggiare.model.FCMResponse
import com.dolphhincapie.introviaggiare.retrofit.IFCMApi
import com.dolphhincapie.introviaggiare.retrofit.RetrofitClient
import retrofit2.Call


class NotificationProvider {

    private var retrofitClient: RetrofitClient = RetrofitClient()
    private val url = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody?): Call<FCMResponse?>? {
        return retrofitClient.getClientObject(url)?.create(IFCMApi::class.java)?.send(body)
    }
}