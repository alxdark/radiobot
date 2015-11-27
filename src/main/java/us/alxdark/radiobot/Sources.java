package us.alxdark.radiobot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Sources implements Serializable {
    private static final long serialVersionUID = -2479633327365267396L;

    private static final Random rand = new Random();
    
    private final Map<String, Sources> sourcesByGenre = new HashMap<String, Sources>();
    private final List<Source> sources = new ArrayList<Source>();
    private final Ordering order;
    
    private boolean hasBeenInited;
    private int position;
    
    public Sources(Ordering ordering) {
        this.order = ordering;
    }
    
    public final void add(final Source source) {
        sources.add(source);
    }

    public final Source getSource() {
        initializeSources();
        if (order == Ordering.RANDOM) {
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
                        genreSources = new Sources(this.order);
                        sourcesByGenre.put(genre, genreSources);
                    }
                    genreSources.add(source);
                }
            }
        }
        return sourcesByGenre.get(selectedGenre);
    }
    
    public boolean isEmpty() {
        return sources.isEmpty();
    }
    
    private void initializeSources() {
        if (!hasBeenInited) {
            if (order == Ordering.SHUFFLE) {
                Collections.shuffle(sources);    
            } else if (order == Ordering.LINEAR) {
                Collections.sort(sources, new Comparator<Source>() {
                    @Override public int compare(Source s1, Source s2) {
                        return s1.getName().compareTo(s2.getName());
                    }
                });
            }
            hasBeenInited = true;
        }
        
    }
}
