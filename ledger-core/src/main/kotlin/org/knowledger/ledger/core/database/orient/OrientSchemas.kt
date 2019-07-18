package org.knowledger.ledger.core.database.orient

import com.orientechnologies.orient.core.metadata.schema.OSchema
import org.knowledger.ledger.core.database.ManagedSchema
import org.knowledger.ledger.core.database.ManagedSchemas

class OrientSchemas(internal val schemas: OSchema) : ManagedSchemas {
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