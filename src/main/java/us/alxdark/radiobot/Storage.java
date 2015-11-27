package us.alxdark.radiobot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class Storage {
    
    public void savePlaylist(Playlist playlist) throws IOException {
        String fileName = getFileName(playlist, "-Playlist.ser");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(playlist);
        out.close();
    }
    
    public void saveSources(Playlist playlist, Sources sources) throws IOException {
        String fileName = getFileName(playlist, "-Sources.ser");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(sources);
        out.close();
    }   
    
    public Playlist loadPlaylist(Playlist suppliedPlaylist) throws IOException {
        String fileName = getFileName(suppliedPlaylist, "-Playlist.ser");
        try (FileInputStream fileIn = new FileInputStream(fileName); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (Playlist)in.readObject();
        } catch(Exception e) {
            return null;
        }
    }
    
    public Sources loadSources(Playlist playlist) throws IOException {
        String fileName = getFileName(playlist, "-Sources.ser");
        try (FileInputStream fileIn = new FileInputStream(fileName); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (Sources)in.readObject();
        } catch(Exception e) {
            return null;
        }
    }
    
    private String getFileName(Playlist playlist, String suffix) throws IOException {
        Path prefsDirectory = Paths.get(FileUtils.getUserDirectoryPath(), ".snappy");
        FileUtils.forceMkdir(prefsDirectory.toFile());
        return Paths.get(prefsDirectory.toString(), playlist.getFileName() + suffix).toString();
    }
}
