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

    }
}