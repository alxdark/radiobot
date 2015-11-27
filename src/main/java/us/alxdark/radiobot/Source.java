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
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Source implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Random rand = new Random();

    private final List<File> musicFiles = new ArrayList<File>();
    private final String parent;
    private final Ordering order;
    private final List<String> genres;

    private int position;

    public Source(String parent, Ordering order, List<String> genres) {
        checkNotNull(parent, "Parent folder for a source is null");
        File file = new File(parent);
        checkArgument(file.exists() && file.isDirectory(), "Parent folder is not a real folder %s", parent.toString());
        checkNotNull(order, "No order specified for a source");
        checkArgument(!genres.isEmpty(),"Found .mix file, but no genre(s) are given: %s", parent.toString());

        this.parent = parent;
        this.order = order;
        this.genres = genres;
    }

    public boolean isApplicableTo(String genre) {
        return genres.contains(genre);
    }

    public String getName() {
        return parent.toString();
    }

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    public String getGenresString() {
        return StringUtils.join(genres, ", ");
    }

    public File getMusicFile() {
        initializeMusicFiles();
        if (order == Ordering.RANDOM) {
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
            if (!Paths.get(parent).toFile().isDirectory()) {
                throw new RuntimeException(Paths.get(parent).toString() + "/ is not a directory, did you rename or delete it?");
            }
            Collection<File> files = FileUtils.listFiles(Paths.get(parent).toFile(), new Mp3FileFilter(), null);
            musicFiles.addAll(files);
            if (musicFiles.isEmpty()) {
                throw new RuntimeException("Does this folder contain mp3s? " + parent);
            }
            // Prepare for certain ordering strategies.
            if (order == Ordering.SHUFFLE) {
                Collections.shuffle(musicFiles);
            } else if (order == Ordering.LINEAR) {
                Collections.sort(musicFiles, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
            }
        }
    }

    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("parent", parent.toString())
            .append("order", order)
            .append("genres", genres)
            .toString();
    }
}
