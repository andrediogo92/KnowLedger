package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;

public class TransactionOutput implements Sizeable {
    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }
}
