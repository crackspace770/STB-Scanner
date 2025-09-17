package com.example.stb_scanner.helper

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.tv.TvContract
import android.net.Uri
import android.util.Log
import com.example.stb_scanner.model.TvChannel


object ChannelHelper {
    fun getAllChannels(context: Context): List<TvChannel> {
        val channels = mutableListOf<TvChannel>()
        val projection = arrayOf(
            TvContract.Channels._ID,
            TvContract.Channels.COLUMN_DISPLAY_NAME,
            TvContract.Channels.COLUMN_DISPLAY_NUMBER,
            TvContract.Channels.COLUMN_INPUT_ID
        )

        val cursor = context.contentResolver.query(
            TvContract.Channels.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(TvContract.Channels._ID)
            val nameIndex = it.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME)
            val numberIndex = it.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NUMBER)
            val inputIdIndex = it.getColumnIndexOrThrow(TvContract.Channels.COLUMN_INPUT_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val inputId = it.getString(inputIdIndex)

                val logoUri = ContentUris.withAppendedId(
                    TvContract.Channels.CONTENT_URI, id
                ).buildUpon()
                    .appendPath(TvContract.Channels.Logo.CONTENT_DIRECTORY)
                    .build()

                channels.add(TvChannel(id, name, number, logoUri, inputId))
            }
        }

        return channels
    }
}
