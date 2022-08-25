package com.sscl.bluetoothlowenergylibrary.intefaces

/**
 * Bluetooth status changed listener
 *
 * @author jackie
 */
interface OnBluetoothStateChangedListener {
    /**
     * 蓝牙正在打开
     */
    fun onBluetoothEnabling()

    /**
     * 蓝牙已打开
     */
    fun onBluetoothEnable()

    /**
     * 蓝牙正在关闭
     */
    fun onBluetoothDisabling()

    /**
     * 蓝牙已关闭
     */
    fun onBluetoothDisable()
}