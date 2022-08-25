package com.sscl.blelibraryforkotlin.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.sscl.baselibrary.utils.DefaultItemDecoration
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.DialogSetStartsFullFilterBinding
import com.sscl.blelibraryforkotlin.ui.adapters.StringListRecyclerViewAdapter
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingDialog
import com.sscl.blelibraryforkotlin.utils.toastL
import com.sscl.bluetoothlowenergylibrary.scanner.BleScanner

/**
 * 设置名称过滤-匹配名称开头 的对话框
 */
class SetFullNameFilterDialog(context: Context, private val bleScanner: BleScanner) :
    BaseDataBindingDialog<DialogSetStartsFullFilterBinding>(context) {
    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.dialog_set_starts_full_filter
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 过滤设备名列表适配器
     */
    private val stringListRecyclerViewAdapter = StringListRecyclerViewAdapter()

    /**
     * 点击事件的处理
     */
    private val onClickListener = View.OnClickListener {
        when (it.id) {
            binding.addNewBtn.id -> {
                showAddFilterFullNameDialog()
                dismiss()
            }
            binding.clearBtn.id -> {
                bleScanner.clearFilterFullName()
                context.toastL(R.string.cleared)
                dismiss()
            }
            binding.cancelBtn.id -> {
                dismiss()
            }
        }
    }

    /**
     * 适配器选项点击事件
     */
    private val onItemClickListener = OnItemClickListener { _, _, position ->
        val value = stringListRecyclerViewAdapter.data[position]
        showConfirmDeleteDialog(value)
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
        initFilterRvData()
    }

    /**
     * 初始化视图事件
     */
    override fun initViewEvents() {
        binding.addNewBtn.setOnClickListener(onClickListener)
        binding.clearBtn.setOnClickListener(onClickListener)
        binding.cancelBtn.setOnClickListener(onClickListener)
        stringListRecyclerViewAdapter.setOnItemClickListener(onItemClickListener)
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
     * 初始化过滤名称-匹配开头列表数据
     */
    private fun initFilterRvData() {
        binding.addedFilterListRv.layoutManager = LinearLayoutManager(context)
        binding.addedFilterListRv.addItemDecoration(DefaultItemDecoration.newLine(Color.GRAY))
        binding.addedFilterListRv.adapter = stringListRecyclerViewAdapter
        stringListRecyclerViewAdapter.addData(bleScanner.getFilterFullNames())
    }

    /**
     * 显示添加设备名过滤-匹配开头的对话框
     */
    private fun showAddFilterFullNameDialog() {
        val view = View.inflate(context, R.layout.view_add_filter_full_name, null)
        AlertDialog.Builder(context)
            .setTitle(R.string.add_new_filter)
            .setView(view)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val text =
                    view.findViewById<EditText>(R.id.filter_full_name_et).text.toString()
                if (text.isEmpty()) {
                    context.toastL(R.string.data_empty)
                    return@setPositiveButton
                }
                bleScanner.addFilterFullName(text)
                context.toastL(R.string.added)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * 显示删除确认对话框
     */
    private fun showConfirmDeleteDialog(value: String) {
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_filter_dialog_title)
            .setMessage(context.getString(R.string.delete_filter_dialog_msg, value))
            .setPositiveButton(R.string.confirm) { _, _ ->
                bleScanner.removeFilterFullName(value)
                stringListRecyclerViewAdapter.data.remove(value)
                context.toastL(R.string.deleted)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}