package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.GraphLayout;

/**
 * Report an approximate size in bytes of the underlying object.
 */
public interface Sizeable {
    default long getApproximateSize() {
       return GraphLayout.parseInstance(this).totalSize();
    }
}
