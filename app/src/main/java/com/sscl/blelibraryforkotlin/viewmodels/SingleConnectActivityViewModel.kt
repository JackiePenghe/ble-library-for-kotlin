package com.sscl.blelibraryforkotlin.viewmodels

import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectPhyMask
import com.sscl.bluetoothlowenergylibrary.enums.connector.BleConnectTransport

class SingleConnectActivityViewModel : ViewModel() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * object声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    object SingleConnectActivityViewModelFactory : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SingleConnectActivityViewModel() as T
        }
    }

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
     * 按钮的文字
     */
    val buttonText = MutableLiveData<String>()

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