package us.alxdark.radiobot;

import java.io.IOException;

public interface Storage {

    <T> T load(String fileName, Class<T> clazz) throws IOException;
    
    <T> void save(String fileName, T object) throws IOException;
    
}
