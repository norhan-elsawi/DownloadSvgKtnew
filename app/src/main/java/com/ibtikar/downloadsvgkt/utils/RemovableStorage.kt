package com.ibtikar.downloadsvgkt.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import java.io.File


/**
 * Created by norhan.elsawi on 7/18/2018.
 */
object RemovableStorage {

    private fun getConfirmedRemovableSDCardDirectory(context: Context): File? {
        if (Environment.isExternalStorageRemovable()) {
            return Environment.getExternalStorageDirectory()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (directory in ContextCompat.getExternalFilesDirs(context, null)) {
                if (Environment.isExternalStorageRemovable(directory)) {
                    return directory
                }
            }
        }

        return null
    }

    fun getPossibleRemovableSDCardDirectories(context: Context): File? {
        return getConfirmedRemovableSDCardDirectory(context)

    }

}