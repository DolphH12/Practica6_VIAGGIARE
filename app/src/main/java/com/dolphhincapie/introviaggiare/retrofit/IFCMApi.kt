package com.dolphhincapie.introviaggiare.retrofit

import com.dolphhincapie.introviaggiare.model.FCMBody
import com.dolphhincapie.introviaggiare.model.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface IFCMApi {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAvb-maKI:APA91bGDirENOMWPbb6w6iPFNgXHom5jU4TYK-hPmWR5AyBrCPOoEnNClxEMJ2UAZaHWBLGLH4xVXu4AcQfHXzc4_sDQnOchkQEe6Szr6MVn_4b-MAYOpPSeC2Bci46yj-OUlw0i7TtN"
    )
    @POST("fcm/send")
    fun send(@Body body: FCMBody?): Call<FCMResponse?>?
}