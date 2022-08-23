package com.sscl.bluetoothlowenergylibrary

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.enums.*
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleScanStateChangedListener
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * BLE扫描器
 */
class BleScanner {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *
         * 属性声明
         *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

        private val TAG: String = BleScanner::class.java.simpleName

        /**
         * 最大重试次数
         */
        private const val MAX_RETRY_COUNT = 3
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 扫描定时器任务
     */
    private val scanTimerRunnable = Runnable {
        stopScan()
        BleManager.handler.post {
            onBleScanStateChangedListener?.onScanComplete()
        }
    }

    /**
     * 蓝牙扫描回调
     */
    private val scanCallback = object : ScanCallback() {

    }

    /**
     * 存储过滤设备名称（全名）的集合，只有符合集合中存在的设备名，该设备才会被扫描到
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val filterFullNames = ArrayList<String>()

    /**
     * 存储过滤设备地址（全地址）的集合，只有符合集合中存在的设备地址，该设备才会被扫描到
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val filterFullAddresses = ArrayList<String>()

    /**
     * 存储过滤设备UUID的集合，只有符合集合中存在的UUID，该设备才会被扫描到
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val filterUuids = ArrayList<String>()

    /**
     * 存储用户自定义过滤条件的集合，只有符合集合中的过滤条件，该设备才会被扫描到
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val customScanFilters = ArrayList<ScanFilter>()

    /**
     * 扫描结果列表
     */
    private val scanResults = java.util.ArrayList<BleDevice>()

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 记录当前扫描器状态
     */
    private var scanning = false

    /**
     * 扫描模式-默认为低延时模式
     */
    private var bleScanMode = BleScanMode.LOW_LATENCY

    /**
     * 扫描结果反馈延时时间
     */
    private var reportDelay = 0L

    /**
     * BLE匹配模式-默认为进击模式
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private var bleMatchMode = BleMatchMode.AGGRESSIVE

    /**
     * BLE回调模式-默认为全部匹配
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private var bleCallbackType = BleCallbackType.CALLBACK_TYPE_ALL_MATCHES

    /**
     * 匹配数量限制-默认为大量匹配
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private val bleNumOfMatches = BleNumOfMatches.MATCH_NUM_MAX_ADVERTISEMENT

    /**
     * 扫描结果中是否只返回旧广告。对于
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private val legacy = false

    /**
     * 扫描物理层-仅当legacy为false时有效
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private var scanPhy = ScanPhy.PHY_LE_ALL_SUPPORTED

    /**
     * 单次扫描的最大时长，为负数表示一直扫描，直到手动停止扫描
     */
    private var scanPeriod: Long = 20000

    /**
     * 单次扫描的最大时长单位，默认值：毫秒
     */
    private var scanPeriodTimeUnit = TimeUnit.MILLISECONDS

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙扫描器
     */
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    /**
     * 蓝牙扫描定时器（在到达指定扫描时长后停止扫描）
     */
    private var scanTimer: ScheduledThreadPoolExecutor? = null

    /**
     * BLE蓝牙扫描结果监听
     */
    private var onBleScanStateChangedListener: OnBleScanStateChangedListener? = null


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    init {
        checkBleSupport()
        bluetoothLeScanner = BleManager.bluetoothAdapter?.bluetoothLeScanner
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 方法声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 公开方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 关闭
     */
    fun close() {
        if (scanning) {
            stopScan()
        }
    }

    /**
     * 停止扫描
     * @return true表示成功
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun stopScan(): Boolean {
        return stopScan(0)
    }

    /**
     * 设置BLE扫描模式
     *
     * @param bleScanMode BLE扫描模式
     */
    fun setBleScanMode(bleScanMode: BleScanMode) {
        this.bleScanMode = bleScanMode
    }

    /**
     * 设置BLE匹配模式
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun setBleMatchMode(bleMatchMode: BleMatchMode) {
        this.bleMatchMode = bleMatchMode
    }

    /**
     * set ble scan state changed listener
     *
     * @param onBleScanStateChangedListener ble scan state changed listener
     */
    fun setOnBleScanStateChangedListener(onBleScanStateChangedListener: OnBleScanStateChangedListener?) {
        this.onBleScanStateChangedListener = onBleScanStateChangedListener
    }

    /**
     * set ble callback type
     *
     * @param bleCallbackType ble callback type
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setBleCallbackType(bleCallbackType: BleCallbackType) {
        this.bleCallbackType = bleCallbackType
    }

    /**
     * 设置扫描结果反馈延时时间
     */
    fun setReportDelay(@IntRange(from = 0L) reportDelay: Long) {
        this.reportDelay = reportDelay
    }

    /**
     * 设置扫描物理层
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setScanPhy(scanPhy: ScanPhy) {
        this.scanPhy = scanPhy
    }

    /**
     * 设置单次扫描周期
     *
     * @param scanPeriod 扫描周期
     */
    fun setScanPeriod(scanPeriod: Long) {
        this.scanPeriod = scanPeriod
    }

    /**
     * 设置扫描周期
     *
     * @param scanPeriodTimeUnit 扫描时长
     */
    fun setScanPeriodTimeUnit(scanPeriodTimeUnit: TimeUnit) {
        this.scanPeriodTimeUnit = scanPeriodTimeUnit
    }

    /**
     * 开始扫描设备
     *
     * @return true means the scanner successfully started scanning
     */
    @SuppressLint("MissingPermission")
    fun startScan(clearScanResult: Boolean = false): Boolean {
        if (clearScanResult) {
            clearScanResults()
            flushPendingScanResults()
        }
        if (scanning) {
            return false
        }
        if (!hasBluetoothScanPermission()) {
            return false
        }
        bluetoothLeScanner?.startScan(refreshScanFilter(), refreshScanSettings(), scanCallback)
        startScanTimer()
        scanning = true
        return true
    }

    /**
     * 清空扫描结果
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearScanResults() {
        scanResults.clear()
    }

    /**
     *
     * 刷新扫描结果列表
     */
    @SuppressLint("MissingPermission")
    fun flushPendingScanResults() {
        if (!hasBluetoothScanPermission()) {
            Logger.log(TAG, "没有扫描权限，无法更新扫描设备回调")
            return
        }
        bluetoothLeScanner?.flushPendingScanResults(scanCallback)
    }

    /* * * * * * * * * * * * * * * * * * * 私有方法 * * * * * * * * * * * * * * * * * * */

    /**
     * 更新设备扫描的过滤条件
     *
     * @return 过滤条件集合
     */
    private fun refreshScanFilter(): ArrayList<ScanFilter> {
        val scanFilters = ArrayList<ScanFilter>()
        for (i in filterFullNames.indices) {
            val filterName: String = filterFullNames[i]
            val scanFilter = ScanFilter.Builder()
                .setDeviceName(filterName)
                .build()
            scanFilters.add(scanFilter)
        }
        for (i in filterFullAddresses.indices) {
            val filterFullAddress: String = filterFullAddresses[i]
            val scanFilter = ScanFilter.Builder()
                .setDeviceAddress(filterFullAddress)
                .build()
            scanFilters.add(scanFilter)
        }
        for (i in filterUuids.indices) {
            val uuid: String = filterUuids[i]
            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(uuid))
                .build()
            scanFilters.add(scanFilter)
        }
        scanFilters.addAll(customScanFilters)
        return scanFilters
    }

    /**
     * 更新扫描设置
     *
     * @return 扫描设置
     */
    private fun refreshScanSettings(): ScanSettings {
        val builder = ScanSettings.Builder()
        builder.setScanMode(bleScanMode.scanMode)
            .setReportDelay(reportDelay)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setMatchMode(bleMatchMode.matchMode)
                .setCallbackType(bleCallbackType.callbackType)
                .setNumOfMatches(bleNumOfMatches.numOfMatches)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Logger.log(TAG, "安卓版本支持手动设置legacy与phy,使用对应的变量值")
            builder.setLegacy(legacy)
            if (BleManager.bluetoothAdapter?.isLeCodedPhySupported == true) {
                builder.setPhy(scanPhy.value)
            }
        }
        return builder.build()
    }

    /**
     * 开启扫描定时器
     */
    private fun startScanTimer() {
        stopScanTimer()
        if (scanPeriod < 0) {
            return
        }
        scanTimer = ScheduledThreadPoolExecutor(1, BleManager.threadFactory)
        scanTimer?.schedule(scanTimerRunnable, scanPeriod, scanPeriodTimeUnit)
    }

    /**
     * 停止扫描定时器
     */
    private fun stopScanTimer() {
        scanTimer?.shutdownNow()
    }

    /**
     * 停止扫描
     *
     * @param tryCount try count
     * @return true means successful
     */
    @SuppressLint("MissingPermission")
    private fun stopScan(tryCount: Int): Boolean {
        val count = tryCount + 1
        if (!scanning) {
            return false
        }
        try {
            if (!hasBluetoothScanPermission()) {
                return false
            }
            bluetoothLeScanner?.stopScan(scanCallback)
            stopScanTimer()
            scanning = false
            return true
        } catch (e: Exception) {
            return if (count >= MAX_RETRY_COUNT) {
                false
            } else {
                stopScan(count)
            }
        }
    }
}