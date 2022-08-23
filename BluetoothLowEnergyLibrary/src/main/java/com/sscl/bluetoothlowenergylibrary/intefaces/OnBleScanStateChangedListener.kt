package com.sscl.bluetoothlowenergylibrary.intefaces

import android.bluetooth.le.ScanResult
import com.sscl.bluetoothlowenergylibrary.BleDevice
import java.util.ArrayList

/**
 * BLE scan result changed listener
 *
 * @author jackie
 */
interface OnBleScanStateChangedListener {
    /**
     * 每发现一个设备就会触发一次此方法
     *
     * @param bleDevice BLE设置
     */
    fun onScanFindOneDevice(bleDevice: BleDevice)

    /**
     * 仅当发现一个新的设备时才会回调此方法
     *
     * @param index      在全部已缓存的设备列表中的位置
     * @param bleDevice  BLE设置.如果为空则表示设备信息有更新
     * @param bleDevices 全部已缓存的设备列表
     */
    fun onScanFindOneNewDevice(index: Int, bleDevice: BleDevice?, bleDevices: ArrayList<BleDevice>)

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
}