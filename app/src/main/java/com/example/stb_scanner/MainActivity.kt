package com.example.stb_scanner

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.content.Intent
import androidx.activity.enableEdgeToEdge
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

    private val channelAdapter by lazy {
        ChannelAdapter { channel ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("channelId", channel.id)     // ✅ sesuai DetailActivity
                putExtra("inputId", channel.inputId) // ✅ sesuai DetailActivity
            }
            startActivity(intent)
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

        binding.recyclerChannels.apply {
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
            .filter { !it.number.isNullOrBlank() } // ✅ hanya ambil channel yang punya number
            .filter { it.number!!.matches(Regex("^\\d+$")) }

        binding.recyclerChannels.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }

        channelAdapter.submitList(channels) // ✅ filter sudah jalan

        binding.recyclerChannels.post {
            binding.recyclerChannels.requestFocus()
        }
    }



}