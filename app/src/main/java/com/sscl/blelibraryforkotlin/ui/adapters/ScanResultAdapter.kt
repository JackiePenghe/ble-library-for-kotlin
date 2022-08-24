package com.sscl.blelibraryforkotlin.ui.adapters

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.AdapterScanResultItemBinding

/**
 * 扫描结果列表适配器
 */
class ScanResultAdapter :
    BaseQuickAdapter<ScanResult, ScanResultAdapter.ViewHolder>(R.layout.adapter_scan_result_item) {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    class ViewHolder(view: View) : BaseDataBindingHolder<AdapterScanResultItemBinding>(view)

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun convert(holder: ViewHolder, item: ScanResult) {
        holder.dataBinding?.item = item
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

}