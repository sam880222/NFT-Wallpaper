package com.hsiang.nftwallpaper.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface AkaswapApiService {
    data class AccountCreationsResponse(
        @SerializedName("tokens")
        val tokens: List<TokenInfo>?,
//        val banned: Boolean?
    )

    data class TokenInfo(
        @SerializedName("displayUri")
        val displayUri: String
    )

    @GET("accounts/{accountToken}/creations")
    suspend fun getAccountCreations(
        @Path("accountToken") accountToken: String
    ): Response<AccountCreationsResponse>

    @GET
    suspend fun getIpfsFile(
        @Url url: String
    ): Response<ResponseBody>
}