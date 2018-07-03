package pt.um.lei.masb.agent.messaging.transaction.ontology;

import jade.content.Concept;
import pt.um.lei.masb.blockchain.data.Category;

import java.util.Objects;

public class JSensorData implements Concept {
    private Category category;
    private JGeoData data;
    private String timestamp;


    public JSensorData(Category category, JGeoData data, String timestamp) {
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


    public JGeoData getData() {
        return data;
    }

    public void setData(JGeoData data) {
        this.data = data;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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
        JSensorData that = (JSensorData) o;
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
        return "JSensorData{" +
                "category=" + category +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
