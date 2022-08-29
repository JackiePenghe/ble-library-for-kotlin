package com.sscl.blelibraryforkotlin.beans

import android.bluetooth.le.ScanResult

/**
 * 带有选择状态的扫描结果
 */
class BleScanResultWithBoolean(val scanResult: ScanResult, var checked: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleScanResultWithBoolean

        return (scanResult.device == other.scanResult.device)
    }

    override fun hashCode(): Int {
        return scanResult.hashCode()
    }
}