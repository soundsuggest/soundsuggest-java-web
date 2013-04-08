<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    /*String redirectURL = "DataService";
    response.sendRedirect(redirectURL);*/
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>SoundSuggest</title>
        <meta name="language" content="english"/>
        <meta name="description" content="SoundSuggest - Thesis KUL - Visualization of music suggestions"/>
        <meta name="keywords" content="soundsuggest visualization of music suggestions" />
        <link rel="shortcut icon" href="data/img/icon.gif" />
        <link rel="stylesheet" type="text/css" href="css/style.css"/>
        <script type="text/javascript" src="js/lib/d3/d3.v3.js"></script>
        <script type="text/javascript" src="js/lib/jquery/jquery-1.8.3.js"></script>
        <script type="text/javascript" src="js/lib/jquery/jquery.lib.purl.js"></script>
        <script type="text/javascript" src="js/lib/lastfm/lastfm.api.md5.js"></script>
        <script type="text/javascript" src="js/lib/lastfm/lastfm.api.cache.js"></script>
        <script type="text/javascript" src="js/lib/lastfm/lastfm.api.js"></script>
        <script type="text/javascript" src="js/lib/spin/spin.js"></script>
        <script type="text/javascript" src="js/lib/infovis/infovis.whitebox.cf.js"></script>
    </head>
    <body>
        <div id="page">
            <div id="header">
                <h1>SoundSuggest - Visualization of Music Suggestions</h1>
                <h2>Digital Prototype</h2>
            </div>
            <div id="content">
                <div id="chart">
                    <div id="spinner"></div>
                    <script type="text/javascript" src="js/main.js"></script>
                </div>
                <div id="users"></div>
                <div id="item-info"></div>
            </div>
            <div id="footer">
                <p>For more info on chord diagrams click <a href="http://bl.ocks.org/4062006" target="_blank">here</a>.</p>
                <p>For more info the thesis click <a href="http://soundsuggest.wordpress.com/" target="_blank">here</a>.</p>
            </div>
        </div>
    </body>
</html>
