package com.sscl.bluetoothlowenergylibrary.connetor.multi

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
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
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * BLE单个设备连接器
 */
class BleMultipleConnector internal constructor() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        private val TAG: String = BleMultipleConnector::class.java.simpleName

        /**
         * 默认的连接超时
         */
        private const val DEFAULT_CONNECT_TIMEOUT = 6000L
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 连接超时的定时器任务
     */
    private val connectTimeoutTimerRunnable = Runnable {
        BleManager.handler.post {
            onBleConnectStateChangedListener?.connectTimeout()
        }
    }

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 连接超时时间-默认值 6000毫秒（6秒）
     */
    private var connectTimeout = DEFAULT_CONNECT_TIMEOUT

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 连接超时定时器
     */
    private var connectTimeoutTimer: ScheduledExecutorService? = null

    /**
     * 连接状态回调
     */
    internal var onBleConnectStateChangedListener: OnBleConnectStateChangedListener? = null

    /**
     * 特征读取回调
     */
    internal var onCharacteristicReadDataListener: OnCharacteristicReadDataListener? = null

    /**
     * 特征写入回调
     */
    internal var onCharacteristicWriteDataListener: OnCharacteristicWriteDataListener? = null

    /**
     * 特征通知回调
     */
    internal var onCharacteristicNotifyDataListener: OnCharacteristicNotifyDataListener? = null

    /**
     * 描述读取回调
     */
    internal var onDescriptorReadDataListener: OnDescriptorReadDataListener? = null

    /**
     * 描述写入回调
     */
    internal var onDescriptorWriteDataListener: OnDescriptorWriteDataListener? = null

    /**
     * 可靠数据写入成功回调
     */
    internal var onReliableWriteCompletedListener: OnReliableWriteCompletedListener? = null

    /**
     * 获取设备RSSI回调
     */
    internal var onReadRemoteRssiListener: OnReadRemoteRssiListener? = null

    /**
     * MTU变化回调
     */
    internal var onMtuChangedListener: OnMtuChangedListener? = null

    /**
     * 物理层读取回调
     */
    internal var onPhyReadListener: OnPhyReadListener? = null

    /**
     * 物理层变更的回调
     */
    internal var onPhyUpdateListener: OnPhyUpdateListener? = null

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

    /* * * * * * * * * * * * * * * * * * * 内部方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 停止连接超时定时器
     */
    internal fun stopConnectTimeoutTimer() {
        connectTimeoutTimer?.shutdownNow()
        connectTimeoutTimer = null
    }

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
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport? = null,
        phyMask: BleConnectPhyMask? = null
    ): Boolean {
        val singleConnectService = BleManager.singleConnectService ?: return false
        val result =
            singleConnectService.connect(
                address,
                autoReconnect,
                bleConnectTransport,
                phyMask
            )
        if (result) {
            startConnectTimeoutTimer()
        }
        return result
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
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport? = null,
        phyMask: BleConnectPhyMask? = null
    ): Boolean {
        val singleConnectService = BleManager.singleConnectService ?: return false
        val result = singleConnectService.connect(
            bluetoothDevice,
            autoReconnect,
            bleConnectTransport,
            phyMask
        )
        if (result) {
            startConnectTimeoutTimer()
        }
        return result
    }

    /**
     * 断开连接
     * @return 是否执行成功
     */
    fun disconnect(): Boolean {
        val singleConnectService = BleManager.singleConnectService ?: return false
        return singleConnectService.disconnect()
    }

    /**
     * 设置连接超时时间，单位：毫秒
     * 设置为0表示使用默认值
     * @param connectTimeout  连接超时时间，单位：毫秒
     */
    fun setConnectTimeout(connectTimeout: Long) {
        if (connectTimeout > 0) {
            this.connectTimeout = connectTimeout
        } else {
            this.connectTimeout = DEFAULT_CONNECT_TIMEOUT
        }
    }

    /**
     * 设置蓝牙连接回调
     * @param onBleConnectStateChangedListener 蓝牙连接回调
     */
    fun setOnBleConnectStateChangedListener(onBleConnectStateChangedListener: OnBleConnectStateChangedListener?) {
        this.onBleConnectStateChangedListener = onBleConnectStateChangedListener
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicReadDataListener 特征数据读取回调
     */
    fun setOnCharacteristicReadDataListener(onCharacteristicReadDataListener: OnCharacteristicReadDataListener?) {
        this.onCharacteristicReadDataListener = onCharacteristicReadDataListener
    }

    /**
     * 设置MTU变化的回调
     * @param onMtuChangedListener MTU变化的回调
     */
    fun setOnMtuChangedListener(onMtuChangedListener: OnMtuChangedListener?) {
        this.onMtuChangedListener = onMtuChangedListener
    }

    /**
     * 设置物理层读取回调
     * @param onPhyReadListener 物理层读取回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setOnPhyReadListener(onPhyReadListener: OnPhyReadListener?) {
        this.onPhyReadListener = onPhyReadListener
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicWriteDataListener 特征数据写入回调
     */
    fun setOnCharacteristicWriteDataListener(onCharacteristicWriteDataListener: OnCharacteristicWriteDataListener?) {
        this.onCharacteristicWriteDataListener = onCharacteristicWriteDataListener
    }

    /**
     * 设置特征数据读取回调
     * @param onCharacteristicNotifyDataListener 特征数据通知回调
     */
    fun setOnCharacteristicNotifyDataListener(onCharacteristicNotifyDataListener: OnCharacteristicNotifyDataListener?) {
        this.onCharacteristicNotifyDataListener = onCharacteristicNotifyDataListener
    }

    /**
     * 设置描述读取回调
     * @param onDescriptorReadDataListener 描述读取回调
     */
    fun setOnDescriptorReadDataListener(onDescriptorReadDataListener: OnDescriptorReadDataListener?) {
        this.onDescriptorReadDataListener = onDescriptorReadDataListener
    }

    /**
     * 设置描述写入回调
     * @param onDescriptorWriteDataListener 描述写入回调
     */
    fun setOnDescriptorWriteDataListener(onDescriptorWriteDataListener: OnDescriptorWriteDataListener?) {
        this.onDescriptorWriteDataListener = onDescriptorWriteDataListener
    }

    /**
     * 设置可靠数据写入回调
     * @param onReliableWriteCompletedListener 可靠数据写入回调
     */
    fun setOnReliableWriteCompletedListener(onReliableWriteCompletedListener: OnReliableWriteCompletedListener?) {
        this.onReliableWriteCompletedListener = onReliableWriteCompletedListener
    }

    /**
     * 设置设备RSSI读取回调
     * @param onReadRemoteRssiListener 可靠数据写入回调
     */
    fun setOnReadRemoteRssiListener(onReadRemoteRssiListener: OnReadRemoteRssiListener?) {
        this.onReadRemoteRssiListener = onReadRemoteRssiListener
    }

    /**
     * 设置设备物理层变更的回调
     * @param onPhyUpdateListener 设备物理层变更的回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setOnPhyUpdateListener(onPhyUpdateListener: OnPhyUpdateListener?) {
        this.onPhyUpdateListener = onPhyUpdateListener
    }

    /**
     * 发现服务
     * @return 是否执行成功
     */
    fun discoverServices(): Boolean {
        return BleManager.singleConnectService?.discoverServices() ?: false
    }

    /**
     * 获取服务列表
     * @return 服务列表
     */
    fun getServices(): MutableList<BluetoothGattService>? {
        val singleConnectService = BleManager.singleConnectService ?: return null
        return singleConnectService.getServices()
    }

    /**
     * 通过UUID获取服务
     *
     * @param uuid UUID
     * @return GATT服务
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getService(uuid: UUID): BluetoothGattService? {
        return BleManager.singleConnectService?.getService(uuid)
    }

    /**
     * 读取特征数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @return  是否执行成功
     */
    fun readCharacteristicData(
        serviceUuidString: String,
        characteristicUuidString: String
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return readCharacteristicData(characteristic)
    }

    /**
     * 读取特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun readCharacteristicData(characteristic: BluetoothGattCharacteristic): Boolean {
        return BleManager.singleConnectService?.readCharacteristicData(characteristic) ?: false
    }

    /**
     * 关闭全部占用的资源
     */
    fun close() {
        disconnect()
        closeGatt()
    }

    /**
     * 关闭GATT
     * 除非出现gatt err
     * @return  是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun closeGatt(): Boolean {
        return BleManager.singleConnectService?.closeGatt() ?: false
    }

    /**
     * 写入特征数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param byteArray 数据内容
     * @return 是否执行成功
     */
    fun writeCharacteristicData(
        serviceUuidString: String,
        characteristicUuidString: String,
        byteArray: ByteArray
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return writeCharacteristicData(characteristic, byteArray)
    }

    /**
     * 写入特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @param byteArray 数据内容
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun writeCharacteristicData(
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ): Boolean {
        return BleManager.singleConnectService?.writeCharacteristicData(characteristic, byteArray)
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
        serviceUuidString: String,
        characteristicUuidString: String,
        enable: Boolean
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        return enableNotification(characteristic, enable)
    }

    /**
     * 打开通知
     * @param characteristic BluetoothGattCharacteristic
     * @param enable 是否开启通知
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun enableNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        return BleManager.singleConnectService?.enableNotification(characteristic, enable) ?: false
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
    fun beginReliableWrite(): Boolean {
        return BleManager.singleConnectService?.beginReliableWrite() ?: false
    }

    /**
     * 取消本次可靠写入模式下写入的数据
     * @return 是否取消成功
     */
    fun abortReliableWrite(): Boolean {
        return BleManager.singleConnectService?.abortReliableWrite() ?: false
    }

    /**
     * 将可靠模式下写入的数据应用到设备中
     * 将会触发回调-onReliableWriteCompleted
     * @return 是否请求成功
     */
    fun executeReliableWrite(): Boolean {
        return BleManager.singleConnectService?.executeReliableWrite() ?: false
    }

    /**
     * 读取描述数据
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @param descriptorUuidString 描述UUID字符串
     * @return 是否执行成功
     */
    fun readDescriptorData(
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        val descriptor =
            characteristic.getDescriptor(UUID.fromString(descriptorUuidString)) ?: return false
        return readDescriptorData(descriptor)
    }

    /**
     * 读取描述数据
     * @param descriptor BluetoothGattDescriptor
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun readDescriptorData(descriptor: BluetoothGattDescriptor): Boolean {
        return BleManager.singleConnectService?.readDescriptorData(descriptor) ?: return false
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
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String,
        permissions: Int
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
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
        return BleManager.singleConnectService?.checkDescriptorPermission(descriptor, permissions)
            ?: false
    }

    /**
     *判断某个特征是否有对应的属性
     * @param serviceUuidString 服务UUID字符串
     * @param characteristicUuidString 特征UUID字符串
     * @return 是否有对应的属性
     */
    fun checkCharacteristicProperties(
        serviceUuidString: String,
        characteristicUuidString: String,
        properties: Int
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
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
        return BleManager.singleConnectService?.checkCharacteristicProperty(
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
        serviceUuidString: String,
        characteristicUuidString: String,
        descriptorUuidString: String,
        value: ByteArray
    ): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false
        val descriptor =
            characteristic.getDescriptor(UUID.fromString(descriptorUuidString)) ?: return false
        return writeDescriptorData(descriptor, value)
    }

    /**
     * 写入描述文件数据
     * @param descriptor BluetoothGattDescriptor
     * @return 是否执行成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun writeDescriptorData(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean {
        return BleManager.singleConnectService?.writeDescriptorData(descriptor, value)
            ?: return false
    }

    /**
     * 读取设备RSSI
     */
    fun readRemoteRssi(): Boolean {
        return BleManager.singleConnectService?.readRemoteRssi() ?: false
    }

    /**
     * 请求MTU
     * @param mtu MTU值
     */
    fun requestMtu(mtu: Int): Boolean {
        return BleManager.singleConnectService?.requestMtu(mtu) ?: false
    }

    /**
     * 读取物理层
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun readPhy(): Boolean {
        return BleManager.singleConnectService?.readPhy() ?: false
    }

    /**
     * 设置物理层偏好
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPreferredPhy(txPhy: BlePhy, rxPhy: BlePhy, phyOptions: BlePhyOptions): Boolean {
        return BleManager.singleConnectService?.setPreferredPhy(txPhy, rxPhy, phyOptions) ?: false
    }

    /* * * * * * * * * * * * * * * * * * * 私有方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 开启连接超时定时器
     */
    private fun startConnectTimeoutTimer() {
        stopConnectTimeoutTimer()
        connectTimeoutTimer = BleManager.newScheduledThreadPoolExecutor()
        connectTimeoutTimer?.schedule(
            connectTimeoutTimerRunnable,
            connectTimeout,
            TimeUnit.MILLISECONDS
        )
    }
}