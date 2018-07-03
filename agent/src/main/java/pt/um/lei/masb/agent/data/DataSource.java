package pt.um.lei.masb.agent.data;

import com.google.api.client.http.GenericUrl;

import java.net.URL;

public class DataSource extends GenericUrl {
    private String id;
    private URL url;

    protected DataSource() {
    }

    public DataSource(String id, URL url) {
        this.id = id;
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public String getID() {
        return id;
    }

}
