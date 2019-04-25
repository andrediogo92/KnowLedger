package pt.um.lei.masb.blockchain.persistance.database

enum class DatabaseMode(val mode: String) {
    EMBEDDED("plocal"),
    MEMORY("memory"),
    REMOTE("remote")
}