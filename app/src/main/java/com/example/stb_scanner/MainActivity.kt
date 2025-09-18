package com.example.stb_scanner

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stb_scanner.adapter.ChannelAdapter
import com.example.stb_scanner.databinding.ActivityMainBinding
import com.example.stb_scanner.helper.ChannelHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val detailLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedChannelId = result.data?.getLongExtra("selectedChannelId", -1L) ?: -1L
                if (selectedChannelId != -1L) {
                    val index = channelAdapter.currentList.indexOfFirst { it.id == selectedChannelId }
                    if (index != -1) {
                        binding.rvChannels.scrollToPosition(index)
                        binding.rvChannels.post {
                            binding.rvChannels.findViewHolderForAdapterPosition(index)
                                ?.itemView?.requestFocus()
                        }
                    }
                }
            }
        }

    private val channelAdapter: ChannelAdapter by lazy {
        ChannelAdapter { channel ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("channelId", channel.id)     // ✅ sesuai DetailActivity
                putExtra("inputId", channel.inputId) // ✅ sesuai DetailActivity
            }
            detailLauncher.launch(intent)
            //startActivity(intent)
        }
    }



    private lateinit var recyclerView: RecyclerView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadChannels()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }

        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {

        val permissionReadTvListings = "android.permission.READ_TV_LISTINGS"

        if (ActivityCompat.checkSelfPermission(
                this,
                permissionReadTvListings
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadChannels()
        } else {
            requestPermissionLauncher.launch(permissionReadTvListings)
        }
    }

    private fun loadChannels() {
        val channels = ChannelHelper.getAllChannels(this)
            .filter { !it.number.isNullOrBlank() } // hanya ambil channel yang punya number
            .filter { it.number!!.matches(Regex("^\\d+$")) } //filter angka solid

        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }

        channelAdapter.submitList(channels)

        binding.rvChannels.post {
            binding.rvChannels.requestFocus()
        }
    }



}