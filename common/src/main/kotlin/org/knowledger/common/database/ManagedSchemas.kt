package org.knowledger.common.database

interface ManagedSchemas {
    fun hasSchema(clazz: String): Boolean
    fun createSchema(clazz: String): ManagedSchema?
    fun getSchema(clazz: String): ManagedSchema?
}
