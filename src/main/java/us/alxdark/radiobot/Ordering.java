package us.alxdark.radiobot;

/**
 * The order that sources are drawn from when creating a playlist, or that files in a source are added to a playlist.
 */
public enum Ordering {
    /**
     * Shuffle all the items and then select them in order. The order will be random, but all files will be returned 
     * before repeating any files, and they will repeat in the same randomized order each time.
     */
    SHUFFLE,
    /**
     * A file will be selected at random each time. One file may appear more than others, and the pattern of selection
     * will not repeat.
     */
    RANDOM,
    /**
     * Starting with the first source or file, in alphabetical order, and proceeding sequentially through the list, 
     * looping at the end. 
     */
    SEQUENTIAL,
    /**
     * Similar to sequential, but it will start at a random place in the list of sources or files, and then proceed 
     * sequentially, looping when it gets to the end of the files.
     */
    LOOPING
}
