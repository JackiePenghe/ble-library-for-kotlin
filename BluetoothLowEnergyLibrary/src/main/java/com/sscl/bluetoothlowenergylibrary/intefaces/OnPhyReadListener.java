package com.sscl.bluetoothlowenergylibrary.intefaces;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.sscl.bluetoothlowenergylibrary.enums.BlePhy;

/**
 * 物理层读取回调
 *
 * @author pengh
 */
@RequiresApi(Build.VERSION_CODES.O)
public interface OnPhyReadListener {
    /**
     * 物理层读取回调
     *
     * @param txPhy 数据发送物理层
     * @param rxPhy 数据接收物理层
     */
    void onPhyRead(@Nullable BlePhy txPhy, @Nullable BlePhy rxPhy);
}
