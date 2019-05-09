package pt.um.masb.common.database

enum class DatabaseMode(val mode: String) {
    EMBEDDED("plocal"),
    MEMORY("memory"),
    REMOTE("remote")
}