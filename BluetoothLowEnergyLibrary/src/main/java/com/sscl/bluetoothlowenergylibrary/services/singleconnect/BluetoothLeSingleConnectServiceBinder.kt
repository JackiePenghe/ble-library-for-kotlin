package com.sscl.bluetoothlowenergylibrary.services.singleconnect

import android.os.Binder

class BluetoothLeSingleConnectServiceBinder(bluetoothLeSingleConnectService: BluetoothLeSingleConnectService) :
    Binder() {

    internal val bluetoothLeSingleConnectService: BluetoothLeSingleConnectService

    init {
        this.bluetoothLeSingleConnectService = bluetoothLeSingleConnectService
    }
}