package pt.um.lei.masb.blockchain.data;

import org.openjdk.jol.info.GraphLayout;
import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;


@Embeddable
public class OtherData<T extends Serializable> implements Sizeable {
    private final T data;

    public OtherData(@NotNull T data) {
        this.data = data;
    }

    protected OtherData() {
        this.data = null;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OtherData<?> otherData = (OtherData<?>) o;
        return Objects.equals(data, otherData.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(data);
    }

    @Override
    public @NotNull String toString() {
        return "OtherData{" +
                "data=" + data +
                '}';
    }
}
