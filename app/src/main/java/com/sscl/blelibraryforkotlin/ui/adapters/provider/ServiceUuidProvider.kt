package com.sscl.blelibraryforkotlin.ui.adapters.provider

import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.ui.adapters.ServicesCharacteristicsListAdapter
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.ServiceUuidItem

class ServiceUuidProvider: BaseNodeProvider() {
    override val itemViewType: Int
        get() = ServicesCharacteristicsListAdapter.TYPE_SERVICE_UUID
    override val layoutId: Int
        get() = R.layout.item_expandable_service_uuid

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val serviceUuidItem = item as ServiceUuidItem
        helper.setText(android.R.id.text1, serviceUuidItem.name)
            .setText(android.R.id.text2, serviceUuidItem.uuid)
            .setImageResource(
                R.id.expanded,
                if (serviceUuidItem.isExpanded) R.drawable.arrow_b else R.drawable.arrow_r
            )
    }
}