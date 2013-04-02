SoundSuggest - Last.fm API data parser
======================================

This project uses a Java Servlet class in combination with the Last.fm API Java Library to make a connection to the Last.fm API data. Next the data is processed to a JSON output file. This JSON file has the following structure:

JSON file structure
*******************

    {
        "items" :
        [
                    {
                            "name"  : "item.SOME_ARTIST",
                            "edges" :
                            [
                                    "item.SOME_ARTIST.user.SOME_USER",
                                    ...
                            ],
                            "owners" :
                            [
                            "SOME_USER",
                            ...
                            ],
                            "description" : "SOME_DESCRIPTION"
                    },
                    ...
            ],
            users :
            [
                    {
                            "name"      : "SOME_USER",
                            "active"    : BOOLEAN
                    },
                    ...
            ]
    }

This file is the input for the Whitebox object of the whitebox.js script. Linking the JSON of the DataService Servlet class with the Whitebox object can be done as follows:

    d3.json("DataService", function(error, data) {
        new Whitebox(data);
    });

For more information consult: http://soundsuggest.wordpress.com/