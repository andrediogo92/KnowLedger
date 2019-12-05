package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.metadata.schema.OSchema
import org.knowledger.ledger.database.ManagedSchema
import org.knowledger.ledger.database.ManagedSchemas

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
inline class OrientSchemas(private val schemas: OSchema) : ManagedSchemas {
    override fun hasSchema(clazz: String): Boolean =
        schemas.existsClass(clazz)

    override fun createSchema(clazz: String): ManagedSchema? {
        val schema = schemas.createClass(clazz)
        return if (schema == null) {
            DocumentSchema(schema)
        } else {
            null
        }
    }

    override fun getSchema(clazz: String): ManagedSchema? {
        val schema = schemas.getClass(clazz)
        return if (schema == null) {
            DocumentSchema(schema)
        } else {
            null
        }
    }
}