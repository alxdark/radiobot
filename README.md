# Usage

This is a command-line Java program bundled as a jar file. To use this program you will need the Java 8 SDK and Maven installed on your system, as well as minimal knowledge of JavaScript (because the "configuration" files are written using simple JavaScript).

## Compile Jar

From the command line:

    git clone https://github.com/alxdark/radiobot

Then cd into that directory. In the src/main/resources directory of this project there is a config.properties file. 
Those values will *definitely* need to be changed to your local machine. You will also need to create a playlist 
configuration file (see below), with an extension of *.js in the playlists directory you configure. 

When this is correct, run

    mvn compile assembly:single

You should then be able to run

    java -jar target/radiobot-1.0-SNAPSHOT-jar-with-dependencies.jar

And it will complain because you have not fed it a configuration file. That's excellent; everything is working.

## Annotating source folders for selection

Rather than use ID3 tags like genre, which were set wrong for most or all of the music I own, I opted instead 
to put a simple configuration file in every directory of mp3 files I wanted to add to playlists (by default, this 
is named mix.js, but this can be changed). The file contains genre and ordering criteria:

	genres = ['otr','audio drama']
	order = 'sequential'

These are JavaScript assigment to global variables. In this case, all the files in the directory are being added 
under the genres 'otr' and 'audio drama', and the playlist tool will select the files sequentially, starting at a random 
file in the folder. Radiobot will scan the root folders you provide in the playlist configuration, and add every folder
 with a mix.js file to the available sources for building a playlist.

## Creating a playlist configuration file

A Playlist is a file ending with the *.js extension that contains basic JavaScript to configure a playlist. Here's 
and example:

	name = "Andromeda Radio"
	roots = ["/Volumes/music/Thematic/","/Volumes/music/Drama/","/Volumes/music/Sounds/Andromeda/"]
	image = "artwork/andromeda.jpg"
	length = 100
	genres = (function() {
	    var array = [];
	    (10).times(function() {
	        (2).times(function() {
	            (2).times(function() {
	                    var song1 = ["swing","swing","swing","jazz","singera"].random();
	                    var song2 = ["swing","swing","swing","jazz","singera"].random();
	                    array.push([
	                        [song1, song2, "fallout"],
	                        [song1, "fallout", song2],
	                        ["fallout", song1, song2]
	                    ].random());
	            });
	            array.push("commercial");
	        });
	        array.push(["psa", "oddity"].random());
	        array.push(["propaganda", "drama"].random());
	        array.push(["instrumental", "exotica", "western","jump","blues"].random());
	    });
	    return array.flatten();
	})();
	
This is just JS assignment to some variables, including some logic in preparing the list of genre tags that will 
be used, sequentially, to select a music file of the same genre from one of your source folders.           

## TODO

This documentation. It's simpler thatn it looks to use this thing.