package com.sscl.blelibraryforkotlin.ui.fragments

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModel
import com.sscl.baselibrary.fragment.BaseDataBindingFragment
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.FragmentMultipleDeviceConnectBinding
import com.sscl.blelibraryforkotlin.viewmodels.fragments.MultipleDeviceConnectFragmentViewModel
import com.sscl.bluetoothlowenergylibrary.connetor.multi.BleMultipleConnector

/**
 * 多设备连接界面，某个设备的选项的单独界面
 */
class MultipleDeviceConnectFragment(
    private val scanResult: ScanResult,
    private val bleMultipleConnector: BleMultipleConnector
) :
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
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private val multipleDeviceConnectFragmentViewModel: MultipleDeviceConnectFragmentViewModel =
        MultipleDeviceConnectFragmentViewModel.MultipleDeviceConnectFragmentViewModelFactory.create(
            MultipleDeviceConnectFragmentViewModel::class.java
        )

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前进行的操作
     */
    override fun doBeforeSetLayout() {
        multipleDeviceConnectFragmentViewModel.scanResult.value = scanResult
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

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //通知Activity,在此Fragment中有菜单选项
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
       inflater.inflate(R.menu.fragment_multile_connect,menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        //TODO
    }
}