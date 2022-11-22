package com.sscl.blelibraryforkotlin.ui.fragments

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.sscl.baselibrary.fragment.BaseDataBindingFragment
import com.sscl.baselibrary.utils.*
import com.sscl.blelibraryforkotlin.MyApp
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.FragmentMultipleDeviceConnectBinding
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.CharacteristicUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.DescriptorUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.ServiceUuidItem
import com.sscl.blelibraryforkotlin.ui.dialogs.ChangeConnectedPhyDialog
import com.sscl.blelibraryforkotlin.ui.dialogs.WriteDataDialog
import com.sscl.blelibraryforkotlin.utils.*
import com.sscl.blelibraryforkotlin.viewmodels.fragments.MultipleDeviceConnectFragmentViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.connetor.multi.BleMultiConnector
import com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.enums.BlePhyOptions
import com.sscl.bluetoothlowenergylibrary.intefaces.*
import com.sscl.bluetoothlowenergylibrary.utils.BleUtils

/**
 * 多设备连接界面，某个设备的选项的单独界面
 */
class MultipleDeviceConnectFragment(val scanResult: ScanResult) :
    BaseDataBindingFragment<FragmentMultipleDeviceConnectBinding>() {

    /**
     * 设置fragment的布局
     *
     * @return 布局id
     */
    override fun setLayout(): Int {
        return R.layout.fragment_multiple_device_connect
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        /**
         * 默认的重连参数
         */
        private const val DEFAULT_RECONNECT = false

        /**
         * 默认的GATT传输层
         */
        @RequiresApi(Build.VERSION_CODES.M)
        private val DEFAULT_BLE_CONNECT_TRANSPORT = BleConnectTransport.TRANSPORT_AUTO

        /**
         * 默认的GATT传输层名称
         */
        private val DEFAULT_BLE_CONNECT_TRANSPORT_NAME =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MyApp.instance.getString(R.string.transport_auto)
            } else {
                MyApp.instance.getString(R.string.ble_connect_transport_not_support_with_low_system_version)
            }

        /**
         * 默认的GATT物理层
         */
        @RequiresApi(Build.VERSION_CODES.O)
        private val DEFAULT_BLE_CONNECT_PHY_MASK = BleConnectPhyMask.PHY_LE_1M_MASK

        /**
         * 默认的GATT物理层名称
         */
        private val DEFAULT_BLE_CONNECT_PHY_MASK_NAME =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                MyApp.instance.getString(R.string.connect_phy_le_1m_mask)
            } else {
                MyApp.instance.getString(R.string.ble_connect_phy_mask_not_support_with_low_system_version)
            }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    private val address: String = scanResult.device.address

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
            connected = false
            binding.circlePointView.setColor(Color.RED)
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
            dismissConnecting()
            connected = false
            binding.circlePointView.setColor(Color.MAGENTA)
            bleMultiConnector.close(address)
            toastL(R.string.discover_services_failed)
        }

        /**
         * 未知的连接状态
         *
         * @param statusCode 参考[android.bluetooth.BluetoothGatt]
         */
        override fun unknownConnectStatus(statusCode: Int) {
            warnOut("GATT连接状态未知")
            dismissConnecting()

            bleMultiConnector.close(address)
            binding.circlePointView.setColor(Color.MAGENTA)
            toastL(getString(R.string.connect_state_err, statusCode))
            connected = false
        }

        /**
         * 设备服务发现完成
         */
        override fun onServicesDiscovered() {
            connected = true
            binding.circlePointView.setColor(Color.GREEN)
            dismissConnecting()
            refreshAdapterData()
            toastL(R.string.connected)
        }

        /**
         * 连接超时
         */
        override fun connectTimeout() {
            dismissConnecting()
            connected = false
            bleMultiConnector.close(address)
            binding.circlePointView.setColor(Color.MAGENTA)
            toastL(R.string.connect_timeout)
        }

        /**
         * GATT状态码异常
         */
        override fun gattStatusError(gattErrorCode: Int) {
            dismissConnecting()
            bleMultiConnector.close(address)
            binding.circlePointView.setColor(Color.MAGENTA)
            connected = false
            toastL(getString(R.string.wrong_gatt_err, gattErrorCode))
        }
    }

    /**
     * 特征数据读取回调
     */
    private val onCharacteristicReadDataListener =
        OnCharacteristicReadDataListener { characteristic, value ->
            showReadDataResultDialog(characteristic.uuid.toString(), value)
        }

    /**
     * 特征数据写入回调
     */
    private val onCharacteristicWriteDataListener =
        OnCharacteristicWriteDataListener { characteristic, value ->
            showWriteDataResultDialog(characteristic.uuid.toString(), value)
        }

    /**
     * 特征数据通知回调
     */
    private val onCharacteristicNotifyDataListener =
        OnCharacteristicNotifyDataListener { characteristic, value ->
            showNotifyDataDialog(characteristic, value)
        }

    /**
     * 描述读取回调
     */
    private val onDescriptorReadDataListener = OnDescriptorReadDataListener { descriptor, value ->
        showReadDataResultDialog(descriptor.uuid.toString(), value)
    }

    /**
     * 描述写入回调
     */
    private val onDescriptorWriteDataListener = OnDescriptorWriteDataListener { descriptor, value ->
        showWriteDataResultDialog(descriptor.uuid.toString(), value)
    }

    /**
     * 可靠数据写入完成的回调
     */
    private val onReliableWriteCompletedListener = OnReliableWriteCompletedListener {
        reliableWriteBegin = false
        toastL(R.string.reliable_data_write_succeed)
    }

    /**
     * RSSI读取回调
     */
    private val onReadRemoteRssiListener = OnReadRemoteRssiListener {
        toastL(it.toString())
    }

    /**
     * MTU变化回调
     */
    private val onMtuChangedListener = OnMtuChangedListener {
        toastL(getString(R.string.mtu_changed, it))
    }

    /**
     * 物理层信息读取回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val onPhyReadListener = OnPhyReadListener { txPhy, rxPhy ->
        showPhyReadResultDialog(txPhy, rxPhy)
    }

    /**
     * 物理层信息有变更的回调
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val onPhyUpdateListener = OnPhyUpdateListener { txPhy, rxPhy ->
        showPhyUpdateDialog(txPhy, rxPhy)
    }

    /**
     * viewModel
     */
    private val multipleDeviceConnectFragmentViewModel = MultipleDeviceConnectFragmentViewModel()

    /**
     * 点击事件
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.connectButton.id -> {
                connectDevice()
            }
            binding.disconnectButton.id -> {
                val succeed = bleMultiConnector.disconnect(address)
                warnOut("断开设备：succeed $succeed")
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
                when (baseNode) {
                    is ServiceUuidItem -> {
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
                    }
                    is CharacteristicUuidItem -> {
                        if (!baseNode.isExpanded) {
                            val childNode: List<BaseNode> = baseNode.childNode
                            if (childNode.isEmpty()) {
                                showCharacteristicOptionsDialog(baseNode)
                                return@OnItemClickListener
                            }
                            servicesCharacteristicsListAdapter.expandAndCollapseOther(position)
                        } else {
                            servicesCharacteristicsListAdapter.collapse(position)
                        }
                        characteristicUuid = if (baseNode.isExpanded) {
                            baseNode.uuid
                        } else {
                            null
                        }
                    }
                    is DescriptorUuidItem -> {
                        warnOut(
                            "characteristicUUID = $characteristicUuid,descriptorUuid = ${baseNode.uuid}"
                        )
                        showDescriptorOptionsDialog(
                            baseNode
                        )
                    }
                }
            }
        }

    private val onItemLongClickListener = OnItemLongClickListener { adapter, _, position ->
        if (adapter == servicesCharacteristicsListAdapter) {
            warnOut(
                "servicesCharacteristicsListAdapter onItemClick position = $position"
            )
            val baseNode: BaseNode =
                servicesCharacteristicsListAdapter.data[position]
            when (baseNode) {
                is ServiceUuidItem -> {
                    warnOut(
                        "serviceUUID = ${baseNode.uuid}"
                    )
                    toastL(baseNode.uuid)
                }
                is CharacteristicUuidItem -> {
                    warnOut(
                        "serviceUUID = $serviceUuid,characteristicUUID = ${baseNode.uuid}"
                    )
                    showCharacteristicOptionsDialog(baseNode)

                }
                is DescriptorUuidItem -> {
                    warnOut(
                        "characteristicUUID = $characteristicUuid,descriptorUuid = ${baseNode.uuid}"
                    )
                    showDescriptorOptionsDialog(baseNode)
                    toastL(baseNode.uuid)
                }
            }

        }
        return@OnItemLongClickListener true
    }

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙连接器
     */
    private lateinit var bleMultiConnector: BleMultiConnector

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 设备是否已连接
     */
    private var connected = false

    /**
     * 记录当前是否已经开启可靠传输事务
     */
    private var reliableWriteBegin: Boolean = false

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 服务UUID缓存
     */
    private var serviceUuid: String? = null

    /**
     * 特征UUID缓存
     */
    private var characteristicUuid: String? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前进行的操作
     */
    override fun doBeforeSetLayout() {
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        binding.viewModel = multipleDeviceConnectFragmentViewModel
        initViewModelData()
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {
        initRecyclerView()
    }

    /**
     * 初始化事件
     */
    override fun initEvents() {
        binding.connectButton.setOnClickListener(onClickListener)
        binding.disconnectButton.setOnClickListener(onClickListener)
        servicesCharacteristicsListAdapter.setOnItemClickListener(onItemClickListener)
        servicesCharacteristicsListAdapter.setOnItemLongClickListener(onItemLongClickListener)
    }

    /**
     * 在最后执行的操作
     */
    override fun doAfterAll() {
        multipleDeviceConnectFragmentViewModel.scanResult.value = scanResult
        initBleConnector()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_single_connect, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.auto_reconnect -> {
                showSetAutoReconnectDialog()
            }
            R.id.ble_connect_transport -> {
                showSetBleConnectTransportDialog()
            }
            R.id.ble_connect_phy_mask -> {
                showSetBleConnectPhyMaskDialog()
            }
            R.id.ble_connect_timeout -> {
                showSetConnectTimeoutDialog()
            }
            R.id.ble_additional_options -> {
                showAdditionalOptionsDialog()
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onDestroy() {
        bleMultiConnector.close(address)
        bleMultiConnector.release(address)
        super.onDestroy()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 初始化BLE连接器
     */
    private fun initBleConnector() {
        bleMultiConnector = BleManager.getBleMultiConnector()
        bleMultiConnector.setOnCharacteristicReadDataListener(
            address,
            onCharacteristicReadDataListener
        )
        bleMultiConnector.setOnCharacteristicWriteDataListener(
            address,
            onCharacteristicWriteDataListener
        )
        bleMultiConnector.setOnCharacteristicNotifyDataListener(
            address, onCharacteristicNotifyDataListener
        )
        bleMultiConnector.setOnDescriptorReadDataListener(address, onDescriptorReadDataListener)
        bleMultiConnector.setOnDescriptorWriteDataListener(address, onDescriptorWriteDataListener)
        bleMultiConnector.setOnReliableWriteCompletedListener(
            address,
            onReliableWriteCompletedListener
        )
        bleMultiConnector.setOnReadRemoteRssiListener(address, onReadRemoteRssiListener)
        bleMultiConnector.setOnMtuChangedListener(address, onMtuChangedListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bleMultiConnector.setOnPhyReadListener(address, onPhyReadListener)
            bleMultiConnector.setOnPhyUpdateListener(address, onPhyUpdateListener)
        }
    }

    /**
     * 初始化ViewModel数据
     */
    private fun initViewModelData() {
        multipleDeviceConnectFragmentViewModel.autoReconnect.value =
            DEFAULT_RECONNECT
        multipleDeviceConnectFragmentViewModel.bleConnectTransportName.value =
            DEFAULT_BLE_CONNECT_TRANSPORT_NAME
        multipleDeviceConnectFragmentViewModel.bleConnectPhyMaskName.value =
            DEFAULT_BLE_CONNECT_PHY_MASK_NAME
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            multipleDeviceConnectFragmentViewModel.bleConnectTransport.value =
                DEFAULT_BLE_CONNECT_TRANSPORT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            multipleDeviceConnectFragmentViewModel.bleConnectPhyMask.value =
                DEFAULT_BLE_CONNECT_PHY_MASK
        }
    }

    /**
     * 连接设备
     */
    private fun connectDevice() {
        val succeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bleMultiConnector.connect(
                scanResult.device,
                onBleConnectStateChangedListener,
                multipleDeviceConnectFragmentViewModel.autoReconnect.value
                    ?: DEFAULT_RECONNECT,
                multipleDeviceConnectFragmentViewModel.bleConnectTransport.value
                    ?: DEFAULT_BLE_CONNECT_TRANSPORT,
                multipleDeviceConnectFragmentViewModel.bleConnectPhyMask.value
                    ?: DEFAULT_BLE_CONNECT_PHY_MASK
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bleMultiConnector.connect(
                scanResult.device,
                onBleConnectStateChangedListener,
                multipleDeviceConnectFragmentViewModel.autoReconnect.value
                    ?: DEFAULT_RECONNECT,
                multipleDeviceConnectFragmentViewModel.bleConnectTransport.value
                    ?: DEFAULT_BLE_CONNECT_TRANSPORT
            )
        } else {
            bleMultiConnector.connect(
                scanResult.device,
                onBleConnectStateChangedListener,
                multipleDeviceConnectFragmentViewModel.autoReconnect.value
                    ?: DEFAULT_RECONNECT
            )
        }
        if (succeed) {
            showConnecting()
            binding.circlePointView.setColor(Color.YELLOW)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshAdapterData() {
        servicesCharacteristicsListAdapter.data.clear()
        servicesCharacteristicsListAdapter.notifyDataSetChanged()
        if (!connected) {
            return
        }
        //获取服务列表
        val deviceServices = bleMultiConnector.getServices(address)
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
                    val characteristicCanRead: Boolean =
                        bleMultiConnector.checkCharacteristicProperties(
                            address,
                            serviceUuidString,
                            characteristicUuidString,
                            BluetoothGattCharacteristic.PROPERTY_READ
                        )
                    val characteristicCanWrite: Boolean =
                        bleMultiConnector.checkCharacteristicProperties(
                            address,
                            serviceUuidString,
                            characteristicUuidString,
                            BluetoothGattCharacteristic.PROPERTY_WRITE
                        )
                    val characteristicCanNotify: Boolean =
                        bleMultiConnector.checkCharacteristicProperties(
                            address,
                            serviceUuidString,
                            characteristicUuidString,
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY
                        )
                    val descriptors = bluetoothGattCharacteristic.descriptors
                    val descriptorUuidItems: MutableList<BaseNode> = ArrayList()
                    for (k in descriptors.indices) {
                        val bluetoothGattDescriptor = descriptors[k]
                        val uuid = bluetoothGattDescriptor.uuid
                        val descriptorCanRead =
                            (bleMultiConnector.checkDescriptorPermission(
                                bluetoothGattDescriptor,
                                BluetoothGattDescriptor.PERMISSION_READ
                            ))
                        val descriptorCanWrite =
                            bleMultiConnector.checkDescriptorPermission(
                                bluetoothGattDescriptor,
                                BluetoothGattDescriptor.PERMISSION_WRITE
                            )
                        descriptorUuidItems.add(
                            DescriptorUuidItem(
                                uuid.toString(),
                                descriptorCanRead,
                                descriptorCanWrite
                            )
                        )
                    }
                    val characteristicUuidItem = CharacteristicUuidItem(
                        BleUtils.getCharacteristicsUuidName(characteristicUuidString),
                        characteristicUuidString,
                        characteristicCanRead,
                        characteristicCanWrite,
                        characteristicCanNotify,
                        descriptorUuidItems
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
        binding.servicesCharacteristicsListRv.layoutManager =
            LinearLayoutManager(this.context ?: return)
        binding.servicesCharacteristicsListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.servicesCharacteristicsListRv.adapter = servicesCharacteristicsListAdapter
    }

    /**
     * 显示特征的操作方式的对话框
     *
     * @param characteristicUuidItem 特征UUID
     */
    private fun showCharacteristicOptionsDialog(
        characteristicUuidItem: CharacteristicUuidItem
    ) {
        val serviceUuid = serviceUuid ?: return
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.please_select_operate_dialog_title)
            .setItems(R.array.characteristic_options) { _, which ->
                when (which) {
                    //读数据
                    0 -> {
                        if (!characteristicUuidItem.canRead) {
                            toastL(R.string.characteristic_can_not_read)
                            return@setItems
                        }
                        if (!bleMultiConnector.readCharacteristicData(
                                address,
                                serviceUuid,
                                characteristicUuidItem.uuid
                            )
                        ) {
                            toastL(R.string.characteristic_read_failed)
                            return@setItems
                        }
                    }
                    //写数据
                    1 -> {
                        if (!characteristicUuidItem.canWrite) {
                            toastL(R.string.characteristic_can_not_write)
                            return@setItems
                        }
                        showWriteCharacteristicDataDialog(serviceUuid, characteristicUuidItem.uuid)
                    }
                    //开启通知
                    2 -> {
                        if (!characteristicUuidItem.canNotify) {
                            toastL(R.string.characteristic_can_not_notify)
                            return@setItems
                        }
                        if (!bleMultiConnector.enableNotification(
                                address,
                                serviceUuid,
                                characteristicUuidItem.uuid,
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
     * 显示描述
     */
    private fun showDescriptorOptionsDialog(descriptorUuidItem: DescriptorUuidItem) {
        val serviceUuid = serviceUuid ?: return
        val characteristicUuid = characteristicUuid ?: return
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.please_select_operate_dialog_title)
            .setItems(R.array.descriptor_options) { _, which ->
                when (which) {
                    //读数据
                    0 -> {
                        if (!descriptorUuidItem.canRead) {
                            toastL(R.string.descriptor_can_not_read)
                            return@setItems
                        }
                        val succeed = bleMultiConnector.readDescriptorData(
                            address,
                            serviceUuid,
                            characteristicUuid,
                            descriptorUuidItem.uuid
                        )
                        if (!succeed) {
                            toastL(R.string.descriptor_read_failed)
                        }
                    }
                    //写数据
                    1 -> {
                        if (!descriptorUuidItem.canWrite) {
                            toastL(R.string.descriptor_can_not_write)
                            return@setItems
                        }
                        showWriteDescriptorDataDialog(descriptorUuidItem)
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
        uuidString: String,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.read_data_result_dialog_title)
            .setMessage(
                "数据来源：$uuidString\n十六进制：$hexValue\n字符串：$stringValue"
            )
            .setPositiveButton(R.string.copy_string_content) { _, _ ->
                Tool.setDataToClipboard(this.context ?: return@setPositiveButton, TAG, stringValue)
                toastL(R.string.string_copied)
            }
            .setNegativeButton(R.string.copy_hex_content) { _, _ ->
                Tool.setDataToClipboard(this.context ?: return@setNegativeButton, TAG, hexValue)
                toastL(R.string.hex_copied)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示写入数据结果回调的对话框
     */
    private fun showWriteDataResultDialog(
        uuidString: String,
        value: ByteArray,
        fromCharacter: Boolean = true
    ) {
        if (fromCharacter && reliableWriteBegin) {
            showReliableWriteDataResultDialog(uuidString, value)
            return
        }
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.write_data_result_dialog_title)
            .setMessage("数据来源：$uuidString\n十六进制：$hexValue\n字符串：$stringValue")
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 显示设置超时时间的对话框
     */
    private fun showSetConnectTimeoutDialog() {
        val view = View.inflate(this.context ?: return, R.layout.view_ble_connect_timeout, null)
        val editText = view.findViewById<EditText>(R.id.connect_timeout_et)
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.set_ble_connect_timeout_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                var text = editText.text.toString()
                if (text.isEmpty()) {
                    text = "0"
                }
                bleMultiConnector.setConnectTimeout(text.toLong())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示通知数据信息的对话框
     */
    private fun showNotifyDataDialog(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.notify_data_dialog_title)
            .setMessage(
                "数据来源：${characteristic.uuid}\n十六进制：$hexValue\n字符串：$stringValue"
            )
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 显示写入数据的对话框
     */
    private fun showWriteCharacteristicDataDialog(serviceUuid: String, characteristicUuid: String) {

        WriteDataDialog(
            this.context ?: return,
            object : WriteDataDialog.OnConfirmButtonClickedListener {
                /**
                 * 字符串数据
                 */
                /**
                 * 字符串数据
                 */
                override fun stringData(string: String) {
                    val byteArray = string.getByteArray(20)
                    if (byteArray == null) {
                        warnOut("字符串数据：$string 格式错误")
                        return
                    }
                    val succeed = bleMultiConnector.writeCharacteristicData(
                        address,
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
                /**
                 * 十六进制数据
                 */
                override fun hexData(byteArray: ByteArray) {
                    warnOut("写入十六进制数据：${byteArray.toHexStringWithSpace()}")
                    val succeed = bleMultiConnector.writeCharacteristicData(
                        address,
                        serviceUuid,
                        characteristicUuid,
                        byteArray
                    )
                    if (!succeed) {
                        toastL(R.string.characteristic_write_failed)
                    }
                }

            }).show()
    }

    /**
     * 显示写入描述数据的对话框
     */
    private fun showWriteDescriptorDataDialog(descriptorUuidItem: DescriptorUuidItem) {
        val serviceUuid = serviceUuid ?: return
        val characteristicUuid = characteristicUuid ?: return
        WriteDataDialog(
            this.context ?: return,
            object : WriteDataDialog.OnConfirmButtonClickedListener {
                /**
                 * 字符串数据
                 */
                /**
                 * 字符串数据
                 */
                override fun stringData(string: String) {
                    val byteArray = string.getByteArray(20)
                    if (byteArray == null) {
                        warnOut("字符串数据：$string 格式错误")
                        return
                    }
                    val succeed = bleMultiConnector.writeDescriptorData(
                        address,
                        serviceUuid,
                        characteristicUuid,
                        descriptorUuidItem.uuid,
                        byteArray
                    )
                    if (!succeed) {
                        toastL(R.string.descriptor_write_failed)
                    }
                }

                /**
                 * 十六进制数据
                 */
                /**
                 * 十六进制数据
                 */
                override fun hexData(byteArray: ByteArray) {
                    warnOut("写入十六进制数据：${byteArray.toHexStringWithSpace()}")
                    val succeed = bleMultiConnector.writeDescriptorData(
                        address,
                        serviceUuid,
                        characteristicUuid,
                        descriptorUuidItem.uuid,
                        byteArray
                    )
                    if (!succeed) {
                        toastL(R.string.descriptor_write_failed)
                    }
                }

            }).show()
    }

    /**
     * 显示设置连接参数的对话框
     */
    private fun showSetAutoReconnectDialog() {
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.auto_reconnect_dialog_title)
            .setItems(R.array.booleans) { _, which ->
                when (which) {
                    //是
                    0 -> {
                        multipleDeviceConnectFragmentViewModel.autoReconnect.value = true
                    }
                    //否
                    1 -> {
                        multipleDeviceConnectFragmentViewModel.autoReconnect.value = false
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示设置GATT传输层的对话框
     */
    private fun showSetBleConnectTransportDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toastL(R.string.ble_connect_transport_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.ble_reconnect_transport_dialog_title)
            .setItems(R.array.ble_reconnect_transport) { _, which ->
                when (which) {
                    //transport_auto
                    0 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_AUTO
                        multipleDeviceConnectFragmentViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_auto)
                    }
                    //transport_br_edr
                    1 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_BR_EDR
                        multipleDeviceConnectFragmentViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_br_edr)
                    }
                    //transport_le
                    2 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_LE
                        multipleDeviceConnectFragmentViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_le)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示设置BLE管理层的对话框
     */
    private fun showSetBleConnectPhyMaskDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.ble_connect_phy_mask_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.ble_connect_phy_mask_dialog_title)
            .setItems(R.array.ble_connect_phy_mask) { _, which ->
                when (which) {
                    //phy_le_1m_mask
                    0 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_1M_MASK
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_1m_mask)
                    }
                    //phy_le_2m_mask
                    1 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_2M_MASK
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_2m_mask)
                    }
                    //phy_le_coded_mask
                    2 -> {
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_CODED_MASK
                        multipleDeviceConnectFragmentViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_coded_mask)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示更多选项的对话框
     */
    private fun showAdditionalOptionsDialog() {
        if (!connected) {
            toastL(R.string.additional_options_need_connect_after)
            return
        }
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.additional_options_dialog_title)
            .setItems(R.array.connect_additional_options) { _, which ->
                when (which) {
                    //开启可靠数据写入事务
                    0 -> {
                        beginReliableWrite()
                    }
                    //取消可靠数据写入模式
                    1 -> {
                        abortReliableWrite()
                    }
                    //读取RSSI
                    2 -> {
                        readRssi()
                    }
                    //更改MTU(单包数据最大值)
                    3 -> {
                        showSetMtuDialog()
                    }
                    //读取当前连接的物理层
                    4 -> {
                        readPhy()
                    }
                    //更改连接物理层
                    5 -> {
                        showChangePhyDialog()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 读取当前连接的物理层
     */
    private fun readPhy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.read_phy_not_support_with_low_system_version)
            return
        }
        val succeed = bleMultiConnector.readPhy(address)
        if (!succeed) {
            toastL(R.string.read_phy_failed)
        }
    }

    /**
     * 显示设置MTU的对话框
     */
    private fun showSetMtuDialog() {
        val view = View.inflate(this.context ?: return, R.layout.view_set_mtu, null)
        val editText = view.findViewById<EditText>(R.id.mtu_et)
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.set_mtu)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isEmpty()) {
                    return@setPositiveButton
                }
                val succeed = bleMultiConnector.requestMtu(address,text.toInt())
                if (!succeed) {
                    toastL(R.string.request_mtu_failed)
                }
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /**
     * 读取设备RSSI
     */
    private fun readRssi() {
        val succeed = bleMultiConnector.readRemoteRssi(address)
        if (!succeed) {
            toastL(R.string.read_rssi_failed)
        }
    }

    /**
     * 开启可靠数据写入事务
     */
    private fun beginReliableWrite() {
        if (reliableWriteBegin) {
            toastL(R.string.already_in_reliable_write_mode)
            return
        }
        reliableWriteBegin = bleMultiConnector.beginReliableWrite(address)
        if (!reliableWriteBegin) {
            toastL(R.string.reliable_write_mode_begin_failed)
        }
    }

    /**
     * 可靠数据写入结果回调对话框
     */
    private fun showReliableWriteDataResultDialog(uuidString: String, value: ByteArray) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.reliable_write_data_result_dialog_title)
            .setMessage("数据来源：$uuidString\n十六进制：$hexValue\n字符串：$stringValue")
            .setPositiveButton(R.string.commit) { _, _ ->
                val succeed = bleMultiConnector.executeReliableWrite(address)
                if (!succeed) {
                    toastL(R.string.reliable_data_write_failed)
                    bleMultiConnector.abortReliableWrite(address)
                    reliableWriteBegin = false
                }
            }
            .setNegativeButton(R.string.cancel_write) { _, _ ->
                abortReliableWrite()
            }
            .setNeutralButton(R.string.continue_write, null)
            .setCancelable(false)
            .show()
    }

    /**
     * 取消可靠数据写入
     */
    private fun abortReliableWrite() {
        if (!reliableWriteBegin) {
            toastL(R.string.not_in_reliable_write_mode)
            return
        }
        val succeed = bleMultiConnector.abortReliableWrite(address)
        if (!succeed) {
            toastL(R.string.reliable_data_cancel_write_failed)
            return
        }
        reliableWriteBegin = false
        toastL(R.string.canceled)
    }

    /**
     * 显示读取物理层信息的结果的对话框
     */
    private fun showPhyReadResultDialog(txPhy: BlePhy?, rxPhy: BlePhy?) {
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.read_phy_result_dialog_title)
            .setMessage(getString(R.string.phy_read_result, txPhy?.name, rxPhy?.name))
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 显示物理层信息变更的对话框
     */
    private fun showPhyUpdateDialog(txPhy: BlePhy?, rxPhy: BlePhy?) {
        AlertDialog.Builder(this.context ?: return)
            .setTitle(R.string.phy_update_dialog_title)
            .setMessage(getString(R.string.phy_read_result, txPhy?.name, rxPhy?.name))
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * 显示更连接改物理层的对话框
     */
    private fun showChangePhyDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.change_phy_not_support_with_low_system_version)
            return
        }
        ChangeConnectedPhyDialog(
            this.context ?: return,
            object : ChangeConnectedPhyDialog.OnConfirmButtonClickListener {
                /**
                 * 确认按钮被点击的回调
                 */
                /**
                 * 确认按钮被点击的回调
                 */
                override fun onConfirmButtonClick(
                    txPhy: BlePhy,
                    rxPhy: BlePhy,
                    blePhyOptions: BlePhyOptions
                ) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        toastL(R.string.change_phy_not_support_with_low_system_version)
                        return
                    }
                    val succeed =
                        bleMultiConnector.setPreferredPhy(address,txPhy, rxPhy, blePhyOptions)
                    if (!succeed) {
                        toastL(R.string.change_phy_failed)
                    }
                }
            })
            .show()
    }
}