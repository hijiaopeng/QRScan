package com.jiaopeng.qrscan

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ScanResult {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        scanCameraPreview?.useCamera2Scan(
//            this@MainActivity,
//            this,
//            ScannerSDK.MLKIT
//        )
//
        btnPhoto?.setOnClickListener {
            ScanUtil.startScan(
                this,
                1000,
                HmsScanAnalyzerOptions.Creator()
                    .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE)
                    .create()
            )
//            EasyPhotos.createAlbum(this@MainActivity, false, GlideEngine4EasyPhoto())
//                .setCount(1)
//                .setPuzzleMenu(false)
//                .filter(Type.IMAGE)
//                .setGif(true)
//                .setCleanMenu(false)
//                .setOriginalMenu(false, false, "")
//                .start(object : SelectCallback() {
//                    override fun onResult(
//                        photos: ArrayList<Photo>?,
//                        paths: ArrayList<String>?,
//                        isOriginal: Boolean
//                    ) {
//                        val r = analyzeQRCode2String(this@MainActivity, paths?.firstOrNull() ?: "")
//                        Toast.makeText(this@MainActivity, r, Toast.LENGTH_SHORT).show()
//                    }
//                })
        }
    }

    override fun onScanResult(result: String) {
        Log.e("TAG", "扫描结果：${result}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode !== RESULT_OK || data == null) {
            return
        }
        if (requestCode === 1000) {
            val obj = data.getParcelableExtra<HmsScan>(ScanUtil.RESULT)
            if (obj != null) {
                Toast.makeText(this@MainActivity, "${obj.originalValue}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}