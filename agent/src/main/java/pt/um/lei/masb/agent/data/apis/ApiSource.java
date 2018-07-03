package pt.um.lei.masb.agent.data.apis;

import pt.um.lei.masb.agent.data.DataSource;

import java.net.URL;
import java.util.Map;

public class ApiSource extends DataSource {
    private Map<String, String> params;

    public ApiSource(String id, URL url) {
        super(id, url);
    }

    protected ApiSource() {
    }

    public void setQuery() {
        params.forEach(this::set);
    }

}
