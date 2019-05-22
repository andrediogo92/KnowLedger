package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.metadata.schema.OType
import pt.um.masb.common.database.StorageType

fun StorageType.toOType(): OType =
    when (this) {
        StorageType.BOOLEAN -> OType.BOOLEAN
        StorageType.BYTE -> OType.BYTE
        StorageType.INTEGER -> OType.INTEGER
        StorageType.LONG -> OType.LONG
        StorageType.FLOAT -> OType.FLOAT
        StorageType.DOUBLE -> OType.DOUBLE
        StorageType.DECIMAL -> OType.DECIMAL
        StorageType.TIME -> OType.DATETIME
        StorageType.STRING -> OType.STRING
        StorageType.BYTES -> OType.BINARY
        StorageType.LINK -> OType.LINK
        StorageType.LIST -> OType.LINKLIST
        StorageType.SET -> OType.LINKSET
        StorageType.MAP -> OType.LINKMAP
        StorageType.LISTEMBEDDED -> OType.EMBEDDEDLIST
    }