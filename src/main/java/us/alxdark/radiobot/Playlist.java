package us.alxdark.radiobot;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Other options
 * 
 * - don't use history
 * - don't include album title as part of song title
 * - change "Various" for album author
 * - include original information as lyrics info? Retrieve lyrics info?
 * - preserve original artwork if none is specified? (test this)
 *
 */
public class Playlist implements Serializable {
    private static final long serialVersionUID = 3306390701867902570L;

    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
    
    private final String name;
    private final String image;
    private final int length;
    private final List<String> roots;
    private final List<String> genres;
    private int position;
    
    @JsonCreator
    public Playlist(@JsonProperty("name") String name, @JsonProperty("image") String image, 
        @JsonProperty("length") int length, @JsonProperty("roots") List<String> roots, 
        @JsonProperty("genres") List<String> genres) {
        
        if (length < 1) {
            logger.info("No length specified, setting to 20 items");
            length = 20;
        }
        if (length > 500) {
            logger.info("Limiting playlist length to 500 items");
            length = 500;
        }
        this.name = name;
        this.image = image;
        this.length = length;
        this.roots = roots;
        this.genres = genres;
    }

    @JsonIgnore
    public final String getNextGenre() {
        int pos = (position++) % genres.size();
        return genres.get(pos);
    }
    
    public List<String> getGenres() {
        return genres;
    }
    
    public final List<String> getRoots() {
        return Collections.unmodifiableList(roots);
    }

    public final String getName() {
        return name;
    }
    
    @JsonIgnore
    public final String getFileName() {
        return (name == null) ? null : name.replaceAll(" ", "_").replaceAll("\\W+", "");
    }
    
    public final int getLength() {
        return length;
    }

    public final String getImage() {
        return image;
    }
    
    @Override 
    public String toString() {
        return new ToStringBuilder(this)
            .append("roots", roots.toString())
            .append("name", name)
            .append("image", image)
            .append("length", length)
            .append("genres", genres)
            .toString();
    }

}
