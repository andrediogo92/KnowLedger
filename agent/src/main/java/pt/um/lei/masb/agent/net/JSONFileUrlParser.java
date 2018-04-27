package pt.um.lei.masb.agent.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A parser for JSON files with an array of urls.
 */
public class JSONFileUrlParser {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger("JSONFileUrlParser");
    private String root;
    private URL file;

    public JSONFileUrlParser(@NotEmpty String file) {
        root = file;
        this.file = getClass().getResource(file);
    }

    public @NotNull JSONMappedURLs parseFromJSON() throws FileNotFoundException {
        var g = new Gson();
        return g.fromJson(
                new FileReader(file.getFile()),
                               JSONMappedURLs.class);
    }
}
