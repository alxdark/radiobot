package us.alxdark.radiobot;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Sources implements Serializable {
    private static final long serialVersionUID = -7681391068210354837L;
    
    private final Map<String,List<Source>> sources;
    private int position;
    
    @JsonCreator
    Sources(@JsonProperty("sources") Map<String, List<Source>> sources, 
            @JsonProperty("position") int position) {
        this.sources = sources;
        this.position = position;
    }

    public Sources() {
        this(Maps.newHashMap(), 0);
    }
    
    public Map<String,List<Source>> getSources() {
        return sources;
    }
    
    public int getPosition() {
        return position;
    }
    
    public List<Source> getSources(String genre) {
        return sources.get(genre);
    }
    
    public void finishLoading() {
        // We could support Ordering here but I have always just wanted to randomize these.
        for (List<Source> list : sources.values()) {
            Collections.shuffle(list);
        }
    }
    
    public void add(Source src) {
        for (String genre : src.getGenres()) {
            sources.putIfAbsent(genre, Lists.newArrayList());
            sources.get(genre).add(src);
        }
    }

    @JsonIgnore
    public final Source getNextSource(String genre) {
        if (!sources.containsKey(genre)) {
            throw new IllegalArgumentException("The sources object has nothing for genre: " + genre);
        }
        List<Source> list = sources.get(genre);
        return list.get( position++ % list.size() );
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, sources);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Sources other = (Sources) obj;
        return Objects.equals(position, other.position) &&
               Objects.equals(sources, other.sources);
    }

    @Override 
    public String toString() {
        return new ToStringBuilder(this)
            .append("sources", sources)
            .append("position", position)
            .toString();
    }
    
}
