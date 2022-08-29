package com.sscl.bluetoothlowenergylibrary.enums

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * BLE 物理层选项
 */
@RequiresApi(Build.VERSION_CODES.O)
enum class BlePhyOptions(val value: Int) {
    /**
     * 不设置偏好
     */
    BLE_PHY_OPTION_NO_PREFERRED(BluetoothDevice.PHY_OPTION_NO_PREFERRED),

    /**
     * 设置偏好 S=2
     */
    BLE_PHY_OPTION_S2(BluetoothDevice.PHY_OPTION_S2),

    /**
     * 设置偏好 S=8
     */
    BLE_PHY_OPTION_S8(BluetoothDevice.PHY_OPTION_S8);
}