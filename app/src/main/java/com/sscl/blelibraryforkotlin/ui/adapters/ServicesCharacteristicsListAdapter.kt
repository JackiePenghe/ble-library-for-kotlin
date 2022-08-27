package com.sscl.blelibraryforkotlin.ui.adapters

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.sscl.blelibraryforkotlin.ui.adapters.provider.CharacteristicUuidProvider
import com.sscl.blelibraryforkotlin.ui.adapters.provider.DescriptorUuidProvider
import com.sscl.blelibraryforkotlin.ui.adapters.provider.ServiceUuidProvider
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.CharacteristicUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.DescriptorUuidItem
import com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity.ServiceUuidItem

/**
 * @author jackie
 * @date 2018/1/22 0022
 */
class ServicesCharacteristicsListAdapter : BaseNodeAdapter() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        const val TYPE_SERVICE_UUID = 0
        const val TYPE_CHARACTERISTIC_UUID = 1
        const val TYPE_DESCRIPTOR_UUID = 2
        const val TYPE_UNKNOWN = -1
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 构造方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    init {
        addNodeProvider(ServiceUuidProvider())
        addNodeProvider(CharacteristicUuidProvider())
        addNodeProvider(DescriptorUuidProvider())
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is ServiceUuidItem -> {
                TYPE_SERVICE_UUID
            }
            is CharacteristicUuidItem -> {
                TYPE_CHARACTERISTIC_UUID
            }
            is DescriptorUuidItem -> {
                TYPE_DESCRIPTOR_UUID
            }
            else -> {
                TYPE_UNKNOWN
            }
        }
    }
}