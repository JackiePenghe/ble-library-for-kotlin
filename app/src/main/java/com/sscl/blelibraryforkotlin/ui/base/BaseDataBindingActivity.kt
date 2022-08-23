package com.sscl.blelibraryforkotlin.ui.base

import androidx.databinding.ViewDataBinding
import com.sscl.baselibrary.activity.BaseAppCompatActivity
import com.sscl.baselibrary.activity.BaseDataBindingAppCompatActivity

abstract class BaseDataBindingActivity<T:ViewDataBinding> : BaseDataBindingAppCompatActivity<T>() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 标题栏的返回按钮被按下的时候回调此方法
     */
    override fun titleBackClicked(): Boolean {
        return false
    }
}