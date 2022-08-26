package com.sscl.blelibraryforkotlin.ui.dialogs

import android.content.Context
import android.view.View
import android.widget.AdapterView
import com.sscl.baselibrary.textwatcher.HexTextAutoAddEmptyCharInputWatcher
import com.sscl.baselibrary.utils.fromHexStringWithSpaceToByteArray
import com.sscl.baselibrary.utils.toHexStringWithSpace
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.DialogWriteDateBinding
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingDialog

/**
 * 写入数据的对话框
 */
class WriteDataDialog(
    context: Context,
    private val onConfirmButtonClickedListener: OnConfirmButtonClickedListener? = null
) : BaseDataBindingDialog<DialogWriteDateBinding>(context) {

    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.dialog_write_date
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 接口声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 确认按钮点击事件
     */
    interface OnConfirmButtonClickedListener {

        /**
         * 字符串数据
         */
        fun stringData(string: String)

        /**
         * 十六进制数据
         */
        fun hexData(byteArray: ByteArray)

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 可空属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 输入十六进制的文本监听
     */
    private lateinit var hexStringWatcher: HexTextAutoAddEmptyCharInputWatcher

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 选中状态变化回调
     */
    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            //输入类型-字符串
            if (position == 0) {
                //移除十六进制文本监听回调
                binding.dataContentEt.removeTextChangedListener(hexStringWatcher)
                val text = binding.dataContentEt.text.toString()
                if (text.isNotEmpty()) {
                    val byteArray = text.fromHexStringWithSpaceToByteArray()
                    binding.dataContentEt.setText(String(byteArray ?: byteArrayOf()))
                }
            }
            //输入类型-十六进制
            else if (position == 1) {
                binding.dataContentEt.addTextChangedListener(hexStringWatcher)
                val text = binding.dataContentEt.text.toString()
                if (text.isNotEmpty()) {
                    binding.dataContentEt.setText(text.toByteArray().toHexStringWithSpace())
                }
            }
            binding.dataContentEt.setSelection(binding.dataContentEt.text.toString().length)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    /**
     * 点击事件
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.cancelBtn.id -> {
                dismiss()
            }
            binding.confirmBtn.id -> {
                confirm()
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
        hexStringWatcher = HexTextAutoAddEmptyCharInputWatcher(binding.dataContentEt, 20)
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
        binding.dataTypeRsp.onItemSelectedListener = onItemSelectedListener
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
        return false
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 确认按钮点击
     */
    private fun confirm() {
        val position = binding.dataTypeRsp.selectedItemPosition
        val text = binding.dataContentEt.text.toString()
        if (position == 0) {
            onConfirmButtonClickedListener?.stringData(text)
        } else if (position == 1) {
            val byteArray = text.fromHexStringWithSpaceToByteArray()
            onConfirmButtonClickedListener?.hexData(byteArray ?: byteArrayOf())
        }
    }
}