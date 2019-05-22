package pt.um.masb.common.database

interface NewInstanceSession {
    fun newInstance(): StorageElement

    fun newInstance(className: String): StorageElement

    fun newInstance(bytes: ByteArray): StorageBytes
}