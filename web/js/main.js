var API_KEY     = '828c109e6a54fffedad5177b194f7107';
var API_SECRET  = '7c2f09e6eb84e8a6183c59e0bc574f70';
var LAST_FM;
var SESSION_KEY;
var USERNAME;
var SPINNER;
var WHITEBOX;

jQuery(document).ready(function() {
    
    createSpinner();
    
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
                        WHITEBOX = new Whitebox();
                        WHITEBOX.create(jsondata);
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
            WHITEBOX = new Whitebox();
            WHITEBOX.create(jsondata);
        });
    }
});

function itemInfo(itemname, isrecommendation, user) {
    lastfm.artist.getInfo({
        artist    : itemname,
        user      : user
    },
    {
        success: function(data) {
            var bio = data.artist.bio.summary;
            jQuery('#item-info-description')
                .append(bio);
            if (isrecommendation) {
                d3.select('#item-info-controls')
                    .append('button')
                    .text('Add to library')
                    .attr('onclick', 'addRecommendation("' + itemname + '");');
            }
        },
        error: function(data) {
            console.error(data.error + " " + data.message);
        }
    });
}

function userInfo(userName, isActiveUser, activeuser) {
    
}

addRecommendation = function(artist) {
    WHITEBOX.destroy();
    createSpinner();
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
            /*
            var tmp =  window.location.toString().split("?");
            window.location = tmp[0] += "?session=" + SESSION_KEY + "&username=" + USERNAME + "&added=" + artist;
            */
            d3.json("DataService?key=" + SESSION_KEY + "&user=" + USERNAME, function(error, jsondata) {
                SPINNER.stop();
                WHITEBOX = new Whitebox();
                WHITEBOX.create(jsondata);
            });
        },
        error: function(data_error) {
            console.error(data_error.error + " : " + data_error.message);
        }
    });
};

createSpinner = function() {
    SPINNER = new Spinner({
         lines: 13,
         length: 20,
         width: 10,
         radius: 50,
         corners: 1,
         rotate: 0,
         direction: 1,
         color: '#000',
         speed: 1,
         trail: 60,
         shadow: false,
         hwaccel: false,
         className: 'spinner',
         zIndex: 2e9,
         top: '200px',
         left: 'auto'
     }).spin(document.getElementById("spinner"));
};