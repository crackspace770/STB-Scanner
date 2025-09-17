package com.example.stb_scanner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stb_scanner.R
import com.example.stb_scanner.databinding.ItemChannelBinding
import com.example.stb_scanner.model.TvChannel

class ChannelAdapter(

    private val onItemClick: (TvChannel) -> Unit
    ) : ListAdapter<TvChannel, ChannelAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val channelLogos = mapOf(
        "Metro TV" to R.drawable.placeholder,
        "GTV" to R.drawable.placeholder,
        "RCTI" to R.drawable.placeholder,
        "MNCTV" to R.drawable.placeholder,
        "Trans TV" to R.drawable.placeholder,
        "Trans7" to R.drawable.placeholder,
        "Indosiar" to R.drawable.placeholder,
        "SCTV" to R.drawable.ic_abc,
        "ANTV" to R.drawable.placeholder,
        "TVRI" to R.drawable.placeholder,
        // tambahin sesuai kebutuhan… total 40 nanti
    )



    inner class ViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: TvChannel) {
            binding.apply {
                txtChannelNumber.text = channel.number ?: "-"
                txtChannelName.text = channel.name ?: "Unknown"

                val logoRes = channelLogos[channel.name]

                if (logoRes != null) {
                    logoView.setImageResource(logoRes)
                } else {
                    if (channel.logoUri != null) {
                        Glide.with(root.context)
                            .load(channel.logoUri)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(logoView)
                    } else {
                        logoView.setImageResource(R.drawable.placeholder)
                    }
                }

                root.setOnClickListener {
                    onItemClick(channel)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(getItem(position))
    }



    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TvChannel> =
            object : DiffUtil.ItemCallback<TvChannel>() {


                override fun areItemsTheSame(oldItem: TvChannel, channelItem: TvChannel): Boolean {
                    return oldItem.id == channelItem.id
                }


                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: TvChannel, channelItem: TvChannel): Boolean {
                    return oldItem == channelItem
                }
            }
    }

}