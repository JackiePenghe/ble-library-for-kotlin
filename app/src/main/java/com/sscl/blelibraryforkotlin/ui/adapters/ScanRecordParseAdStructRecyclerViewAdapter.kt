package com.sscl.blelibraryforkotlin.ui.adapters

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.AdapterScanRecordParseAdStructBinding
import com.sscl.bluetoothlowenergylibrary.AdvertiseStruct

/**
 * 广播包解析-AD结构适配器列表
 */
class ScanRecordParseAdStructRecyclerViewAdapter :
    BaseQuickAdapter<AdvertiseStruct, ScanRecordParseAdStructRecyclerViewAdapter.ViewHolder>(R.layout.adapter_scan_record_parse_ad_struct) {

    class ViewHolder(view: View) :
        BaseDataBindingHolder<AdapterScanRecordParseAdStructBinding>(view)

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun convert(holder: ViewHolder, item: AdvertiseStruct) {
        holder.dataBinding?.advertiseStruct = item
    }
}