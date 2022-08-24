package com.sscl.bluetoothlowenergylibrary

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.sscl.bluetoothlowenergylibrary.exceptions.BluetoothLENotSupportException
import java.util.concurrent.ThreadFactory

/**
 * BLE管理类
 */
@SuppressLint("StaticFieldLeak")
object BleManager {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * TAG
     */
    private val TAG: String = BleManager::class.java.simpleName

    /**
     * 线程工厂
     */
    internal val threadFactory = ThreadFactory { r -> Thread(r) }

    /**
     * handler
     */
    internal val handler = Handler(Looper.getMainLooper())

    /**
     * 蓝牙状态广播接收者
     */
    private val bluetoothStateReceiver = BluetoothStateReceiver()

    /**
     * 蓝牙扫描器缓存
     */
    private val bleScanners = ArrayList<BleScanner>()

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 上下文
     */
    internal lateinit var context: Context

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙状态广播接收者是否已经注册
     */
    private var bluetoothStateReceiverRegistered = false

    /**
     * 记录设备初始化状态
     */
    private var initialized = false

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙管理器
     */
    private var bluetoothManager: BluetoothManager? = null

    /**
     * 蓝牙适配器
     */
    internal var bluetoothAdapter: BluetoothAdapter? = null

    /**
     * 蓝牙扫描器单例
     */
    private var bleScannerInstance: BleScanner? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 方法声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 公开方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 初始化
     */
    @Synchronized
    fun initialize(context: Context) {
        if (initialized) {
            Logger.log(TAG, "已经初始化，拦截重复初始化操作")
            return
        }
        BleManager.context = context.applicationContext
        registerBluetoothStateReceiver()
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        Logger.log(TAG, "bluetoothManager = $bluetoothManager")
        bluetoothAdapter = bluetoothManager?.adapter
        Logger.log(TAG, "bluetoothAdapter = $bluetoothAdapter")
        initialized = true
    }

    /**
     * 请求开启蓝牙开关
     *
     * @return true means request success
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth(enable: Boolean): Boolean {
        checkInitialState()
        if (!supportBluetooth()) {
            return false
        }
        val adapter = bluetoothAdapter ?: return false
        if (!hasBluetoothConnectPermission()){
            return false
        }
        return if (enable) {
            adapter.enable()
        } else {
            adapter.disable()
        }
    }

    /**
     *  获取蓝牙扫描器单例
     */
    @Synchronized
    fun getBleScannerInstance(): BleScanner {
        checkInitialState()
        if (bleScannerInstance == null) {
            bleScannerInstance = BleScanner()
        }
        return bleScannerInstance!!
    }

    /**
     * 创建一个新的蓝牙扫描器实例
     */
    @Synchronized
    fun newBleScanner(): BleScanner {
        checkInitialState()
        val bleScanner = BleScanner()
        bleScanners.add(bleScanner)
        return bleScanner
    }

    /**
     * 判断设备是否支持蓝牙功能
     */
    fun supportBluetooth(): Boolean {
        checkInitialState()
        return bluetoothManager != null
    }

    /**
     * 判断设备是否支持蓝牙功能
     */
    fun supportBluetoothLe(): Boolean {
        checkInitialState()
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    /**
     * 释放BLE扫描器的内存
     */
    fun releaseBleScanner(bleScanner: BleScanner): Boolean {
        checkInitialState()
        bleScanner.close()
        return bleScanners.remove(bleScanner)
    }

    /**
     * 释放BLE扫描器单例的内存
     */
    fun releaseBleScannerInstance() {
        checkInitialState()
        bleScannerInstance?.close()
        bleScannerInstance = null
    }

    /* * * * * * * * * * * * * * * * * * * 私有方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 注册蓝牙状态的广播接收者
     */
    @Synchronized
    private fun registerBluetoothStateReceiver() {
        if (bluetoothStateReceiverRegistered) {
            return
        }
        context.registerReceiver(bluetoothStateReceiver, BluetoothStateReceiver.intentFilter)
        bluetoothStateReceiverRegistered = true
    }

    /**
     * 检查初始化状态
     */
    private fun checkInitialState() {
        if (!initialized) {
            throw IllegalStateException("未初始化，请先初始化")
        }
    }
}