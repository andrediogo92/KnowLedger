package pt.um.lei.masb.blockchain.persistance.schema

import com.orientechnologies.orient.core.metadata.schema.OType

data class PreConfiguredSchemaProvider(
    override val id: String,
    override val properties: Map<String, OType>
) : SchemaProvider