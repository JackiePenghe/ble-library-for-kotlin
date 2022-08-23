package com.sscl.bluetoothlowenergylibrary.enums

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
    STICKY(ScanSettings.MATCH_MODE_STICKY),

    /**
     * 进击模式，即使信号强度较弱且在一段时间内看到/匹配的次数很少，硬件也会更快地确定匹配。
     */
    AGGRESSIVE(ScanSettings.MATCH_MODE_AGGRESSIVE);

}