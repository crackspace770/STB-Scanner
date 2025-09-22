package com.example.stb_scanner

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler

import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stb_scanner.adapter.ChannelAdapter
import com.example.stb_scanner.databinding.ActivityMainBinding
import com.example.stb_scanner.helper.ChannelHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadChannels()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }

        updateCalender()
        updateClock()
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
            .filter { it.number!!.matches(Regex("^\\d+$")) } // filter angka solid
            .sortedBy { it.number!!.toInt() }

        if (channels.isEmpty()) {
            // ❌ Tidak ada channel -> tampilkan noChannelView
            binding.rvChannels.visibility = View.GONE
            binding.noChannelView.visibility = View.VISIBLE
        } else {
            // ✅ Ada channel -> tampilkan RecyclerView
            binding.rvChannels.visibility = View.VISIBLE
            binding.noChannelView.visibility = View.GONE

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateClock() {
        Handler().postDelayed({
            val currentTime = getCurrentTime()
            binding.tvClock.text = currentTime
            updateClock()
        }, 1000) // Update the clock every 1000ms (1 second)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTime(): String {

        val zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()
        val currentZonedDateTime: java.time.ZonedDateTime = java.time.ZonedDateTime.now(zoneId)
        val currentTimestamp: Long = currentZonedDateTime.toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()
        val seconds = currentTimestamp / 1000
        val minutes = seconds / 60
        val hours = minutes / 60


        val date = Date(currentTimestamp)
        val formatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")

        return formatter.format(date)
    }

    private fun updateCalender() {

        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekString = when (dayOfWeek) {
            Calendar.SUNDAY -> "Minggu"
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jum'at"
            Calendar.SATURDAY -> "Sabtu"
            else -> "Tidak Ada"
        }
        val date = Date()


        val formattedDate = SimpleDateFormat("dd MMM yyyy").format(date)

        binding.tvDate.text = "$dayOfWeekString, $formattedDate"
        println("Current day: $dayOfWeek")
//        println("Current date: $formattedDate")
    }



}