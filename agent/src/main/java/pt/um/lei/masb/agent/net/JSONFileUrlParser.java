package pt.um.lei.masb.agent.net;

import com.google.gson.Gson;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * A parser for JSON files with an array of urls.
 */
public class JSONFileUrlParser {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger("JSONFileUrlParser");
    private final URL file;

    /**
     * A parser for a file based on a path string from the classpath resources.
     *
     * @param file the path string to a resource in the classpath.
     */
    public JSONFileUrlParser(@NotEmpty String file) {
        this.file = getClass().getResource(file);
    }

    /**
     * A parser for a file at the specified URL.
     *
     * @param file
     */
    public JSONFileUrlParser(@NotEmpty URL file) {
        this.file = file;
    }

    public @NotNull JSONMappedURLs parseFromJSON() throws FileNotFoundException {
        var g = new Gson();
        return g.fromJson(
                new FileReader(file.getFile()),
                               JSONMappedURLs.class);
    }
}
