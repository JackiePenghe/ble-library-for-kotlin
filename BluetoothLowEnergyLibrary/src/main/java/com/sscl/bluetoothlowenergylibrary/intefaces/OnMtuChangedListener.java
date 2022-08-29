package com.sscl.bluetoothlowenergylibrary.intefaces;

/**
 * MTU变化的回调
 *
 * @author pengh
 */

public interface OnMtuChangedListener {

    /**
     * MTU变化回调
     *
     * @param mtu 当前有效MTU值（实际可用于传输的大小需要减3，蓝牙本身会占用3字节长度）
     */
    void onMtuChanged(int mtu);
}
