package com.sscl.blelibraryforkotlin.ui.activities.connect.multi

import android.bluetooth.le.ScanResult
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.beans.BleScanResultWithBoolean
import com.sscl.blelibraryforkotlin.databinding.ActivityMultiDeviceScanBinding
import com.sscl.blelibraryforkotlin.ui.adapters.MultipleSelectScanResultRecyclerViewAdapter
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.utils.IntentConstants
import com.sscl.blelibraryforkotlin.utils.indexOfScanResults
import com.sscl.blelibraryforkotlin.utils.toastL
import com.sscl.blelibraryforkotlin.viewmodels.activities.MultiDeviceScanActivityViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.getFailMsg
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleScanListener
import com.sscl.bluetoothlowenergylibrary.scanner.BleScanner

/**
 * 多个设备连接时的扫描界面
 */
class MultiDeviceScanActivity : BaseDataBindingActivity<ActivityMultiDeviceScanBinding>() {
    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_multi_device_scan
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
            val bleScanResultWithBoolean = BleScanResultWithBoolean(scanResult)
            if (multipleSelectScanResultRecyclerViewAdapter.data.indexOfScanResults(
                    bleScanResultWithBoolean
                ) < 0
            ) {
                multipleSelectScanResultRecyclerViewAdapter.addData(bleScanResultWithBoolean)
            }
        }

        /**
         * 扫描结束（扫描时间达到设置的最大扫描时长）
         */
        override fun onScanComplete() {
            multiDeviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        }

        /**
         * BaseBleConnectCallback when batch results are delivered.
         *
         * @param results List of scan results that are previously scanned.
         */
        override fun onBatchScanResults(results: List<ScanResult>) {
            //某些特殊的扫描参数会在此方法中回调扫描结果
            for (result in results) {
                val bleScanResultWithBoolean = BleScanResultWithBoolean(result)
                val index = multipleSelectScanResultRecyclerViewAdapter.data.indexOfScanResults(
                    bleScanResultWithBoolean
                )
                if (index < 0) {
                    multipleSelectScanResultRecyclerViewAdapter.addData(bleScanResultWithBoolean)
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
            multiDeviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        }
    }

    /**
     * 设备列表适配器
     */
    private val multipleSelectScanResultRecyclerViewAdapter =
        MultipleSelectScanResultRecyclerViewAdapter()

    /**
     * ViewModel
     */
    private val multiDeviceScanActivityViewModel by viewModels<MultiDeviceScanActivityViewModel> {
        MultiDeviceScanActivityViewModel.MultiDeviceScanActivityViewModelFactory
    }

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
        setTitleText(R.string.multi_device_scan_title)
        binding.viewModel = multiDeviceScanActivityViewModel
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
    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {
        multiDeviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_multi_device_scan, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.next -> {
                doNext()
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
     * 初始化扫描结果列表数据
     */
    private fun initScanResultListRvData() {
        binding.deviceListRv.layoutManager = LinearLayoutManager(this)
        binding.deviceListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.deviceListRv.adapter = multipleSelectScanResultRecyclerViewAdapter
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
            multiDeviceScanActivityViewModel.searchBtnText.value = getString(R.string.start_scan)
        } else {
            multipleSelectScanResultRecyclerViewAdapter.clear()
            val succeed = bleScanner.startScan(true)
            if (!succeed) {
                toastL(R.string.start_scan_failed)
                return
            }
            multiDeviceScanActivityViewModel.searchBtnText.value = getString(R.string.stop_scan)
        }
    }

    /**
     * 执行下一步
     */
    private fun doNext() {
        val selectedScanResultList =
            multipleSelectScanResultRecyclerViewAdapter.getSelectedScanResultList()
        if (selectedScanResultList.isEmpty()) {
            toastL(R.string.select_none_device)
            return
        }
        val intent = Intent(this, MultipleDeviceConnectActivity::class.java)
        intent.putExtra(IntentConstants.SCAN_RESULT_LIST, selectedScanResultList)
        startActivity(intent)
    }
}