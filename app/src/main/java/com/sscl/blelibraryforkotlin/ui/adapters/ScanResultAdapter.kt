package com.sscl.blelibraryforkotlin.ui.adapters

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

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param holder A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: ViewHolder, item: ScanResult) {
        holder.dataBinding?.item = item
    }

}