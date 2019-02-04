package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SourcesTest {

    private static final String DIRECTORY = "src/test/resources/Bea Wain";
    private static final String GENRE = "genre1";

    @Test
    public void serializeJSON() throws Exception {
        Storage storage = new JsonStorage("target");
        Sources sources = createSources();
        sources.getNextSource(GENRE);

        storage.save("Name", sources);
        Sources newSources = storage.load("Name", Sources.class);

        assertEquals(sources, newSources);
        assertEquals(sources.getSources(GENRE), newSources.getSources(GENRE));
        assertEquals(1, sources.getPosition());
    }

    @Test
    public void serialize() throws Exception {
        Storage storage = new SerializationStorage("target");
        Sources sources = createSources();
        sources.getNextSource(GENRE);

        storage.save("Name", sources);
        Sources newSources = storage.load("Name", Sources.class);

        assertEquals(sources, newSources);
        assertEquals(sources.getSources(GENRE), newSources.getSources(GENRE));
        assertEquals(1, sources.getPosition());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionOnBadGenre() {
        Sources sources = createSources();
        sources.getNextSource("badGenre");
    }

    @Test
    public void getNextSourceSequentiallyWorks() { // also multiple genres work
        Sources sources = createSources();
        sources.add(new Source("src/test/resources/Louis Jordan", Ordering.SEQUENTIAL, Sets.newHashSet("western drama")));
        sources.finishLoading();

        assertEquals(1, sources.getSources(GENRE).size());
        assertEquals(1, sources.getSources("western drama").size());

        // These files are sequential
        for (int i = 0; i < 5; i++) {
            String fileName = sources.getNextSource("western drama").getNextFile();
            int fileNum = ((i) % 3) + 1;
            assertTrue(fileName.contains("Jordan/0" + fileNum));
        }
    }

    @Test
    public void getNextSourceShuffledWorks() {
        Sources sources = createSources();
        sources.finishLoading();

        List<String> output = fill(sources.getNextSource(GENRE), 2);
        List<String> output2 = fill(sources.getNextSource(GENRE), 2);

        assertNotEquals(output, output2);
    }

    private Sources createSources() {
        Source source = new Source(DIRECTORY, Ordering.SHUFFLE, Sets.newHashSet(GENRE));
        Sources sources = new Sources();
        sources.add(source);
        return sources;
    }

    private List<String> fill(Source source, int count) {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            list.add(source.getNextFile());
        }
        return list;
    }

}
