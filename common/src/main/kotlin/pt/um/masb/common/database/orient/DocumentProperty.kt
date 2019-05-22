package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.metadata.schema.OProperty
import pt.um.masb.common.database.SchemaProperty

data class DocumentProperty internal constructor(
    internal val property: OProperty
) : SchemaProperty