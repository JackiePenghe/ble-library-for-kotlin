package com.sscl.bluetoothlowenergylibrary.connetor.multi

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
internal class BleMultiBluetoothGattCallback : BluetoothGattCallback() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        /**
         * TAG
         */
        private val TAG = BleMultiBluetoothGattCallback::class.java.simpleName
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
        val multiConnectService = BleManager.multiConnectService ?: return
        val address = gatt.device.address
        if (multiConnectService.bluetoothGatts[address] == null) {
            Logger.log(TAG, "收到一个回调，但GATT不在缓存列表中")
            return
        }
        when (newState) {
            BluetoothGatt.STATE_DISCONNECTED -> {
                Logger.log(TAG, "status = $status")
                if (status == BluetoothGatt.STATE_CONNECTED || status == BluetoothGatt.STATE_CONNECTING || status == BluetoothGatt.STATE_DISCONNECTED || status == BluetoothGatt.STATE_DISCONNECTING) {
                    Logger.log(TAG, "设备已断开连接")
                    performDeviceDisconnectedListener(address)
                } else {
                    performGattStatusErrorListener(address, status)
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
                performDeviceConnectedListener(address)
                Logger.log(TAG, "正在执行服务发现")
                if (!gatt.discoverServices()) {
                    Logger.log(TAG, "服务发现命令返回false，命令执行失败")
                    performAutoDiscoverServicesFailedListener(address)
                }
            }
            BluetoothGatt.STATE_DISCONNECTING -> {
                Logger.log(TAG, "设备正在断开连接")
            }
            else -> {
                Logger.log(TAG, "未知的设备状态:$newState")
                performGattUnknownStatusListener(address, newState)
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
        val address = gatt.device.address
        BleManager.multiConnectService?.stopConnectTimeoutTimer(address)
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "发现服务失败 status $status")
            performGattStatusErrorListener(address, status)
        } else {
            Logger.log(TAG, "发现服务完成")
            performDeviceServicesDiscoveredListener(address)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "读取特征数据失败")
            performGattStatusErrorListener(address, status)
        } else {
            val value = characteristic.value
            Logger.log(TAG, "读取特征数据成功 value = ${value.toHexString()}")
            performDeviceCharacteristicReadListener(address, characteristic, value)
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
        val address = gatt.device.address
        Logger.log(TAG, "onCharacteristicWrite")
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入特征数据失败")
            performGattStatusErrorListener(address, status)
        } else {
            val value = characteristic.value
            Logger.log(TAG, "写入特征数据成功 value = ${value.toHexString()}")
            performDeviceCharacteristicWriteListener(address, characteristic, value)
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
        performDeviceCharacteristicNotifyListener(gatt.device.address, characteristic, value)
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
        val address = gatt.device.address
        Logger.log(TAG, "onDescriptorRead")
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "读取描述数据失败")
            performGattStatusErrorListener(address, status)
        } else {
            val value = descriptor.value
            Logger.log(TAG, "读取描述数据成功 value = ${value.toHexString()}")
            performDeviceDescriptorReadListener(address, descriptor, value)
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
        val address = gatt.device.address
        Logger.log(TAG, "onDescriptorWrite")
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入描述数据失败")
            performGattStatusErrorListener(address, status)
        } else {
            val value = descriptor.value
            Logger.log(TAG, "写入描述数据成功 value = ${value.toHexString()}")
            performDeviceDescriptorWriteListener(address, descriptor, value)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "写入可靠数据失败")
            performGattStatusErrorListener(address, status)
        } else {
            performReliableWriteCompletedListener(address)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "获取设备RSSI失败")
            performGattStatusErrorListener(address, status)
        } else {
            performReadRemoteRssiListener(address, rssi)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "设备MTU变更失败")
            performGattStatusErrorListener(address, status)
        } else {
            performMtuChangedListener(address, mtu)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "设备MTU变更失败")
            performGattStatusErrorListener(address, status)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performPhyUpdateListener(address, txPhy, rxPhy)
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
        val address = gatt.device.address
        if (BluetoothGatt.GATT_SUCCESS != status) {
            Logger.log(TAG, "获取物理层信息失败")
            performGattStatusErrorListener(address, status)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performPhyReadListener(address, txPhy, rxPhy)
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

    private fun performDeviceDisconnectedListener(address: String) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.onDisconnected()
        }
    }

    private fun performGattStatusErrorListener(address: String, status: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.gattStatusError(
                status
            )
        }
    }

    private fun performDeviceConnectedListener(address: String) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.onConnected()
        }
    }

    private fun performAutoDiscoverServicesFailedListener(address: String) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.autoDiscoverServicesFailed()
        }
    }

    private fun performGattUnknownStatusListener(address: String, newState: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.unknownConnectStatus(
                newState
            )
        }
    }

    private fun performDeviceServicesDiscoveredListener(address: String) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onBleConnectStateChangedListeners[address]?.onServicesDiscovered()
        }
    }

    private fun performDeviceCharacteristicReadListener(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onCharacteristicReadDataListeners[address]?.onCharacteristicReadData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceCharacteristicWriteListener(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onCharacteristicWriteDataListeners[address]?.onCharacteristicWriteData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceCharacteristicNotifyListener(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onCharacteristicNotifyDataListeners[address]?.onCharacteristicNotifyData(
                characteristic,
                value
            )
        }
    }

    private fun performDeviceDescriptorReadListener(
        address: String,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onDescriptorReadDataListeners[address]?.onDescriptorReadData(
                descriptor,
                value
            )
        }
    }

    private fun performDeviceDescriptorWriteListener(
        address: String,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onDescriptorWriteDataListeners[address]?.onDescriptorWriteData(
                descriptor,
                value
            )
        }
    }

    private fun performReliableWriteCompletedListener(address: String) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onReliableWriteCompletedListeners[address]?.onReliableWriteCompleted()
        }
    }

    private fun performReadRemoteRssiListener(address: String, rssi: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onReadRemoteRssiListeners[address]?.onReadRemoteRssi(rssi)
        }
    }

    private fun performMtuChangedListener(address: String, mtu: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onMtuChangedListeners[address]?.onMtuChanged(mtu)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performPhyReadListener(address: String, txPhy: Int, rxPhy: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onPhyReadListeners[address]?.onPhyRead(
                BlePhy.fromValue(
                    txPhy
                ), BlePhy.fromValue(rxPhy)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performPhyUpdateListener(address: String, txPhy: Int, rxPhy: Int) {
        val bluetoothLeMultiConnectService = BleManager.multiConnectService ?: return
        BleManager.handler.post {
            bluetoothLeMultiConnectService.onPhyUpdateListeners[address]?.onPhyUpdate(
                BlePhy.fromValue(
                    txPhy
                ), BlePhy.fromValue(rxPhy)
            )
        }
    }
}