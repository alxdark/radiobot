package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Source implements Serializable {
    private static final long serialVersionUID = -6541751343464326229L;

    private static final Random rand = new Random();

    private final String dir;
    private final List<String> files;
    private final Ordering ordering;
    private final String genre;
    private int position;

    @JsonCreator
    public Source(@JsonProperty("dir") String dir, @JsonProperty("ordering") Ordering ordering,
            @JsonProperty("genre") String genre) {
        checkNotNull(dir, "Parent folder for a source is null");
        checkNotNull(ordering, "No ordering specified for a source");
        if (!Paths.get(dir).toFile().isDirectory()) {
            throw new RuntimeException(Paths.get(dir).toString() + "/ is not a directory, did you rename or delete it?");
        }
        this.dir = dir;
        this.ordering = ordering;
        this.genre = genre;
        this.files = initializeFiles();
    }

    public String getDir() {
        return dir.toString();
    }

    public String getGenre() {
        return genre;
    }

    public Ordering getOrdering() {
        return ordering;
    }
    
    public List<String> getFiles() {
        return files;
    }
    
    public int getPosition() {
        return position;
    }
    
    @JsonIgnore
    public String getNextFile() {
        if (ordering == Ordering.RANDOM) {
            return files.get( rand.nextInt(files.size()) );
        } else {
            return files.get( position++ % files.size() );
        }
    }

    private List<String> initializeFiles() {
        List<String> newFiles = FileUtils.listFiles(Paths.get(dir).toFile(), new Mp3FileFilter(), null)
                .stream().map(File::getAbsolutePath).collect(Collectors.toList());
        if (newFiles.isEmpty()) {
            throw new RuntimeException("Does this folder contain mp3s? " + dir);
        }
        // Prepare for certain ordering strategies.
        if (ordering == Ordering.SHUFFLE) {
            Collections.shuffle(newFiles);
        } else if (ordering == Ordering.SEQUENTIAL) {
            Collections.sort(newFiles, String.CASE_INSENSITIVE_ORDER);
        }
        return newFiles;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dir, genre, files, ordering, position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Source other = (Source) obj;
        return Objects.equals(dir, other.dir) && 
                Objects.equals(genre, other.genre) &&
                Objects.equals(files, other.files) && 
                Objects.equals(ordering, other.ordering) && 
                Objects.equals(position, other.position);
    }

    @Override 
    public String toString() {
        return new ToStringBuilder(this)
            .append("dir", dir)
            .append("files", files)
            .append("ordering", ordering)
            .append("genre", genre)
            .append("position", position)
            .toString();
    }
    
}
