package com.sscl.blelibraryforkotlin.ui.adapters

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.AdapterStringListRecylerViewBinding

class StringListRecyclerViewAdapter :
    BaseQuickAdapter<String, StringListRecyclerViewAdapter.ViewHolder>(R.layout.adapter_string_list_recyler_view) {

    class ViewHolder(view: View) : BaseDataBindingHolder<AdapterStringListRecylerViewBinding>(view)

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param holder A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: ViewHolder, item: String) {
        holder.dataBinding?.textTv?.text = item
    }

}