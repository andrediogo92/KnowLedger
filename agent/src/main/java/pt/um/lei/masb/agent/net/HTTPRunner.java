package pt.um.lei.masb.agent.net;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.um.lei.masb.agent.data.apis.ApiAdapter;
import pt.um.lei.masb.agent.data.apis.ApiSource;

import java.io.IOException;
import java.util.*;

public class HTTPRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPRunner.class);
    private static final HttpTransport httpTransport = new NetHttpTransport();
    private static final JsonFactory jsonFactory = new GsonFactory();
    private List<ApiSource> apis;
    private Map<String, Class<?>> matchers;
    private Random r;


    public HTTPRunner() {
        apis = new ArrayList<>();
        matchers = new HashMap<>();
    }

    public void registerMatcher(String toMatch, Class<?> toDeserialize) {
        matchers.put(toMatch, toDeserialize);
    }

    public void registerMatchers(Map<String, Class<?>> matchers) {
        this.matchers.putAll(matchers);
    }


    public void registerSources(List<ApiSource> apis) {
        this.apis.addAll(apis);
        apis.forEach(ApiSource::setQuery);
        r = new Random(this.apis.stream()
                                .map(ApiSource::toString)
                                .reduce(0,
                                        (i, s) -> i + s.chars().sum(),
                                        (i1, i2) -> i1 + i2));
    }

    public Optional<ApiAdapter> runRandom() {
        Optional<ApiAdapter> res;
        res = runApiQuery(r.nextInt(apis.size()));
        return res;
    }

    public int registeredApis() {
        return apis.size();
    }

    public Optional<ApiAdapter> run(int i) {
        Optional<ApiAdapter> res;
        if (i < registeredApis() && i > 0) {
            res = runApiQuery(i);
        } else {
            LOGGER.error("", new IndexOutOfBoundsException());
            res = Optional.empty();
        }
        return res;
    }

    private Optional<ApiAdapter> runApiQuery(int i) {
        Optional<ApiAdapter> res;
        var key = apis.get(i);
        if (matchers.containsKey(key.getID())) {
            var val = matchers.get(key.getID());
            if (Arrays.stream(val.getInterfaces())
                      .anyMatch(c -> c.equals(ApiAdapter.class))) {
                var requestFactory = httpTransport.createRequestFactory(
                        r -> r.setParser(new JsonObjectParser(jsonFactory))
                                                                       );
                try {
                    var request = requestFactory.buildGetRequest(key);
                    res = Optional.of((ApiAdapter) request.execute().parseAs(val));
                } catch (IOException e) {
                    LOGGER.error("", e);
                    res = Optional.empty();
                }
            } else {
                LOGGER.error("Not instance of ApiAdapter: " + val);
                res = Optional.empty();
            }
        } else {
            LOGGER.error("Id doesn't match any known ApiAdapters" + key);
            res = Optional.empty();
        }
        return res;
    }

}
