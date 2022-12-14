package com.sscl.blelibraryforkotlin.ui.activities.connect.single

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanResult
import android.graphics.Color
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.baselibrary.utils.Tool
import com.sscl.baselibrary.utils.getByteArray
import com.sscl.baselibrary.utils.toHexStringWithSpace
import com.sscl.blelibraryforkotlin.MyApp
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivitySingleConnectBinding
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.CharacteristicUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.DescriptorUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.ServiceUuidItem
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.ui.dialogs.ChangeConnectedPhyDialog
import com.sscl.blelibraryforkotlin.ui.dialogs.WriteDataDialog
import com.sscl.blelibraryforkotlin.utils.*
import com.sscl.blelibraryforkotlin.viewmodels.activities.SingleConnectActivityViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.connetor.single.BleSingleConnector
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectTransport
import com.sscl.bluetoothlowenergylibrary.enums.BlePhyOptions
import com.sscl.bluetoothlowenergylibrary.intefaces.*
import com.sscl.bluetoothlowenergylibrary.utils.BleUtils

/**
 * ????????????????????????
 */
class SingleConnectActivity : BaseDataBindingActivity<ActivitySingleConnectBinding>() {

    /**
     * ????????????
     */
    override fun setLayout(): Int {
        return R.layout.activity_single_connect
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * ????????????
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        /**
         * ?????????????????????
         */
        private const val DEFAULT_RECONNECT = false

        /**
         * ?????????GATT?????????
         */
        @RequiresApi(Build.VERSION_CODES.M)
        private val DEFAULT_BLE_CONNECT_TRANSPORT = BleConnectTransport.TRANSPORT_AUTO

        /**
         * ?????????GATT???????????????
         */
        private val DEFAULT_BLE_CONNECT_TRANSPORT_NAME =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MyApp.instance.getString(R.string.transport_auto)
            } else {
                MyApp.instance.getString(R.string.ble_connect_transport_not_support_with_low_system_version)
            }

        /**
         * ?????????GATT?????????
         */
        @RequiresApi(Build.VERSION_CODES.O)
        private val DEFAULT_BLE_CONNECT_PHY_MASK = BleConnectPhyMask.PHY_LE_1M_MASK

        /**
         * ?????????GATT???????????????
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
     * ????????????
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * ???????????? * * * * * * * * * * * * * * * * * * */

    /**
     * ???????????????????????????????????????
     */
    private val servicesCharacteristicsListAdapter = ServicesCharacteristicsListAdapter()


    /**
     * ??????????????????
     */
    private val onBleConnectStateChangedListener = object : OnBleConnectStateChangedListener {
        /**
         * ???????????????
         * ??????????????????????????????????????????????????????
         * ???????????????????????????????????? [android.bluetooth.BluetoothGatt.discoverServices]??????
         * ??????[OnBleConnectStateChangedListener.onServicesDiscovered]???????????????????????????????????????
         */
        override fun onConnected() {
            binding.circlePointView.setColor(Color.BLUE)
        }

        /**
         * ??????????????????
         */
        override fun onDisconnected() {
            connected = false
            binding.circlePointView.setColor(Color.RED)
            singleConnectActivityViewModel.buttonText.value = getString(R.string.connect_device)
            refreshAdapterData()
        }

        /**
         * ??????????????????
         * ?????????????????????????????????????????????
         * ???????????????????????????????????????????????????
         * ???????????????????????????????????????????????? [BleSingleConnector.discoverServices]
         * ???????????????????????????????????????
         */
        override fun autoDiscoverServicesFailed() {
            dismissConnecting()
            connected = false
            binding.circlePointView.setColor(Color.MAGENTA)
            bleConnectorInstance.close()
            toastL(R.string.discover_services_failed)
        }

        /**
         * ?????????????????????
         *
         * @param statusCode ??????[android.bluetooth.BluetoothGatt]
         */
        override fun unknownConnectStatus(statusCode: Int) {
            warnOut("GATT??????????????????")
            dismissConnecting()
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
            toastL(getString(R.string.connect_state_err, statusCode))
            connected = false
        }

        /**
         * ????????????????????????
         */
        override fun onServicesDiscovered() {
            connected = true
            binding.circlePointView.setColor(Color.GREEN)
            singleConnectActivityViewModel.buttonText.value = getString(R.string.disconnect_device)
            dismissConnecting()
            refreshAdapterData()
            toastL(R.string.connected)
        }

        /**
         * ????????????
         */
        override fun connectTimeout() {
            dismissConnecting()
            connected = false
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
            toastL(R.string.connect_timeout)
        }

        /**
         * GATT???????????????
         */
        override fun gattStatusError(gattErrorCode: Int) {
            dismissConnecting()
            bleConnectorInstance.close()
            binding.circlePointView.setColor(Color.MAGENTA)
            connected = false
            singleConnectActivityViewModel.buttonText.value = getString(R.string.connect_device)
            toastL(getString(R.string.wrong_gatt_err, gattErrorCode))
        }
    }

    /**
     * ????????????????????????
     */
    private val onCharacteristicReadDataListener =
        OnCharacteristicReadDataListener { characteristic, value ->
            showReadDataResultDialog(characteristic.uuid.toString(), value)
        }

    /**
     * ????????????????????????
     */
    private val onCharacteristicWriteDataListener =
        OnCharacteristicWriteDataListener { characteristic, value ->
            showWriteDataResultDialog(characteristic.uuid.toString(), value)
        }

    /**
     * ????????????????????????
     */
    private val onCharacteristicNotifyDataListener =
        OnCharacteristicNotifyDataListener { characteristic, value ->
            showNotifyDataDialog(characteristic, value)
        }

    /**
     * ??????????????????
     */
    private val onDescriptorReadDataListener = OnDescriptorReadDataListener { descriptor, value ->
        showReadDataResultDialog(descriptor.uuid.toString(), value)
    }

    /**
     * ??????????????????
     */
    private val onDescriptorWriteDataListener = OnDescriptorWriteDataListener { descriptor, value ->
        showWriteDataResultDialog(descriptor.uuid.toString(), value)
    }

    /**
     * ?????????????????????????????????
     */
    private val onReliableWriteCompletedListener = OnReliableWriteCompletedListener {
        reliableWriteBegin = false
        toastL(R.string.reliable_data_write_succeed)
    }

    /**
     * RSSI????????????
     */
    private val onReadRemoteRssiListener = OnReadRemoteRssiListener {
        toastL(it.toString())
    }

    /**
     * MTU????????????
     */
    private val onMtuChangedListener = OnMtuChangedListener {
        toastL(getString(R.string.mtu_changed, it))
    }

    /**
     * ???????????????????????????
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val onPhyReadListener = OnPhyReadListener { txPhy, rxPhy ->
        showPhyReadResultDialog(txPhy, rxPhy)
    }

    /**
     * ?????????????????????????????????
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val onPhyUpdateListener = OnPhyUpdateListener { txPhy, rxPhy ->
        showPhyUpdateDialog(txPhy, rxPhy)
    }

    /**
     * viewModel
     */
    private val singleConnectActivityViewModel by viewModels<SingleConnectActivityViewModel> {
        SingleConnectActivityViewModel.SingleConnectActivityViewModelFactory
    }

    /**
     * ????????????
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.button.id -> {
                doButtonClicked()
            }
        }
    }

    /**
     * ????????????????????????
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

    /* * * * * * * * * * * * * * * * * * * ????????????????????? * * * * * * * * * * * * * * * * * * */

    /**
     * ???????????????
     */
    private lateinit var bleConnectorInstance: BleSingleConnector

    /* * * * * * * * * * * * * * * * * * * ???????????? * * * * * * * * * * * * * * * * * * */

    /**
     * ?????????????????????
     */
    private var connected = false

    /**
     * ????????????????????????????????????????????????
     */
    private var reliableWriteBegin: Boolean = false

    /* * * * * * * * * * * * * * * * * * * ???????????? * * * * * * * * * * * * * * * * * * */

    /**
     * ???????????????
     */
    private var scanResult: ScanResult? = null

    /**
     * ??????UUID??????
     */
    private var serviceUuid: String? = null

    /**
     * ??????UUID??????
     */
    private var characteristicUuid: String? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * ????????????
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    /**
     * ??????????????????????????????????????????
     */
    override fun doBeforeSetLayout() {
        getIntentData()
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    override fun doBeforeInitOthers() {
    }

    /**
     * ??????DataBinding
     * ??????????????????binding???viewModel?????????????????????
     */
    override fun setBinding() {
        binding.viewModel = singleConnectActivityViewModel
        initViewModelData()
    }

    /**
     * ?????????????????????
     */
    override fun initViewData() {
        initRecyclerView()
    }

    /**
     * ?????????????????????
     */
    override fun initOtherData() {

    }

    /**
     * ???????????????
     */
    override fun initEvents() {
        binding.button.setOnClickListener(onClickListener)
        servicesCharacteristicsListAdapter.setOnItemClickListener(onItemClickListener)
        servicesCharacteristicsListAdapter.setOnItemLongClickListener(onItemLongClickListener)
    }

    /**
     * ????????????????????????
     */
    override fun doAfterAll() {
        if (scanResult == null) {
            warnOut("???????????????????????????????????????")
            finish()
            return
        }
        singleConnectActivityViewModel.scanResult.value = scanResult
        initBleConnector()
        singleConnectActivityViewModel.buttonText.value = getString(R.string.connect_device)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * ????????????
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_single_connect, menu)
        return true
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
        super.onDestroy()
        BleManager.releaseBleConnectorInstance()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * ????????????
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * ??????Intent???????????????
     */
    private fun getIntentData() {
        scanResult = intent.getParcelableExtra(IntentConstants.SCAN_RESULT)
    }

    /**
     * ?????????BLE?????????
     */
    private fun initBleConnector() {
        bleConnectorInstance = BleManager.getBleConnectorInstance()
        bleConnectorInstance.setOnBleConnectStateChangedListener(onBleConnectStateChangedListener)
        bleConnectorInstance.setOnCharacteristicReadDataListener(onCharacteristicReadDataListener)
        bleConnectorInstance.setOnCharacteristicWriteDataListener(onCharacteristicWriteDataListener)
        bleConnectorInstance.setOnCharacteristicNotifyDataListener(
            onCharacteristicNotifyDataListener
        )
        bleConnectorInstance.setOnDescriptorReadDataListener(onDescriptorReadDataListener)
        bleConnectorInstance.setOnDescriptorWriteDataListener(onDescriptorWriteDataListener)
        bleConnectorInstance.setOnReliableWriteCompletedListener(onReliableWriteCompletedListener)
        bleConnectorInstance.setOnReadRemoteRssiListener(onReadRemoteRssiListener)
        bleConnectorInstance.setOnMtuChangedListener(onMtuChangedListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bleConnectorInstance.setOnPhyReadListener(onPhyReadListener)
            bleConnectorInstance.setOnPhyUpdateListener(onPhyUpdateListener)
        }
    }

    /**
     * ?????????ViewModel??????
     */
    private fun initViewModelData() {
        singleConnectActivityViewModel.autoReconnect.value = DEFAULT_RECONNECT
        singleConnectActivityViewModel.bleConnectTransportName.value =
            DEFAULT_BLE_CONNECT_TRANSPORT_NAME
        singleConnectActivityViewModel.bleConnectPhyMaskName.value =
            DEFAULT_BLE_CONNECT_PHY_MASK_NAME
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            singleConnectActivityViewModel.bleConnectTransport.value = DEFAULT_BLE_CONNECT_TRANSPORT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            singleConnectActivityViewModel.bleConnectPhyMask.value = DEFAULT_BLE_CONNECT_PHY_MASK
        }
    }

    /**
     * ?????????????????????
     */
    @Synchronized
    private fun doButtonClicked() {
        if (connected) {
            val succeed = bleConnectorInstance.disconnect()
            warnOut("???????????????succeed $succeed")
        } else {
            connectDevice()
        }
    }

    /**
     * ????????????
     */
    private fun connectDevice() {
        val scanResult = scanResult ?: return
        val succeed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bleConnectorInstance.connect(
                scanResult.device,
                singleConnectActivityViewModel.autoReconnect.value ?: DEFAULT_RECONNECT,
                singleConnectActivityViewModel.bleConnectTransport.value
                    ?: DEFAULT_BLE_CONNECT_TRANSPORT,
                singleConnectActivityViewModel.bleConnectPhyMask.value
                    ?: DEFAULT_BLE_CONNECT_PHY_MASK
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bleConnectorInstance.connect(
                scanResult.device,
                singleConnectActivityViewModel.autoReconnect.value ?: DEFAULT_RECONNECT,
                singleConnectActivityViewModel.bleConnectTransport.value
                    ?: DEFAULT_BLE_CONNECT_TRANSPORT
            )
        } else {
            bleConnectorInstance.connect(
                scanResult.device,
                singleConnectActivityViewModel.autoReconnect.value ?: DEFAULT_RECONNECT
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
        //??????????????????
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
                    val characteristicCanRead: Boolean =
                        bleConnectorInstance.checkCharacteristicProperties(
                            serviceUuidString,
                            characteristicUuidString,
                            BluetoothGattCharacteristic.PROPERTY_READ
                        )
                    val characteristicCanWrite: Boolean =
                        bleConnectorInstance.checkCharacteristicProperties(
                            serviceUuidString,
                            characteristicUuidString,
                            BluetoothGattCharacteristic.PROPERTY_WRITE
                        )
                    val characteristicCanNotify: Boolean =
                        bleConnectorInstance.checkCharacteristicProperties(
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
                            (bleConnectorInstance.checkDescriptorPermission(
                                bluetoothGattDescriptor,
                                BluetoothGattDescriptor.PERMISSION_READ
                            ))
                        val descriptorCanWrite =
                            bleConnectorInstance.checkDescriptorPermission(
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
     * ?????????RecyclerView?????????
     */
    private fun initRecyclerView() {
        binding.servicesCharacteristicsListRv.layoutManager = LinearLayoutManager(this)
        binding.servicesCharacteristicsListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.servicesCharacteristicsListRv.adapter = servicesCharacteristicsListAdapter
    }

    /**
     * ???????????????????????????????????????
     *
     * @param characteristicUuidItem ??????UUID
     */
    private fun showCharacteristicOptionsDialog(
        characteristicUuidItem: CharacteristicUuidItem
    ) {
        val serviceUuid = serviceUuid ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.please_select_operate_dialog_title)
            .setItems(R.array.characteristic_options) { _, which ->
                when (which) {
                    //?????????
                    0 -> {
                        if (!characteristicUuidItem.canRead) {
                            toastL(R.string.characteristic_can_not_read)
                            return@setItems
                        }
                        if (!bleConnectorInstance.readCharacteristicData(
                                serviceUuid,
                                characteristicUuidItem.uuid
                            )
                        ) {
                            toastL(R.string.characteristic_read_failed)
                            return@setItems
                        }
                    }
                    //?????????
                    1 -> {
                        if (!characteristicUuidItem.canWrite) {
                            toastL(R.string.characteristic_can_not_write)
                            return@setItems
                        }
                        showWriteCharacteristicDataDialog(serviceUuid, characteristicUuidItem.uuid)
                    }
                    //????????????
                    2 -> {
                        if (!characteristicUuidItem.canNotify) {
                            toastL(R.string.characteristic_can_not_notify)
                            return@setItems
                        }
                        if (!bleConnectorInstance.enableNotification(
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
     * ????????????
     */
    private fun showDescriptorOptionsDialog(descriptorUuidItem: DescriptorUuidItem) {
        val serviceUuid = serviceUuid ?: return
        val characteristicUuid = characteristicUuid ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.please_select_operate_dialog_title)
            .setItems(R.array.descriptor_options) { _, which ->
                when (which) {
                    //?????????
                    0 -> {
                        if (!descriptorUuidItem.canRead) {
                            toastL(R.string.descriptor_can_not_read)
                            return@setItems
                        }
                        val succeed = bleConnectorInstance.readDescriptorData(
                            serviceUuid,
                            characteristicUuid,
                            descriptorUuidItem.uuid
                        )
                        if (!succeed) {
                            toastL(R.string.descriptor_read_failed)
                        }
                    }
                    //?????????
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
     * ????????????????????????????????????
     */
    private fun showReadDataResultDialog(
        uuidString: String,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this)
            .setTitle(R.string.read_data_result_dialog_title)
            .setMessage(
                "???????????????$uuidString\n???????????????$hexValue\n????????????$stringValue"
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
     * ??????????????????????????????????????????
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
        AlertDialog.Builder(this)
            .setTitle(R.string.write_data_result_dialog_title)
            .setMessage("???????????????$uuidString\n???????????????$hexValue\n????????????$stringValue")
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * ????????????????????????????????????
     */
    private fun showSetConnectTimeoutDialog() {
        val view = View.inflate(this, R.layout.view_ble_connect_timeout, null)
        val editText = view.findViewById<EditText>(R.id.connect_timeout_et)
        AlertDialog.Builder(this)
            .setTitle(R.string.set_ble_connect_timeout_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                var text = editText.text.toString()
                if (text.isEmpty()) {
                    text = "0"
                }
                bleConnectorInstance.setConnectTimeout(text.toLong())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * ????????????????????????????????????
     */
    private fun showNotifyDataDialog(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this)
            .setTitle(R.string.notify_data_dialog_title)
            .setMessage(
                "???????????????${characteristic.uuid}\n???????????????$hexValue\n????????????$stringValue"
            )
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * ??????????????????????????????
     */
    private fun showWriteCharacteristicDataDialog(serviceUuid: String, characteristicUuid: String) {

        WriteDataDialog(this, object : WriteDataDialog.OnConfirmButtonClickedListener {
            /**
             * ???????????????
             */
            override fun stringData(string: String) {
                val byteArray = string.getByteArray(20)
                if (byteArray == null) {
                    warnOut("??????????????????$string ????????????")
                    return
                }
                val succeed = bleConnectorInstance.writeCharacteristicData(
                    serviceUuid,
                    characteristicUuid,
                    byteArray
                )
                if (!succeed) {
                    toastL(R.string.characteristic_write_failed)
                }
            }

            /**
             * ??????????????????
             */
            override fun hexData(byteArray: ByteArray) {
                warnOut("???????????????????????????${byteArray.toHexStringWithSpace()}")
                val succeed = bleConnectorInstance.writeCharacteristicData(
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
     * ????????????????????????????????????
     */
    private fun showWriteDescriptorDataDialog(descriptorUuidItem: DescriptorUuidItem) {
        val serviceUuid = serviceUuid ?: return
        val characteristicUuid = characteristicUuid ?: return
        WriteDataDialog(this, object : WriteDataDialog.OnConfirmButtonClickedListener {
            /**
             * ???????????????
             */
            override fun stringData(string: String) {
                val byteArray = string.getByteArray(20)
                if (byteArray == null) {
                    warnOut("??????????????????$string ????????????")
                    return
                }
                val succeed = bleConnectorInstance.writeDescriptorData(
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
             * ??????????????????
             */
            override fun hexData(byteArray: ByteArray) {
                warnOut("???????????????????????????${byteArray.toHexStringWithSpace()}")
                val succeed = bleConnectorInstance.writeDescriptorData(
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
     * ????????????????????????????????????
     */
    private fun showSetAutoReconnectDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.auto_reconnect_dialog_title)
            .setItems(R.array.booleans) { _, which ->
                when (which) {
                    //???
                    0 -> {
                        singleConnectActivityViewModel.autoReconnect.value = true
                    }
                    //???
                    1 -> {
                        singleConnectActivityViewModel.autoReconnect.value = false
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * ????????????GATT?????????????????????
     */
    private fun showSetBleConnectTransportDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toastL(R.string.ble_connect_transport_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.ble_reconnect_transport_dialog_title)
            .setItems(R.array.ble_reconnect_transport) { _, which ->
                when (which) {
                    //transport_auto
                    0 -> {
                        singleConnectActivityViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_AUTO
                        singleConnectActivityViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_auto)
                    }
                    //transport_br_edr
                    1 -> {
                        singleConnectActivityViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_BR_EDR
                        singleConnectActivityViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_br_edr)
                    }
                    //transport_le
                    2 -> {
                        singleConnectActivityViewModel.bleConnectTransport.value =
                            BleConnectTransport.TRANSPORT_LE
                        singleConnectActivityViewModel.bleConnectTransportName.value =
                            getString(R.string.transport_le)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * ????????????BLE?????????????????????
     */
    private fun showSetBleConnectPhyMaskDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.ble_connect_phy_mask_not_support_with_low_system_version)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.ble_connect_phy_mask_dialog_title)
            .setItems(R.array.ble_connect_phy_mask) { _, which ->
                when (which) {
                    //phy_le_1m_mask
                    0 -> {
                        singleConnectActivityViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_1M_MASK
                        singleConnectActivityViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_1m_mask)
                    }
                    //phy_le_2m_mask
                    1 -> {
                        singleConnectActivityViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_2M_MASK
                        singleConnectActivityViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_2m_mask)
                    }
                    //phy_le_coded_mask
                    2 -> {
                        singleConnectActivityViewModel.bleConnectPhyMask.value =
                            BleConnectPhyMask.PHY_LE_CODED_MASK
                        singleConnectActivityViewModel.bleConnectPhyMaskName.value =
                            getString(R.string.connect_phy_le_coded_mask)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * ??????????????????????????????
     */
    private fun showAdditionalOptionsDialog() {
        if (!connected) {
            toastL(R.string.additional_options_need_connect_after)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.additional_options_dialog_title)
            .setItems(R.array.connect_additional_options) { _, which ->
                when (which) {
                    //??????????????????????????????
                    0 -> {
                        beginReliableWrite()
                    }
                    //??????????????????????????????
                    1 -> {
                        abortReliableWrite()
                    }
                    //??????RSSI
                    2 -> {
                        readRssi()
                    }
                    //??????MTU(?????????????????????)
                    3 -> {
                        showSetMtuDialog()
                    }
                    //??????????????????????????????
                    4 -> {
                        readPhy()
                    }
                    //?????????????????????
                    5 -> {
                        showChangePhyDialog()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * ??????????????????????????????
     */
    private fun readPhy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.read_phy_not_support_with_low_system_version)
            return
        }
        val succeed = bleConnectorInstance.readPhy()
        if (!succeed) {
            toastL(R.string.read_phy_failed)
        }
    }

    /**
     * ????????????MTU????????????
     */
    private fun showSetMtuDialog() {
        val view = View.inflate(this, R.layout.view_set_mtu, null)
        val editText = view.findViewById<EditText>(R.id.mtu_et)
        AlertDialog.Builder(this)
            .setTitle(R.string.set_mtu)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isEmpty()) {
                    return@setPositiveButton
                }
                val succeed = bleConnectorInstance.requestMtu(text.toInt())
                if (!succeed) {
                    toastL(R.string.request_mtu_failed)
                }
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    /**
     * ????????????RSSI
     */
    private fun readRssi() {
        val succeed = bleConnectorInstance.readRemoteRssi()
        if (!succeed) {
            toastL(R.string.read_rssi_failed)
        }
    }

    /**
     * ??????????????????????????????
     */
    private fun beginReliableWrite() {
        if (reliableWriteBegin) {
            toastL(R.string.already_in_reliable_write_mode)
            return
        }
        reliableWriteBegin = bleConnectorInstance.beginReliableWrite()
        if (!reliableWriteBegin) {
            toastL(R.string.reliable_write_mode_begin_failed)
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private fun showReliableWriteDataResultDialog(uuidString: String, value: ByteArray) {
        val stringValue = String(value)
        val hexValue = value.toHexStringWithSpace() ?: ""
        AlertDialog.Builder(this)
            .setTitle(R.string.reliable_write_data_result_dialog_title)
            .setMessage("???????????????$uuidString\n???????????????$hexValue\n????????????$stringValue")
            .setPositiveButton(R.string.commit) { _, _ ->
                val succeed = bleConnectorInstance.executeReliableWrite()
                if (!succeed) {
                    toastL(R.string.reliable_data_write_failed)
                    bleConnectorInstance.abortReliableWrite()
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
     * ????????????????????????
     */
    private fun abortReliableWrite() {
        if (!reliableWriteBegin) {
            toastL(R.string.not_in_reliable_write_mode)
            return
        }
        val succeed = bleConnectorInstance.abortReliableWrite()
        if (!succeed) {
            toastL(R.string.reliable_data_cancel_write_failed)
            return
        }
        reliableWriteBegin = false
        toastL(R.string.canceled)
    }

    /**
     * ????????????????????????????????????????????????
     */
    private fun showPhyReadResultDialog(txPhy: BlePhy?, rxPhy: BlePhy?) {
        AlertDialog.Builder(this)
            .setTitle(R.string.read_phy_result_dialog_title)
            .setMessage(getString(R.string.phy_read_result, txPhy?.name, rxPhy?.name))
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * ???????????????????????????????????????
     */
    private fun showPhyUpdateDialog(txPhy: BlePhy?, rxPhy: BlePhy?) {
        AlertDialog.Builder(this)
            .setTitle(R.string.phy_update_dialog_title)
            .setMessage(getString(R.string.phy_read_result, txPhy?.name, rxPhy?.name))
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    /**
     * ???????????????????????????????????????
     */
    private fun showChangePhyDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toastL(R.string.change_phy_not_support_with_low_system_version)
            return
        }
        ChangeConnectedPhyDialog(
            this,
            object : ChangeConnectedPhyDialog.OnConfirmButtonClickListener {
                /**
                 * ??????????????????????????????
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
                        bleConnectorInstance.setPreferredPhy(txPhy, rxPhy, blePhyOptions)
                    if (!succeed) {
                        toastL(R.string.change_phy_failed)
                    }
                }
            })
            .show()
    }
}