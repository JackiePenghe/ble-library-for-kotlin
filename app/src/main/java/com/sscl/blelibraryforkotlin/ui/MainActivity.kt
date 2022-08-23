package com.sscl.blelibraryforkotlin.ui

import androidx.activity.viewModels
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityMainBinding
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.viewmodels.MainActivityViewModel
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.BleScanner
import com.sscl.bluetoothlowenergylibrary.enums.BleScanMode

class MainActivity : BaseDataBindingActivity<ActivityMainBinding>() {

    /**
     * 设置布局
     *
     * @return 布局id
     */
    override fun setLayout(): Int {
        return R.layout.activity_main
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * viewModel
     */
    private val mainActivityViewModel by viewModels<MainActivityViewModel> {
        MainActivityViewModel.MainActivityViewModelFactory
    }

    /* * * * * * * * * * * * * * * * * * * 延时初始化属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 蓝牙扫描单例
     */
    private lateinit var bleScanner: BleScanner

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */



    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {
        initBleScanner()
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
        binding.viewModel = mainActivityViewModel
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {

    }

    /**
     * 初始化其他数据
     */
    override fun initOtherData() {

    }

    /**
     * 初始化事件
     */
    override fun initEvents() {

    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 初始化蓝牙扫描器
     */
    private fun initBleScanner() {
        bleScanner = BleManager.newBleScanner()
    }
}