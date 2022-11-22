package com.sscl.blelibraryforkotlin.ui.activities.connect.multi

import android.bluetooth.le.ScanResult
import com.google.android.material.tabs.TabLayoutMediator
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityMultipleDeviceConnectBinding
import com.sscl.blelibraryforkotlin.ui.adapters.MultipleDeviceConnectFragmentPager2Adapter
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.ui.fragments.MultipleDeviceConnectFragment
import com.sscl.blelibraryforkotlin.utils.IntentConstants
import com.sscl.bluetoothlowenergylibrary.BleManager

/**
 * 多设备连接的界面
 */
class MultipleDeviceConnectActivity :
    BaseDataBindingActivity<ActivityMultipleDeviceConnectBinding>() {
    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_multiple_device_connect
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * fragment缓存
     */
    private val fragments = ArrayList<MultipleDeviceConnectFragment>()

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 扫描结果列表
     */
    private var scanResultList: ArrayList<ScanResult>? = null

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {
        getIntentData()
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
    }

    /**
     * 设置DataBinding
     * 可在此处设置binding的viewModel或观察者等操作
     */
    override fun setBinding() {
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {
        initTabs()
        initFragments()
        binding.viewPager2.adapter =
            MultipleDeviceConnectFragmentPager2Adapter(this, fragments)
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager2
        ) { tab, position ->
            tab.text = scanResultList?.get(position)?.device?.address
        }.attach()
        binding.viewPager2.offscreenPageLimit = 3
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
        if (scanResultList == null) {
            finish()
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getBleMultiConnector().closeAll()
        BleManager.getBleMultiConnector().releaseAll()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 获取Intent传递的数据
     */
    private fun getIntentData() {
        @Suppress("UNCHECKED_CAST")
        scanResultList =
            intent.getSerializableExtra(IntentConstants.SCAN_RESULT_LIST) as ArrayList<ScanResult>?
    }

    /**
     * 初始化tab选项
     */
    private fun initTabs() {
        val scanResultList = scanResultList ?: return
        for (i in scanResultList.indices) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }
    }

    /**
     * 初始化Fragment
     */
    private fun initFragments() {
        val scanResultList = scanResultList ?: return

        for (scanResult in scanResultList) {
            fragments.add(MultipleDeviceConnectFragment(scanResult))
        }
    }
}