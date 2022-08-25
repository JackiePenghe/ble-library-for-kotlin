package com.sscl.bluetoothlowenergylibrary.connetor.single

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.checkBleSupport
import com.sscl.bluetoothlowenergylibrary.checkBluetoothSupport
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleConnectStateChangedListener
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * BLE单个设备连接器
 */
class BleSingleConnector internal constructor() {

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
    private var connectTimeout = 6000L

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 连接超时定时器
     */
    private var connectTimeoutTimer: ScheduledExecutorService? = null

    /**
     * 连接状态回调
     */
    internal var onBleConnectStateChangedListener: OnBleConnectStateChangedListener? = null

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
//            closed = false
            startConnectTimeoutTimer()
        }
        return result
    }

    /**
     * 断开连接
     */
    fun disconnect(): Boolean {
        val singleConnectService = BleManager.singleConnectService ?: return false
        return singleConnectService.disconnect()
    }

    /**
     * 设置连接超时时间，单位：毫秒
     */
    fun setConnectTimeout(connectTimeout: Long) {
        this.connectTimeout = connectTimeout
    }

    /**
     * 设置蓝牙连接回调
     */
    fun setOnBleConnectStateChangedListener(onBleConnectStateChangedListener: OnBleConnectStateChangedListener?) {
        this.onBleConnectStateChangedListener = onBleConnectStateChangedListener
    }

    /**
     * 发现服务
     */
    fun discoverServices() {
        BleManager.singleConnectService?.discoverServices()
    }

    /**
     * 获取服务列表
     */
    fun getServices(): MutableList<BluetoothGattService>? {
        val singleConnectService = BleManager.singleConnectService ?: return null
        return singleConnectService.getServices()
    }

    /**
     * 判断某个特征是否可读
     */
    fun canRead(serviceUuidString: String, characteristicUuidString: String): Boolean {
        val service = getService(UUID.fromString(serviceUuidString)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuidString))
            ?: return false

        return canRead(characteristic)
    }

    /**
     * 判断某个特征是否可读
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示可读
     */
    fun canRead(characteristic: BluetoothGattCharacteristic): Boolean {
        return BleManager.singleConnectService?.canRead(characteristic) ?: false
    }

    /**
     * 判断某个特征是否可写
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示可写
     */
    fun canWrite(serviceUUID: String, characteristicUUID: String): Boolean {
        val service = getService(UUID.fromString(serviceUUID)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID))
            ?: return false
        return canWrite(characteristic)
    }

    /**
     * 判断某个特征是否可写
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示可写
     */
    fun canWrite(characteristic: BluetoothGattCharacteristic): Boolean {
        return BleManager.singleConnectService?.canWrite(characteristic) ?: false
    }

    /**
     * 判断某个特征是否支持通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持通知
     */
    fun canNotify(serviceUUID: String, characteristicUUID: String): Boolean {
        val service = getService(UUID.fromString(serviceUUID)) ?: return false
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID))
            ?: return false
        return canNotify(characteristic)
    }

    /**
     * 判断某个特征是否支持通知
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示支持通知
     */
    fun canNotify(characteristic: BluetoothGattCharacteristic): Boolean {
        return BleManager.singleConnectService?.canNotify(characteristic) ?: false
    }

    /**
     * 通过UUID获取服务
     *
     * @param uuid UUID
     * @return GATT服务
     */
    fun getService(uuid: UUID): BluetoothGattService? {
        return BleManager.singleConnectService?.getService(uuid)
    }

    /**
     * 关闭全部占用的资源
     */
    fun close() {
        disconnect()
        closeGatt()
        return
    }

    /**
     * 关闭GATT
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun closeGatt():Boolean {
        return BleManager.singleConnectService?.closeGatt()?: false
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