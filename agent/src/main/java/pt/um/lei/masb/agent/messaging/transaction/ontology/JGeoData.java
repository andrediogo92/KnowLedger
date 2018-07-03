package pt.um.lei.masb.agent.messaging.transaction.ontology;

import jade.content.Concept;

public abstract class JGeoData implements Concept {
    private String lat;
    private String lng;

    public JGeoData(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }


    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
