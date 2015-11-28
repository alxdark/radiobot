package us.alxdark.radiobot;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class SourcesTest {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Test
    public void serializer() throws Exception {
        String parent = "src/test/resources/Bea Wain";
        Ordering ordering = Ordering.RANDOM;
        List<String> genres = Lists.newArrayList("genre1","genre2");
        
        Sources sources = new Sources(Ordering.LINEAR);
        sources.add(new Source(parent, ordering, genres));
        
        String json = MAPPER.writeValueAsString(sources);
        Sources newSources = MAPPER.readValue(json, Sources.class);
        
        assertEquals(sources, newSources);
    }
    
}
