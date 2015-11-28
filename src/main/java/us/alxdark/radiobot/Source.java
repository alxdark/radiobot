package us.alxdark.radiobot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Source implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Random rand = new Random();

    private final List<File> musicFiles = new ArrayList<File>();
    private final String dir;
    private final Ordering ordering;
    private final List<String> genres;

    private int position;

    @JsonCreator
    public Source(@JsonProperty("dir") String dir, @JsonProperty("ordering") Ordering ordering, @JsonProperty("genres") List<String> genres) {
        checkNotNull(dir, "Parent folder for a source is null");
        File file = new File(dir);
        checkArgument(file.exists() && file.isDirectory(), "Parent folder is not a real folder %s", dir.toString());
        checkNotNull(ordering, "No ordering specified for a source");
        checkArgument(!genres.isEmpty(),"Found genre file, but no genre(s) are given: %s", dir.toString());

        this.dir = dir;
        this.ordering = ordering;
        this.genres = genres;
    }

    public String getDir() {
        return dir.toString();
    }

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    @JsonIgnore
    public String getGenresString() {
        return StringUtils.join(genres, ", ");
    }

    public Ordering getOrdering() {
        return ordering;
    }
    
    public List<File> getMusicFiles() {
        initializeMusicFiles();
        return musicFiles;
    }
    
    @JsonIgnore
    public File getNextMusicFile() {
        initializeMusicFiles();
        if (ordering == Ordering.RANDOM) {
            // Return one at randome
            return musicFiles.get( rand.nextInt(musicFiles.size()) );
        } else {
            // Shuffled (if it was shuffled) or else linear starting with the first.
            return musicFiles.get( position++ % musicFiles.size() );
        }
    }

    private void initializeMusicFiles() {
        if (musicFiles.isEmpty()) {
            // This happens when the .ser files are out-of-date with the filesystem.
            // The whole thing is going to fail shortly
            if (!Paths.get(dir).toFile().isDirectory()) {
                throw new RuntimeException(Paths.get(dir).toString() + "/ is not a directory, did you rename or delete it?");
            }
            Collection<File> files = FileUtils.listFiles(Paths.get(dir).toFile(), new Mp3FileFilter(), null);
            musicFiles.addAll(files);
            if (musicFiles.isEmpty()) {
                throw new RuntimeException("Does this folder contain mp3s? " + dir);
            }
            // Prepare for certain ordering strategies.
            if (ordering == Ordering.SHUFFLE) {
                Collections.shuffle(musicFiles);
            } else if (ordering == Ordering.LINEAR) {
                Collections.sort(musicFiles, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dir, genres, musicFiles, ordering, position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Source other = (Source) obj;
        return Objects.equals(dir, other.dir) && 
                Objects.equals(genres, other.genres) &&
                Objects.equals(musicFiles, other.musicFiles) && 
                Objects.equals(ordering, other.ordering) && 
                Objects.equals(position, other.position);
    }

    @Override
    public String toString() {
        return "Source [musicFiles=" + musicFiles + ", dir=" + dir + ", ordering=" + ordering + ", genres=" + genres
                + ", position=" + position + "]";
    }
}
