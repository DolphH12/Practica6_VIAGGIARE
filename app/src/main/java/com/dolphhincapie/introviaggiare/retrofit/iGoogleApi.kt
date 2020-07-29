package com.dolphhincapie.introviaggiare.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


interface iGoogleApi {

    @GET
    fun getDirections(@Url url: String?): Call<String>?


}