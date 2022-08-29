package com.sscl.bluetoothlowenergylibrary.intefaces

import android.bluetooth.le.ScanResult

/**
 * BLE扫描回调
 *
 * @author jackie
 */
interface OnBleScanListener {
    /**
     * 每发现一个设备就会触发一次此方法
     *
     * @param scanResult BLE扫描结果
     */
    fun onScanFindOneDevice(scanResult: ScanResult) {

    }

    /**
     * 仅当发现一个新的设备时才会回调此方法
     *
     * @param scanResult  BLE扫描结果.如果为空则表示设备信息有更新
     */
    fun onScanFindOneNewDevice(scanResult: ScanResult)

    /**
     * 扫描结束（扫描时间达到设置的最大扫描时长）
     */
    fun onScanComplete()

    /**
     * BaseBleConnectCallback when batch results are delivered.
     *
     * @param results List of scan results that are previously scanned.
     */
    fun onBatchScanResults(results: List<ScanResult>)

    /**
     * 扫描开启失败
     *
     * @param errorCode 扫描失败的错误代码
     */
    fun onScanFailed(errorCode: Int)

    /**
     * 扫描结果信息有更新
     */
    fun onScanResultInfoUpdate(result: ScanResult) {

    }
}