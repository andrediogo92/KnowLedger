package pt.um.masb.ledger.storage.schema

import com.orientechnologies.orient.core.metadata.schema.OType

interface SchemaProvider {
    val id: String
    val properties: Map<String, OType>
}