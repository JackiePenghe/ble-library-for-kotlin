package com.sscl.bluetoothlowenergylibrary.connetor.single

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.hasBluetoothConnectPermission
import com.sscl.bluetoothlowenergylibrary.toHexString

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
        BleManager.bleSingleConnector?.stopConnectTimeoutTimer()
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "发现服务失败 status $status")
            performGattStatusErrorListener(status)
        } else {
            Logger.log(TAG, "发现服务完成")
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "读取特征数据失败")
            performGattStatusErrorListener(status)
        } else {
            val value = characteristic.value
            Logger.log(TAG, "读取特征数据成功 value = ${value.toHexString()}")
            performDeviceCharacteristicReadListener(characteristic, value)
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入特征数据失败")
            performGattStatusErrorListener(status)
        } else {
            val value = characteristic.value
            Logger.log(TAG, "写入特征数据成功 value = ${value.toHexString()}")
            performDeviceCharacteristicWriteListener(characteristic, value)
        }
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
        val value = characteristic.value
        Logger.log(TAG, "特征数据有通知 value = ${value.toHexString()}")
        performDeviceCharacteristicNotifyListener(characteristic, value)
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "读取描述数据失败")
            performGattStatusErrorListener(status)
        } else {
            val value = descriptor.value
            Logger.log(TAG, "读取描述数据成功 value = ${value.toHexString()}")
            performDeviceDescriptorReadListener(descriptor, value)
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入描述数据失败")
            performGattStatusErrorListener(status)
        } else {
            val value = descriptor.value
            Logger.log(TAG, "写入描述数据成功 value = ${value.toHexString()}")
            performDeviceDescriptorWriteListener(descriptor, value)
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入可靠数据失败")
            performGattStatusErrorListener(status)
        } else {
            performReliableWriteCompletedListener()
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "获取设备RSSI失败")
            performGattStatusErrorListener(status)
        } else {
            performReadRemoteRssiListener(rssi)
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "设备MTU变更失败")
            performGattStatusErrorListener(status)
        } else {
            performMtuChangedListener(mtu)
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "设备MTU变更失败")
            performGattStatusErrorListener(status)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performPhyUpdateListener(txPhy, rxPhy)
            } else {
                Logger.log(TAG, "变更物理层信息回调，系统版本过低，未处理")
            }
        }
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
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "获取物理层信息失败")
            performGattStatusErrorListener(status)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performPhyReadListener(txPhy, rxPhy)
            } else {
                Logger.log(TAG, "获取物理层信息成功，系统版本过低，未处理")
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private fun performDeviceDisconnectedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.onDisconnected()
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
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.onConnected()
        }
    }

    private fun performAutoDiscoverServicesFailedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.autoDiscoverServicesFailed()
        }
    }

    private fun performGattUnknownStatusListener(newState: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.unknownConnectStatus(
                newState
            )
        }
    }

    private fun performDeviceServicesDiscoveredListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onBleConnectStateChangedListener?.onServicesDiscovered()
        }
    }

    private fun performDeviceCharacteristicReadListener(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onCharacteristicReadDataListener?.onCharacteristicReadData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceCharacteristicWriteListener(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onCharacteristicWriteDataListener?.onCharacteristicWriteData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceCharacteristicNotifyListener(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onCharacteristicNotifyDataListener?.onCharacteristicNotifyData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceDescriptorReadListener(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onDescriptorReadDataListener?.onDescriptorReadData(
                descriptor,
                value
            )
        }
    }

    private fun performDeviceDescriptorWriteListener(
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onDescriptorWriteDataListener?.onDescriptorWriteData(
                descriptor,
                value
            )
        }
    }

    private fun performReliableWriteCompletedListener() {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onReliableWriteCompletedListener?.onReliableWriteCompleted()
        }
    }

    private fun performReadRemoteRssiListener(rssi: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onReadRemoteRssiListener?.onReadRemoteRssi(rssi)
        }
    }

    private fun performMtuChangedListener(mtu: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onMtuChangedListener?.onMtuChanged(mtu)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performPhyReadListener(txPhy: Int, rxPhy: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onPhyReadListener?.onPhyRead(
                BlePhy.fromValue(
                    txPhy
                ), BlePhy.fromValue(rxPhy)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performPhyUpdateListener(txPhy: Int, rxPhy: Int) {
        BleManager.handler.post {
            BleManager.bleSingleConnector?.onPhyUpdateListener?.onPhyUpdate(
                BlePhy.fromValue(
                    txPhy), BlePhy.fromValue(rxPhy)
            )
        }
    }
}