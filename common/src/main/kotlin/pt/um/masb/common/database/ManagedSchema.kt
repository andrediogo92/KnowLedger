package pt.um.masb.common.database

interface ManagedSchema {

    fun createProperty(
        key: String, value: StorageType
    ): SchemaProperty

    fun dropProperty(key: String)
    fun declaredPropertyNames(): List<String>
    fun addCluster(clusterName: String): ManagedSchema
}