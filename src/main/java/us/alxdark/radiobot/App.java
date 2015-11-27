package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) throws Exception {
        //try {
            InputStream inputStream = App.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(inputStream);
            
            checkArgument(args.length == 1, "Usage: java -jar snappy.jar path/to/config/file.js");
            File configFile = new File(args[0]);
            checkArgument(configFile.exists() && configFile.isFile(), "Cannot find playlist file: %s", configFile.getAbsolutePath());
            
            Storage storage = new Storage();
            
            Playlist playlistConfig = ConfigFactory.createPlaylist(configFile);
            Playlist playlist = storage.loadPlaylist(playlistConfig);
            if (playlist == null) {
                logger.info("Creating playlist from scratch");
                playlist = playlistConfig;
            }

            Sources sources = storage.loadSources(playlist);
            if (sources == null) {
                logger.info("Creating sources from scratch (this can take some time)");
                sources = ConfigFactory.createSources(playlist);
            }
            CompilationGenerator generator = new CompilationGenerator(playlist, sources);
            generator.createCompilation();
            
            // Don't save these if something is wrong that led to an exception.
            storage.savePlaylist(playlist);
            storage.saveSources(playlist, sources);
            /*
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        */
    }
    
}
