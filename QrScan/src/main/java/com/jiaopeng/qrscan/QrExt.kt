package com.jiaopeng.qrscan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.io.ByteArrayOutputStream
import java.lang.Boolean.TRUE
import java.util.*
import java.util.concurrent.Executors


/**
 * 描述：二维码扫描扩展类
 *
 * @author JiaoPeng by 1/12/21
 */
var analyzer: ImageAnalysis.Analyzer? = null
val cameraExecutor = Executors.newSingleThreadExecutor()
val preview = Preview.Builder().build()
val cameraSelector = CameraSelector.Builder()
    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
    .build()
val imageAnalysis = ImageAnalysis.Builder()
    .setTargetResolution(Size(1280, 720))
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
var mScannerSDK: ScannerSDK = ScannerSDK.ZXING

/**
 * @data： 1/18/21 11:25 AM
 * @author： JPeng
 * @param： context：AppCompatActivity用于绑定Lifecycle
 *          scanResult：扫描回调监听
 *          scannerSDK：配置扫描模式：ScannerSDK.ZXING 或 ScannerSDK.MLKIT
 * @descripion： 开始二维码扫描
 */
fun PreviewView.useCamera2Scan(
    context: AppCompatActivity,
    scanResult: ScanResult? = null,
    scannerSDK: ScannerSDK = ScannerSDK.ZXING
) {
    mScannerSDK = scannerSDK
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider?.unbindAll()
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageAnalysis.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        analyzer = when (scannerSDK) {
            ScannerSDK.ZXING -> {
                ZXingBarcodeAnalyzer {
                    scanResult?.onScanResult(it)
                }
            }
            ScannerSDK.MLKIT -> {
                MLKitBarcodeAnalyzer {
                    scanResult?.onScanResult(it)
                }
            }
        }
        analyzer?.let {
            imageAnalysis.setAnalyzer(cameraExecutor, it)
        }
        preview.setSurfaceProvider(this.surfaceProvider)
        val camera =
            cameraProvider?.bindToLifecycle(
                context,
                cameraSelector,
                imageAnalysis,
                preview
            )
    }, ContextCompat.getMainExecutor(context))

}

/**
 * @data： 1/18/21 11:30 AM
 * @author： JPeng
 * @param：
 * @descripion：重置扫描
 *              注意：回调如果重置之后的处理和useCamera2Scan一致，建议和useCamera2Scan函数使用同一个
 */
fun reScan(scanResult: ScanResult? = null) {
    Handler(Looper.getMainLooper()).postDelayed({
        imageAnalysis.clearAnalyzer()
        when (mScannerSDK) {
            ScannerSDK.ZXING -> {
                imageAnalysis.setAnalyzer(cameraExecutor, ZXingBarcodeAnalyzer {
                    scanResult?.onScanResult(it)
                })
            }
            ScannerSDK.MLKIT -> {
                imageAnalysis.setAnalyzer(cameraExecutor, MLKitBarcodeAnalyzer {
                    scanResult?.onScanResult(it)
                })
            }
        }
    }, 3000)
}

/**
 * @data： 1/18/21 11:31 AM
 * @author： JPeng
 * @param：
 * @descripion：销毁二维码扫描
 */
fun destroyCamera2Scan() {
    cameraExecutor.shutdown()
}

/**
 * @data： 1/18/21 3:42 PM
 * @author： JPeng
 * @param：
 * @descripion：解析二维码图片获取String
 *              注意：Android 10 要在 AndroidManifest.xml文件中加上 android:requestLegacyExternalStorage="true"
 */
fun analyzeQRCode2String(context: Context, imagePath: String): String {
    var result = ""
    val realUrl = RealPathFromUriUtils.getRealPathFromUri(context, Uri.parse(imagePath))
    val b = BitmapFactory.decodeFile(realUrl)
    val scaleBitmap = scale(b, 0.5F, 0.5F, false)
    val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
    //添加可以解析的编码类型
    val decodeFormats = Vector<BarcodeFormat>()
    decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
    decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
    decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS)
    decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS)
    hints[DecodeHintType.CHARACTER_SET] = "utf-8"
    hints[DecodeHintType.TRY_HARDER] = TRUE
    hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
    val qRCodeResult = parseQRCodeResult(scaleBitmap, hints)
    if (qRCodeResult != null && !TextUtils.isEmpty(qRCodeResult.text)) {
        result = qRCodeResult.text
    } else {
        val nb = compressBySampleSize(b, 4, false)
        val nRCodeResult = parseQRCodeResult(nb, hints)
        if (nRCodeResult != null && !TextUtils.isEmpty(nRCodeResult.text)) {
            result = nRCodeResult.text
        } else {
            Toast.makeText(context, "二维码识别失败", Toast.LENGTH_SHORT).show()
        }
    }
    return result
}

private fun scale(
    src: Bitmap,
    scaleWidth: Float,
    scaleHeight: Float,
    recycle: Boolean
): Bitmap? {
    if (isEmptyBitmap(src)) return null
    val matrix = Matrix()
    matrix.setScale(scaleWidth, scaleHeight)
    val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    if (recycle && !src.isRecycled && ret != src) src.recycle()
    return ret
}

private fun isEmptyBitmap(src: Bitmap?): Boolean {
    return src == null || src.width == 0 || src.height == 0
}

/**
 * 解析二维码图片
 *
 * @return
 */
private fun parseQRCodeResult(bitmapV: Bitmap?, hints: Map<DecodeHintType, Any>?): Result? {
    var result: Result? = null
    try {
        val reader = QRCodeReader()
        val source: RGBLuminanceSource = getRGBLuminanceSource(bitmapV)
        var isReDecode: Boolean
        try {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            result = reader.decode(bitmap, hints)
            isReDecode = false
        } catch (e: Exception) {
            isReDecode = true
        }
        if (isReDecode) {
            try {
                val bitmap = BinaryBitmap(HybridBinarizer(source.invert()))
                result = reader.decode(bitmap, hints)
                isReDecode = false
            } catch (e: Exception) {
                isReDecode = true
            }
        }
        if (isReDecode) {
            try {
                val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
                result = reader.decode(bitmap, hints)
                isReDecode = false
            } catch (e: Exception) {
                isReDecode = true
            }
        }
        if (isReDecode && source.isRotateSupported) {
            try {
                val bitmap = BinaryBitmap(HybridBinarizer(source.rotateCounterClockwise()))
                result = reader.decode(bitmap, hints)
            } catch (e: Exception) {
            }
        }
        reader.reset()
    } catch (e: Exception) {
        Log.wtf("QrExt", e.message)
    }
    return result
}

private fun getRGBLuminanceSource(bitmap: Bitmap?): RGBLuminanceSource {
    val width = bitmap?.width
    val height = bitmap?.height
    val r = width?.times(height ?: 0) ?: 0
    val pixels = IntArray(r)
    bitmap?.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    return RGBLuminanceSource(width ?: 0, height ?: 0, pixels)
}

private fun compressBySampleSize(
    src: Bitmap,
    sampleSize: Int,
    recycle: Boolean
): Bitmap? {
    if (isEmptyBitmap(src)) return null
    val options = BitmapFactory.Options()
    options.inSampleSize = sampleSize
    val baos = ByteArrayOutputStream()
    src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val bytes: ByteArray = baos.toByteArray()
    if (recycle && !src.isRecycled) src.recycle()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}


