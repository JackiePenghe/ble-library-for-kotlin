package com.sscl.blelibraryforkotlin.utils

import android.app.Activity
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.sscl.baselibrary.utils.DebugUtil
import com.sscl.baselibrary.utils.ToastUtil
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.beans.BleScanResultWithBoolean
import java.util.*

private var connectingDialog: AlertDialog? = null

fun Context.toastL(@StringRes msgRes: Int) {
    ToastUtil.toastLong(this, msgRes)
}

fun Context.toastL(msg: String) {
    ToastUtil.toastLong(this, msg)
}

fun <T : Activity> Activity.startActivity(clazz: Class<T>) {
    this.startActivity(Intent(this, clazz))
}

fun Any.warnOut(msg: String?) {
    if (msg == null) {
        DebugUtil.warnOut(this.javaClass.simpleName, "null")
    } else {
        DebugUtil.warnOut(
            this.javaClass.simpleName,
            msg
        )
    }
}

fun Any.errorOut(msg: String) {
    DebugUtil.errorOut(this.javaClass.simpleName, msg)
}

fun Context.showConnecting() {
    dismissConnecting(false)
    warnOut("showConnecting")
    connectingDialog = AlertDialog.Builder(this)
        .setMessage(R.string.connecting)
        .setCancelable(false)
        .show()
}

fun Context.dismissConnecting(needLog: Boolean = true) {
    if (needLog) {
        warnOut("dismissConnecting")
    }
    connectingDialog?.dismiss()
    connectingDialog = null
}

fun Byte?.toHexString(): String? {
    this ?: return null
    val hexString = Integer.toHexString(toInt() and 0xFF).uppercase(Locale.CHINA)
    if (hexString.length == 1) {
        return "0$hexString"
    }
    return hexString
}

fun Int?.toHexString(): String? {
    this ?: return null
    val hexString = Integer.toHexString(this).uppercase(Locale.CHINA)
    if (hexString.length == 1) {
        return "0$hexString"
    }
    return hexString
}

/**
 * 获取扫描结果列表中包含的某个扫描结果的索引 -1表示扫描结果列表中不存在该扫描结果
 */
fun MutableList<BleScanResultWithBoolean>.indexOfScanResults(bleScanResultWithBoolean: BleScanResultWithBoolean): Int {
    return this.indexOf(bleScanResultWithBoolean)
}

/**
 * 获取扫描结果列表中包含的某个扫描结果的索引 -1表示扫描结果列表中不存在该扫描结果
 */
fun MutableList<BleScanResultWithBoolean>.indexOfScanResults(scanResult: ScanResult): Int {
    for (i in indices) {
        if (this[i].scanResult.device == scanResult.device) {
            return i
        }
    }
    return -1
}