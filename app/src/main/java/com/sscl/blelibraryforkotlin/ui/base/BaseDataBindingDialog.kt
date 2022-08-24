package com.sscl.blelibraryforkotlin.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * 基于DataBiding的Dialog基类
 */
abstract class BaseDataBindingDialog<T : ViewDataBinding>(context: Context) :
    Dialog(context, com.sscl.baselibrary.R.style.FullScreenDialog) {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    protected lateinit var binding: T

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate(savedInstanceState: Bundle?) {
        doBeforeSuperCreate()
        super.onCreate(savedInstanceState)
        doBeforeSetLayout()
        binding = DataBindingUtil.inflate(layoutInflater, setLayout(), null, false)
        setContentView(binding.root)
        initDataBindingParam()
        initViewData()
        initViewEvents()
        doAfterAll()
        setOnDismissListener {
            dialogDismiss()
        }
        setCancelable(needCancelable())
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 抽象方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在super.onCreate()方法执行前需要执行的操作
     */
    abstract fun doBeforeSuperCreate()

    /**
     * 在super.onCreate()方法执行之后
     * 在setContentView()方法执行之前
     * 期间执行的操作
     */
    abstract fun doBeforeSetLayout()

    /**
     * 设置布局
     */
    abstract fun setLayout(): Int

    /**
     * 初始化dataBinding的数据
     */
    abstract fun initDataBindingParam()

    /**
     * 初始化视图数据
     */
    abstract fun initViewData()

    /**
     * 初始化视图事件
     */
    abstract fun initViewEvents()

    /**
     * 在最后执行的操作
     */
    abstract fun doAfterAll()

    /**
     * 对话框消失时会触发
     */
    abstract fun dialogDismiss()

    /**
     * 是否cancelable
     */
    abstract fun needCancelable(): Boolean
}
