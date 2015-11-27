= Usage =

This is a command-line Java program bundled as a jar file. To use this program you will need the Java 8 SDK and Maven installed on your system, as well as minimal knowledge of JavaScript (because the "configuration" files are written using simple JavaScript).

== Compile Jar ==

From the command line:

    git clone https://github.com/alxdark/radiobot

somewhere on your filesystem. Then cd into that directory. Run

    mvn compile assembly:single

You should then be able to run

    java -jar target/radiobot-1.0-SNAPSHOT-jar-with-dependencies.jar

And it will complain because you have not fed it a configuration file. That's excellent; everything is working.

== Configuring Compilations ==
