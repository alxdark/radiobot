package us.alxdark.radiobot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Sources implements Serializable {
    private static final long serialVersionUID = -7681391068210354837L;

    private static final Random rand = new Random();
    
    private final Map<String,List<Source>> sources;
    private final Ordering ordering;
    private int position;
    
    @JsonCreator
    Sources(@JsonProperty("ordering") Ordering ordering,
            @JsonProperty("sources") Map<String, List<Source>> sources, 
            @JsonProperty("position") int position) {
        this.ordering = ordering;
        this.sources = sources;
        this.position = position;
    }

    public Sources(Ordering ordering) {
        this(ordering, Maps.newHashMap(), 0);
    }
    
    public Ordering getOrdering() {
        return ordering;
    }
    
    public List<Source> getSources(String genre) {
        return sources.get(genre);
    }
    
    public Map<String,List<Source>> getSources() {
        return sources;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void finishLoading() {
        for (List<Source> list : sources.values()) {
            if (ordering == Ordering.SHUFFLE) {
                Collections.shuffle(list);
            } else if (ordering == Ordering.SEQUENTIAL) {
                list.sort(Comparator.comparing(Source::getDir));
            }
        }
    }
    
    public void add(Source src) {
        sources.putIfAbsent(src.getGenre(), Lists.newArrayList());
        sources.get(src.getGenre()).add(src);
    }

    @JsonIgnore
    public final Source getNextSource(String genre) {
        if (!sources.containsKey(genre)) {
            throw new IllegalArgumentException("The sources object has nothing for genre: " + genre);
        }
        List<Source> list = sources.get(genre);
        if (ordering == Ordering.RANDOM) {
            return list.get( rand.nextInt(list.size()) );
        } else {
            return list.get( position++ % list.size() );
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordering, position, sources);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Sources other = (Sources) obj;
        return Objects.equals(ordering, other.ordering) &&
               Objects.equals(position, other.position) &&
               Objects.equals(sources, other.sources);
    }

    @Override 
    public String toString() {
        return new ToStringBuilder(this)
            .append("sources", sources)
            .append("ordering", ordering)
            .append("position", position)
            .toString();
    }
    
}
