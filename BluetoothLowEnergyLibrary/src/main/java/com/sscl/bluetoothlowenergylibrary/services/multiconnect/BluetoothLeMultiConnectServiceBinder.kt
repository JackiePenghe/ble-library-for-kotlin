package com.sscl.bluetoothlowenergylibrary.services.multiconnect

import android.os.Binder

class BluetoothLeMultiConnectServiceBinder(bluetoothLeMultiConnectService: BluetoothLeMultiConnectService) :
    Binder() {

    internal val bluetoothLeMultiConnectService: BluetoothLeMultiConnectService

    init {
        this.bluetoothLeMultiConnectService = bluetoothLeMultiConnectService
    }
}