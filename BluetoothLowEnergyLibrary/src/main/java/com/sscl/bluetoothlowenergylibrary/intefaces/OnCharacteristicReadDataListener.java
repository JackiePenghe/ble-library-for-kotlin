package com.sscl.bluetoothlowenergylibrary.intefaces;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;

/**
 * 特征数据读取回调
 *
 * @author pengh
 */
public interface OnCharacteristicReadDataListener {
    /**
     * 读取特征数据的回调
     *
     * @param characteristic BluetoothGattCharacteristic
     * @param value          数据
     */
    void onCharacteristicReadData(@NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value);
}
