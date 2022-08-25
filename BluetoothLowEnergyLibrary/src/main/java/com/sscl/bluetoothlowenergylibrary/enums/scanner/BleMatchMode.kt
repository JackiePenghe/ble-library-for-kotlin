package com.sscl.bluetoothlowenergylibrary.enums.scanner

import androidx.annotation.RequiresApi
import android.os.Build
import android.bluetooth.le.ScanSettings

/**
 * BLE匹配模式
 *
 * @author jackie
 */
@RequiresApi(Build.VERSION_CODES.M)
enum class BleMatchMode(val matchMode: Int) {
    /**
     * 粘性模式，需要更高的信号强度与阈值
     */
    MATCH_MODE_STICKY(ScanSettings.MATCH_MODE_STICKY),

    /**
     * 进击模式，即使信号强度较弱且在一段时间内看到/匹配的次数很少，硬件也会更快地确定匹配。
     */
    MATCH_MODE_AGGRESSIVE(ScanSettings.MATCH_MODE_AGGRESSIVE);

}