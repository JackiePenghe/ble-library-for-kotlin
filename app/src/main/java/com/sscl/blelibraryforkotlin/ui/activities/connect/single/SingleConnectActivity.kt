package com.sscl.blelibraryforkotlin.ui.activities.connect.single

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.sscl.baselibrary.files.FileUtil
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.baselibrary.utils.Tool
import com.sscl.baselibrary.utils.getByteArray
import com.sscl.baselibrary.utils.toHexStringWithSpace
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivitySingleConnectBinding
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.CharacteristicUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.ServiceUuidItem
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.ui.dialogs.WriteDataDialog
import com.sscl.blelibraryforkotlin.utils.*
import com.sscl.blelibraryforkotlin.viewmodels.SingleConnectActivityViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector
import com.sscl.bluetoothlowenergylibrary.intefaces.OnBleConnectStateChangedListener
import com.sscl.bluetoothlowenergylibrary.intefaces.OnCharacteristicNotifyDataListener
import com.sscl.bluetoothlowenergylibrary.intefaces.OnCharacteristicReadDataListener
import com.sscl.bluetoothlowenergylibrary.intefaces.OnCharacteristicWriteDataListener
import com.sscl.bluetoothlowenergylibrary.utils.BleUtils
import java.io.File
import java.io.FileWriter

/**
 * 单个设备连接界面
 */
class SingleConnectActivity : BaseDataBindingActivity<ActivitySingleConnectBinding>() {

    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_single_connect
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙服务与特征显示的适配器
     */
    private val servicesCharacteristicsListAdapter = ServicesCharacteristicsListAdapter()


    /**
     * 蓝牙连接回调
     */
    private val onBleConnectStateChangedListener = object : OnBleConnectStateChangedListener {
        /**
         * 设备已连接
         * 不建议在此方法中执行设备连接后的操作
         * 蓝牙库会在这个回调中执行 [android.bluetooth.BluetoothGatt.discoverServices]方法
         * 请在[OnBleConnectStateChangedListener.onServicesDiscovered]回调中执行设备连接后的操作
         */
        override fun onConnected() {
            binding.circlePointView.setColor(Color.BLUE)
        }

        /**
         * 设备断开连接
         */
        override fun onDisconnected() {
            isConnected = false
            binding.circlePointView.setColor(Color.RED)
            singleConnectActivityViewModel.buttonText.value = getString(R.string.connect_device)
            refreshAdapterData()
        }

        /**
         * 服务发现失败
         * 在设备连接后会自动触发服务发现
         * 如果服务发现调用失败则会触发此方法
         * 如果你想重新发现服务可以手动调用 [BleSingleConnector.discoverServices]
         * 但是通常情况下这依然会失败
         */
        override fun autoDiscoverServicesFailed() {
            binding.circlePointView.setColor(Color.RED)
            bleConnectorInstance.disconnect()
            dismissConnecting()
        }

        /**
         * 未知的操作GATT状态码
         *
         * @param statusCode GATT状态码
         */
        override fun unknownConnectStatus(statusCode: Int) {
            warnOut("GATT连接状态未知")
            dismissConnecting()
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
        }

        /**
         * 设备服务发现完成
         */
        override fun onServicesDiscovered() {
            isConnected = true
            binding.circlePointView.setColor(Color.GREEN)
            singleConnectActivityViewModel.buttonText.value = getString(R.string.disconnect_device)
            dismissConnecting()
            refreshAdapterData()
        }

        /**
         * 连接超时
         */
        override fun connectTimeout() {
            dismissConnecting()
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
        }

        /**
         * GATT状态码异常
         */
        override fun gattStatusError(gattErrorCode: Int) {
            dismissConnecting()
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
            toastL(getString(R.string.wrong_gatt_err, gattErrorCode))
        }
    }

    /**
     * 特征数据读取回调
     */
    private val onCharacteristicReadDataListener =
        OnCharacteristicReadDataListener { characteristic, value ->
            showReadDataResultDialog(characteristic, value)
        }

    /**
     * 特征数据写入回调
     */
    private val onCharacteristicWriteDataListener =
        OnCharacteristicWriteDataListener { characteristic, value ->
            showWriteDataResultDialog(characteristic, value)
        }

    /**
     * 特征数据通知回调
     */
    private val onCharacteristicNotifyDataListener =
        OnCharacteristicNotifyDataListener { characteristic, value ->
            showNotifyDataDialog(characteristic, value)
        }

    /**
     * viewModel
     */
    private val singleConnectActivityViewModel by viewModels<SingleConnectActivityViewModel> {
        SingleConnectActivityViewModel.SingleConnectActivityViewModelFactory
    }

    /**
     * 点击事件
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.button.id -> {
                doButtonClicked()
            }
        }
    }

    /**
     * 列表选项点击事件
     */
    private val onItemClickListener =
        OnItemClickListener { adapter, _, position ->
            if (adapter === servicesCharacteristicsListAdapter) {
                warnOut(
                    "servicesCharacteristicsListAdapter onItemClick position = $position"
                )
                val baseNode: BaseNode =
                    servicesCharacteristicsListAdapter.data[position]
                if (baseNode is ServiceUuidItem) {
                    if (!baseNode.isExpanded) {
                        val childNode: List<BaseNode> = baseNode.childNode
                        if (childNode.isEmpty()) {
                            toastL(
                                R.string.no_characteristic,
                            )
                            return@OnItemClickListener
                        }
                        servicesCharacteristicsListAdapter.expandAndCollapseOther(position)
                    } else {
                        servicesCharacteristicsListAdapter.collapse(position)
                    }
                    serviceUuid = if (baseNode.isExpanded) {
                        baseNode.uuid
                    } else {
                        null
                    }
                } else if (baseNode is CharacteristicUuidItem) {
                    warnOut(
                        "serviceUUID = $serviceUuid,characteristicUUID = $baseNode"
                    )
                    showOptionsDialog(serviceUuid ?: return@OnItemClickListener, baseNode.uuid)
                }
            }
        }

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙连接器
     */
    private lateinit var bleConnectorInstance: BleSingleConnector

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 设备是否已连接
     */
    private var isConnected = false

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 扫描的结果
     */
    private var scanResult: ScanResult? = null

    /**
     * 服务UUID缓存
     */
    private var serviceUuid: String? = null

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
        binding.viewModel = singleConnectActivityViewModel
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {
        initRecyclerView()
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
        binding.button.setOnClickListener(onClickListener)
        servicesCharacteristicsListAdapter.setOnItemClickListener(onItemClickListener)
    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {
        if (scanResult == null) {
            warnOut("测试设备信息，结束当前界面")
            finish()
            return
        }
        singleConnectActivityViewModel.scanResult.value = scanResult
        initBleConnector()
        singleConnectActivityViewModel.buttonText.value = getString(R.string.connect_device)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onDestroy() {
        super.onDestroy()
        bleConnectorInstance.disconnect()
        BleManager.releaseBleConnectorInstance()
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
     * 初始化BLE连接器
     */
    private fun initBleConnector() {
        bleConnectorInstance = BleManager.getBleConnectorInstance()
        bleConnectorInstance.setOnBleConnectStateChangedListener(onBleConnectStateChangedListener)
        bleConnectorInstance.setOnCharacteristicReadDataListener(onCharacteristicReadDataListener)
        bleConnectorInstance.setOnCharacteristicWriteDataListener(onCharacteristicWriteDataListener)
        bleConnectorInstance.setOnCharacteristicNotifyDataListener(
            onCharacteristicNotifyDataListener
        )
    }

    /**
     * 按钮点击的处理
     */
    @Synchronized
    private fun doButtonClicked() {
        if (isConnected) {
            val succeed = bleConnectorInstance.disconnect()
            warnOut("断开设备：succeed $succeed")
        } else {
            val scanResult = scanResult ?: return
            val succeed = bleConnectorInstance.connect(scanResult.device)
            if (succeed) {
                showConnecting()
                binding.circlePointView.setColor(Color.YELLOW)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshAdapterData() {
        servicesCharacteristicsListAdapter.data.clear()
        servicesCharacteristicsListAdapter.notifyDataSetChanged()
        if (!isConnected) {
            return
        }
        //获取服务列表
        val deviceServices = bleConnectorInstance.getServices()
        if (deviceServices != null) {
            val adapterData: MutableList<BaseNode> = ArrayList()
            for (i in deviceServices.indices) {
                val bluetoothGattService = deviceServices[i]
                val serviceUuidString = bluetoothGattService.uuid.toString()
                warnOut(
                    "bluetoothGattService UUID = $serviceUuidString"
                )
                val characteristics = bluetoothGattService.characteristics
                val characteristicUuidItems: MutableList<BaseNode> = ArrayList()
                for (j in characteristics.indices) {
                    val bluetoothGattCharacteristic = characteristics[j]
                    val characteristicUuidString = bluetoothGattCharacteristic.uuid.toString()
                    val canRead: Boolean =
                        bleConnectorInstance.canRead(serviceUuidString, characteristicUuidString)
                    val canWrite: Boolean =
                        bleConnectorInstance.canWrite(serviceUuidString, characteristicUuidString)
                    val canNotify: Boolean =
                        bleConnectorInstance.canNotify(serviceUuidString, characteristicUuidString)
                    val characteristicUuidItem = CharacteristicUuidItem(
                        BleUtils.getCharacteristicsUuidName(characteristicUuidString),
                        characteristicUuidString,
                        canRead,
                        canWrite,
                        canNotify
                    )
                    characteristicUuidItems.add(characteristicUuidItem)
                }
                val serviceUuidItem = ServiceUuidItem(
                    BleUtils.getServiceUuidName(serviceUuidString),
                    serviceUuidString,
                    characteristicUuidItems
                )
                adapterData.add(serviceUuidItem)
            }
            servicesCharacteristicsListAdapter.setList(adapterData)
        }
    }

    /**
     * 初始化RecyclerView的数据
     */
    private fun initRecyclerView() {
        binding.servicesCharacteristicsListRv.layoutManager = LinearLayoutManager(this)
        binding.servicesCharacteristicsListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.servicesCharacteristicsListRv.adapter = servicesCharacteristicsListAdapter
    }

    /**
     * 显示操作方式的对话框
     *
     * @param serviceUuidString        服务UUID
     * @param characteristicUuidString 特征UUID
     */
    private fun showOptionsDialog(serviceUuidString: String, characteristicUuidString: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.please_select_operate_dialog_title)
            .setItems(R.array.options) { _, which ->
                when (which) {
                    //读数据
                    0 -> {
                        if (!bleConnectorInstance.canRead(
                                serviceUuidString,
                                characteristicUuidString
                            )
                        ) {
                            toastL(R.string.characteristic_can_not_read)
                            return@setItems
                        }
                        if (!bleConnectorInstance.readData(
                                serviceUuidString,
                                characteristicUuidString
                            )
                        ) {
                            toastL(R.string.characteristic_read_failed)
                            return@setItems
                        }
                    }
                    //写数据
                    1 -> {
                        if (!bleConnectorInstance.canWrite(
                                serviceUuidString,
                                characteristicUuidString
                            )
                        ) {
                            toastL(R.string.characteristic_can_not_write)
                            return@setItems
                        }
                        showWriteDataDialog(serviceUuidString, characteristicUuidString)
                    }
                    //开启通知
                    2 -> {
                        if (!bleConnectorInstance.canNotify(
                                serviceUuidString,
                                characteristicUuidString
                            )
                        ) {
                            toastL(R.string.characteristic_can_not_notify)
                            return@setItems
                        }
                        if (!bleConnectorInstance.enableNotification(
                                serviceUuidString,
                                characteristicUuidString,
                                true
                            )
                        ) {
                            toastL(R.string.characteristic_notify_enable_failed)
                        } else {
                            toastL(R.string.characteristic_notify_enable_succeed)
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 展示读取到的数据的对话框
     */
    private fun showReadDataResultDialog(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this)
            .setTitle(R.string.read_data_result_dialog_title)
            .setMessage(
                "数据来源：${characteristic.uuid}\n十六进制：$hexValue\n字符串：$stringValue"
            )
            .setPositiveButton(R.string.copy_string_content) { _, _ ->
                Tool.setDataToClipboard(this@SingleConnectActivity, TAG, stringValue)
                toastL(R.string.string_copied)
            }
            .setNegativeButton(R.string.copy_hex_content) { _, _ ->
                Tool.setDataToClipboard(this@SingleConnectActivity, TAG, hexValue)
                toastL(R.string.hex_copied)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示写入数据结果回调的对话框
     */
    private fun showWriteDataResultDialog(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this)
            .setTitle(R.string.write_data_result_dialog_title)
            .setMessage(
                "数据来源：${characteristic.uuid}\n十六进制：$hexValue\n字符串：$stringValue"
            )
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 显示通知数据信息的对话框
     */
    private fun showNotifyDataDialog(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        saveToLog(characteristic.uuid.toString(), value)
//        val stringValue = String(value)
//        val hexValue = value.toHexStringWithSpace() ?: ""
//        AlertDialog.Builder(this)
//            .setTitle(R.string.notify_data_dialog_title)
//            .setMessage(
//                "数据来源：${characteristic.uuid}\n十六进制：$hexValue\n字符串：$stringValue"
//            )
//            .setPositiveButton(R.string.confirm, null)
//            .show()
    }

    /**
     * 显示写入数据的对话框
     */
    private fun showWriteDataDialog(serviceUuid: String, characteristicUuid: String) {
        WriteDataDialog(this, object : WriteDataDialog.OnConfirmButtonClickedListener {
            /**
             * 字符串数据
             */
            override fun stringData(string: String) {
                val byteArray = string.getByteArray(20)
                if (byteArray == null) {
                    warnOut("字符串数据：$string 格式错误")
                    return
                }
                val succeed = bleConnectorInstance.writeData(
                    serviceUuid,
                    characteristicUuid,
                    byteArray
                )
                if (!succeed) {
                    toastL(R.string.characteristic_write_failed)
                }
            }

            /**
             * 十六进制数据
             */
            override fun hexData(byteArray: ByteArray) {
                warnOut("写入十六进制数据：${byteArray.toHexStringWithSpace()}")
            }

        }).show()
    }

    /**
     * 保存到文件
     */
    @Synchronized
    private fun saveToLog(uuidString: String, value: ByteArray) {
        val file = File(FileUtil.sdCardCacheDir, "$uuidString.txt")
        warnOut("收到通知数据：uuidString = $uuidString + data = ${value.toHexStringWithSpace()}")
        if (file.exists()) {
            if (file.isDirectory) {
                file.delete()
                file.createNewFile()
            }
        } else {
            file.createNewFile()
        }
        val writer = FileWriter(file, true)
        writer.append(value.toHexStringWithSpace())
            .append("\n")
            .flush()
        writer.close()
    }
}