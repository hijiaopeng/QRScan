package com.jiaopeng.qrscan

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class ZXingBarcodeAnalyzer(
    private val scanningResultListener: ((String) -> Unit)? = null
) : ImageAnalysis.Analyzer {

    private var multiFormatReader: MultiFormatReader = MultiFormatReader()
    private var isScanning = AtomicBoolean(false)

    override fun analyze(image: ImageProxy) {
        if (isScanning.get()) {
            image.close()
            return
        }

        isScanning.set(true)
        if (
            (image.format == ImageFormat.YUV_420_888 ||
                    image.format == ImageFormat.YUV_422_888 ||
                    image.format == ImageFormat.YUV_444_888)
            && image.planes.size == 3
        ) {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val rotatedImage = RotatedImage(bytes, image.width, image.height)
            rotateImageArray(rotatedImage, image.imageInfo.rotationDegrees)

            val planarYUVLuminanceSource = PlanarYUVLuminanceSource(
                rotatedImage.byteArray,
                rotatedImage.width,
                rotatedImage.height,
                0, 0,
                rotatedImage.width,
                rotatedImage.height,
                false
            )
            val hybridBinarizer = HybridBinarizer(planarYUVLuminanceSource)
            val binaryBitmap = BinaryBitmap(hybridBinarizer)
            try {
                val rawResult = multiFormatReader.decodeWithState(binaryBitmap)

                scanningResultListener?.invoke(rawResult.text)
                return
            } catch (e: NotFoundException) {
                e.printStackTrace()
            } finally {
                multiFormatReader.reset()
                image.close()
            }
            isScanning.set(false)
        }
    }

    private fun rotateImageArray(imageToRotate: RotatedImage, rotationDegrees: Int) {
        if (rotationDegrees == 0) return
        if (rotationDegrees % 90 != 0) return

        val width = imageToRotate.width
        val height = imageToRotate.height

        val rotatedData = ByteArray(imageToRotate.byteArray.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                when (rotationDegrees) {
                    90 -> rotatedData[x * height + height - y - 1] =
                        imageToRotate.byteArray[x + y * width]
                    180 -> rotatedData[width * (height - y - 1) + width - x - 1] =
                        imageToRotate.byteArray[x + y * width]
                    270 -> rotatedData[y + x * height] =
                        imageToRotate.byteArray[y * width + width - x - 1]
                }
            }
        }
        imageToRotate.byteArray = rotatedData
        if (rotationDegrees != 180) {
            imageToRotate.height = width
            imageToRotate.width = height
        }
    }

    private class RotatedImage(var byteArray: ByteArray, var width: Int, var height: Int)

}