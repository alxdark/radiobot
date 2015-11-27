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

    public MixFileVisitor(Ordering order) {
        this.sources = new Sources(order);
    }
    
    public Sources getSources() {
        return sources;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().endsWith("mix.js")) {
            Source source = ConfigFactory.createSource(file);
            sources.add(source);
            logger.info("Found source ({}): {}", source.getGenresString(), file.getParent().toString());
        }
        return FileVisitResult.CONTINUE;
    }
            
    
}
