name = "Test Playlist"
roots = ["/Users/alx/java/snappy/src/test/resources"]
image = "src/main/resources/artwork/swing.jpg"
length = 2
genres = (function() {
    var array = [];
    (10).times(function() {
        array.push(["swing","jazz"].random());
    });
    return array.flatten();
})();
