package com.sscl.bluetoothlowenergylibrary.enums.connector

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 用于连接到远程设备的首选物理层
 *
 * [BluetoothDevice.PHY_LE_1M_MASK]
 * [BluetoothDevice.PHY_LE_2M_MASK]
 * [BluetoothDevice.PHY_LE_CODED_MASK]
 * 当使用自动连接参数autoConnect为true时，这些选项将不会生效
 *
 * @author pengh
 */
@RequiresApi(Build.VERSION_CODES.O)
enum class BleConnectPhyMask(val value: Int) {
    /**
     * BLE 1M 物理通道
     */
    PHY_LE_1M_MASK(BluetoothDevice.PHY_LE_1M_MASK),

    /**
     * BLE 2M 物理通道
     */
    PHY_LE_2M_MASK(BluetoothDevice.PHY_LE_2M_MASK),

    /**
     * BLE CODED 物理通道
     */
    PHY_LE_CODED_MASK(BluetoothDevice.PHY_LE_CODED_MASK);

}