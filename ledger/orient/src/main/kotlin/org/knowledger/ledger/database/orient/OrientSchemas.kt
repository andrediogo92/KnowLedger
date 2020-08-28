package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.metadata.schema.OSchema
import org.knowledger.ledger.database.ManagedSchema
import org.knowledger.ledger.database.ManagedSchemas

inline class OrientSchemas(private val schemas: OSchema) : ManagedSchemas {
    override fun hasSchema(clazz: String): Boolean =
        schemas.existsClass(clazz)

    override fun createSchema(clazz: String): ManagedSchema? =
        schemas.createClass(clazz)?.let(::DocumentSchema)

    override fun getSchema(clazz: String): ManagedSchema? =
        schemas.getClass(clazz)?.let(::DocumentSchema)
}