package pt.um.lei.masb.agent.data.transaction.ontology;

import java.util.Arrays;
import java.util.Objects;

public class OtherData extends GeoData {
    private String className;
    private byte[] data;

    public OtherData(String lat,
                     String lng,
                     String className,
                     byte[] data) {
        super(lat, lng);
        this.className = className;
        this.data = data;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OtherData otherData = (OtherData) o;
        return Objects.equals(className, otherData.className) &&
                Arrays.equals(data, otherData.data);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(className);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "OtherData{" +
                "className='" + className + '\'' +
                ", data=" + data +
                '}';
    }


}
