package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class PlaylistTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String NAME = "name";
    private static final String IMAGE = "src/test/resources/cover.jpg";
    private static final int LENGTH = 20;
    private static final List<String> ROOTS = Lists.newArrayList("src/main","src/test");
    private static final List<String> GENRES = Lists.newArrayList("genre1","genre2");
    private static final Ordering ORDERING = Ordering.SEQUENTIAL;
    
    @Test
    public void serializer() throws Exception {
        Playlist playlist = new Playlist(NAME, IMAGE, LENGTH, ROOTS, GENRES, ORDERING);
        String json = MAPPER.writeValueAsString(playlist);
        Playlist newPlaylist = MAPPER.readValue(json, Playlist.class);
        
        assertEquals(ROOTS, newPlaylist.getRoots());
        assertEquals(NAME, newPlaylist.getName());
        assertEquals(IMAGE, newPlaylist.getImage());
        assertEquals(LENGTH, newPlaylist.getLength());
        assertEquals(GENRES, newPlaylist.getGenres());
        assertEquals(ORDERING, newPlaylist.getOrdering());
    }
  
    @Test
    public void lengthLimitedTo20() {
        Playlist playlist = new Playlist(NAME, IMAGE, 0, ROOTS, GENRES, ORDERING);
        assertEquals(20, playlist.getLength());
    }
    
    @Test
    public void lengthLimitedTo500() {
        Playlist playlist = new Playlist(NAME, IMAGE, 600, ROOTS, GENRES, ORDERING);
        assertEquals(500, playlist.getLength());
    }
    
    @Test
    public void nextGenreWorks() {
        Playlist playlist = new Playlist(NAME, IMAGE, LENGTH, ROOTS, GENRES, ORDERING);
        
        List<String> genres = Lists.newArrayList();
        for (int i=0; i < 5; i++) {
            genres.add(playlist.getNextGenre());
        }
        assertEquals(Lists.newArrayList("genre1","genre2","genre1","genre2","genre1"), genres);
    }
    
    @Test
    public void fileName() {
        Playlist playlist = new Playlist("This is a complicated name: it should work!", IMAGE, LENGTH, ROOTS, GENRES, ORDERING);
        
        assertEquals("This_is_a_complicated_name_it_should_work", playlist.getFileName());
    }
    
}
