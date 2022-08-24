package com.sscl.blelibraryforkotlin.ui.activities.scan

import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityDeviceScanBinding
import com.sscl.blelibraryforkotlin.ui.adapters.ScanResultAdapter
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.utils.toastL
import com.sscl.blelibraryforkotlin.utils.warnOut
import com.sscl.blelibraryforkotlin.viewmodels.DeviceScanActivityViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.BleScanner
import com.sscl.bluetoothlowenergylibrary.enums.BleCallbackType
import com.sscl.bluetoothlowenergylibrary.enums.BleMatchMode
import com.sscl.bluetoothlowenergylibrary.enums.BleScanMode
import com.sscl.bluetoothlowenergylibrary.getFailMsg
import com.sscl.bluetoothlowenergylibrary.indexOfScanResults
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleScanListener

/**
 * 设备扫描界面
 */
class DeviceScanActivity : BaseDataBindingActivity<ActivityDeviceScanBinding>() {

    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_device_scan
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 设备列表点击事件
     */
    private val onItemClickListener = OnItemClickListener { _, _, position ->
        toConnectActivity(scanResultAdapter.data[position])
    }

    /**
     * 蓝牙扫描回调
     */
    private val onBleScanListener = object : OnBleScanListener {

        /**
         * 仅当发现一个新的设备时才会回调此方法
         *
         * @param scanResult  BLE扫描结果.如果为空则表示设备信息有更新
         */
        override fun onScanFindOneNewDevice(scanResult: ScanResult) {
            scanResultAdapter.addData(scanResult)
        }

        /**
         * 每发现一个设备就会触发一次此方法
         *
         * @param scanResult BLE扫描结果
         */
        override fun onScanFindOneDevice(scanResult: ScanResult) {
            //do nothing
        }

        /**
         * 扫描结果信息有更新
         */
        override fun onScanResultInfoUpdate(result: ScanResult) {
            val index = scanResultAdapter.data.indexOfScanResults(result)
            if (index >= 0) {
                scanResultAdapter.setData(index, result)
            }
        }

        /**
         * 扫描结束（扫描时间达到设置的最大扫描时长）
         */
        override fun onScanComplete() {
            deviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        }

        /**
         * BaseBleConnectCallback when batch results are delivered.
         *
         * @param results List of scan results that are previously scanned.
         */
        override fun onBatchScanResults(results: List<ScanResult>) {
            //某些特殊的扫描参数会在此方法中回调扫描结果
            for (result in results) {
                val index = scanResultAdapter.data.indexOfScanResults(result)
                if (index < 0) {
                    scanResultAdapter.addData(result)
                } else {
                    scanResultAdapter.setData(index, result)
                }
            }
        }

        /**
         * 扫描开启失败
         *
         * @param errorCode 扫描失败的错误代码
         */
        override fun onScanFailed(errorCode: Int) {
            toastL("扫描失败 ${errorCode.getFailMsg()}")
            bleScanner.stopScan()
            deviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        }
    }

    /**
     * viewModel
     */
    private val deviceScanActivityViewModel by viewModels<DeviceScanActivityViewModel> {
        DeviceScanActivityViewModel.DeviceScanActivityViewModelFactory
    }

    /**
     * 扫描结果列表适配器
     */
    private val scanResultAdapter = ScanResultAdapter()

    /**
     * 点击事件的监听
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.searchBtn.id -> {
                doSearchBtnClicked()
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙扫描单例
     */
    private lateinit var bleScanner: BleScanner

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {
        initBleScanner()
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
        setTitleText(R.string.device_scan_title)
        binding.viewModel = deviceScanActivityViewModel
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {
        initScanResultListRvData()
    }

    /**
     * 初始化其他数据
     */
    override fun initOtherData() {

    }

    /**
     * 初始化事件
     */
    override fun initEvents() {
        binding.searchBtn.setOnClickListener(onClickListener)
        scanResultAdapter.setOnItemClickListener(onItemClickListener)
    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {
        deviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_device_scan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_ble_scan_mode -> {
                showSelectBleScanModeDialog()
            }
            R.id.set_ble_match_mode -> {
                showSelectBleMatchModeDialog()
            }
            R.id.set_ble_callback_type -> {
                showSelectBleCallbackTypeDialog()
            }
            R.id.set_ble_report_delay -> {
                showSetBleReportDelayDialog()
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.releaseBleScanner(bleScanner)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 初始化蓝牙扫描器
     */
    private fun initBleScanner() {
        bleScanner = BleManager.newBleScanner()
        bleScanner.setOnBleScanStateChangedListener(onBleScanListener)
    }

    /**
     * 扫描按钮被点击的处理
     */
    private fun doSearchBtnClicked() {
        if (bleScanner.scanning) {
            val succeed = bleScanner.stopScan()
            if (!succeed) {
                toastL(R.string.stop_scan_failed)
                return
            }
            deviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        } else {
            val succeed = bleScanner.startScan(true)
            if (!succeed) {
                toastL(R.string.start_scan_failed)
                return
            }
            deviceScanActivityViewModel.searchBtnText.value = getString(R.string.stop_scan)
        }
    }

    /**
     * 初始化扫描结果列表数据
     */
    private fun initScanResultListRvData() {
        binding.deviceListRv.layoutManager = LinearLayoutManager(this)
        binding.deviceListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.deviceListRv.adapter = scanResultAdapter
    }

    /**
     * 显示BLE扫描模式设置对话框
     */
    private fun showSelectBleScanModeDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.set_ble_scan_mode_dialog_title)
            .setItems(R.array.ble_mode) { _, which ->
                val bleScanMode = BleScanMode.values()[which]
                bleScanner.setBleScanMode(bleScanMode)
                warnOut("bleScanMode $bleScanMode")
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示BLE匹配模式对话框
     */
    private fun showSelectBleMatchModeDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toastL(R.string.ble_match_mode_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.set_ble_match_mode_dialog_title)
            .setItems(R.array.ble_match_mode) { _, which ->
                val bleMatchMode = BleMatchMode.values()[which]
                bleScanner.setBleMatchMode(bleMatchMode)
                warnOut("bleMatchMode $bleMatchMode")
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 设置回调类型对话框
     */
    private fun showSelectBleCallbackTypeDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toastL(R.string.ble_callback_type_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.set_ble_callback_type_dialog_title)
            .setItems(R.array.ble_callback_type) { _, which ->
                val bleCallbackType = BleCallbackType.values()[which]
                bleScanner.setBleCallbackType(bleCallbackType)
                warnOut("bleCallbackType $bleCallbackType")
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示设置扫描反馈延时时间的对话框
     */
    private fun showSetBleReportDelayDialog() {
        val view = View.inflate(this, R.layout.view_ble_report_delay, null)
        AlertDialog.Builder(this)
            .setTitle(R.string.set_ble_report_delay_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                var text =
                    view.findViewById<EditText>(R.id.scan_report_delay_et).text.toString().trim()
                if (text.isEmpty()) {
                    text = "0"
                }
                val result = try {
                    text.toLong()
                } catch (e: Exception) {
                    0
                }
                warnOut("setReportDelay $result")
                bleScanner.setReportDelay(result)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()

    }

    /**
     * 跳转到设备连接界面
     */
    private fun toConnectActivity(@Suppress("UNUSED_PARAMETER") scanResult: ScanResult) {
        if (bleScanner.scanning) {
            val succeed = bleScanner.stopScan()
            if (succeed) {
                deviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
            }
        }
        TODO("跳转到设备连接界面 未实现")
    }

}