package com.sscl.blelibraryforkotlin.ui.dialogs

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.DialogChangeConnectPhyBinding
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingDialog
import com.sscl.bluetoothlowenergylibrary.enums.BlePhy
import com.sscl.bluetoothlowenergylibrary.enums.BlePhyOptions

/**
 * 更改连接物理层对话框
 */
@RequiresApi(Build.VERSION_CODES.O)
class ChangeConnectedPhyDialog(
    context: Context,
    private val onConfirmButtonClickListener: OnConfirmButtonClickListener? = null
) :
    BaseDataBindingDialog<DialogChangeConnectPhyBinding>(context) {

    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.dialog_change_connect_phy
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 接口声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 确认按钮点击事件
     */
    interface OnConfirmButtonClickListener {
        /**
         * 确认按钮被点击的回调
         */
        fun onConfirmButtonClick(txPhy: BlePhy, rxPhy: BlePhy, blePhyOptions: BlePhyOptions)

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 点击事件的监听
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.cancelBtn.id -> {
                dismiss()
            }
            binding.confirmBtn.id -> {
                confirmButtonClick()
                dismiss()
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在super.onCreate()方法执行前需要执行的操作
     */
    override fun doBeforeSuperCreate() {
    }

    /**
     * 在super.onCreate()方法执行之后
     * 在setContentView()方法执行之前
     * 期间执行的操作
     */
    override fun doBeforeSetLayout() {
    }

    /**
     * 初始化dataBinding的数据
     */
    override fun initDataBindingParam() {
    }

    /**
     * 初始化视图数据
     */
    override fun initViewData() {
    }

    /**
     * 初始化视图事件
     */
    override fun initViewEvents() {
        binding.cancelBtn.setOnClickListener(onClickListener)
        binding.confirmBtn.setOnClickListener(onClickListener)
    }

    /**
     * 在最后执行的操作
     */
    override fun doAfterAll() {
    }

    /**
     * 对话框消失时会触发
     */
    override fun dialogDismiss() {
    }

    /**
     * 是否cancelable
     */
    override fun needCancelable(): Boolean {
        return true
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 确认按钮点击事件
     */
    private fun confirmButtonClick() {
        val txPhyPosition = binding.txPhyRsp.selectedItemPosition
        val rxPhyPosition = binding.rxPhyRsp.selectedItemPosition
        val phyOptionsPosition = binding.phyOptionsRsp.selectedItemPosition
        onConfirmButtonClickListener?.onConfirmButtonClick(
            BlePhy.values()[txPhyPosition],
            BlePhy.values()[rxPhyPosition],
            BlePhyOptions.values()[phyOptionsPosition]
        )
    }
}