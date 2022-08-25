package com.sscl.bluetoothlowenergylibrary.connetor.single

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger
import com.sscl.bluetoothlowenergylibrary.hasBluetoothConnectPermission

/**
 * BLE GATT 回调
 *
 * @author jackie
 */
internal class BleBluetoothGattCallback : BluetoothGattCallback() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        /**
         * TAG
         */
        private val TAG = BleBluetoothGattCallback::class.java.simpleName
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 当GATT客户端的连接状态改变时会触发此方法
     *
     * @param gatt     GATT
     * @param status   原状态
     * @param newState 新状态（当前状态）
     */
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (gatt != BleManager.singleConnectService?.bluetoothGatt) {
            Logger.log(TAG, "收到一个回调，但GATT非当前设备GATT，拦截")
            return
        }
        when (newState) {
            BluetoothGatt.STATE_DISCONNECTED -> {
                Logger.log(TAG, "status = $status")
                if (status == BluetoothGatt.STATE_CONNECTED || status == BluetoothGatt.STATE_CONNECTING || status == BluetoothGatt.STATE_DISCONNECTED || status == BluetoothGatt.STATE_DISCONNECTING) {
                    Logger.log(TAG, "设备已断开连接")
                    performDeviceDisconnectedListener()
                } else {
                    performGattStatusErrorListener(status)
                }
            }
            BluetoothGatt.STATE_CONNECTING -> {
                Logger.log(TAG, "设备正在连接")
            }
            BluetoothGatt.STATE_CONNECTED -> {
                if (!hasBluetoothConnectPermission()) {
                    Logger.log(TAG, "没有 BLUETOOTH_CONNECT 权限，拦截")
                    return
                }
                Logger.log(TAG, "设备已连接")
                performDeviceConnectedListener()
                Logger.log(TAG, "正在执行服务发现")
                if (!gatt.discoverServices()) {
                    Logger.log(TAG, "服务发现命令返回false，命令执行失败")
                    performAutoDiscoverServicesFailedListener()
                }
            }
            BluetoothGatt.STATE_DISCONNECTING -> {
                Logger.log(TAG, "设备正在断开连接")
            }
            else -> {
                Logger.log(TAG, "未知的设备状态:$newState")
                performGattUnknownStatusListener(newState)
            }
        }
    }

    /**
     * 当GATT发现服务完成时回调此方法
     *
     * @param gatt   GATT
     * @param status 状态码
     * successfully.
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Logger.log(TAG, "发现服务完成")
        BleManager.bleSingleConnector?.stopConnectTimeoutTimer()
        if (BluetoothGatt.GATT_SUCCESS != status) {
            performGattPerformTaskFailedListener(status, "onServicesDiscovered")
        } else {
            performDeviceServicesDiscoveredListener()
        }
    }

    /**
     * 读取GATT特征的数据的回调
     *
     * @param gatt           GATT
     * @param characteristic 特征
     * @param status         GATT状态
     * successfully.
     */
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        Logger.log(TAG, "onCharacteristicRead")
        //TODO
    }

    /**
     * 写入GATT特征数据的回调
     * 需要判断返回的数据是否与写入的数据是否一致，来确定数据是否写入成功
     *
     *
     * @param gatt           GATT
     * @param characteristic Characteristic
     * @param status         GATT状态码
     * operation succeeds.
     */
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        Logger.log(TAG, "onCharacteristicWrite")
        //TODO
    }

    /**
     * 特征的数据变化回调（通知事件）
     *
     * @param gatt           GATT
     * @param characteristic Characteristic
     */
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        Logger.log(TAG, "onReceivedNotification")
        //TODO
    }

    /**
     * 读取描述的回调
     *
     * @param gatt       GATT
     * @param descriptor Descriptor
     * @param status     GATT状态码
     * successfully
     */
    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        Logger.log(TAG, "onDescriptorRead")
        //TODO
    }

    /**
     * 写入描述的回调
     *
     * @param gatt       GATT
     * @param descriptor Descriptor
     * @param status     GATT状态码
     * operation succeeds.
     */
    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        Logger.log(TAG, "onDescriptorWrite")
        //TODO
    }

    /**
     * 使用可靠写入的回调
     *
     * @param gatt   GATT
     * @param status GATT状态码
     * executed successfully
     */
    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        Logger.log(TAG, "onReliableWriteCompleted")
        //TODO
    }

    /**
     * 获取设备RSSI（信号强度）的回调
     *
     *
     * @param gatt   GATT
     * @param rssi   RSSI
     * @param status GATT状态码
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        Logger.log(TAG, "onReadRemoteRssi")
        //TODO
    }

    /**
     * 传输的MTU（单包数据最大的传输字节数），通常默认为23，可用有效数据为20，GATT内部占用3字节
     *
     *
     * @param gatt   GATT
     * @param mtu    新的MTU值，需要与设置的值做对比，以判断是否设置成功
     * @param status GATT状态码
     */
    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        Logger.log(TAG, "onMtuChanged")
        //TODO

    }

    /**
     * 更新物理层的回调
     *
     * @param gatt   GATT
     * @param txPhy  数据发送物理层
     * @param rxPhy  数据接收物理层
     * @param status GATT状态码
     */
    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        Logger.log(TAG, "onPhyUpdate")
        //TODO
    }

    /**
     * 获取物理层信息的回调
     *
     * @param gatt   GATT
     * @param txPhy  数据发送物理层
     * @param rxPhy  数据接收物理层
     * @param status GATT状态码
     */
    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        Logger.log(TAG, "onPhyRead")
        //TODO
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private fun performDeviceDisconnectedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.disconnected()
        }
    }

    private fun performGattStatusErrorListener(status: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.gattStatusError(
                status
            )
        }
    }

    private fun performDeviceConnectedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.connected()
        }
    }

    private fun performAutoDiscoverServicesFailedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.autoDiscoverServicesFailed()
        }
    }

    private fun performGattUnknownStatusListener(newState: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.unknownStatus(
                newState
            )
        }
    }

    private fun performGattPerformTaskFailedListener(status: Int, methodName: String) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.gattPerformTaskFailed(
                status,
                methodName
            )
        }
    }

    private fun performDeviceServicesDiscoveredListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.servicesDiscovered()
        }
    }
}