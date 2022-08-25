package com.sscl.bluetoothlowenergylibrary.services.singleconnect

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger

/**
 * 单个蓝牙连接服务的连接器
 */
class BluetoothSingleConnectServiceConnection : ServiceConnection {

    companion object {
        private val TAG: String = BluetoothLeSingleConnectService::class.java.simpleName
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        if (service is BluetoothLeSingleConnectServiceBinder) {
            Logger.log(TAG, "单个蓝牙的连接服务绑定成功")
            BleManager.singleConnectService = service.bluetoothLeSingleConnectService
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Logger.log(TAG, "单个蓝牙的连接服务已断开")
        BleManager.singleConnectService = null
    }
}