package pt.um.masb.common.database.orient

enum class OrientDatabaseMode(val mode: String) {
    EMBEDDED("plocal"),
    MEMORY("memory"),
    REMOTE("remote")
}