package pt.um.lei.masb.blockchain

import org.openjdk.jol.info.GraphLayout

/**
 * Report an approximate size in bytes
 * of the underlying object.
 */
interface Sizeable {
    val approximateSize: Long
        get() = GraphLayout
            .parseInstance(this)
            .totalSize()
}
