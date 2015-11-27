package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final long serialVersionUID = 4142904260536063434L;
    
    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
    
    private final List<String> roots;
    private final String name;
    private final File image;
    private final int length;
    private final List<String> genres;
    private final Ordering order;

    private int position;
    
    public Playlist(List<String> roots, String name, File image, int length, List<String> genres, Ordering order) {
        checkArgument(image.exists() && image.isFile(), "Cannot find the image file: %s", image.getAbsolutePath());
        checkArgument(StringUtils.isNotBlank(name), "Your playlist does not specify a name (e.g. name = 'My Playlist')");
        checkArgument(!genres.isEmpty(), "Your playlist does not specify any genres");
        checkArgument(!roots.isEmpty(), "No root directories were specified");
        for (String root : roots) {
            File path = new File(root);
            checkArgument((path.exists() && path.isDirectory()), "Root directory doesn't exist or isn't a directory: %s", root.toString());
        }
        if (length == -1) {
            logger.info("No length specified, setting to 20 items");
            length = 20;
        }
        if (length > 500) {
            logger.info("Limiting playlist length to 500 items");
            length = 500;
        }
        
        this.roots = roots;
        this.name = name;
        this.image = image;
        this.length = length;
        this.genres = genres;
        this.order = order;
    }

    public Set<String> getGenreSet() {
        return new HashSet<String>(genres);
    }
    
    public static final String getName(String thisName) {
        return thisName.replaceAll(" ", "_").replaceAll("\\W+", "");
    }
    
    public final List<String> getRoots() {
        return Collections.unmodifiableList(roots);
    }

    public final String getName() {
        return name;
    }
    
    public final String getFileName() {
        return Playlist.getName(name);
    }
    
    public final int getLength() {
        return length;
    }

    public final File getImage() {
        return image;
    }
    
    public final Ordering getOrder() {
        return order;
    }
    
    public final String getNextGenre() {
        int pos = (position++) % genres.size();
        return genres.get(pos);
    }
    
    @Override 
    public String toString() {
        return new ToStringBuilder(this)
            .append("roots", roots.toString())
            .append("name", name)
            .append("imagePath", image.getAbsolutePath())
            .append("length", length)
            .append("genres", genres)
            .append("order", order)
            .toString();
    }

}
