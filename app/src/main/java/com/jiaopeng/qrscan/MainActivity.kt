package com.jiaopeng.qrscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.constant.Type
import com.huantansheng.easyphotos.models.album.entity.Photo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ScanResult {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanCameraPreview?.useCamera2Scan(
            this@MainActivity,
            this,
            ScannerSDK.MLKIT
        )

        btnPhoto?.setOnClickListener {
            EasyPhotos.createAlbum(this@MainActivity, false, GlideEngine4EasyPhoto())
                .setCount(1)
                .setPuzzleMenu(false)
                .filter(Type.IMAGE)
                .setGif(true)
                .setCleanMenu(false)
                .setOriginalMenu(false, false, "")
                .start(object : SelectCallback() {
                    override fun onResult(
                        photos: ArrayList<Photo>?,
                        paths: ArrayList<String>?,
                        isOriginal: Boolean
                    ) {
                        val r = analyzeQRCode2String(this@MainActivity, paths?.firstOrNull() ?: "")
                        Toast.makeText(this@MainActivity, r, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onScanResult(result: String) {
        Log.e("TAG", "扫描结果：${result}")
    }
}