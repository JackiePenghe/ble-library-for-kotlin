package com.sscl.blelibraryforkotlin.ui.adapters.provider

import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.CharacteristicUuidItem

class CharacteristicUuidProvider : BaseNodeProvider() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override val itemViewType: Int
        get() = ServicesCharacteristicsListAdapter.TYPE_CHARACTERISTIC_UUID
    override val layoutId: Int
        get() = R.layout.item_expandable_characteristic_uuid

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val characteristicUuidItem = item as CharacteristicUuidItem
        helper.setText(android.R.id.text1, characteristicUuidItem.name)
            .setText(android.R.id.text2, characteristicUuidItem.uuid)
            .setText(
                R.id.properties,
                getProperties(
                    characteristicUuidItem.canRead,
                    characteristicUuidItem.canWrite,
                    characteristicUuidItem.canNotify
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
     * @param canNotify 是否可通知
     * @return 特征属性
     */
    private fun getProperties(canRead: Boolean, canWrite: Boolean, canNotify: Boolean): String {
        val mContext = context
        if (!canRead && !canWrite && !canNotify) {
            return mContext.getString(R.string.null_)
        }
        val stringBuilder = StringBuilder()
        if (canRead) {
            stringBuilder.append(mContext.getString(R.string.can_read))
        }
        if (canWrite) {
            stringBuilder.append(mContext.getString(R.string.can_write))
        }
        if (canNotify) {
            stringBuilder.append(mContext.getString(R.string.can_notify))
        }
        return stringBuilder.toString()
    }
}