package com.hsiang.nftwallpaper.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

const val AKASWAP_IPFS_BASEURL = "https://assets.akaswap.com/ipfs/"

object NetworkManager {

    private const val AKASWAP_API_BASEURL = "https://akaswap.com/api/v2/"
    val akaswapApi: AkaswapApiService

    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(AKASWAP_API_BASEURL)
            .client(UnsafeOkHttpClient.getUnsafeOkHttpClient().build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        akaswapApi = retrofit.create(AkaswapApiService::class.java)
    }

}