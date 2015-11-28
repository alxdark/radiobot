package us.alxdark.radiobot;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixFileVisitor extends SimpleFileVisitor<Path> {
    
    private static final Logger logger = LoggerFactory.getLogger(MixFileVisitor.class);
    
    private final Sources sources;
    private final ConfigFactory factory;

    public MixFileVisitor(ConfigFactory factory, Ordering order) {
        this.factory = factory;
        this.sources = new Sources(order);
    }
    
    public Sources getSources() {
        return sources;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().endsWith("mix.js")) {
            Source source = factory.createSource(file);
            sources.add(source);
            logger.info(String.format("Found source (%s) ordering '%s': %s", source.getGenresString(), source
                    .getOrdering().name().toLowerCase(), file.getParent().toString()));
        }
        return FileVisitResult.CONTINUE;
    }
            
    
}
