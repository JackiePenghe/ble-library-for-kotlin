package com.sscl.blelibraryforkotlin.ui.fragments

import com.sscl.baselibrary.fragment.BaseDataBindingFragment
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.FragmentMultipleDeviceConnectBinding

/**
 * 多设备连接界面，某个设备的选项的单独界面
 */
class MultipleDeviceConnectFragment :
    BaseDataBindingFragment<FragmentMultipleDeviceConnectBinding>() {
    /**
     * 设置fragment的布局
     *
     * @return 布局id
     */
    override fun setLayout(): Int {
        return R.layout.fragment_multiple_device_connect
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前进行的操作
     */
    override fun doBeforeSetLayout() {
    }

    /**
     * 初始化数据
     */
    override fun initData() {

    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {

    }

    /**
     * 初始化事件
     */
    override fun initEvents() {

    }

    /**
     * 在最后执行的操作
     */
    override fun doAfterAll() {

    }
}