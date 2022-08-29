package com.sscl.bluetoothlowenergylibrary.intefaces;

/**
 * 获取设备RSSI相关回调
 *
 * @author pengh
 */
public interface OnReadRemoteRssiListener {
    /**
     * RSSI读取成功
     *
     * @param rssi 信号强度
     */
    void onReadRemoteRssi(int rssi);
}
