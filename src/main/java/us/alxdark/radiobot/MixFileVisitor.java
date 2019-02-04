package us.alxdark.radiobot;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class MixFileVisitor extends SimpleFileVisitor<Path> {
    
    private static final Logger logger = LoggerFactory.getLogger(MixFileVisitor.class);
    
    private final ConfigFactory factory;
    private final Set<String> playlistGenres;
    private final Sources sources;

    public MixFileVisitor(ConfigFactory factory, List<String> playlistGenres) {
        this.factory = factory;
        this.sources = new Sources();
        // playlist genres are duplicated in arbitrary ways for variability, here we just need unique values.
        this.playlistGenres = Sets.newHashSet(playlistGenres);
    }
    
    public Sources getSources() {
        sources.finishLoading();
        return sources;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().endsWith( factory.getGenresFilename() )) {
            Source src = factory.createSource(file);
            if (src != null && !Sets.intersection(playlistGenres, src.getGenres()).isEmpty()) {
                sources.add(src);        
                logger.info(String.format("Found source (%s) ordering '%s': %s", Joiner.on(", ").join(src.getGenres()), src.getOrdering()
                        .name().toLowerCase(), file.getParent().toString()));
            }
        }
        return FileVisitResult.CONTINUE;
    }

}
