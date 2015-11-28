package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) throws Exception {
        checkArgument(args.length == 1, "Usage: java -jar radiobot.jar configFileName");
        
        InputStream inputStream = App.class.getResourceAsStream("/logging.properties");
        LogManager.getLogManager().readConfiguration(inputStream);
        
        ConfigFactory factory = new ConfigFactory();
        Playlist playlistConfig = factory.createPlaylist(args[0]);
        
        Storage storage = new Storage();
        Playlist playlist = storage.loadPlaylist(playlistConfig);
        if (playlist == null) {
            logger.info("Creating playlist from scratch");
            playlist = playlistConfig;
        }

        Sources sources = storage.loadSources(playlist);
        if (sources == null) {
            logger.info("Creating sources from scratch (this can take some time)");
            sources = factory.createSources(playlist);
        }
        CompilationGenerator generator = new CompilationGenerator(playlist, sources);
        generator.createCompilation();
        
        storage.savePlaylist(playlist);
        storage.saveSources(playlist, sources);
    }
    
}
