package com.jiaopeng.qrscan

import android.content.Intent
import android.net.Uri
import com.google.zxing.BarcodeFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * 描述：
 *
 * @author JiaoPeng by 1/18/21
 */
object DecodeFormatManager {

    var PRODUCT_FORMATS: EnumSet<BarcodeFormat>? = null
    var INDUSTRIAL_FORMATS: EnumSet<BarcodeFormat>? = null
    var ONE_D_FORMATS: EnumSet<BarcodeFormat>? = null
    var QR_CODE_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.QR_CODE)
    var DATA_MATRIX_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.DATA_MATRIX)
    var AZTEC_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.AZTEC)
    var PDF417_FORMATS: Set<BarcodeFormat> = EnumSet.of(BarcodeFormat.PDF_417)
    private var FORMATS_FOR_MODE: HashMap<String, Set<BarcodeFormat>> = HashMap()

    init {
        PRODUCT_FORMATS = EnumSet.of(
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED
        )
        INDUSTRIAL_FORMATS = EnumSet.of(
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.CODE_128,
            BarcodeFormat.ITF,
            BarcodeFormat.CODABAR
        )
        ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS)
        ONE_D_FORMATS?.addAll(INDUSTRIAL_FORMATS!!)
    }

    init {
        FORMATS_FOR_MODE[Intents.Scan.ONE_D_MODE] = ONE_D_FORMATS!!
        FORMATS_FOR_MODE[Intents.Scan.PRODUCT_MODE] = PRODUCT_FORMATS!!
        FORMATS_FOR_MODE[Intents.Scan.QR_CODE_MODE] =
            QR_CODE_FORMATS
        FORMATS_FOR_MODE[Intents.Scan.DATA_MATRIX_MODE] = DATA_MATRIX_FORMATS
        FORMATS_FOR_MODE[Intents.Scan.AZTEC_MODE] = AZTEC_FORMATS
        FORMATS_FOR_MODE[Intents.Scan.PDF417_MODE] =
            PDF417_FORMATS
    }
}