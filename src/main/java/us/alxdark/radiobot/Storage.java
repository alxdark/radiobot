package us.alxdark.radiobot;

import java.io.IOException;

public interface Storage {

    public <T> T load(String fileName, Class<T> clazz) throws IOException ;
    
    public <T> void save(String fileName, T object) throws IOException ;
    
}
