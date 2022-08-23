package com.sscl.bluetoothlowenergylibrary.exceptions

/**
 * 设备不支持BLE异常。此异常会在创建或获取[com.sscl.bluetoothlowenergylibrary.BleScanner]对象时抛出
 */
class BluetoothLENotSupportException : RuntimeException("your device not support ble feature")