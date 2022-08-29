package com.sscl.bluetoothlowenergylibrary.enums

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
enum class BlePhy(val value: Int) {
    /**
     * BLE 1M 物理通道
     */
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),

    /**
     * BLE 2M 物理通道
     */
    PHY_LE_2M(BluetoothDevice.PHY_LE_2M),

    /**
     * BLE CODED 物理通道
     */
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {

        /**
         * 通过值获取当前枚举的实例
         */
        internal fun fromValue(value: Int): BlePhy? {
            val values = values()
            for (aValue in values) {
                if (aValue.value == value) {
                    return aValue
                }
            }
            return null
        }
    }
}