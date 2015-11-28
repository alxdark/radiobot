package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class SourceTest {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void serialize() throws Exception {
        // You know what? I hate the File object.
        List<File> musicFiles = Lists.newArrayList(
            new File("src/test/resources/Bea Wain/BeaWain-ChatanoogaChoCho1942.mp3"),
            new File("src/test/resources/Bea Wain/BeaWain-DoILoveYou.mp3"),
            new File("src/test/resources/Bea Wain/BeaWain-StormyWeather1941.mp3")
        );
        String parent = "src/test/resources/Bea Wain";
        Ordering ordering = Ordering.RANDOM;
        List<String> genres = Lists.newArrayList("swing");
        
        Source source = new Source(parent, ordering, genres);
        String json = MAPPER.writeValueAsString(source);
        Source newSource = MAPPER.readValue(json, Source.class);
        
        assertEquals(Ordering.RANDOM, newSource.getOrdering());
        
        assertEquals(genres, newSource.getGenres());
        assertEquals(musicFiles.size(), newSource.getMusicFiles().size());
        for (int i=0; i < newSource.getMusicFiles().size(); i++) {
            assertTrue(newSource.getMusicFiles().get(i).getAbsolutePath().contains(musicFiles.get(i).getAbsolutePath()));
        }
    }
    
}
