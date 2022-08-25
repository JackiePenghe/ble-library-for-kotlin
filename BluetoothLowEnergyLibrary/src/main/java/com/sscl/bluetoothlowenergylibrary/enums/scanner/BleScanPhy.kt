package com.sscl.bluetoothlowenergylibrary.enums.scanner

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 扫描物理层
 * @author jackie
 */
@RequiresApi(Build.VERSION_CODES.O)
enum class BleScanPhy(val value: Int) {
    /**
     * 全部
     */
    PHY_LE_ALL_SUPPORTED(ScanSettings.PHY_LE_ALL_SUPPORTED),

    /**
     * PHY_LE_1M
     */
    PHY_LE_1M(BluetoothDevice.PHY_LE_1M),

    /**
     * PHY_LE_CODED
     */
    PHY_LE_CODED(BluetoothDevice.PHY_LE_CODED);

}