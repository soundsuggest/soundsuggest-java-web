var API_KEY     = '828c109e6a54fffedad5177b194f7107';
var API_SECRET  = '7c2f09e6eb84e8a6183c59e0bc574f70';
var AUTH_URL;
var LAST_FM;
var SESSION_KEY;
var USERNAME;

jQuery(document).ready(function() {
    
    USERNAME = window.prompt("Enter your Last.fm username", "noisedriver");
    AUTH_URL = 'http://www.last.fm/api/auth/?api_key='
             + API_KEY + '&cb=' + window.location;
    LAST_FM  = new LastFM({
                    apiKey: api_key,
                    apiSecret: api_secret,
                    cache: new LastFMCache()
                });
    
    if (!$.url().param('token')) {
        window.location = AUTH_URL;
    } else {
        var token = $.url().param('token');
        LAST_FM.auth.getSession({
            token: token
        }, {
            success: function(data_sess) {
                SESSION_KEY = data_sess.session.key;
                
                d3.json("DataService?key=" + SESSION_KEY + "&user=" + USERNAME, function(error, data) {
                    new Whitebox(data);
                });
            },
            error : function(data_error) {
                console.error(data_error.error + " : " + data_error.message);
            }
        });
    }
});

function addRecommendation(artist) {
    LAST_FM.library.addArtist({
        artist  : artist,
        api_key : API_KEY
    },
    {
        key : SESSION_KEY
    },
    {
        success: function(data) {
            console.log(data);
            var refresh = window.location;
            window.location = refresh;
        },
        error: function(data_error) {
            console.error(data_error.error + " : " + data_error.message);
        }
    });
}