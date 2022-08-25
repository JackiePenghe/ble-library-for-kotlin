package com.sscl.bluetoothlowenergylibrary

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class BluetoothStateReceiver : BroadcastReceiver() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *
         * 属性声明
         *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        /**
         * 广播过滤器
         */
        internal val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *
         * 方法声明
         *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                BluetoothAdapter.STATE_OFF -> performBluetoothStateOffListener()
                BluetoothAdapter.STATE_ON -> performBluetoothStateOnListener()
                BluetoothAdapter.STATE_TURNING_OFF -> performBluetoothStateTurningOffListener()
                BluetoothAdapter.STATE_TURNING_ON -> performBluetoothStateTurningOnListener()
                else -> {}
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 触发蓝牙开关关闭的回调
     */
    private fun performBluetoothStateOffListener() {
        for (onBluetoothStateChangedListener in BleManager.onBluetoothStateChangedListeners) {
            onBluetoothStateChangedListener.onBluetoothDisable()
        }
    }

    /**
     * 触发蓝牙开关打开的回调
     */
    private fun performBluetoothStateOnListener() {
        for (onBluetoothStateChangedListener in BleManager.onBluetoothStateChangedListeners) {
            onBluetoothStateChangedListener.onBluetoothEnable()
        }
    }

    /**
     * 触发蓝牙开关正在关闭的回调
     */
    private fun performBluetoothStateTurningOffListener() {
        for (onBluetoothStateChangedListener in BleManager.onBluetoothStateChangedListeners) {
            onBluetoothStateChangedListener.onBluetoothDisabling()
        }
    }

    /**
     * 触发蓝牙开关正在打开的回调
     */
    private fun performBluetoothStateTurningOnListener() {
        for (onBluetoothStateChangedListener in BleManager.onBluetoothStateChangedListeners) {
            onBluetoothStateChangedListener.onBluetoothEnabling()
        }
    }
}