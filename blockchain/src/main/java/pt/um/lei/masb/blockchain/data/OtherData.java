package pt.um.lei.masb.blockchain.data;

import pt.um.lei.masb.blockchain.Sizeable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;


@Entity
public final class OtherData<T extends Serializable>
        extends GeoData
        implements Sizeable {

    @Id
    @GeneratedValue
    private long id;

    @Basic(optional = false)
    private final T data;

    protected OtherData() {
        this.data = null;
    }

    public OtherData(@NotNull T data,
                     BigDecimal lat,
                     BigDecimal lng) {
        super(lat, lng);
        this.data = data;
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
