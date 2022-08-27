package com.sscl.bluetoothlowenergylibrary.intefaces;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import androidx.annotation.NonNull;

/**
 * 特征数据读取回调
 *
 * @author pengh
 */
public interface OnDescriptorReadDataListener {
    /**
     * 读取特征数据的回调
     *
     * @param descriptor BluetoothGattDescriptor
     * @param value      数据
     */
    void onDescriptorReadData(@NonNull BluetoothGattDescriptor descriptor, @NonNull byte[] value);
}
