package com.jiaopeng.qrscan

/**
 * 描述：二维码扫描回调
 *
 * @author JiaoPeng by 1/13/21
 */
interface ScanResult {

    /**
     * @data： 1/18/21 2:34 PM
     * @author： JPeng
     * @param： result：扫描返回结果
     * @descripion：扫描结果在子线程中，只做数据处理，
     *              如果要UI操作，请自行切换到主线程
     */
    fun onScanResult(result: String)

}