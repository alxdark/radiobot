package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makin' stuff, often from JS-based config files.
 */
public class ConfigFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);
    
    private static final String CMD = "<cmd>";
    private static final String ROOT = "root";
    private static final String GENRES = "genres";
    private static final String LENGTH = "length";
    private static final String IMAGE = "image";
    private static final String NAME = "name";
    private static final String ORDER = "order";
    private static final String GENRE = "genre";
    private Properties props = new Properties();
    
    public ConfigFactory() {
        try {
            InputStream configStream = ConfigFactory.class.getResourceAsStream("/config.properties");
            props.load(configStream);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
        
    public Sources createSources(Playlist playlist) throws Exception {
        MixFileVisitor mixFileVisitor = new MixFileVisitor(this, playlist.getOrdering());
        for (String root : playlist.getRoots()) {
            logger.info("Looking for mix.js files in {}", root);
            Path path = Paths.get(root);
            Files.walkFileTree(path, mixFileVisitor);    
        }
        Sources sources = mixFileVisitor.getSources();
        for (String genre : playlist.getGenreSet()) {
            Sources against = sources.forGenre(genre);
            if (against == null || against.getSources().isEmpty()) {
                throw new Exception("Could not find any sources for the genre: " + genre);
            }
        }            
        return sources;
    }
    
    public Source createSource(Path sourcePath) throws IOException {
        try {
            // You want as fast as possible to establish that the directory belongs to this playlist,
            // and do no work until you figure this out. Source might be configured in two steps.
            String parent = sourcePath.getParent().toFile().getAbsolutePath();
            
            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            String script = FileUtils.readFileToString(sourcePath.toFile());
            context.evaluateString(scope, script, CMD, 1, null);

            List<String> genres = getSingularOrList(scope, GENRE);
            Ordering order = getOrder(scope, ORDER, Ordering.SHUFFLE);
            
            return new Source(parent, order, genres);
            
        } catch(Exception e) {
            throw new RuntimeException("Error in file " + sourcePath.toFile().getAbsolutePath() + ": " + e.getMessage());
        } finally {
            Context.exit();    
        }
    }

    public Playlist createPlaylist(String configFileName) throws IllegalArgumentException, IOException, ScriptException {
        File configFile = createPlaylistConfiguration(configFileName);
        try {
            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            InputStream inputStream = ConfigFactory.class.getResourceAsStream("/core.js");
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String core = writer.toString();
            
            context.evaluateString(scope, core, CMD, 1, null);
            
            String script = FileUtils.readFileToString(configFile);
            context.evaluateString(scope, script, CMD, 1, null);
            
            String name = getString(scope, NAME);
            String imagePath = getString(scope, IMAGE);
            File image = createImageFile(configFile.getParent(), imagePath);
            int length = getInt(scope, LENGTH);
            List<String> genres = getList(scope, GENRES);
            List<String> rootPaths = getSingularOrList(scope, ROOT);
            Ordering order = getOrder(scope, ORDER, Ordering.RANDOM);
            
            return new Playlist(rootPaths, name, image, length, genres, order);

        } catch(Exception e) {
            throw new RuntimeException("Error in file " + configFile.getAbsolutePath() + ": " + e.getMessage());
        } finally {
            Context.exit();    
        }
    }

    private File createPlaylistConfiguration(String name) {
        File configFile = new File(props.getProperty("playlists.directory") + "/" + name + ".js");
        checkArgument(configFile.exists() && configFile.isFile(), "Cannot find playlist file: %s", configFile.getAbsolutePath());
        return configFile;
    }
    
    private File createImageFile(String configFilePath, String imagePath) {
        if (imagePath.startsWith("/")) {
            return new File(imagePath);
        }
        return Paths.get(configFilePath, imagePath).toFile();
    }
    
    private Ordering getOrder(Scriptable scope, String key, Ordering defaultOrder) {
        try {
            String string = (String)scope.get(key, scope);
            return Ordering.valueOf(string.toUpperCase());
        } catch(Throwable t) {
            return defaultOrder;
        }
    }
    private String getString(Scriptable scope, String key) {
        try {
            return (String)scope.get(key, scope);
        } catch(Throwable t) {
            return null;
        }
    }
    private int getInt(Scriptable scope, String key) {
        try {
            return (int)scope.get(key, scope);
        } catch(Throwable t) {
            return -1;
        }
    }
    private List<String> getList(Scriptable scope, String key) {
        try {
            List<String> list = new LinkedList<String>();
            NativeArray array = (NativeArray)scope.get(key, scope);
            long len = array.getLength();
            for (int i=0; i < len; i++) {
                list.add( (String)array.get(i, null) );
            }
            return list;
        } catch(Throwable t) {
            return Collections.emptyList();
        }
    }
    private List<String> getSingularOrList(Scriptable scope, String key) {
        // Lot of ways to specify the relevant playlist.
        List<String> list = new LinkedList<String>();
        String value = getString(scope, key);
        if (value == null) {
            value = getString(scope, key+"s");
        }
        if (value != null) {
            list.add(value);
        } else {
            list.addAll(getList(scope, key+"s"));    
        }
        return list;
    }
}
