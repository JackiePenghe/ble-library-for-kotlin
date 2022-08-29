package com.sscl.bluetoothlowenergylibrary.enums

import androidx.annotation.RequiresApi
import android.os.Build
import android.bluetooth.BluetoothDevice

/**
 * GATT 连接到远程双模设备的首选传输方式
 *
 * [BluetoothDevice.TRANSPORT_AUTO]
 * [BluetoothDevice.TRANSPORT_BREDR]
 * [BluetoothDevice.TRANSPORT_LE]
 *
 * @author pengh
 */
@RequiresApi(Build.VERSION_CODES.M)
enum class BleConnectTransport(val value: Int) {
    /**
     * 不设置偏好，由系统自动处理
     */
    TRANSPORT_AUTO(BluetoothDevice.TRANSPORT_AUTO),

    /**
     * 首选 BR/EDR 传输
     */
    TRANSPORT_BR_EDR(BluetoothDevice.TRANSPORT_BREDR),

    /**
     * 首选 LE 传输
     */
    TRANSPORT_LE(BluetoothDevice.TRANSPORT_LE);

}