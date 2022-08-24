package com.sscl.blelibraryforkotlin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import com.sscl.baselibrary.utils.DebugUtil
import com.sscl.baselibrary.utils.ToastUtil
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity

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