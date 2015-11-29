package us.alxdark.radiobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Uses JSON rather than Java's object serialization, but it is very very slow to load large JSON files. 
 * Preferable to go with serialization.
 */
public class JsonStorage implements Storage {
    
    private String savePath;
    private static ObjectMapper MAPPER = new ObjectMapper();
    
    public JsonStorage(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public <T> T load(String name, Class<T> clazz) throws IOException {
        String fileName = getFileName(name, clazz);
        if (new File(fileName).exists()) {
            try (FileInputStream fileIn = new FileInputStream(fileName)) {
                return MAPPER.readValue(fileIn, clazz);
            }
        }
        return null;
    }

    @Override
    public <T> void save(String name, T object) throws IOException {
        String fileName = getFileName(name, object.getClass());
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(MAPPER.writeValueAsString(object));
        }
    }
    
    private String getFileName(String name, Class<?> clazz) throws IOException {
        Path saveDirectory = Paths.get(savePath);
        FileUtils.forceMkdir(saveDirectory.toFile());
        return Paths.get(saveDirectory.toString(), name + "-" + clazz.getSimpleName() + ".json").toString();
    }
}
