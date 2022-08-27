package com.sscl.blelibraryforkotlin.ui.adapters.provider

import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.DescriptorUuidItem

class DescriptorUuidProvider : BaseNodeProvider() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override val itemViewType: Int
        get() = ServicesCharacteristicsListAdapter.TYPE_DESCRIPTOR_UUID
    override val layoutId: Int
        get() = R.layout.item_expandable_descriptor_uuid

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val descriptorUuidItem = item as DescriptorUuidItem
        helper.setText(android.R.id.text1, descriptorUuidItem.uuid)
            .setText(
                R.id.properties,
                getProperties(
                    descriptorUuidItem.canRead,
                    descriptorUuidItem.canWrite
                )
            )
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 获取特征属性
     *
     * @param canRead   是否可读
     * @param canWrite  是否可写
     * @return 特征属性
     */
    private fun getProperties(canRead: Boolean, canWrite: Boolean): String {
        val mContext = context
        if (!canRead && !canWrite) {
            return mContext.getString(R.string.null_)
        }
        val stringBuilder = StringBuilder()
        if (canRead) {
            stringBuilder.append(mContext.getString(R.string.can_read))
        }
        if (canWrite) {
            stringBuilder.append(mContext.getString(R.string.can_write))
        }
        return stringBuilder.toString()
    }
}