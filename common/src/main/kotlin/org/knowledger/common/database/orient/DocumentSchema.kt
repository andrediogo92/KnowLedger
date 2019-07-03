package org.knowledger.common.database.orient

import com.orientechnologies.orient.core.metadata.schema.OClass
import org.knowledger.common.database.ManagedSchema
import org.knowledger.common.database.SchemaProperty
import org.knowledger.common.database.StorageType

data class DocumentSchema internal constructor(
    internal val clazz: OClass
) : ManagedSchema {
    override fun addCluster(clusterName: String) =
        apply {
            clazz.addCluster(clusterName)
        }

    override fun createProperty(
        key: String, value: StorageType
    ): SchemaProperty =
        DocumentProperty(
            clazz.createProperty(key, value.toOType())
        )

    override fun dropProperty(key: String) =
        clazz.dropProperty(key)

    override fun declaredPropertyNames(): List<String> =
        clazz.declaredProperties().map { it.name }
}