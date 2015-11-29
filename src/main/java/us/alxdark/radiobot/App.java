package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) throws Exception {
        checkArgument(args.length == 1, "Usage: java -jar radiobot.jar configFileName");
        
        InputStream inputStream = App.class.getResourceAsStream("/logging.properties");
        LogManager.getLogManager().readConfiguration(inputStream);
        
        ConfigFactory factory = new ConfigFactory();
        Playlist playlistConfig = factory.createPlaylist(args[0]);
        
        //JsonStorage storage = new JsonStorage(factory);
        SerializationStorage storage = new SerializationStorage(factory.getSaveDirectory());
        
        Playlist playlist = storage.load(playlistConfig.getFileName(), Playlist.class);
        if (playlist == null) {
            logger.info("Creating playlist from scratch");
            playlist = playlistConfig;
        }
        storage.save(playlist.getFileName(), playlist);
        
        Sources sources = storage.load(playlist.getFileName(), Sources.class);
        if (sources == null) {
            logger.info("Creating sources from scratch (this can take some time)");
            sources = factory.createSources(playlist);
        }
        // Do this twice, it's often common to go through the whole scan and get an error, and it takes awhile
        storage.save(playlist.getFileName(), sources);
        
        CompilationGenerator generator = new CompilationGenerator(factory, playlist, sources);
        generator.createCompilation();
        storage.save(playlist.getFileName(), sources);
    }
    
}
