package pt.um.lei.masb.agent.net;

import pt.um.lei.masb.blockchain.data.Category;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class JSONMappedURLs {
    private URL apis[];
    private Map<Category, URL[]> tcp;
    private URL jade[];

    private JSONMappedURLs() {
    }

    public URL[] getApis() {
        return apis;
    }

    public Map<Category, URL[]> getTcp() {
        return tcp;
    }

    public URL[] getTemperatureCategory() {
        return tcp.getOrDefault(Category.TEMPERATURE, new URL[0]);
    }

    public URL[] getHumidityCategory() {
        return tcp.getOrDefault(Category.HUMIDITY, new URL[0]);
    }

    public URL[] getLuminosityCategory() {
        return tcp.getOrDefault(Category.LUMINOSITY, new URL[0]);
    }

    public URL[] getNoiseCategory() {
        return tcp.getOrDefault(Category.NOISE, new URL[0]);
    }

    public URL[] getOtherCategory() {
        return tcp.getOrDefault(Category.OTHER, new URL[0]);
    }

    public URL[] getJade() {
        return jade;
    }

}
