package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

public class SourceTest {
    
    private static final String DIRECTORY = "src/test/resources/Bea Wain";
    private static final Set<String> GENRE = Sets.newHashSet("swing");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> EXPECTED_FILES = Sets.newHashSet(
        "src/test/resources/Bea Wain/01 BeaWain-ChatanoogaChoCho1942.mp3",
        "src/test/resources/Bea Wain/02 BeaWain-DoILoveYou.mp3",
        "src/test/resources/Bea Wain/03 BeaWain-StormyWeather1941.mp3"
    );

    @Test
    public void serializeJSON() throws Exception {
        Source source = new Source(DIRECTORY, Ordering.SHUFFLE, GENRE);
        String json = MAPPER.writeValueAsString(source);
        Source newSource = MAPPER.readValue(json, Source.class);
        
        assertEquals(DIRECTORY, newSource.getDir());
        assertEquals(GENRE, newSource.getGenres());
        assertEquals(Ordering.SHUFFLE, newSource.getOrdering());
        assertEquals(EXPECTED_FILES.size(), newSource.getFiles().size());
        assertAllFilesFound(newSource.getFiles());
    }
    
    @Test
    public void serialize() throws Exception {
        Storage storage = new SerializationStorage("target");
        
        Source source = new Source(DIRECTORY, Ordering.SHUFFLE, GENRE);
        source.getNextFile(); // move position
        
        storage.save("Name", source);
        Source newSource = storage.load("Name", Source.class);
        
        assertEquals(DIRECTORY, newSource.getDir());
        assertEquals(GENRE, newSource.getGenres());
        assertEquals(Ordering.SHUFFLE, newSource.getOrdering());
        assertEquals(EXPECTED_FILES.size(), newSource.getFiles().size());
        assertAllFilesFound(newSource.getFiles());
    }
    
    private void assertAllFilesFound(List<String> files) {
        for (String file : files) {
            if (!doesHaveFile(file)) {
                fail("File " +file+ " not found");
            };
        }
    }

    private boolean doesHaveFile(String file) {
        for (String match : EXPECTED_FILES) {
            if (file.contains(match)) {
                return true;
            }
        }
        return false;
    }
    
}
