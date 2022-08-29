package com.sscl.blelibraryforkotlin.ui.adapters

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.os.Build
import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.beans.BleScanResultWithBoolean
import com.sscl.blelibraryforkotlin.utils.toastL

/**
 * 扫描结果列表适配器(可多选,最多7个)
 */
class MultipleSelectScanResultRecyclerViewAdapter :
    BaseQuickAdapter<BleScanResultWithBoolean, BaseViewHolder>(
        R.layout.adapter_multiple_scan_result_item
    ) {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 选中的选项位置
     */
    private val checkedPosition = ArrayList<Int>()

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    init {
        setOnItemClickListener { _, _, position ->
            val scanResultWithBoolean = data[position]
            val checked = scanResultWithBoolean.checked
            if (checked) {
                scanResultWithBoolean.checked = !checked
                checkedPosition.remove(position)
                notifyItemChanged(position)
            } else {
                if (checkedPosition.size >= 7) {
                    context.toastL(R.string.max_select_size_7)
                    return@setOnItemClickListener
                }
                scanResultWithBoolean.checked = !checked
                checkedPosition.add(position)
                notifyItemChanged(position)
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun convert(holder: BaseViewHolder, item: BleScanResultWithBoolean) {
        holder.getView<CheckBox>(R.id.check_state_cb).isChecked = item.checked
        holder.setText(R.id.device_name_tv, item.scanResult.scanRecord?.deviceName)
            .setText(R.id.device_address_tv, item.scanResult.device.address)
            .setText(R.id.rssi_tv,item.scanResult.rssi.toString())
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            holder.setText(R.id.advertising_interval_tv,item.scanResult.periodicAdvertisingInterval.toString())
        }else{
            holder.setText(R.id.advertising_interval_tv,context.getString(R.string.NA))
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 公开方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 清除列表数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    /**
     * 获取选中的设备信息列表
     */
    fun getSelectedScanResultList(): ArrayList<ScanResult> {
        val result = ArrayList<ScanResult>()
        for (position in checkedPosition) {
            val bleScanResultWithBoolean = data[position]
            result.add(bleScanResultWithBoolean.scanResult)
        }
        return result
    }
}