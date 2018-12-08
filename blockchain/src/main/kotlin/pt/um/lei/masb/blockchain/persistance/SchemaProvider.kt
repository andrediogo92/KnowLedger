package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.metadata.schema.OType

data class SchemaProvider(
    val id: String,
    val properties: Map<String, OType>
)