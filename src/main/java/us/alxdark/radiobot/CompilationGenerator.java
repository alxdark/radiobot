package us.alxdark.radiobot;

import static org.jaudiotagger.tag.FieldKey.ALBUM;
import static org.jaudiotagger.tag.FieldKey.ALBUM_ARTIST;
import static org.jaudiotagger.tag.FieldKey.ARTIST;
import static org.jaudiotagger.tag.FieldKey.COVER_ART;
import static org.jaudiotagger.tag.FieldKey.DISC_NO;
import static org.jaudiotagger.tag.FieldKey.IS_COMPILATION;
import static org.jaudiotagger.tag.FieldKey.TITLE;
import static org.jaudiotagger.tag.FieldKey.TRACK;
import static org.jaudiotagger.tag.FieldKey.TRACK_TOTAL;
import static org.jaudiotagger.tag.FieldKey.YEAR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilationGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CompilationGenerator.class);

    private static final String ALBUM_AUTHOR = "Various";
    private static final String EXPORT = "export";

    // private static final String MP3_GAIN = "C:\\Program Files\\mp3gain-win-full-1_2_5\\mp3gain.exe /s r /s s /r /k ";
    // Really AAC Gain, but whatever
    private static final String MP3_GAIN = "/Applications/MacMP3Gain.app/Contents/Resources/aacgain.i386 /s r /s s /r /k ";

    private final Playlist playlist;
    private final Sources sources;
    private final int paddingLength;
    private final String yearString;
    private final Artwork artwork;

    public CompilationGenerator(Playlist playlist, Sources sources) throws IOException {
        this.playlist = playlist;
        this.sources = sources;

        this.paddingLength = getPaddingLength(playlist.getLength());
        this.artwork = ArtworkFactory.createArtworkFromFile(playlist.getImage());
        this.yearString = new SimpleDateFormat("yyyy").format(new Date());
    }

    public void createCompilation() throws Exception {
        File dir = new File(EXPORT + File.separator + playlist.getFileName());
        FileUtils.forceMkdir(dir);
        FileUtils.cleanDirectory(dir);

        for (int i=0; i < playlist.getLength(); i++) {
            String genre = playlist.getNextGenre();
            Sources sources = this.sources.forGenre(genre);
            if (sources == null) {
                throw new IllegalArgumentException("No source folders were found for the genre " + genre);
            }

            // human readable index
            int position = (i+1);
            // Get a random source, get a random file in the source
            File srcFile = sources.getSource().getNextMusicFile();
            File destFile = getDestFile(position, FilenameUtils.getExtension(srcFile.getName()));

            FileUtils.copyFile(srcFile, destFile);
        	logger.debug(String.format("   (rewriting data for %s)", srcFile.getPath()));
            rewriteMetadataAndAdjustGain(destFile, position);
            logger.info(String.format("   #%s %s: %s", Integer.toString(i), genre, srcFile.getName()));
        }
    }

    private void rewriteMetadataAndAdjustGain(File destFile, int trackNumber) throws Exception {
    	MP3File audio = (MP3File)AudioFileIO.read(destFile);	
        ID3v11Tag v1tag = getVersionOneTag(audio);
        AbstractID3v2Tag v23tag = getVersionTwoTag(audio);

        String trackTitle = v1tag.getFirstTitle();
        if (StringUtils.isBlank(trackTitle)) {
            trackTitle = v23tag.getFirst(TITLE);
        }
        String artist = v1tag.getFirstArtist();
        if (StringUtils.isBlank(artist)) {
            artist = v23tag.getFirst(ARTIST);
        }
        String album = v1tag.getFirstAlbum();
        if (StringUtils.isBlank(album)) {
            album = v23tag.getFirst(ALBUM);
        }
        if (StringUtils.isNotBlank(album)) {
            trackTitle = (trackTitle + " (" + album + ")");
        }
        v1tag.deleteField(TITLE);
        v1tag.deleteField(ALBUM);
        v1tag.deleteField(ARTIST);
        v1tag.deleteField(TRACK);

        // Can screw up the order of tracks, as it lists all the tracks
        // that don't have this field, then all the ones that have a
        // 1 in this field, etc.
        v23tag.deleteField(DISC_NO);

        // Deletes are probably not necessary.
        v23tag.deleteField(ALBUM_ARTIST);
        v23tag.setField(ALBUM_ARTIST, ALBUM_AUTHOR);

        v23tag.deleteField(YEAR);
        v23tag.setField(YEAR, yearString);

        if (StringUtils.isNotBlank(trackTitle)) {
            v23tag.deleteField(TITLE);
            v23tag.setField(TITLE, trackTitle);

            //setSongLyric(v23tag, trackTitle);
        }
        if (StringUtils.isNotBlank(artist)) {
            v23tag.deleteField(ARTIST);
            v23tag.setField(ARTIST, artist);
        }

        v23tag.deleteField(IS_COMPILATION);
        v23tag.setField(IS_COMPILATION, "1");

        v23tag.deleteField(ALBUM);
        v23tag.setField(ALBUM, playlist.getName());

        v23tag.deleteField(TRACK);
        v23tag.setField(TRACK, Integer.toString(trackNumber));
        v23tag.setField(TRACK_TOTAL, Integer.toString(playlist.getLength()));

        if (artwork != null) {
            v23tag.deleteField(COVER_ART);
            v23tag.setField(artwork);
        }

        audio.setTag(v23tag);
        audio.commit();

        // Not portable between operating system, for sure. Using this distribution of
        // a knock-off of MP3 Gain:
        //
        // http://homepage.mac.com/beryrinaldo/AudioTron/MacMP3Gain/
        //
        // The actual AAC Gain library is only distributed in source form, so this is the only binary
        // out there to do this.
        if (destFile.getAbsolutePath().indexOf(".mp3") > -1 ||
            destFile.getAbsolutePath().indexOf(".MP3") > -1) {
            Runtime run = Runtime.getRuntime();
            run.exec(MP3_GAIN + destFile.getAbsolutePath());
            // Process p = run.exec(MP3_GAIN + destFile.getAbsolutePath());
            //printMe(p.getErrorStream());
            //printMe(p.getInputStream());
        }
    }

    private ID3v11Tag getVersionOneTag(MP3File audio) throws Exception {
        ID3v11Tag v1tag = null;
        try {
            v1tag = (audio.hasID3v1Tag()) ? (ID3v11Tag)audio.getID3v1Tag() : new ID3v11Tag();
        } catch(ClassCastException cce) {
            // File has v1 tag and we want a v1.1 tag so we can add a track
            v1tag = new ID3v11Tag();
        }
        return v1tag;
    }

    private AbstractID3v2Tag getVersionTwoTag(MP3File audio) {
        return (audio.hasID3v2Tag()) ? (AbstractID3v2Tag)audio.getID3v2Tag() : new ID3v24Tag();
    }

    private File getDestFile(int position, String extension) {
        String fileName = StringUtils.leftPad(Integer.toString(position), paddingLength, "0") + "." + extension;
        return Paths.get(EXPORT, playlist.getFileName(), fileName).toFile();
    }

    private int getPaddingLength(int size) {
        int n = 1;
        while (size > 1) {
            size = size/10;
            n++;
        }
        return n;
    }
}
