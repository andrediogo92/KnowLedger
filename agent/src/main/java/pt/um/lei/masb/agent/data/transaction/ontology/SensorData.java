package pt.um.lei.masb.agent.data.transaction.ontology;

import jade.content.Concept;
import pt.um.lei.masb.blockchain.data.Category;

import java.util.Objects;

public class SensorData implements Concept {
    private Category category;
    private GeoData data;
    private long timestamp;


    public SensorData(Category category, GeoData data, long timestamp) {
        this.category = category;
        this.data = data;
        this.timestamp = timestamp;
    }


    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


    public GeoData getData() {
        return data;
    }

    public void setData(GeoData data) {
        this.data = data;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SensorData that = (SensorData) o;
        return timestamp == that.timestamp &&
                category == that.category &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(category, data, timestamp);
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "category=" + category +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
