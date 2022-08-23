package com.sscl.bluetoothlowenergylibrary.enums

import androidx.annotation.RequiresApi
import android.os.Build
import android.bluetooth.le.ScanSettings

/**
 * BLE num if matches
 *
 *
 * Determines how many advertisements to match per filter, as this is scarce hw resource
 *
 * @author jackie
 */
@RequiresApi(api = Build.VERSION_CODES.M)
enum class BleNumOfMatches(val numOfMatches: Int) {
    /**
     * 单独匹配
     * 第一个过滤器匹配一个广播包
     */
    MATCH_NUM_ONE_ADVERTISEMENT(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT),

    /**
     * 小量匹配
     * 每个过滤器匹配少量广告，这取决于硬件中资源的当前能力和可用性
     */
    MATCH_NUM_FEW_ADVERTISEMENT(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT),

    /**
     * 大量匹配
     * 根据硬件中资源的当前能力和可用性，尽可能多地匹配每个过滤器的广告
     */
    MATCH_NUM_MAX_ADVERTISEMENT(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);

}