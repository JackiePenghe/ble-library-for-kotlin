package com.sscl.bluetoothlowenergylibrary.intefaces

import android.bluetooth.BluetoothGattCharacteristic

/**
 * BLE设备连接相关的回调
 *
 * @author jackie
 */
interface OnBleConnectStateChangedListener {

    /**
     * 设备已连接
     * 不建议在此方法中执行设备连接后的操作
     * 蓝牙库会在这个回调中执行 [android.bluetooth.BluetoothGatt.discoverServices]方法
     * 请在[OnBleConnectStateChangedListener.onServicesDiscovered]回调中执行设备连接后的操作
     */
    fun onConnected()

    /**
     * 设备断开连接
     */
    fun onDisconnected()

    /**
     * 服务发现失败
     * 在设备连接后会自动触发服务发现
     * 如果服务发现调用失败则会触发此方法
     * 如果你想重新发现服务可以手动调用 [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.discoverServices]
     * 但是通常情况下这依然会失败
     */
    fun autoDiscoverServicesFailed()

    /**
     * 未知的连接状态
     *
     * @param statusCode 参考[android.bluetooth.BluetoothGatt]
     */
    fun unknownConnectStatus(statusCode: Int)

    /**
     * 设备服务发现完成
     */
    fun onServicesDiscovered()

    /**
     * 连接超时
     */
    fun connectTimeout()

    /**
     * GATT状态码异常
     * @param gattErrorCode GATT错误码
     */
    fun gattStatusError(gattErrorCode: Int)
}