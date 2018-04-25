package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embeddable;
import java.io.Serializable;


@Embeddable
public class OtherData<T extends Serializable> implements Sizeable {
    private final T data = null;

}
