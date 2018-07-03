package pt.um.lei.masb.agent.net;

import pt.um.lei.masb.agent.data.DataSource;
import pt.um.lei.masb.agent.data.apis.ApiSource;
import pt.um.lei.masb.blockchain.data.Category;

import java.util.List;
import java.util.Map;

public class JSONMappedURLs {
    private List<ApiSource> apis;
    private Map<Category, DataSource[]> tcp;
    private DataSource jade[];

    JSONMappedURLs() {
    }

    public List<ApiSource> getApis() {
        return apis;
    }

    public Map<Category, DataSource[]> getTcp() {
        return tcp;
    }

    public DataSource[] getTemperatureCategory() {
        return tcp.getOrDefault(Category.TEMPERATURE, new DataSource[0]);
    }

    public DataSource[] getHumidityCategory() {
        return tcp.getOrDefault(Category.HUMIDITY, new DataSource[0]);
    }

    public DataSource[] getLuminosityCategory() {
        return tcp.getOrDefault(Category.LUMINOSITY, new DataSource[0]);
    }

    public DataSource[] getNoiseCategory() {
        return tcp.getOrDefault(Category.NOISE, new DataSource[0]);
    }

    public DataSource[] getOtherCategory() {
        return tcp.getOrDefault(Category.OTHER, new DataSource[0]);
    }

    public DataSource[] getJade() {
        return jade;
    }

}
