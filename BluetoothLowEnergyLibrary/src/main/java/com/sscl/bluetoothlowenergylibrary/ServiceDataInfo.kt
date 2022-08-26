package com.sscl.bluetoothlowenergylibrary

import android.os.ParcelUuid
import java.io.Serializable

/**
 * 广播包中单个服务信息的Bean类
 */
data class ServiceDataInfo internal constructor(
    val parcelUuid: ParcelUuid,
    val value: ByteArray
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceDataInfo

        if (parcelUuid != other.parcelUuid) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parcelUuid.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

}
