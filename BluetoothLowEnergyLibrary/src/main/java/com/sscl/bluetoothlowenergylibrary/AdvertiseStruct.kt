package com.sscl.bluetoothlowenergylibrary

import java.io.Serializable

/**
 * 广播数据中一个单独的AD结构Bean类
 *
 * @author jackie
 */
data class AdvertiseStruct internal constructor(
    /**
     * Data length
     */
    val length: Int,
    /**
     * AD type
     */
    val type: Byte,
    /**
     * AD data
     */
    val data: ByteArray
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdvertiseStruct

        if (length != other.length) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + type
        result = 31 * result + data.contentHashCode()
        return result
    }
}