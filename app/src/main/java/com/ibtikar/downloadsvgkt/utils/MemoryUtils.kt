package com.ibtikar.downloadsvgkt.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.ibtikar.downloadsvgkt.utils.RemovableStorage.getPossibleRemovableSDCardDirectories


/**
 * Created by norhan.elsawi on 7/18/2018.
 */
object MemoryUtils {
    fun getAvailableInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return stat.availableBytes
    }


    fun getAvailableExternalMemorySize(context: Context): Long {
        val path = getPossibleRemovableSDCardDirectories(context)
        return when {
            path != null -> {
                val stat = StatFs(path.path)
                stat.availableBytes
            }
            else -> -1L
        }
    }

    fun getTotalExternalMemorySize(context: Context): Long {
        val path = getPossibleRemovableSDCardDirectories(context)
        return when {
            path != null-> {
                val stat = StatFs(path.path)
                stat.totalBytes
            }
            else -> -1L
        }
    }


    fun formatSize(size: Long): String {
        val gegAvailable: Double = (size.toDouble() / (1024 * 1024 * 1024))
        return "$gegAvailable  GB"
    }
}