package com.sscl.bluetoothlowenergylibrary.enums.scanner

import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * BLE扫描模式
 *
 * @author jackie
 */
enum class BleScanMode(val scanMode: Int) {
    /**
     * 平衡模式
     */
    SCAN_MODE_BALANCED(ScanSettings.SCAN_MODE_BALANCED),

    /**
     * 低延时模式（最高的扫描频率）
     */
    SCAN_MODE_LOW_LATENCY(ScanSettings.SCAN_MODE_LOW_LATENCY),

    /**
     * 低功耗模式
     */
    SCAN_MODE_LOW_POWER(ScanSettings.SCAN_MODE_LOW_POWER),

    /**
     * 投机模式，一个特殊的扫描模式。
     * 使用此模式，程序仅在其他程序进行扫描时，触发扫描回调。
     * 但此程序本身不执行扫描
     */
    @RequiresApi(Build.VERSION_CODES.M)
    SCAN_MODE_OPPORTUNISTIC(ScanSettings.SCAN_MODE_OPPORTUNISTIC);

}