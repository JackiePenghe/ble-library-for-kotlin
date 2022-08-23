package com.sscl.bluetoothlowenergylibrary

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.sscl.bluetoothlowenergylibrary.exceptions.BluetoothLENotSupportException

/**
 * 验证设备是否支持蓝牙BLE
 */
fun Any.checkBleSupport() {
    Logger.log(this.javaClass.simpleName, "验证BLE支持状态")
    if (!BleManager.context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        throw BluetoothLENotSupportException()
    }
}

/**
 * 检查BLUETOOTH_SCAN权限
 */
fun Any.hasBluetoothScanPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val result = ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        Logger.log(this.javaClass.simpleName, "判断BLUETOOTH_SCAN权限 result = $result")
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
            "安卓版本低于31，BLUETOOTH_SCAN权限判断变更为定位权限判断 result = $result"
        )
        result
    }
}

fun Any.hasBluetoothConnectPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Logger.log(this.javaClass.simpleName, "判断BLUETOOTH_CONNECT权限")
        ActivityCompat.checkSelfPermission(
            BleManager.context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        Logger.log(this.javaClass.simpleName, "安卓版本低于31，BLUETOOTH_CONNECT权限判断直接返回true")
        true
    }
}