package pt.um.lei.masb.agent.data.feed;

public class AdafruitPublishJSON {
    private String value;
    private String lat;
    private String created_at;
    private String lon;
    //private String ele;

    public AdafruitPublishJSON(){

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
