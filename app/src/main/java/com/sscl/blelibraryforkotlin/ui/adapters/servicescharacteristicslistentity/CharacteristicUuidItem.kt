package com.sscl.blelibraryforkotlin.ui.adapters.servicescharacteristicslistentity

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class CharacteristicUuidItem(
    val name: String,
    val uuid: String,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canNotify: Boolean,
    private val descriptorUuidItems:MutableList<BaseNode>
) : BaseExpandNode() {


    init {
        isExpanded = false
    }
    /**
     * 重写此方法，获取子节点。如果没有子节点，返回 null 或者 空数组
     *
     * 如果返回 null，则无法对子节点的数据进行新增和删除等操作
     */
    override val childNode: MutableList<BaseNode>
        get() = descriptorUuidItems
}