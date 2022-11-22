package com.sscl.blelibraryforkotlin.viewmodels.fragments

import android.bluetooth.le.ScanResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.BleConnectTransport

class MultipleDeviceConnectFragmentViewModel : ViewModel() {


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 扫描的设备信息
     */
    val scanResult = MutableLiveData<ScanResult>()

    /**
     * 连接参数-是否自动重连
     */
    val autoReconnect = MutableLiveData<Boolean>()

    /**
     * 连接参数-指定GATT传输层
     */
    val bleConnectTransport = MutableLiveData<BleConnectTransport>()

    /**
     * 连接参数-GATT传输层名称
     */
    val bleConnectTransportName = MutableLiveData<String>()

    /**
     * 连接参数-GATT物理层
     */
    val bleConnectPhyMask = MutableLiveData<BleConnectPhyMask>()

    /**
     * 连接参数-GATT物理层名称
     */
    val bleConnectPhyMaskName = MutableLiveData<String>()
}