package com.sscl.bluetoothlowenergylibrary.services.multiconnect

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger

/**
 * 单个蓝牙连接服务的连接器
 */
class BluetoothMultiConnectServiceConnection : ServiceConnection {

    companion object {
        private val TAG: String = BluetoothLeMultiConnectService::class.java.simpleName
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        if (service is BluetoothLeMultiConnectServiceBinder) {
            Logger.log(TAG, "多个蓝牙的连接服务绑定成功")
            BleManager.multiConnectService = service.bluetoothLeMultiConnectService
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Logger.log(TAG, "多个蓝牙的连接服务已断开")
        BleManager.multiConnectService = null
    }
}