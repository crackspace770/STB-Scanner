package com.example.stb_scanner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.media.tv.TvView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.example.stb_scanner.databinding.ActivityDetailBinding
import com.example.stb_scanner.helper.ChannelHelper
import com.example.stb_scanner.model.TvChannel


class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var channels: List<TvChannel>
    private var currentIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val channelId = intent.getLongExtra("channelId", -1L)
        val inputId = intent.getStringExtra("inputId")


        // ✅ Ambil semua channel
        channels = ChannelHelper.getAllChannels(this)

        // ✅ Cari posisi channel yg dipilih
        currentIndex = channels.indexOfFirst { it.id == channelId }

        if (currentIndex != -1 && inputId != null) {
            tuneChannel(inputId, channels[currentIndex].id)
        }

        if (currentIndex != -1) {
            showChannelInfo(channels[currentIndex])



        }
    }

    private fun tuneChannel(inputId: String, channelId: Long) {
        val channelUri = TvContract.buildChannelUri(channelId)

        binding.tvView.setCallback(object : TvView.TvInputCallback() {
            override fun onVideoAvailable(inputId: String?) {
                binding.unavailableView.visibility = View.GONE
                Log.d("DetailActivity", "Video available ✅")
            }

            override fun onVideoUnavailable(inputId: String?, reason: Int) {
                binding.unavailableView.visibility = View.VISIBLE
                val message = when (reason) {
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN -> "Channel Tidak Tersedia"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING -> "Loading"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL -> "Sinyal Lemah"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING -> "Buffering"
                    TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY -> "Audio Only"
                    else -> "Unknown Error"
                }
                binding.tvUnavailable.text = message
            }
        })

        binding.tvView.tune(inputId, channelUri)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                moveToNextChannel()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                moveToPrevChannel()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        val channelId = if (currentIndex != -1) channels[currentIndex].id else -1L
        val resultIntent = Intent().apply {
            putExtra("selectedChannelId", channelId)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }



    private fun moveToNextChannel() {
        if (channels.isNotEmpty() && currentIndex < channels.size - 1) {
            currentIndex++
            val channel = channels[currentIndex]
            tuneChannel(channel.inputId, channel.id)
            showChannelInfo(channel) // ✅ tampilkan info tiap pindah channel
        }
    }

    private fun moveToPrevChannel() {
        if (channels.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            val channel = channels[currentIndex]
            tuneChannel(channel.inputId, channel.id)
            showChannelInfo(channel) // ✅ tampilkan info tiap pindah channel
        }
    }


    private fun showChannelInfo(channel: TvChannel) {
        // ✅ Tampilkan nama & nomor
        binding.txtChannelInfo.text = channel.name ?: "No Name"
        binding.txtChannelNumber.text = channel.number ?: "-"

        // ✅ Clear setelah 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            binding.txtChannelInfo.text = ""
            binding.txtChannelNumber.text = ""
        }, 5000L)
    }


    override fun onStop() {
        super.onStop()
        binding.tvView.reset()
    }
}
