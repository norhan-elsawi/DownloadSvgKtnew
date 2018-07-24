package com.ibtikar.downloadsvgkt

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.ibtikar.downloadsvgkt.utils.Decompress
import com.ibtikar.downloadsvgkt.utils.MemoryUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 200

    private var svg = "https://drive.google.com/file/d/1S9oH3D3BRZHtzzFdEFGb9B7TOVqr0IT5/view?usp=sharing"
    private var uri = arrayOf(
            "https://doc-0o-6g-docs.googleusercontent.com/docs/securesc/ha0ro937gcuc7l7deffksulhg5h7mbp1/shrbo1m1kds3rnlaq5r2fdusqmchde98/1532016000000/13914858012923046523/*/14ldmOkHk_L0lFw0AKZ5u9CSQDcPHwuGP?e=download",
            "https://doc-10-6g-docs.googleusercontent.com/docs/securesc/ha0ro937gcuc7l7deffksulhg5h7mbp1/sn8k4d4vvvhepmnrajh8bd1ok5g95nd7/1532008800000/13914858012923046523/*/1gq92PInNoklIb_fssgwgW0LCmBl30kvW?e=download")

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
                var downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                if (downloadId == 0L) return

                var cursor = mgr.query(
                        DownloadManager.Query().setFilterById(downloadId))

                if (cursor.moveToFirst()) {
                    var downloadedTo = cursor.getString(
                            cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val decompress = Decompress(downloadedTo.removePrefix("file:///"),
                            "${Environment.getExternalStorageDirectory()}/test/unzipped/")
                    decompress.unzip()

//                    ZipArchive.unzip(downloadedTo.removePrefix("file:///"), "${Environment.getExternalStorageDirectory()}/unzipped", "")
                    // deleteFileIfExists(downloadedTo.removePrefix("file:///"))
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

