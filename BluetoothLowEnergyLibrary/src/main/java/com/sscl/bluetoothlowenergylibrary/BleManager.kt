package com.sscl.bluetoothlowenergylibrary

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
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
    fun initialize(context: Context) {
        BleManager.context = context.applicationContext
        registerBluetoothStateReceiver()
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        Logger.log(TAG, "bluetoothManager = $bluetoothManager")
        bluetoothAdapter = bluetoothManager?.adapter
        Logger.log(TAG, "bluetoothAdapter = $bluetoothAdapter")
    }

    /**
     *  获取蓝牙扫描器单例
     */
    @Synchronized
    fun getBleScannerInstance(): BleScanner {
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
        val bleScanner = BleScanner()
        bleScanners.add(bleScanner)
        return bleScanner
    }

    fun releaseBleScanner(bleScanner: BleScanner): Boolean {
        bleScanner.close()
        return bleScanners.remove(bleScanner)
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
}