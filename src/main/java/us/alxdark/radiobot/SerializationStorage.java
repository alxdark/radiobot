package us.alxdark.radiobot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class SerializationStorage implements Storage {
    
    private String savePath;
    
    public SerializationStorage(String savePath) {
        this.savePath = savePath;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T load(String fileName, Class<T> clazz) throws IOException {
        String path = getFileName(fileName, clazz);
        try (FileInputStream fileIn = new FileInputStream(path); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (T)in.readObject();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public <T> void save(String fileName, T object) throws IOException {
        String path = getFileName(fileName, object.getClass());
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
        out.writeObject(object);
        out.close();
    }
     
    private String getFileName(String name, Class<?> clazz) throws IOException {
        Path saveDirectory = Paths.get(savePath);
        FileUtils.forceMkdir(saveDirectory.toFile());
        return Paths.get(saveDirectory.toString(), name + "-" + clazz.getSimpleName() + ".ser").toString();
    }

}
