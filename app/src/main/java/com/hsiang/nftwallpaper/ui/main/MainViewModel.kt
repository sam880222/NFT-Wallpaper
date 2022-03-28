package com.hsiang.nftwallpaper.ui.main

import android.os.Debug
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsiang.nftwallpaper.network.NetworkManager.akaswapApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"

    fun getCreations() {
        viewModelScope.launch(Dispatchers.IO) {
//            val res = akaswapApi.getAccountCreations("tz1fGgpKXtxHqTZyY7YdHGCNTs9J2Wen6euq")
            val res = akaswapApi.getIpfsFile("https://assets.akaswap.com/ipfs/QmQmjgSZofrtosMwMyUiohdrqFSvhDXzwMpUGGFkT2botm")
            if (res.isSuccessful) {
//                val body = res.body()!!
//                getAkaObj(body.creations?.get(0)?.tokenId)
//                Log.d(TAG, body.creations?.get(0)?.tokenId.toString())
//                Log.d(TAG, body.creations?.get(0)?.tokenId.toString())
                Log.d(TAG, "!!!!!")
            }
        }
    }
}