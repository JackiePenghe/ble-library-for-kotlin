package com.sscl.bluetoothlowenergylibrary.enums.scanner

import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * BLE回调模式
 * @author jackie
 */
enum class BleCallbackType(val callbackType: Int) {
    /**
     * 全部匹配
     * 在扫描到每一个满足匹配条件的设备广播时触发回调
     * 如果没有激活的过滤条件，所有的广播包都会反馈到回调中
     */
    CALLBACK_TYPE_ALL_MATCHES(ScanSettings.CALLBACK_TYPE_ALL_MATCHES),

    /**
     * 首次匹配
     * 在第一次扫描到一个满足匹配条件的设备广播时，将此设备触发到回调，同一个设备仅会触发一次回调
     */
    @RequiresApi(Build.VERSION_CODES.M)
    CALLBACK_TYPE_FIRST_MATCH(ScanSettings.CALLBACK_TYPE_FIRST_MATCH),

    /**
     * 丢失模式
     * 一直扫描设备，当一个设备的广播包不再被搜索到时，触发回调
     */
    @RequiresApi(Build.VERSION_CODES.M)
    CALLBACK_TYPE_MATCH_LOST(ScanSettings.CALLBACK_TYPE_MATCH_LOST);

}