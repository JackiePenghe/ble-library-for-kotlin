@file:Suppress("unused")

package com.sscl.bluetoothlowenergylibrary.connetor.multi

import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.checkBleSupport
import com.sscl.bluetoothlowenergylibrary.checkBluetoothSupport
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.enums.BlePhyOptions
import com.sscl.bluetoothlowenergylibrary.intefaces.*
import com.sscl.bluetoothlowenergylibrary.services.multiconnect.BluetoothLeMultiConnectService
import java.util.*

class BleMultiConnector internal constructor() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    init {
        checkBluetoothSupport()
        checkBleSupport()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 方法声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 公开方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 请求连接设备
     *
     * @param address       设备地址
     * @param autoReconnect 是否自动重连
     * @param bleConnectTransport     GATT 连接到远程双模设备的首选传输方式
     * @param phyMask       用于连接到远程设备的首选物理层
     * @return true表示请求已成功发起
     */
    @Synchronized
    @JvmOverloads
    fun connect(
        address: String,
        onBleConnectStateChangedListener: OnBleConnectStateChangedListener,
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport? = null,
        phyMask: BleConnectPhyMask? = null
    ): Boolean {
        val multiConnectService = BleManager.multiConnectService ?: return false
        return multiConnectService.connect(
            address,
            onBleConnectStateChangedListener,
            autoReconnect,
            bleConnectTransport,
            phyMask
        )
    }

    /**
     * 请求连接设备
     *
     * @param bluetoothDevice 蓝牙设备
     * @param autoReconnect   是否自动重连
     * @param bleConnectTransport       GATT 连接到远程双模设备的首选传输方式
     * @param phyMask         用于连接到远程设备的首选物理层
     * @return true表示请求已成功发起，真正的连接结果在回调中
     */
    @Synchronized
    @JvmOverloads
    fun connect(
        bluetoothDevice: BluetoothDevice,
        onBleConnectStateChangedListener: OnBleConnectStateChangedListener,
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport? = null,
        phyMask: BleConnectPhyMask? = null
    ): Boolean {
        val multiConnectService = BleManager.multiConnectService ?: return false
        return multiConnectService.connect(
            bluetoothDevice,
            onBleConnectStateChangedListener,
            autoReconnect,
            bleConnectTransport,
            phyMask
        )
    }


    /**
     * 断开连接
     * @return 是否执行成功
     */
    fun disconnect(address: String): Boolean {
        val multiConnectService = BleManager.multiConnectService ?: return false
        return multiConnectService.disconnect(address)
    }

    /**
     * 设置连接超时时间，单位：毫秒
     * 设置为0表示使用默认值
     * @param connectTimeout  连接超时时间，单位：毫秒
     */
    fun setConnectTimeout(connectTimeout: Long) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (connectTimeout > 0) {
            multiConnectService.connectTimeout = connectTimeout
        } else {
            multiConnectService.connectTimeout =
                BluetoothLeMultiConnectService.DEFAULT_CONNECT_TIMEOUT
        }
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicReadDataListener 特征数据读取回调
     */
    fun setOnCharacteristicReadDataListener(
        address: String,
        onCharacteristicReadDataListener: OnCharacteristicReadDataListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onCharacteristicReadDataListener != null) {
            multiConnectService.onCharacteristicReadDataListeners[address] =
                onCharacteristicReadDataListener
        } else {
            multiConnectService.onCharacteristicReadDataListeners.remove(address)
        }
    }

    /**
     * 设置MTU变化的回调
     * @param onMtuChangedListener MTU变化的回调
     */
    fun setOnMtuChangedListener(address: String, onMtuChangedListener: OnMtuChangedListener?) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onMtuChangedListener != null) {
            multiConnectService.onMtuChangedListeners[address] = onMtuChangedListener
        } else {
            multiConnectService.onMtuChangedListeners.remove(address)
        }
    }

    /**
     * 设置物理层读取回调
     * @param onPhyReadListener 物理层读取回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setOnPhyReadListener(address: String, onPhyReadListener: OnPhyReadListener?) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onPhyReadListener != null) {
            multiConnectService.onPhyReadListeners[address] = onPhyReadListener
        } else {
            multiConnectService.onPhyReadListeners.remove(address)
        }
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicWriteDataListener 特征数据写入回调
     */
    fun setOnCharacteristicWriteDataListener(
        address: String,
        onCharacteristicWriteDataListener: OnCharacteristicWriteDataListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onCharacteristicWriteDataListener != null) {
            multiConnectService.onCharacteristicWriteDataListeners[address] =
                onCharacteristicWriteDataListener
        } else {
            multiConnectService.onCharacteristicWriteDataListeners.remove(address)
        }
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicNotifyDataListener 特征数据通知回调
     */
    fun setOnCharacteristicNotifyDataListener(
        address: String,
        onCharacteristicNotifyDataListener: OnCharacteristicNotifyDataListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onCharacteristicNotifyDataListener != null) {
            multiConnectService.onCharacteristicNotifyDataListeners[address] =
                onCharacteristicNotifyDataListener
        } else {
            multiConnectService.onCharacteristicNotifyDataListeners.remove(address)
        }
    }

    /**
     * 设置描述读取回调
     * @param onDescriptorReadDataListener 描述读取回调
     */
    fun setOnDescriptorReadDataListener(
        address: String,
        onDescriptorReadDataListener: OnDescriptorReadDataListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onDescriptorReadDataListener != null) {
            multiConnectService.onDescriptorReadDataListeners[address] =
                onDescriptorReadDataListener
        } else {
            multiConnectService.onDescriptorReadDataListeners.remove(address)
        }
    }

    /**
     * 设置描述写入回调
     * @param onDescriptorWriteDataListener 描述写入回调
     */
    fun setOnDescriptorWriteDataListener(
        address: String,
        onDescriptorWriteDataListener: OnDescriptorWriteDataListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onDescriptorWriteDataListener != null) {
            multiConnectService.onDescriptorWriteDataListeners[address] =
                onDescriptorWriteDataListener
        } else {
            multiConnectService.onDescriptorWriteDataListeners.remove(address)
        }
    }

    /**
     * 设置可靠数据写入回调
     * @param onReliableWriteCompletedListener 可靠数据写入回调
     */
    fun setOnReliableWriteCompletedListener(
        address: String,
        onReliableWriteCompletedListener: OnReliableWriteCompletedListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onReliableWriteCompletedListener != null) {
            multiConnectService.onReliableWriteCompletedListeners[address] =
                onReliableWriteCompletedListener
        } else {
            multiConnectService.onReliableWriteCompletedListeners.remove(address)
        }
    }

    /**
     * 设置设备RSSI读取回调
     * @param onReadRemoteRssiListener 可靠数据写入回调
     */
    fun setOnReadRemoteRssiListener(
        address: String,
        onReadRemoteRssiListener: OnReadRemoteRssiListener?
    ) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onReadRemoteRssiListener != null) {
            multiConnectService.onReadRemoteRssiListeners[address] =
                onReadRemoteRssiListener
        } else {
            multiConnectService.onReadRemoteRssiListeners.remove(address)
        }
    }

    /**
     * 设置设备物理层变更的回调
     * @param onPhyUpdateListener 设备物理层变更的回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setOnPhyUpdateListener(address: String, onPhyUpdateListener: OnPhyUpdateListener?) {
        val multiConnectService = BleManager.multiConnectService ?: return
        if (onPhyUpdateListener != null) {
            multiConnectService.onPhyUpdateListeners[address] =
                onPhyUpdateListener
        } else {
            multiConnectService.onPhyUpdateListeners.remove(address)
        }
    }

    /**
     * 发现服务
     * @return 是否执行成功
     */
    fun discoverServices(address: String): Boolean {
        return BleManager.multiConnectService?.discoverServices(address) ?: false
    }

    /**
     * 获取服务列表
     * @return 服务列表
     */
    fun getServices(address: String): MutableList<BluetoothGattService>? {
        val multiConnectService = BleManager.multiConnectService ?: return null
        return multiConnectService.getServices(address)
    }

    /**
     * 通过UUID获取服务
     *
     * @param uuid UUID
     * @return GATT服务
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getService(address: String, uuid: UUID): BluetoothGattService? {
        return BleManager.multiConnectService?.getService(address, uuid)
    }

    /**
     * 读取特征数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @return  是否执行成功
     */
    fun readCharacteristicData(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String
    ): Boolean {
        val service = getService(address, UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return readCharacteristicData(address, characteristic)
    }

    /**
     * 读取特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun readCharacteristicData(
        address: String,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        return BleManager.multiConnectService?.readCharacteristicData(address, characteristic)
            ?: false
    }

    /**
     * 关闭全部占用的资源
     */
    fun close(address: String) {
        disconnect(address)
        closeGatt(address)
    }

    /**
     * 释放资源
     */
    fun release(address: String){
       BleManager.multiConnectService?.release(address)
    }

    /**
     * 关闭全部占用的资源
     */
    fun closeAll() {
        BleManager.multiConnectService?.closeAll()
    }

    /**
     * 释放全部资源
     */
    fun releaseAll(){
        BleManager.multiConnectService?.releaseAll()
    }

    /**
     * 关闭GATT
     * 除非出现gatt err
     * @return  是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun closeGatt(address: String): Boolean {
        return BleManager.multiConnectService?.closeGatt(address) ?: false
    }

    /**
     * 写入特征数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param byteArray 数据内容
     * @return 是否执行成功
     */
    fun writeCharacteristicData(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        byteArray: ByteArray
    ): Boolean {
        val service = getService(address, UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return writeCharacteristicData(address, characteristic, byteArray)
    }

    /**
     * 写入特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @param byteArray 数据内容
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun writeCharacteristicData(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ): Boolean {
        return BleManager.multiConnectService?.writeCharacteristicData(
            address,
            characteristic,
            byteArray
        )
            ?: false
    }

    /**
     * 打开通知
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param enable 是否开启通知
     * @return 是否执行成功
     */
    fun enableNotification(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        enable: Boolean
    ): Boolean {
        val service = getService(address, UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return enableNotification(address, characteristic, enable)
    }

    /**
     * 打开通知
     * @param characteristic BluetoothGattCharacteristic
     * @param enable 是否开启通知
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun enableNotification(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        return BleManager.multiConnectService?.enableNotification(address, characteristic, enable)
            ?: false
    }

    /**
     * 为给定的远程设备启动可靠的写入事务。
     * 此方法并非全部BLE设备都支持,需要先确认设备是否处理了此方式的数据写入
     * 一旦启动了可靠的写入事务，所有对 [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.writeCharacteristicData] 的调用都会发送到远程设备进行验证并排队等待执行。
     * 应用程序将收到一个 [com.sscl.bluetoothlowenergylibrary.intefaces.OnCharacteristicWriteDataListener.onCharacteristicWriteData]回调
     * 回调以响应每个  [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.writeCharacteristicData] 调用，并负责验证该值是否已准确传输。
     * 在所有特征都排队并验证后，[com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.executeReliableWrite] 将执行所有写入。
     * 如果未正确写入特征，则调用 [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.abortReliableWrite] 将取消当前事务，而不在远程设备上提交任何值。
     * @return true 可靠的写事务已经启动
     */
    fun beginReliableWrite(address: String): Boolean {
        return BleManager.multiConnectService?.beginReliableWrite(address) ?: false
    }

    /**
     * 取消本次可靠写入模式下写入的数据
     * @return 是否取消成功
     */
    fun abortReliableWrite(address: String): Boolean {
        return BleManager.multiConnectService?.abortReliableWrite(address) ?: false
    }

    /**
     * 将可靠模式下写入的数据应用到设备中
     * 将会触发回调-onReliableWriteCompleted
     * @return 是否请求成功
     */
    fun executeReliableWrite(address: String): Boolean {
        return BleManager.multiConnectService?.executeReliableWrite(address) ?: false
    }

    /**
     * 读取描述数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param descriptorUuidString 描述UUID字符串
     * @return 是否执行成功
     */
    fun readDescriptorData(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String
    ): Boolean {
        val service = getService(address, UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        val descriptor =
            characteristic.getDescriptor(UUID.fromString(descriptorUuidString)) ?: return false
        return readDescriptorData(address, descriptor)
    }

    /**
     * 读取描述数据
     * @param descriptor BluetoothGattDescriptor
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun readDescriptorData(address: String, descriptor: BluetoothGattDescriptor): Boolean {
        return BleManager.multiConnectService?.readDescriptorData(address,descriptor) ?: return false
    }

    /**
     * 判断某个描述是否有相应的权限
     *
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param descriptorUuidString 描述UUID字符串
     * @param permissions 需要判断的权限,同时判断多个权限可用或运算传入参数
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM]
     *
     * @return 是否有相应的权限
     */
    fun checkDescriptorPermission(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String,
        permissions: Int
    ): Boolean {
        val service = getService(address,UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        val descriptor =
            characteristic.getDescriptor(UUID.fromString(descriptorUuidString)) ?: return false
        return checkDescriptorPermission(descriptor, permissions)
    }

    /**
     * 判断某个描述是否有相应的权限
     * @param descriptor BluetoothGattDescriptor
     * @param permissions 请传入以下参数,同时判断多个权限请使用或运算将权限传入
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM]
     *  [android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM]
     */
    fun checkDescriptorPermission(descriptor: BluetoothGattDescriptor, permissions: Int): Boolean {
        return BleManager.multiConnectService?.checkDescriptorPermission(descriptor, permissions)
            ?: false
    }

    /**
     *判断某个特征是否有对应的属性
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @return 是否有对应的属性
     */
    fun checkCharacteristicProperties(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        properties: Int
    ): Boolean {
        val service = getService(address,UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return checkCharacteristicProperties(characteristic, properties)
    }

    /**
     * 判断某个特征是否有对应的属性
     *  @param characteristic BluetoothGattCharacteristic
     *  @param properties 属性
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkCharacteristicProperties(
        characteristic: BluetoothGattCharacteristic,
        properties: Int
    ): Boolean {
        return BleManager.multiConnectService?.checkCharacteristicProperty(
            characteristic,
            properties
        ) ?: false
    }

    /**
     * 写入描述文件数据
     *
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param descriptorUuidString 描述UUID字符串
     * @return 是否执行成功
     */
    fun writeDescriptorData(
        address: String,
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String,
        value: ByteArray
    ): Boolean {
        val service = getService(address,UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        val descriptor =
            characteristic.getDescriptor(UUID.fromString(descriptorUuidString)) ?: return false
        return writeDescriptorData(address,descriptor, value)
    }

    /**
     * 写入描述文件数据
     * @param descriptor BluetoothGattDescriptor
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun writeDescriptorData(address: String,descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean {
        return BleManager.multiConnectService?.writeDescriptorData(address,descriptor, value)
            ?: return false
    }

    /**
     * 读取设备RSSI
     */
    fun readRemoteRssi(address: String): Boolean {
        return BleManager.multiConnectService?.readRemoteRssi(address) ?: false
    }

    /**
     * 请求MTU
     * @param mtu MTU值
     */
    fun requestMtu(address: String,mtu: Int): Boolean {
        return BleManager.multiConnectService?.requestMtu(address,mtu) ?: false
    }

    /**
     * 读取物理层
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun readPhy(address: String): Boolean {
        return BleManager.multiConnectService?.readPhy(address) ?: false
    }

    /**
     * 设置物理层偏好
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPreferredPhy(address: String,txPhy: BlePhy, rxPhy: BlePhy, phyOptions: BlePhyOptions): Boolean {
        return BleManager.multiConnectService?.setPreferredPhy(address,txPhy, rxPhy, phyOptions) ?: false
    }
}