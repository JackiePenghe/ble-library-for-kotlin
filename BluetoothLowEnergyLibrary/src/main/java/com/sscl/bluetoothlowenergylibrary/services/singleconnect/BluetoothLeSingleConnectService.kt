package com.sscl.bluetoothlowenergylibrary.services.singleconnect

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger
import com.sscl.bluetoothlowenergylibrary.connetor.single.BleBluetoothGattCallback
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.hasBluetoothConnectPermission
import com.sscl.bluetoothlowenergylibrary.isValidBluetoothAddress
import com.sscl.bluetoothlowenergylibrary.utils.BleConstants
import java.util.*

class BluetoothLeSingleConnectService : Service() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        private val TAG: String = BluetoothLeSingleConnectService::class.java.simpleName
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * f* */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * BLE GATT回调
     */
    private val bleBluetoothGattCallback = BleBluetoothGattCallback()

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 服务Binder类
     */
    private lateinit var bluetoothLeSingleConnectServiceBinder: BluetoothLeSingleConnectServiceBinder

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * Bluetooth Gatt
     */
    internal var bluetoothGatt: BluetoothGatt? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onBind(intent: Intent): IBinder {
        return bluetoothLeSingleConnectServiceBinder
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate() {
        super.onCreate()
        bluetoothLeSingleConnectServiceBinder = BluetoothLeSingleConnectServiceBinder(this)
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
        autoReconnect: Boolean,
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
        return connect(remoteDevice, autoReconnect, bleConnectTransport, bleConnectPhyMask)
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
        autoReconnect: Boolean = false,
        bleConnectTransport: BleConnectTransport?,
        bleConnectPhyMask: BleConnectPhyMask?
    ): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "发起连接失败，没有BLUETOOTH_CONNECT权限")
            return false
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothDevice.connectGatt(
                this,
                autoReconnect,
                bleBluetoothGattCallback,
                bleConnectTransport?.value ?: BleConnectTransport.TRANSPORT_AUTO.value,
                bleConnectPhyMask?.value ?: BleConnectPhyMask.PHY_LE_1M_MASK.value
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(
                this,
                autoReconnect,
                bleBluetoothGattCallback,
                bleConnectTransport?.value ?: BleConnectTransport.TRANSPORT_AUTO.value
            )
        } else {
            bluetoothDevice.connectGatt(this, autoReconnect, bleBluetoothGattCallback)
        }
        val result = bluetoothGatt != null
        Logger.log(TAG, "连接设备-发起请求结果 $result")
        return result
    }

    /**
     * 发现服务
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun discoverServices(): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "没有 BLUETOOTH_CONNECT 权限，discoverServices 失败")
            return false
        }
        val result = bluetoothGatt?.discoverServices() ?: false
        Logger.log(TAG, "discoverServices result $result")
        return result
    }

    /**
     * 断开设备连接
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun disconnect(): Boolean {
        if (!hasBluetoothConnectPermission()) {
            Logger.log(TAG, "没有 BLUETOOTH_CONNECT 权限，disconnect 失败")
            return false
        }
        bluetoothGatt?.disconnect()
        val result: Boolean = bluetoothGatt != null
        Logger.log(TAG, "disconnect result $result")
        return result
    }

    /**
     * 获取服务列表
     * @return 服务列表
     */
    internal fun getServices(): MutableList<BluetoothGattService>? {
        val result = bluetoothGatt?.services
        Logger.log(TAG, "getServices result $result")
        return result
    }

    /**
     * 通过UUID获取GATT服务
     *
     * @param uuid UUID
     * @return GATT服务
     */
    internal fun getService(uuid: UUID): BluetoothGattService? {
        val result = bluetoothGatt?.getService(uuid)
        Logger.log(TAG, "getService by uuid $uuid result $result")
        return result
    }

    /**
     * 检查特征是否可读
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示可读
     */
    internal fun canRead(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        val result = properties and BluetoothGattCharacteristic.PROPERTY_READ != 0
        Logger.log(TAG, "检查特征是否可读 characteristic ${characteristic.uuid} result $result")
        return result
    }

    /**
     * 检查特征是否可写
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示可写
     */
    internal fun canWrite(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        val result = properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
        Logger.log(TAG, "检查特征是否可写 characteristic ${characteristic.uuid} result $result")
        return result
    }

    /**
     * 检查特征是否支持通知
     *
     * @param characteristic BluetoothGattCharacteristic
     * @return true表示支持通知
     */
    internal fun canNotify(characteristic: BluetoothGattCharacteristic): Boolean {
        val properties = characteristic.properties
        val result = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
        Logger.log(TAG, "检查特征是否支持通知 characteristic ${characteristic.uuid} result $result ")
        return result
    }

    /**
     * 关闭GATT
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun closeGatt(): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            bluetoothGatt?.close()
            bluetoothGatt != null
        }
        bluetoothGatt = null
        Logger.log(TAG, "关闭GATT result $result")
        return result
    }

    /**
     * 读取数据
     * @param characteristic BluetoothGattCharacteristic
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun readData(characteristic: BluetoothGattCharacteristic): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            if (!canRead(characteristic)) {
                false
            } else {
                bluetoothGatt?.readCharacteristic(characteristic)
                bluetoothGatt != null
            }
        }
        Logger.log(TAG, "读取特征数据 result $result")
        return result
    }

    /**
     * 写入数据
     * @param characteristic BluetoothGattCharacteristic
     * @param byteArray 数据
     * @return 是否执行成功
     */
    @SuppressLint("MissingPermission")
    internal fun writeData(
        characteristic: BluetoothGattCharacteristic,
        byteArray: ByteArray
    ): Boolean {
        val result = if (!hasBluetoothConnectPermission()) {
            false
        } else {
            if (!canWrite(characteristic)) {
                false
            } else {
                characteristic.value = byteArray
                bluetoothGatt?.writeCharacteristic(characteristic) ?: false
            }
        }
        Logger.log(TAG, "写入特征数据 result $result")
        return result
    }

    /**
     * 打开通知
     */
    @SuppressLint("MissingPermission")
    internal fun enableNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        val result: Boolean
        if (!hasBluetoothConnectPermission()) {
            result = false
        } else {
            if (!canNotify(characteristic)) {
                result = false
            } else {
                val cacheResult =
                    bluetoothGatt?.setCharacteristicNotification(characteristic, enable) ?: false
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
                        result = bluetoothGatt?.writeDescriptor(bluetoothGattDescriptor) ?: false
                    }
                }
            }
        }
        Logger.log(TAG, "打开通知 result $result")
        return result
    }
}