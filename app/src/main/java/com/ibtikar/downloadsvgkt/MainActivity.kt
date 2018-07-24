package com.ibtikar.downloadsvgkt

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ibtikar.downloadsvgkt.utils.Decompress
import com.ibtikar.downloadsvgkt.utils.MemoryUtils
import com.ibtikar.downloadsvgkt.utils.ProgressHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), ProgressHelper {

    override fun addProgress(progress: Long, total: Long) {

        progressBar.progress = ((progress * 100) / total).toInt()
        println("progress ${((progress * 100) / total).toInt()}")


    }


    private val PERMISSION_REQUEST_CODE = 200

    private var svg = "https://drive.google.com/file/d/1S9oH3D3BRZHtzzFdEFGb9B7TOVqr0IT5/view?usp=sharing"
    private var uri = arrayOf(
            "https://drive.google.com/a/ibtikar.net.sa/uc?authuser=1&id=1H3XwWO5a1qITvQS4AgOa0LwLJsYGqckg&export=download")

    lateinit var mgr: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mgr = this.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        memory_info.text = "available internal memory size: " + MemoryUtils.formatSize(MemoryUtils.getAvailableInternalMemorySize()) + "\n" +
                "total external memory size: ${when { MemoryUtils.getTotalExternalMemorySize(this) == -1L -> "no writable sd card"
                    else ->
                        MemoryUtils.formatSize(MemoryUtils.getTotalExternalMemorySize(this))
                }}" + "\n" +
                "available external memory size: ${if (MemoryUtils.getAvailableExternalMemorySize(this) == -1L) "no writable sd card"
                else
                    MemoryUtils.formatSize(MemoryUtils.getAvailableExternalMemorySize(this))}" + "\n"


        preFileDownload()

    }

    private fun preFileDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        } else {
            fileDownload()
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val downloadAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (downloadAccepted) {
                    fileDownload()
                }

            }
        }
    }

    private fun fileDownload() {
        val direct = File("${Environment.getExternalStorageDirectory()}/test")
        if (!direct.exists()) {
            direct.mkdirs()
        }

        for (i in uri.indices) {
            val downloadUri = Uri.parse(uri[i])
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Demo")
                    .setDescription("Something useful. No, really.")
                    .setDestinationInExternalPublicDir("/test", "$i.zip")

            mgr.enqueue(request)
        }
    }


    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {

            var action = intent.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                var query: DownloadManager.Query = DownloadManager.Query()
                query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                var cursor: Cursor = mgr.query(query)
                if (cursor.moveToFirst()) {
                    if (cursor.count > 0) {
                        var status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            var downloadedTo = cursor.getString(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            val decompress = Decompress(downloadedTo.removePrefix("file:///"),
                                    "${Environment.getExternalStorageDirectory()}/test/unzipped/", this@MainActivity)
                            deleteFileIfExists(downloadedTo.removePrefix("file:///"))
                        } else {
                            var message = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                            Log.e("download_error", message)
                            // So something here on failed.
                        }
                    }
                }

            }
        }
    }


    private fun deleteFileIfExists(filename: String): Boolean {
        val folder = File(filename)
        return if (folder.exists())
            folder.delete()
        else true

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

}

