var API_KEY     = '828c109e6a54fffedad5177b194f7107';
var API_SECRET  = '7c2f09e6eb84e8a6183c59e0bc574f70';
var LAST_FM;
var SESSION_KEY;
var USERNAME;
var SPINNER;

jQuery(document).ready(function() {
    
    SPINNER = new Spinner({
        lines: 13, // The number of lines to draw
        length: 20, // The length of each line
        width: 10, // The line thickness
        radius: 50, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '200px', // Top position relative to parent in px
        left: 'auto' // Left position relative to parent in px
    }).spin(document.getElementById("spinner"));
    
    LAST_FM = new LastFM({
        apiKey      : API_KEY,
        apiSecret   : API_SECRET,
        cache       : new LastFMCache()
    });
    
    if ($.url().param('added')) {
        alert("Artist [" + $.url().param('added') + "] was successfully added!");
    }
    
    if (!$.url().param('username')) {
        USERNAME = window.prompt("Enter your Last.fm username", "noisedriver");
    } else {
        USERNAME = $.url().param('username');
    }
    
    // If NO session THEN getSession ELSE loadData END-IF.
    if (!$.url().param('session')) {
        if (!$.url().param('token')) {
            window.location = 'http://www.last.fm/api/auth/?api_key='
                 + API_KEY + "&username=" + USERNAME + '&cb=' + window.location;
        } else {
            var token = $.url().param('token');
            LAST_FM.auth.getSession({
                token: token
            }, {
                success: function(data_sess) {
                    SESSION_KEY = data_sess.session.key;

                    d3.json("DataService?key=" + SESSION_KEY + "&user=" + USERNAME, function(error, jsondata) {
                        SPINNER.stop();
                        new Whitebox(jsondata);
                    });
                },
                error : function(data_error) {
                    console.error(data_error.error + " : " + data_error.message);
                }
            });
        }
    } else {
        SESSION_KEY = $.url().param('session');
        
        d3.json("DataService?key=" + SESSION_KEY + "&user=" + USERNAME, function(error, jsondata) {
            SPINNER.stop();
            new Whitebox(jsondata);
        });
    }
});

function addRecommendation(artist) {
    var artist_name = artist.replace("_", " ");
    LAST_FM.library.addArtist({
        artist  : artist_name,
        api_key : API_KEY
    },
    {
        key : SESSION_KEY
    },
    {
        success: function(data) {
            console.log(data);
            var tmp =  window.location.toString().split("?");
            window.location = tmp[0] += "?session=" + SESSION_KEY + "&username=" + USERNAME + "&added=" + artist_name;
        },
        error: function(data_error) {
            console.error(data_error.error + " : " + data_error.message);
        }
    });
}