package com.sscl.blelibraryforkotlin.ui.activities

import android.view.View
import androidx.activity.viewModels
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityMainBinding
import com.sscl.blelibraryforkotlin.ui.activities.connect.multi.MultiDeviceScanActivity
import com.sscl.blelibraryforkotlin.ui.activities.connect.single.DeviceScanActivity
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.utils.startActivity
import com.sscl.blelibraryforkotlin.viewmodels.activities.MainActivityViewModel

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
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * viewModel
     */
    private val mainActivityViewModel by viewModels<MainActivityViewModel> {
        MainActivityViewModel.MainActivityViewModelFactory
    }

    /**
     * 点击事件的处理
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.scanAndConnectBtn.id -> {
                toDeviceScanActivity()
            }
            binding.multipleConnectBtn.id ->{
                toMultiDeviceScanActivity()
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {

    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
        hideTitleBackButton()
    }

    /**
     * 设置DataBinding
     * 可在此处设置binding的viewModel或观察者等操作
     */
    override fun setBinding() {
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
        binding.scanAndConnectBtn.setOnClickListener(onClickListener)
        binding.multipleConnectBtn.setOnClickListener(onClickListener)
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
     * 跳转到设备扫描界面
     */
    private fun toDeviceScanActivity() {
        startActivity(DeviceScanActivity::class.java)
    }

    /**
     * 跳转到多个设备连接时的扫描界面
     */
    private fun toMultiDeviceScanActivity() {
        startActivity(MultiDeviceScanActivity::class.java)
    }
}