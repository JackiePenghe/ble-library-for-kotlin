package com.sscl.bluetoothlowenergylibrary.intefaces;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.sscl.bluetoothlowenergylibrary.enums.BlePhy;

import org.jetbrains.annotations.Nullable;

/**
 * 物理层变化的回调
 *
 * @author pengh
 */
@RequiresApi(Build.VERSION_CODES.O)
public interface OnPhyUpdateListener {

    /**
     * 物理层有变化
     *
     * @param txPhy 数据发送物理层
     * @param rxPhy 接收数据物理层
     */
    void onPhyUpdate(@Nullable BlePhy txPhy, @Nullable BlePhy rxPhy);
}
