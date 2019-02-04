package us.alxdark.radiobot;

import static org.jaudiotagger.tag.FieldKey.ALBUM;
import static org.jaudiotagger.tag.FieldKey.ALBUM_ARTIST;
import static org.jaudiotagger.tag.FieldKey.ARTIST;
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
import java.util.List;

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

// TODO: jaudiotagger has issues, try replacing it with: https://github.com/mpatric/mp3agic
public class CompilationGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CompilationGenerator.class);

    private final ConfigFactory factory;
    private final Playlist playlist;
    private final Sources sources;
    private final int paddingLength;
    private final String yearString;
    private final Artwork artwork;
    private boolean mp3GainExists;

    public CompilationGenerator(ConfigFactory factory, Playlist playlist, Sources sources) throws IOException {
        this.factory = factory;
        this.playlist = playlist;
        this.sources = sources;

        this.paddingLength = getPaddingLength(playlist.getLength());
        if (playlist.getImage() != null) {
            File file = Paths.get(factory.getPlaylistsDirectory(), playlist.getImage()).toFile();
            if (file.exists()) {
                this.artwork = ArtworkFactory.createArtworkFromFile(file);    
            } else {
                throw new RuntimeException("This image doesn't exist for artwork: " + file.getAbsolutePath());
            }
        } else {
            this.artwork = null;
        }
        this.yearString = new SimpleDateFormat("yyyy").format(new Date());
    }

    public void createCompilation() throws Exception {
        File dir = Paths.get(factory.getExportDirectory(), playlist.getFileName()).toFile();
        FileUtils.forceMkdir(dir);
        FileUtils.cleanDirectory(dir);

        String mp3GainFile = factory.getMp3Gain().split(" ")[0];
        mp3GainExists = new File(mp3GainFile).exists();
        if (!mp3GainExists) {
            logger.warn("mp3 gain not installed or configured, file volumes will not be normalized");
        } else {
            logger.info("mp3 gain found, files will be normalized");
        }
        
        for (int i=0; i < playlist.getLength(); i++) {
            String genre = playlist.getNextGenre();
            List<Source> sources = this.sources.getSources(genre);
            if (sources == null || sources.isEmpty()) {
                throw new IllegalArgumentException("No source folders were found for the genre " + genre);
            }

            // human readable index
            int position = (i+1);
            // Get a random source, get a random file in the source
            File srcFile = new File(this.sources.getNextSource(genre).getNextFile());
            File destFile = getDestFile(position, FilenameUtils.getExtension(srcFile.getName()));

            FileUtils.copyFile(srcFile, destFile);
        	logger.debug(String.format("   (rewriting data for %s)", srcFile.getPath()));
            rewriteMetadataAndAdjustGain(destFile, position);
            logger.info(String.format("   #%s %s: %s", Integer.toString(i), genre, srcFile.getName()));
        }
    }

    private void rewriteMetadataAndAdjustGain(File destFile, int trackNumber) throws Exception {
        System.out.println(destFile.getAbsolutePath());
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
        
        audio.delete(v1tag);
        audio.delete(v23tag);
        
        v23tag = new ID3v24Tag();
        
        v23tag.setField(ALBUM_ARTIST, factory.getAlbumAuthor());
        v23tag.setField(YEAR, yearString);
        if (StringUtils.isNotBlank(trackTitle)) {
            v23tag.setField(TITLE, trackTitle);
        }
        if (StringUtils.isNotBlank(artist)) {
            v23tag.setField(ARTIST, artist);
        }
        v23tag.setField(IS_COMPILATION, "1");
        v23tag.setField(ALBUM, playlist.getName());
        v23tag.setField(TRACK, Integer.toString(trackNumber));
        v23tag.setField(TRACK_TOTAL, Integer.toString(playlist.getLength()));
        if (artwork != null) {
            v23tag.setField(artwork);
        }
        audio.setTag(v23tag);
        audio.commit();
        
        if (mp3GainExists && 
           (destFile.getAbsolutePath().indexOf(".mp3") > -1 ||
            destFile.getAbsolutePath().indexOf(".MP3") > -1)) {
                logger.info("   Using Mp3 Gain to normalize volume of mp3");
                Runtime run = Runtime.getRuntime();
                run.exec(factory.getMp3Gain() + destFile.getAbsolutePath());
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
        return Paths.get(factory.getExportDirectory(), playlist.getFileName(), fileName).toFile();
    }

    private int getPaddingLength(int size) {
        return (int)(Math.log10(size) + 1);
    }
}
