package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class PlaylistTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Test
    public void serializer() throws Exception {
        List<String> roots = Lists.newArrayList("src/main","src/test");
        String name = "name";
        File image = new File("src/test/resources/cover.jpg");
        int length = 20;
        List<String> genres = Lists.newArrayList("genre1","genre2");
        Ordering ordering = Ordering.LINEAR;
        
        Playlist playlist = new Playlist(roots, name, image, length, genres, ordering);
        String json = MAPPER.writeValueAsString(playlist);
        Playlist newPlaylist = MAPPER.readValue(json, Playlist.class);
        
        assertEquals(roots, newPlaylist.getRoots());
        assertEquals(name, newPlaylist.getName());
        assertEquals(image.getAbsolutePath(), newPlaylist.getImage().getAbsolutePath());
        assertEquals(length, newPlaylist.getLength());
        assertEquals(genres, newPlaylist.getGenres());
        assertEquals(ordering, newPlaylist.getOrdering());
    }
  
    
}
