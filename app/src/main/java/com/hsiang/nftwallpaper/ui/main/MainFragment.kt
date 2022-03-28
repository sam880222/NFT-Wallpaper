package com.hsiang.nftwallpaper.ui.main

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.hsiang.nftwallpaper.R
import com.hsiang.nftwallpaper.utils.NFTWallpaperService
import com.hsiang.nftwallpaper.databinding.MainFragmentBinding
import com.hsiang.nftwallpaper.network.AkaswapApiService

class MainFragment : Fragment() {
    private lateinit var binding: MainFragmentBinding
    private val TAG = "MainFragment"

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.buttonStart.setOnClickListener {
            Log.d(TAG, "~~~~~")
            context?.let { context ->
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, NFTWallpaperService::class.java)
                )
                startActivity(intent)
            }
//            viewModel.getCreations()
        }

        binding.buttonDone.setOnClickListener {
            val prefs = context?.let { it ->
                PreferenceManager
                    .getDefaultSharedPreferences(it)
            }
            val accountToken = binding.addressInput.editText?.text?.toString()
            if (!accountToken.isNullOrEmpty()) {
                prefs?.edit()?.putString("accountToken", accountToken)?.apply()
                binding.addressInput.editText?.setText("")
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}