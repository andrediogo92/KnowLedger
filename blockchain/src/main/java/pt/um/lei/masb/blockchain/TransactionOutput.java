package pt.um.lei.masb.blockchain;

import org.openjdk.jol.info.ClassLayout;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TransactionOutput implements Sizeable {
    @Id
    @GeneratedValue
    private long id;

    @Override
    public long getApproximateSize() {
        return ClassLayout.parseClass(this.getClass()).instanceSize();
    }
}
