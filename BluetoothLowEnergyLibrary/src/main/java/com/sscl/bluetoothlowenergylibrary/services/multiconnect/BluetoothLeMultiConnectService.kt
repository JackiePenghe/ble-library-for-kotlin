package com.sscl.bluetoothlowenergylibrary.services.multiconnect

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger
import com.sscl.bluetoothlowenergylibrary.connetor.multi.BleMultiBluetoothGattCallback
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.enums.BlePhyOptions
import com.sscl.bluetoothlowenergylibrary.hasBluetoothConnectPermission
import com.sscl.bluetoothlowenergylibrary.intefaces.*
import com.sscl.bluetoothlowenergylibrary.isValidBluetoothAddress
import com.sscl.bluetoothlowenergylibrary.utils.BleConstants
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class BluetoothLeMultiConnectService : Service() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        private val TAG: String = BluetoothLeMultiConnectService::class.java.simpleName

        internal const val DEFAULT_CONNECT_TIMEOUT = 6000L
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * f* */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙连接回调
     */
    private val bleMultiBluetoothGattCallback = BleMultiBluetoothGattCallback()

    /**
     * Bluetooth Gatt
     */
    internal val bluetoothGatts = HashMap<String, BluetoothGatt>()

    /**
     * 连接超时定时器
     */
    private val connectTimeoutTimers = HashMap<String, ScheduledExecutorService?>()

    /**
     * 连接状态回调
     */
    internal val onBleConnectStateChangedListeners =
        HashMap<String, OnBleConnectStateChangedListener>()

    /**
     * 特征读取回调
     */
    internal val onCharacteristicReadDataListeners =
        HashMap<String, OnCharacteristicReadDataListener>()

    /**
     * 特征写入回调
     */
    internal val onCharacteristicWriteDataListeners =
        HashMap<String, OnCharacteristicWriteDataListener>()

    /**
     * 特征通知回调
     */
    internal val onCharacteristicNotifyDataListeners =
        HashMap<String, OnCharacteristicNotifyDataListener>()

    /**
     * 描述读取回调
     */
    internal val onDescriptorReadDataListeners =
        HashMap<String, OnDescriptorReadDataListener>()

    /**
     * 描述写入回调
     */
    internal val onDescriptorWriteDataListeners =
        HashMap<String, OnDescriptorWriteDataListener>()

    /**
     * 可靠数据写入成功回调
     */
    internal val onReliableWriteCompletedListeners =
        kotlin.collections.HashMap<String, OnReliableWriteCompletedListener>()

    /**
     * 获取设备RSSI回调
     */
    internal val onReadRemoteRssiListeners =
        kotlin.collections.HashMap<String, OnReadRemoteRssiListener>()

    /**
     * MTU变化回调
     */
    internal val onMtuChangedListeners = kotlin.collections.HashMap<String, OnMtuChangedListener>()

    /**
     * 物理层读取回调
     */
    internal val onPhyReadListeners = kotlin.collections.HashMap<String, OnPhyReadListener>()

    /**
     * 物理层变更的回调
     */
    internal val onPhyUpdateListeners = kotlin.collections.HashMap<String, OnPhyUpdateListener>()

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 服务Binder类
     */
    private lateinit var bluetoothLeMultiConnectServiceBinder: BluetoothLeMultiConnectServiceBinder

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 连接超时时间-默认值 6000毫秒（6秒）
     */
    internal var connectTimeout = DEFAULT_CONNECT_TIMEOUT

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onBind(intent: Intent): IBinder {
        return bluetoothLeMultiConnectServiceBinder
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate() {
        super.onCreate()
        bluetoothLeMultiConnectServiceBinder = BluetoothLeMultiConnectServiceBinder(this)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 内部方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 连接设备
     * @param address 设备地址
     * @param autoReconnect 是否自动重连
     * @param bleConnectTransport  GATT连接到远程双模设备的首选传输方式
     * @param bleConnectPhyMask       用于连接到远程设备的首选物理层
     * @return 是否执行成功
     */
    @Synchronized
    internal fun connect(
        address: String,
        onBleConnectStateChangedListener: OnBleConnectStateChangedListener,
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport?,
        bleConnectPhyMask: BleConnectPhyMask?
    ): Boolean {
        if (!address.isValidBluetoothAddress()) {
            Logger.log(TAG, "设备地址不合法")
            return false
        }
        val bluetoothAdapter = if (BleManager.bluetoothAdapter != null) {
            BleManager.bluetoothAdapter!!
        } else {
            Logger.log(TAG, "蓝牙适配器")
            return false
        }
        val cacheDevice = bluetoothAdapter.getRemoteDevice(address)
        val remoteDevice = if (cacheDevice != null) {
            cacheDevice
        } else {
            Logger.log(TAG, "无法通过设备地址获取蓝牙设备信息")
            return false
        }
        return connect(
            remoteDevice,
            onBleConnectStateChangedListener,
            autoReconnect,
            bleConnectTransport,
            bleConnectPhyMask
        )
    }

    /**
     * 连接设备
     * @param bluetoothDevice 设备
     * @param autoReconnect 是否自动重连
     * @param bleConnectTransport  GATT连接到远程双模设备的首选传输方式
     * @param bleConnectPhyMask       用于连接到远程设备的首选物理层
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    internal fun connect(
        bluetoothDevice: BluetoothDevice,
        onBleConnectStateChangedListener: OnBleConnectStateChangedListener,
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport?,
        bleConnectPhyMask: BleConnectPhyMask?
    ): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "发起连接失败，没有BLUETOOTH_CONNECT权限")
            return false
        }
        var bluetoothGatt = bluetoothGatts[bluetoothDevice.address]
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatts.remove(bluetoothDevice.address)

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothDevice.connectGatt(
                this,
                autoReconnect,
                bleMultiBluetoothGattCallback,
                bleConnectTransport?.value ?: BleConnectTransport.TRANSPORT_AUTO.value,
                bleConnectPhyMask?.value ?: BleConnectPhyMask.PHY_LE_1M_MASK.value
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(
                this,
                autoReconnect,
                bleMultiBluetoothGattCallback,
                bleConnectTransport?.value ?: BleConnectTransport.TRANSPORT_AUTO.value
            )
        } else {
            bluetoothDevice.connectGatt(this, autoReconnect, bleMultiBluetoothGattCallback)
        }
        val result = bluetoothGatt != null
        Logger.log(TAG, "连接设备-发起请求结果 $result")
        if (result) {
            onBleConnectStateChangedListeners[bluetoothDevice.address] =
                onBleConnectStateChangedListener
            bluetoothGatts[bluetoothDevice.address] = bluetoothGatt
            startConnectTimeoutTimer(bluetoothDevice.address)
        }
        return result
    }

    /**
     * 发现服务
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun discoverServices(address: String): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "没有 BLUETOOTH_CONNECT 权限，discoverServices 失败")
            return false
        }
        val result = bluetoothGatts[address]?.discoverServices() ?: false
        Logger.log(TAG, "discoverServices result $result")
        return result
    }

    /**
     * 断开设备连接
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun disconnect(address: String): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "没有 BLUETOOTH_CONNECT 权限，disconnect 失败")
            return false
        }
        bluetoothGatts[address]?.disconnect()
        val result: Boolean = bluetoothGatts[address] != null
        Logger.log(TAG, "disconnect result $result")
        return result
    }

    /**
     * 获取服务列表
     * @return 服务列表
     */
    internal fun getServices(address: String): MutableList<BluetoothGattService>? {
        val result = bluetoothGatts[address]?.services
        Logger.log(TAG, "getServices result $result")
        return result
    }

    /**
     * 通过UUID获取GATT服务
     *
     * @param uuid UUID
     * @return GATT服务
     */
    internal fun getService(address: String, uuid: UUID): BluetoothGattService? {
        val result = bluetoothGatts[address]?.getService(uuid)
        Logger.log(TAG, "getService by uuid $uuid result ${result?.uuid}")
        return result
    }

    /**
     * 关闭GATT
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun closeGatt(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.close()
            bluetoothGatts[address] != null
        }
        bluetoothGatts.remove(address)
        Logger.log(TAG, "关闭GATT result $result")
        return result
    }

    /**
     * 读取特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun readCharacteristicData(
        address: String,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            if (!checkCharacteristicProperty(
                    characteristic,
                    BluetoothGattCharacteristic.PROPERTY_READ
                )
            ) {
                false
            } else {
                bluetoothGatts[address]?.readCharacteristic(characteristic) ?: false
            }
        }
        Logger.log(TAG, "读取特征数据 result $result")
        return result
    }

    /**
     * 写入特征数据
     * @param characteristic BluetoothGattCharacteristic
     * @param byteArray 数据
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun writeCharacteristicData(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            if (!checkCharacteristicProperty(
                    characteristic,
                    BluetoothGattCharacteristic.PROPERTY_WRITE
                )
            ) {
                false
            } else {
                if (!characteristic.setValue(byteArray)) {
                    false
                } else {
                    bluetoothGatts[address]?.writeCharacteristic(characteristic) ?: false
                }
            }
        }
        Logger.log(TAG, "写入特征数据 result $result")
        return result
    }

    /**
     * 打开通知
     * @param characteristic BluetoothGattCharacteristic
     * @param enable 是否打开通知
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun enableNotification(
        address: String,
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        val result: Boolean
        if (!hasBluetoothConnectPermission()) {
            result = false
        } else {
            if (!checkCharacteristicProperty(
                    characteristic,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY
                )
            ) {
                result = false
            } else {
                val cacheResult =
                    bluetoothGatts[address]?.setCharacteristicNotification(characteristic, enable)
                        ?: false
                if (!cacheResult) {
                    result = false
                } else {
                    val bluetoothGattDescriptor: BluetoothGattDescriptor? =
                        characteristic.getDescriptor(UUID.fromString(BleConstants.CLIENT_CHARACTERISTIC_CONFIG))
                    if (bluetoothGattDescriptor == null) {
                        Logger.log(TAG, "bluetoothGattDescriptor == null")
                        result = true
                    } else {
                        bluetoothGattDescriptor.value =
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        result = bluetoothGatts[address]?.writeDescriptor(bluetoothGattDescriptor)
                            ?: false
                    }
                }
            }
        }
        Logger.log(TAG, "打开通知 result $result")
        return result
    }

    /**
     * 为给定的远程设备启动可靠的写入事务。
     * 此方法并非全部BLE设备都支持,需要先确认设备是否处理了此方式的数据写入
     * 一旦启动了可靠的写入事务，所有对 [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.writeCharacteristicData] 的调用都会发送到远程设备进行验证并排队等待执行。
     * 应用程序将收到一个 [com.sscl.bluetoothlowenergylibrary.intefaces.OnCharacteristicWriteDataListener.onCharacteristicWriteData]回调
     * 回调以响应每个  [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.writeCharacteristicData] 调用，并负责验证该值是否已准确传输。
     * 在所有特征都排队并验证后，[com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.executeReliableWrite] 将执行所有写入。
     * 如果未正确写入特征，则调用 [com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector.abortReliableWrite] 将取消当前事务，而不在远程设备上提交任何值。
     * @return true，如果可靠的写事务已经启动
     */
    @SuppressLint("MissingPermission")
    internal fun beginReliableWrite(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            return false
        } else {
            bluetoothGatts[address]?.beginReliableWrite() ?: false
        }
        Logger.log(TAG, "开启可靠传输事务 result $result")
        return result
    }

    /**
     * 取消本次可靠写入模式下写入的数据
     * @return 是否取消成功
     */
    @SuppressLint("MissingPermission")
    internal fun abortReliableWrite(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            return false
        } else {
            bluetoothGatts[address]?.abortReliableWrite()
            bluetoothGatts[address] != null
        }
        Logger.log(TAG, "取消可靠传输事务 result $result")
        return result
    }

    /**
     * 将可靠模式下写入的数据应用到设备中
     * @return 是否请求成功
     */
    @SuppressLint("MissingPermission")
    internal fun executeReliableWrite(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.executeReliableWrite() ?: false
        }
        Logger.log(TAG, "应用可靠模式下写入的数据 result $result")
        return result
    }

    /**
     * 读取特征描述信息
     * @param descriptor BluetoothGattDescriptor
     * @return  是否成功
     */
    @SuppressLint("MissingPermission")
    internal fun readDescriptorData(address: String, descriptor: BluetoothGattDescriptor): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.readDescriptor(descriptor) ?: false
        }
        Logger.log(TAG, "读取特征描述信息 result $result")
        return result
    }

    /**
     * 写入特征描述信息
     * @param descriptor BluetoothGattDescriptor
     * @param value 数据内容
     * @return  是否成功
     */
    @SuppressLint("MissingPermission")
    internal fun writeDescriptorData(
        address: String,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            if (!descriptor.setValue(value)) {
                false
            } else {
                bluetoothGatts[address]?.writeDescriptor(descriptor) ?: false
            }
        }
        Logger.log(TAG, "读取特征描述信息 result $result")
        return result
    }

    /**
     * 判断某个描述是否有对应的权限
     * @param descriptor BluetoothGattDescriptor
     * @param permissions 要判断的权限
     * @return 是否有权限
     */
    internal fun checkDescriptorPermission(
        descriptor: BluetoothGattDescriptor,
        permissions: Int
    ): Boolean {
        val result = descriptor.permissions and permissions == permissions
        Logger.log(TAG, "检查描述是否有指定的权限 descriptor ${descriptor.uuid} result $result")
        return result
    }

    /**
     * 判断某个描述是否有对应的权限
     * @param characteristic BluetoothGattCharacteristic
     * @param properties BluetoothGattCharacteristic
     * @return 是否有对应属性
     */
    internal fun checkCharacteristicProperty(
        characteristic: BluetoothGattCharacteristic,
        properties: Int
    ): Boolean {
        val result = characteristic.properties and properties == properties
        Logger.log(TAG, "检查特征是否有指定的权限 characteristic ${characteristic.uuid} result $result")
        return result
    }

    /**
     *获取设备的RSSI
     * @return 是否请求成功
     */
    @SuppressLint("MissingPermission")
    internal fun readRemoteRssi(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.readRemoteRssi() ?: false
        }
        Logger.log(TAG, "读取设备RSSI result $result")
        return result
    }

    /**
     * 请求更改MTU大小
     * @param mtu MTU大小
     */
    @SuppressLint("MissingPermission")
    internal fun requestMtu(address: String, mtu: Int): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.requestMtu(mtu) ?: false
        }
        Logger.log(TAG, "请求更改MTU大小 result $result")
        return result
    }

    /**
     * 读取物理层
     * @return 是否请求成功
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    internal fun readPhy(address: String): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.readPhy()
            bluetoothGatts[address] != null
        }
        Logger.log(TAG, "读取当前连接的物理层 result $result")
        return result
    }

    /**
     * 设置偏好物理层
     *  @return 是否请求成功
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    internal fun setPreferredPhy(
        address: String,
        txPhy: BlePhy,
        rxPhy: BlePhy,
        phyOptions: BlePhyOptions
    ): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatts[address]?.setPreferredPhy(txPhy.value, rxPhy.value, phyOptions.value)
            bluetoothGatts[address] != null
        }
        Logger.log(TAG, "设置偏好物理层 result $result")
        return result
    }

    /**
     * 停止连接超时定时器
     */
    internal fun stopConnectTimeoutTimer(address: String?) {
        val scheduledExecutorService = connectTimeoutTimers[address]
        scheduledExecutorService?.shutdownNow()
        connectTimeoutTimers.remove(address)
    }

    /**
     * 关闭全部连接
     */
    internal fun closeAll() {
        val keys = bluetoothGatts.keys
        for (address in keys) {
            disconnect(address)
            closeGatt(address)
        }
    }

    /**
     * 释放资源
     */
    internal fun release(address: String) {
        onBleConnectStateChangedListeners.remove(address)
        onCharacteristicReadDataListeners.remove(address)
        onCharacteristicWriteDataListeners.remove(address)
        onCharacteristicNotifyDataListeners.remove(address)
        onDescriptorReadDataListeners.remove(address)
        onDescriptorWriteDataListeners.remove(address)
        onReliableWriteCompletedListeners.remove(address)
        onReadRemoteRssiListeners.remove(address)
        onMtuChangedListeners.remove(address)
        onPhyReadListeners.remove(address)
        onPhyUpdateListeners.remove(address)
    }

    /**
     * 释放全部资源
     */
    internal fun releaseAll() {
        onBleConnectStateChangedListeners.clear()
        onCharacteristicReadDataListeners.clear()
        onCharacteristicWriteDataListeners.clear()
        onCharacteristicNotifyDataListeners.clear()
        onDescriptorReadDataListeners.clear()
        onDescriptorWriteDataListeners.clear()
        onReliableWriteCompletedListeners.clear()
        onReadRemoteRssiListeners.clear()
        onMtuChangedListeners.clear()
        onPhyReadListeners.clear()
        onPhyUpdateListeners.clear()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 开启连接超时定时器
     */
    private fun startConnectTimeoutTimer(address: String) {
        stopConnectTimeoutTimer(address)
        val connectTimeoutTimer = BleManager.newScheduledThreadPoolExecutor()
        connectTimeoutTimer.schedule(
            {
                BleManager.handler.post {
                    onBleConnectStateChangedListeners[address]?.connectTimeout()
                }
                connectTimeoutTimers.remove(address)
            },
            connectTimeout,
            TimeUnit.MILLISECONDS
        )
        connectTimeoutTimers[address] = connectTimeoutTimer
    }
}