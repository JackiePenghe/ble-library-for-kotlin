package com.sscl.bluetoothlowenergylibrary

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBluetoothStateChangedListener
import com.sscl.bluetoothlowenergylibrary.scanner.BleScanner
import com.sscl.bluetoothlowenergylibrary.services.singleconnect.BluetoothLeSingleConnectService
import com.sscl.bluetoothlowenergylibrary.services.singleconnect.BluetoothSingleConnectServiceConnection
import java.util.concurrent.ScheduledThreadPoolExecutor
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

    /**
     * 蓝牙单个服务连接器
     */
    private val bluetoothSingleConnectServiceConnection = BluetoothSingleConnectServiceConnection()

    /**
     * 蓝牙状态变化的回调监听
     */
    internal val onBluetoothStateChangedListeners = ArrayList<OnBluetoothStateChangedListener>()


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

    /**
     * 记录单个设备连接服务是否已经执行过绑定
     */
    private var bindSingleConnectServiceExecute = false

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙管理器
     */
    internal var bluetoothManager: BluetoothManager? = null

    /**
     * 蓝牙适配器
     */
    internal var bluetoothAdapter: BluetoothAdapter? = null

    /**
     * 蓝牙扫描器单例
     */
    private var bleScannerInstance: BleScanner? = null

    /**
     * 单个设备连接器
     */
    internal var bleSingleConnector: BleSingleConnector? = null

    /**
     * 蓝牙单连接服务
     */
    internal var singleConnectService: BluetoothLeSingleConnectService? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 方法声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 内部方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 创建一个线程定时任务
     */
    internal fun newScheduledThreadPoolExecutor(): ScheduledThreadPoolExecutor {
        return ScheduledThreadPoolExecutor(1, threadFactory)
    }

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
        bindSingleConnectService()
        initialized = true
    }

    /**
     * 请求开启蓝牙开关
     * 蓝牙开关状态变化的监听需要添加回调[com.sscl.bluetoothlowenergylibrary.BleManager.addOnBluetoothStateChangedListener]
     * @return true表示请求成功发送
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth(enable: Boolean): Boolean {
        checkInitialState()
        if (!supportBluetooth()) {
            return false
        }
        val adapter = bluetoothAdapter ?: return false
        if (!hasBluetoothConnectPermission()) {
            return false
        }
        return if (enable) {
            adapter.enable()
        } else {
            adapter.disable()
        }
    }

    /**
     * 判断设备是否LE CODED
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isLeCodedPhySupported(): Boolean {
        return bluetoothAdapter?.isLeCodedPhySupported ?: false
    }

    /**
     * 添加蓝牙状态变化回调监听
     *
     * @param onBluetoothStateChangedListener 蓝牙状态变化回调监听
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun addOnBluetoothStateChangedListener(onBluetoothStateChangedListener: OnBluetoothStateChangedListener) {
        onBluetoothStateChangedListeners.add(onBluetoothStateChangedListener)
    }

    /**
     * 移除某个蓝牙状态变化回调监听
     *
     * @param onBluetoothStateChangedListener 蓝牙状态变化回调监听
     */
    fun removeOnBluetoothStateChangedListener(onBluetoothStateChangedListener: OnBluetoothStateChangedListener) {
        onBluetoothStateChangedListeners.remove(onBluetoothStateChangedListener)
    }

    /**
     * 移除全部蓝牙状态变化回调监听
     */
    fun removeOnBluetoothStateChangedListeners() {
        onBluetoothStateChangedListeners.clear()
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
     * 获取蓝牙连接器单例
     */
    @Synchronized
    fun getBleConnectorInstance(): BleSingleConnector {
        checkInitialState()
        if (bleSingleConnector == null) {
            bleSingleConnector = BleSingleConnector()
        }
        return bleSingleConnector!!
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

    /**
     * 释放单设备BLE连接器
     */
    fun releaseBleConnectorInstance() {
        bleSingleConnector?.close()
        bleSingleConnector = null
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

    /**
     * 绑定单个设备连接的服务
     */
    @Synchronized
    private fun bindSingleConnectService() {
        if (bindSingleConnectServiceExecute) {
            return
        }
        val intent = Intent(context, BluetoothLeSingleConnectService::class.java)
        val succeed = context.bindService(
            intent,
            bluetoothSingleConnectServiceConnection,
            Context.BIND_AUTO_CREATE
        )
        Logger.log(TAG, "绑定单个蓝牙连接的服务：$succeed")
    }
}