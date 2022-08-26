package com.sscl.bluetoothlowenergylibrary

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.sscl.bluetoothlowenergylibrary.exceptions.BluetoothLENotSupportException
import com.sscl.bluetoothlowenergylibrary.exceptions.BluetoothNotSupportException
import java.util.*

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * 内部方法
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * 验证设备是否支持蓝牙BLE
 */
internal fun Any.checkBleSupport() {
    Logger.log(this.javaClass.simpleName, "验证BLE支持状态")
    if (!BleManager.supportBluetoothLe()) {
        throw BluetoothLENotSupportException()
    }
}

/**
 * 验证设备是否支持蓝牙
 */
internal fun Any.checkBluetoothSupport() {
    Logger.log(this.javaClass.simpleName, "验证BLE支持状态")
    if (!BleManager.supportBluetooth()) {
        throw BluetoothNotSupportException()
    }
}

/**
 * 判断蓝牙MAC地址是否合法
 */
internal fun String.isValidBluetoothAddress(): Boolean {
    Logger.log(this.javaClass.simpleName, "验证蓝牙MAC地址是否合法：$this")
    return BluetoothAdapter.checkBluetoothAddress(this)
}

/**
 * 检查BLUETOOTH_SCAN权限
 */
internal fun Any.hasBluetoothScanPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val result = ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        Logger.log(this.javaClass.simpleName, "判断 BLUETOOTH_SCAN 权限 result = $result")
        result
    } else {
        val result = (ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
        Logger.log(
            this.javaClass.simpleName,
            "安卓版本低于31， BLUETOOTH_SCAN 权限判断变更为定位权限判断 result = $result"
        )
        result
    }
}

/**
 * 检查BLUETOOTH_CONNECT权限
 */
internal fun Any.hasBluetoothConnectPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Logger.log(this.javaClass.simpleName, "判断 BLUETOOTH_CONNECT 权限")
        ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        Logger.log(
            this.javaClass.simpleName,
            "安卓版本低于31，BLUETOOTH_CONNECT 权限直接返回true"
        )
        true
    }
}

/**
 * 字节数组转十六进制字符串
 */
internal fun ByteArray?.toHexString(): String {
    this?:return ""
    var stmp: String
    val sb = StringBuilder()
    for (aByte in this) {
        stmp = Integer.toHexString(aByte.toInt() and 0xFF)
        sb.append(if (stmp.length == 1) "0$stmp" else stmp)
        sb.append(" ")
    }
    return sb.toString().uppercase(Locale.getDefault()).trim()
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * 公开方法
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * 判断是否扫描结果列表中是否包含某个扫描结果
 */
fun MutableList<ScanResult>.containsScanResults(scanResult: ScanResult): Boolean {
    val device = scanResult.device
    for (result in this) {
        if (result.device.equals(device)) {
            return true
        }
    }
    return false
}

/**
 * 获取扫描结果列表中包含的某个扫描结果的索引 -1表示扫描结果列表中不存在该扫描结果
 */
fun MutableList<ScanResult>.indexOfScanResults(scanResult: ScanResult): Int {
    val device = scanResult.device
    for (i in this.indices) {
        val result = this[i]
        if (result.device.equals(device)) {
            return i
        }
    }
    return -1
}

/**
 * 将扫描失败的错误码转换为字符串
 */
fun Int.getFailMsg(): String {
    return when (this) {
        ScanCallback.SCAN_FAILED_ALREADY_STARTED -> {
            "扫描已经开启"
        }
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> {
            "扫描器注册失败"
        }
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> {
            "有不支持的扫描参数或特性"
        }
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> {
            "内部错误"
        }
        else -> {
            "未知的错误 $this,请查看 android.bluetooth.le.ScanCallback 源码获取对应的错误信息"
        }
    }
}