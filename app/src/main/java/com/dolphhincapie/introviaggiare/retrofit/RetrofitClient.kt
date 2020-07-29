package com.dolphhincapie.introviaggiare.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitClient {


    fun getClient(url: String): Retrofit? {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit
    }

    fun getClientObject(url: String): Retrofit? {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }
}