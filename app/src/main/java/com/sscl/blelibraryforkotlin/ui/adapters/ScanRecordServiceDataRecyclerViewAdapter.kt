package com.sscl.blelibraryforkotlin.ui.adapters

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.AdapterScanRecordParseAdStructBinding
import com.sscl.blelibraryforkotlin.databinding.AdapterScanRecordServiceDataBinding
import com.sscl.bluetoothlowenergylibrary.AdvertiseStruct
import com.sscl.bluetoothlowenergylibrary.ServiceDataInfo

/**
 * 广播包解析-AD结构适配器列表
 */
class ScanRecordServiceDataRecyclerViewAdapter :
    BaseQuickAdapter<ServiceDataInfo, ScanRecordServiceDataRecyclerViewAdapter.ViewHolder>(R.layout.adapter_scan_record_service_data) {

    class ViewHolder(view: View) :
        BaseDataBindingHolder<AdapterScanRecordServiceDataBinding>(view)

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun convert(holder: ViewHolder, item: ServiceDataInfo) {
        holder.dataBinding?.serviceDataInfo = item
    }
}