package com.sscl.bluetoothlowenergylibrary.scanner

import android.annotation.SuppressLint
import android.bluetooth.le.*
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import com.sscl.bluetoothlowenergylibrary.*
import com.sscl.bluetoothlowenergylibrary.checkBluetoothSupport
import com.sscl.bluetoothlowenergylibrary.enums.scanner.*
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleScanListener
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * BLE扫描器
 */
class BleScanner internal constructor(){

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
            onBleScanListener?.onScanComplete()
        }
    }

    /**
     * 蓝牙扫描回调
     */
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Logger.log(TAG, "onScanResult  $result")
            onScanResultProcessor(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            Logger.log(TAG, "onBatchScanResults  $results")
            BleManager.handler.post {
                onBleScanListener?.onBatchScanResults(results)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Logger.log(TAG, "onScanFailed  ${errorCode.getFailMsg()}")
            BleManager.handler.post {
                onBleScanListener?.onScanFailed(errorCode)
            }
        }
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
    private val scanResults = ArrayList<ScanResult>()

    /**
     * 用于存储需要过滤的设备名
     * 以集合中的字符串开头的设备名的扫描结果都会触发到回调
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val filterNames = ArrayList<String>()

    /**
     * 用于存储需要过滤的设备地址
     * 以集合中的字符串开头的设备名地址扫描结果都会触发到回调
     * 地址需要包含冒号，且必须为大写字母。例如："A" 或 "AA" 或 "AA:" 或 "AA:B"
     * 如果此集合为空，表示不生效此过滤条件
     */
    private val filterAddresses = ArrayList<String>()

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 记录当前扫描器状态
     */
    var scanning = false
        private set

    /**
     * 扫描模式-默认为低延时模式
     */
    private var bleScanMode = BleScanMode.SCAN_MODE_LOW_LATENCY

    /**
     * 扫描结果反馈延时时间，单位：ms
     */
    private var reportDelay = 0L

    /**
     * BLE匹配模式-默认为进击模式
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private var bleMatchMode = BleMatchMode.MATCH_MODE_AGGRESSIVE

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
    private var bleScanPhy = BleScanPhy.PHY_LE_ALL_SUPPORTED

    /**
     * 单次扫描的最大时长，为负数表示一直扫描，直到手动停止扫描
     */
    private var scanTimeout: Long = 20000

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
     * BLE蓝牙扫描监听
     */
    private var onBleScanListener: OnBleScanListener? = null


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    init {
        checkBluetoothSupport()
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
     * 设置蓝牙扫描回调
     *
     * @param onBleScanListener 蓝牙扫描回调
     */
    fun setOnBleScanStateChangedListener(onBleScanListener: OnBleScanListener?) {
        this.onBleScanListener = onBleScanListener
    }

    /**
     * 设置蓝牙回调类型
     *
     * @param bleCallbackType 蓝牙回调类型
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setBleCallbackType(bleCallbackType: BleCallbackType) {
        this.bleCallbackType = bleCallbackType
    }

    /**
     * 设置扫描结果反馈延时时间，单位：ms
     */
    fun setReportDelay(@IntRange(from = 0L) reportDelay: Long) {
        this.reportDelay = reportDelay
    }

    /**
     * 设置扫描物理层
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setScanPhy(bleScanPhy: BleScanPhy) {
        this.bleScanPhy = bleScanPhy
    }

    /**
     * 设置单次扫描最大时长（小于0表示一直扫描）
     *
     * @param scanTimeout 单次扫描最大时长
     */
    fun setScanTimeout(scanTimeout: Long) {
        this.scanTimeout = scanTimeout
    }

    /**
     * 获取当前缓存的 过滤设备名-全名 列表
     */
    fun getFilterFullNames(): ArrayList<String> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(filterFullNames)
    }

    /**
     * 获取当前缓存的 过滤地址-全地址 列表
     */
    fun getFilterFullAddresses(): ArrayList<String> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(filterFullAddresses)
    }

    /**
     * 获取当前缓存的 UUID 列表
     */
    fun getFilterUuids(): ArrayList<String> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(filterUuids)
    }

    /**
     * 获取当前缓存的用户自定义过滤条件
     */
    fun getCustomScanFilters(): ArrayList<ScanFilter> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(customScanFilters)
    }

    /**
     * 获取 过滤名称-开头匹配 的列表
     */
    fun getFilterStartsNames(): ArrayList<String> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(filterNames)
    }

    /**
     * 获取过滤地址-开头匹配的列表
     */
    fun getFilterStartsAddresses(): ArrayList<String> {
        //返回一个新创建的对象，防止外部使用获取到的引用更改列表中的内容导致过滤条件出现异常
        return ArrayList(filterAddresses)
    }

    /**
     * 开始扫描设备
     *
     * @return true表示命令已成功发送（执行失败会触发回调中的 scanFailed） false表示命令发送失败
     */
    @SuppressLint("MissingPermission")
    fun startScan(clearScanResult: Boolean = false): Boolean {
        if (clearScanResult) {
            clearScanResults()
            flushPendingScanResults()
        }
        if (BleManager.bluetoothAdapter?.isEnabled != true) {
            val result = tryEnableBluetooth()
            Logger.log(TAG, "开启蓝牙开关结果：$result")
            return false
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

    /**
     * 获取扫描结果
     *
     * @return 扫描结果
     */
    fun getScanResults(): java.util.ArrayList<ScanResult> {
        return scanResults
    }

    /**
     * 添加用户自定义的过滤条件
     *
     * @param scanFilter 用户自定义的过滤条件
     */
    fun addCustomFilter(scanFilter: ScanFilter): Boolean {
        return customScanFilters.add(scanFilter)
    }

    /**
     * 添加用户自定义的过滤条件
     *
     * @param scanFilters 用户自定义的过滤条件列表
     */
    fun addCustomFilter(scanFilters: ArrayList<ScanFilter>): Boolean {
        return customScanFilters.addAll(scanFilters)
    }

    /**
     * 删除用户自定义的过滤条件
     *
     * @param scanFilter 用户自定义的过滤条件
     */
    fun removeCustomFilter(scanFilter: ScanFilter): Boolean {
        return customScanFilters.remove(scanFilter)
    }

    /**
     * 清空全部的用户自定义的过滤条件
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearCustomFilters() {
        customScanFilters.clear()
    }

    /**
     * 添加一个过滤名称
     * 如果设备名称以这个过滤名称开头，将会触发回调
     *
     * @param startsName 过滤字符串
     */
    fun addFilterStartsName(startsName: String): Boolean {
        return filterNames.add(startsName)
    }

    /**
     * 添加一个过滤名称
     * 如果设备名称以这个过滤名称开头，将会触发回调
     *
     * @param startsNames 过滤字符串列表
     */
    fun addFilterStartsName(startsNames: ArrayList<String>): Boolean {
        return filterNames.addAll(startsNames)
    }

    /**
     * 移除一个设备过滤名称
     *
     * @param startsName 过滤字符串
     */
    fun removeFilterStartsName(startsName: String): Boolean {
        return filterNames.remove(startsName)
    }

    /**
     * 清空设备过滤名称
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearFilterStartsNames() {
        filterNames.clear()
    }

    /**
     * 添加UUID过滤名称
     * 如果设备的ServiceUuid有同名的UUID，将会触发回调
     *
     * @param uuid uuid
     */
    fun addFilterUuid(uuid: String): Boolean {
        return filterUuids.add(uuid.lowercase(Locale.getDefault()))
    }

    /**
     * 添加UUID过滤
     * 如果设备的ServiceUuid有同名的UUID，将会触发回调
     *
     * @param uuids uuid列表
     */
    fun addFilterUuid(uuids: ArrayList<String>): Boolean {
        val uuidCaches = ArrayList<String>()
        for (uuid in uuids) {
            uuidCaches.add(uuid.lowercase(Locale.getDefault()))
        }
        return uuids.addAll(uuidCaches)
    }

    /**
     * 移除UUID过滤
     *
     * @param uuid UUID
     */
    fun removeFilterUuid(uuid: String): Boolean {
        return filterUuids.remove(uuid.lowercase(Locale.getDefault()))
    }

    /**
     * 清空全部UUID
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearFilterUuid() {
        filterUuids.clear()
    }

    /**
     * 添加一个过滤名-全名
     * 如果一个设备名的全名与其一致，将会触发回调
     *
     * @param fullName 过滤名-全名
     */
    fun addFilterFullName(fullName: String): Boolean {
        return filterFullNames.add(fullName)
    }

    /**
     * 添加 过滤名-全名 的列表
     * 如果一个设备名的全名与列表中任意一项一致，将会触发回调
     *
     * @param fullNames 过滤名-全名列表
     */
    fun addFilterFullName(fullNames: ArrayList<String>): Boolean {
        return filterFullNames.addAll(fullNames)
    }

    /**
     * 移除 过滤名-全名 的列表
     *
     * @param fullName 过滤名-全名
     */
    fun removeFilterFullName(fullName: String) {
        filterFullNames.remove(fullName)
    }

    /**
     * 清空 过滤名-全名 的所有过滤
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearFilterFullName() {
        filterFullNames.clear()
    }

    /**
     * 添加一个过滤地址
     * 如果设备地址以指定的字符串开头，将会触发回调
     *
     * @param startsAddress 过滤地址
     */
    fun addFilterStartsAddress(startsAddress: String): Boolean {
        return filterAddresses.add(startsAddress.uppercase(Locale.getDefault()))
    }

    /**
     * 添加 过滤地址 列表
     *
     * @param startsAddresses 过滤地址列表
     */
    fun addFilterStartsAddress(startsAddresses: ArrayList<String>): Boolean {
        val startsAddressCache = ArrayList<String>()
        for (address in startsAddresses) {
            startsAddressCache.add(address.uppercase(Locale.getDefault()))
        }
        return filterAddresses.addAll(startsAddressCache)
    }

    /**
     * 移除一个过滤地址
     *
     * @param startsAddress 过滤地址
     */
    fun removeFilterStartsAddress(startsAddress: String): Boolean {
        return filterAddresses.remove(startsAddress.uppercase(Locale.getDefault()))
    }

    /**
     * 清空过滤地址列表
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearFilterStartsAddress() {
        filterAddresses.clear()
    }

    /**
     * 添加一个过滤地址-全地址
     * 如果设备地址以指定的字符串一致，将会触发回调
     *
     * @param fullAddress 过滤地址-全地址
     */
    fun addFilterFullAddress(fullAddress: String): Boolean {
        val address = fullAddress.uppercase(Locale.getDefault())
        if (!address.isValidBluetoothAddress()) {
            Logger.log(TAG, "MAC地址不合法")
            return false
        }
        return filterFullAddresses.add(address)
    }

    /**
     * 添加一个过滤地址-全地址
     * 如果设备地址以指定的字符串一致，将会触发回调
     *
     * @param fullAddresses 过滤地址-全地址
     */
    fun addFilterFullAddress(fullAddresses: ArrayList<String>): Boolean {
        val addressCaches = ArrayList<String>()
        for (address in fullAddresses) {
            val addressCache = address.uppercase(Locale.getDefault())
            if (!address.isValidBluetoothAddress()) {
                Logger.log(TAG, "MAC地址不合法")
                continue
            }
            addressCaches.add(addressCache)
        }

        return filterFullAddresses.addAll(addressCaches)
    }

    /**
     * 移除设备地址-全地址
     *
     * @param fullAddress 过滤地址-全地址
     */
    fun removeFilterFullAddress(fullAddress: String): Boolean {
        return filterFullAddresses.remove(fullAddress.uppercase(Locale.getDefault()))
    }

    /**
     * 清空设备地址-全地址
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearFilterFullAddress() {
        filterFullAddresses.clear()
    }

    /**
     * 清空全部过滤列表
     */
    fun clearAllFilters() {
        clearCustomFilters()
        clearFilterFullAddress()
        clearFilterStartsAddress()
        clearFilterFullName()
        clearFilterStartsNames()
        clearFilterUuid()
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
                builder.setPhy(bleScanPhy.value)
            }
        }
        return builder.build()
    }

    /**
     * 开启扫描定时器
     */
    private fun startScanTimer() {
        stopScanTimer()
        if (scanTimeout < 0) {
            return
        }
        scanTimer = BleManager.newScheduledThreadPoolExecutor()
        scanTimer?.schedule(scanTimerRunnable, scanTimeout, TimeUnit.MILLISECONDS)
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

    /**
     * 尝试开启蓝牙开关
     */
    private fun tryEnableBluetooth(): Boolean {
        if (!BleManager.supportBluetooth()) {
            Logger.log(TAG, "尝试开启蓝牙失败，设备不支持蓝牙功能")
            return false
        }
        return BleManager.enableBluetooth(true)
    }

    /**
     * 扫描结果处理
     */
    @SuppressLint("MissingPermission")
    private fun onScanResultProcessor(result: ScanResult) {
        if (!hasBluetoothConnectPermission()) {
            return
        }
        val scanRecord = result.scanRecord ?: return
        var deviceName: String?
        val device = result.device
        deviceName = device.name
        Logger.log(TAG, "deviceName = $deviceName")
        if (null == deviceName || "" == deviceName) {
            deviceName = scanRecord.deviceName
            Logger.log(TAG, "deviceName = $deviceName")
        }
        if (null == deviceName || "" == deviceName) {
            deviceName = scanRecord.deviceName
            Logger.log(TAG, "deviceName = $deviceName")
        }
        val address: String = device.address
        Logger.log(TAG, "address = $address")

        if (!filterNames(deviceName)) {
            return
        }

        if (!filterAddress(address)) {
            return
        }
        BleManager.handler.post {
            onBleScanListener?.onScanFindOneDevice(result)
        }
        if (!containsScanResults(result)) {
            scanResults.add(result)
            BleManager.handler.post {
                onBleScanListener?.onScanFindOneNewDevice(result)
            }
        } else {
            val index = indexOfScanResults(result)
            if (index < 0) {
                return
            }
            scanResults[index] = result
            BleManager.handler.post {
                onBleScanListener?.onScanResultInfoUpdate(result)
            }
        }
    }

    /**
     * 判断扫描结果是否在搜索列表中
     */
    private fun containsScanResults(scanResult: ScanResult): Boolean {
        return scanResults.containsScanResults(scanResult)
    }

    /**
     * 获取判断扫描结果在搜索列表中的索引
     */
    private fun indexOfScanResults(scanResult: ScanResult): Int {
        return scanResults.indexOfScanResults(scanResult)
    }

    /**
     * 只有以指定的字符串开头的设备名才允许触发回调
     *
     * @param name 设备名称
     * @return true表示设备名允许通过
     */
    private fun filterNames(name: String?): Boolean {
        name ?: return false
        if (filterNames.size != 0) {
            var pass = false
            for (i in filterNames.indices) {
                val filterName: String = filterNames[i]
                if (name.startsWith(filterName)) {
                    pass = true
                    break
                }
            }
            return pass
        }
        return true
    }

    /**
     * 只有以指定的字符串开头的设备地址才能通过
     *
     * @param address 设备地址
     * @return true 表示设备地址通过
     */
    private fun filterAddress(address: String): Boolean {
        if (filterAddresses.size != 0) {
            var pass = false
            for (i in filterAddresses.indices) {
                val filterName: String = filterAddresses.get(i)
                if (address.startsWith(filterName)) {
                    pass = true
                    break
                }
            }
            return pass
        }
        return true
    }
}