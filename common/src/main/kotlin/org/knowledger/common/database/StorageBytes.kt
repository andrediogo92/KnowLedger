package org.knowledger.common.database

import java.io.ByteArrayOutputStream

interface StorageBytes : Discardable<StorageBytes> {
    fun toOutputStream(bos: ByteArrayOutputStream)

    val bytes: ByteArray
}