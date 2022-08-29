package com.sscl.blelibraryforkotlin.ui.activities

import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.baselibrary.utils.Tool
import com.sscl.baselibrary.utils.toHexStringWithSpace
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityScanRecordParseBinding
import com.sscl.blelibraryforkotlin.ui.adapters.ScanRecordParseAdStructRecyclerViewAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.ScanRecordServiceDataRecyclerViewAdapter
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.utils.IntentConstants
import com.sscl.blelibraryforkotlin.utils.toastL
import com.sscl.blelibraryforkotlin.utils.warnOut
import com.sscl.blelibraryforkotlin.viewmodels.activities.ScanRecordParseActivityViewModel
import com.sscl.bluetoothlowenergylibrary.AdvertiseStruct
import com.sscl.bluetoothlowenergylibrary.ServiceDataInfo
import com.sscl.bluetoothlowenergylibrary.toByteArray
import com.sscl.bluetoothlowenergylibrary.utils.BleUtils

/**
 * 广播包数据解析界面
 */
class ScanRecordParseActivity : BaseDataBindingActivity<ActivityScanRecordParseBinding>() {
    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_scan_record_parse
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * ViewModel
     */
    private val scanRecordParseActivityViewModel by viewModels<ScanRecordParseActivityViewModel> {
        ScanRecordParseActivityViewModel.ScanRecordParseActivityViewModelFactory
    }

    /**
     * 点击事件
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.copyScanRecordBtn.id -> {
                Tool.setDataToClipboard(
                    this@ScanRecordParseActivity,
                    TAG,
                    scanResult?.scanRecord?.bytes.toHexStringWithSpace() ?: ""
                )
                toastL(R.string.scan_record_copied)
            }
        }
    }

    /**
     * 广播包AD结构列表适配器
     */
    private val scanRecordParseAdStructRecyclerViewAdapter =
        ScanRecordParseAdStructRecyclerViewAdapter()

    /**
     * 广播包服务信息列表适配器
     */
    private val scanRecordServiceDataRecyclerViewAdapter =
        ScanRecordServiceDataRecyclerViewAdapter()

    /**
     * 列表选项点击事件
     */
    private val onItemClickListener = OnItemClickListener { adapter, view, position ->
        if (adapter == scanRecordParseAdStructRecyclerViewAdapter) {
            showAdStructItemOptionsDialog(scanRecordParseAdStructRecyclerViewAdapter.data[position])
        } else if (adapter == scanRecordServiceDataRecyclerViewAdapter) {
            showServiceDataOptionsDialog(scanRecordServiceDataRecyclerViewAdapter.data[position])
        }
    }

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 某个设备
     */
    private var scanResult: ScanResult? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {
        getIntentData()
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
        binding.viewModel = scanRecordParseActivityViewModel
        setTitleText(R.string.view_scan_record)
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {
        initAdStructRvData()
        initServiceDataRvData()
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
        binding.copyScanRecordBtn.setOnClickListener(onClickListener)
        scanRecordParseAdStructRecyclerViewAdapter.setOnItemClickListener(onItemClickListener)
        scanRecordServiceDataRecyclerViewAdapter.setOnItemClickListener(onItemClickListener)
    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {
        if (scanResult == null) {
            finish()
            return
        }
        setData()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 获取Intent传递的数据
     */
    private fun getIntentData() {
        scanResult = intent.getParcelableExtra(IntentConstants.SCAN_RESULT)
    }

    /**
     * 初始化AD结构展示列表数据
     */
    private fun initAdStructRvData() {
        binding.adStructListRv.layoutManager = LinearLayoutManager(this)
        binding.adStructListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.adStructListRv.adapter = scanRecordParseAdStructRecyclerViewAdapter
    }

    /**
     * 初始化服务信息展示列表数据
     */
    private fun initServiceDataRvData() {
        binding.serviceInfoListRv.layoutManager = LinearLayoutManager(this)
        binding.serviceInfoListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.serviceInfoListRv.adapter = scanRecordServiceDataRecyclerViewAdapter
    }

    /**
     * 设置数据展示
     */
    private fun setData() {
        val scanResult = scanResult ?: return
        scanRecordParseActivityViewModel.scanResult.value = scanResult

        val scanRecord = scanResult.scanRecord ?: return
        scanRecordParseAdStructRecyclerViewAdapter.addData(
            BleUtils.getAdvertiseRecords(
                scanRecord
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceSolicitationUuids =
                scanRecord.serviceSolicitationUuids
            if (serviceSolicitationUuids.isEmpty()) {
                warnOut("serviceSolicitationUuids 为空")
            } else {
                for (serviceSolicitationUuid in serviceSolicitationUuids) {
                    warnOut("serviceSolicitationUuid = ${serviceSolicitationUuid.uuid}")
                }
            }
        }
        scanRecordServiceDataRecyclerViewAdapter.addData(BleUtils.getServiceDataInfoList(scanRecord))
        val advertiseFlags = scanRecord.advertiseFlags
        warnOut("advertiseFlags = $advertiseFlags")
        val serviceUuids = scanRecord.serviceUuids
        if (serviceUuids == null || serviceUuids.isEmpty()) {
            warnOut("serviceUuids 为空")
        } else {
            for (serviceUuid in serviceUuids) {
                warnOut("serviceUuid ${serviceUuid.uuid}")
            }
        }
        val txPowerLevel = scanRecord.txPowerLevel
        warnOut("txPowerLevel $txPowerLevel")
    }

    /**
     * 显示AD结构的选项被点击的操作选项
     */
    private fun showAdStructItemOptionsDialog(advertiseStruct: AdvertiseStruct) {
        AlertDialog.Builder(this)
            .setTitle(R.string.ad_struct_options_dialog_title)
            .setItems(R.array.ad_struct_options) { _, which ->
                when (which) {
                    //复制整个AD结构数据
                    0 -> {
                        val byteArray = advertiseStruct.toByteArray()
                        Tool.setDataToClipboard(
                            this@ScanRecordParseActivity,
                            TAG,
                            byteArray.toHexStringWithSpace() ?: ""
                        )
                        toastL(R.string.ad_struct_copied)
                    }
                    //仅复制AD数据
                    1 -> {
                        val data = advertiseStruct.data
                        Tool.setDataToClipboard(
                            this@ScanRecordParseActivity,
                            TAG,
                            data.toHexStringWithSpace() ?: ""
                        )
                        toastL(R.string.ad_data_copied)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示服务信息的选项被点击的操作选项
     */
    private fun showServiceDataOptionsDialog(serviceDataInfo: ServiceDataInfo) {
        AlertDialog.Builder(this)
            .setTitle(R.string.service_data_options_dialog_title)
            .setItems(R.array.service_data_options) { _, which ->
                when (which) {
                    //复制服务UUID
                    0 -> {
                        val uuidString = serviceDataInfo.parcelUuid.toString()
                        Tool.setDataToClipboard(
                            this@ScanRecordParseActivity,
                            TAG,
                            uuidString
                        )
                        toastL(R.string.service_uuid_copied)
                    }
                    //仅复制AD数据
                    1 -> {
                        val data = serviceDataInfo.value
                        warnOut(data.toHexStringWithSpace())
                        Tool.setDataToClipboard(
                            this@ScanRecordParseActivity,
                            TAG,
                            data.toHexStringWithSpace() ?: ""
                        )
                        toastL(R.string.service_data_copied)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}