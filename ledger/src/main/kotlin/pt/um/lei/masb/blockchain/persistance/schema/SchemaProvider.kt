package pt.um.lei.masb.blockchain.persistance.schema

import com.orientechnologies.orient.core.metadata.schema.OType

interface SchemaProvider {
    val id: String
    val properties: Map<String, OType>
}