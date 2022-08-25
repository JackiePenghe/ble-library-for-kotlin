package com.sscl.blelibraryforkotlin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.sscl.baselibrary.utils.DebugUtil
import com.sscl.baselibrary.utils.ToastUtil
import com.sscl.blelibraryforkotlin.R

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

fun Any.warnOut(msg: String) {
    DebugUtil.warnOut(this.javaClass.simpleName, msg)
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