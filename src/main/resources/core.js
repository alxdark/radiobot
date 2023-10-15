Array.prototype.random = function() {
    return this[ Math.floor(Math.random()*this.length) ];
};
Array.prototype.flatten = function() {
    var array = arguments[0] || [];
    this.forEach(function(element) {
        if (element instanceof Array)
            element.flatten(array);
        else
            array.push(element);
    }, this);
    return array;
}
Number.prototype.times = function(func) {
    for (var i=0; i < this; i++)
        func();
}