package us.alxdark.radiobot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Sources implements Serializable {
    private static final long serialVersionUID = -2479633327365267396L;

    private static final Random rand = new Random();
    
    private final Map<String, Sources> sourcesByGenre = Maps.newHashMap();
    private final List<Source> sources = Lists.newArrayList();
    private final Ordering ordering;
    
    private boolean hasBeenInited;
    private int position;
    
    public Sources(Ordering ordering) {
        this.ordering = ordering;
    }
    
    @JsonCreator
    private Sources(@JsonProperty("sources") List<Source> sources, @JsonProperty("ordering" ) Ordering ordering) {
        this.sources.addAll(sources);
        this.ordering = ordering;
    }

    public Ordering getOrdering() {
        return ordering;
    }
    
    public List<Source> getSources() {
        return sources;
    }
    
    public final void add(final Source source) {
        sources.add(source);
    }

    @JsonIgnore
    public final Source getSource() {
        initializeSources();
        if (ordering == Ordering.RANDOM) {
            // Return one at random...
            return sources.get( rand.nextInt(sources.size()) );
        } else {
            // Else, return them in sorted order or shuffled order
            return sources.get( position++ % sources.size() );
        }
    }
    
    public final Sources forGenre(String selectedGenre) {
        // This has to be delayed until we have a complete list of all sources
        // in this collection, and we can call initializeSources() on them to 
        // sort them accordingly.
        initializeSources();
        if (sourcesByGenre.isEmpty()) {
            for (Source source : sources) {
                for (String genre : source.getGenres()) {
                    Sources genreSources = sourcesByGenre.get(genre);
                    if (genreSources == null) {
                        genreSources = new Sources(this.ordering);
                        sourcesByGenre.put(genre, genreSources);
                    }
                    genreSources.add(source);
                }
            }
        }
        return sourcesByGenre.get(selectedGenre);
    }
    
    private void initializeSources() {
        if (!hasBeenInited) {
            if (ordering == Ordering.SHUFFLE) {
                Collections.shuffle(sources);    
            } else if (ordering == Ordering.LINEAR) {
                sources.sort(Comparator.comparing(Source::getDir));
            }
            hasBeenInited = true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasBeenInited, ordering, position, sources, sourcesByGenre);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Sources other = (Sources) obj;
        return Objects.equals(hasBeenInited, other.hasBeenInited) && 
               Objects.equals(ordering, other.ordering) &&
               Objects.equals(position, other.position) &&
               Objects.equals(sources, other.sources) &&
               Objects.equals(sourcesByGenre, other.sourcesByGenre);
    }

    @Override
    public String toString() {
        return "Sources [sourcesByGenre=" + sourcesByGenre + ", sources=" + sources + ", ordering=" + ordering
                + ", hasBeenInited=" + hasBeenInited + ", position=" + position + "]";
    }
    
}
