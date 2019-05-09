package pt.um.masb.ledger.storage.schema

import com.orientechnologies.orient.core.metadata.schema.OType

data class PreConfiguredSchemaProvider(
    override val id: String,
    override val properties: Map<String, OType>
) : SchemaProvider