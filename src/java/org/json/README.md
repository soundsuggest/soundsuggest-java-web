JSON in Java [package org.json]
===============================

Douglas Crockford
douglas@crockford.com

2011-02-02

Source : http://json.org/java/

********************************

JSON is a light-weight, language independent, data interchange format.
See http://www.JSON.org/

The files in this package implement JSON encoders/decoders in Java.
It also includes the capability to convert between JSON and XML, HTTP
headers, Cookies, and CDL.

This is a reference implementation. There is a large number of JSON packages
in Java. Perhaps someday the Java community will standardize on one. Until
then, choose carefully.

The license includes this restriction: "The software shall be used for good,
not evil." If your conscience cannot live with that, then choose a different
package.

The package compiles on Java 1.2 thru Java 1.4.

********************************************************************************

* `JSONObject.java:` The JSONObject can parse text from a String or a JSONTokener
to produce a map-like object. The object provides methods for manipulating its
contents, and for producing a JSON compliant object serialization.

* `JSONArray.java:` The JSONObject can parse text from a String or a JSONTokener
to produce a vector-like object. The object provides methods for manipulating
its contents, and for producing a JSON compliant array serialization.

* `JSONTokener.java:` The JSONTokener breaks a text into a sequence of individual
tokens. It can be constructed from a String, Reader, or InputStream.

* `JSONException.java:` The JSONException is the standard exception type thrown
by this package.


* `JSONString.java:` The JSONString interface requires a toJSONString method,
allowing an object to provide its own serialization.

* `JSONStringer.java:` The JSONStringer provides a convenient facility for
building JSON strings.

* `JSONWriter.java:` The JSONWriter provides a convenient facility for building
JSON text through a writer.

* `CDL.java:` CDL provides support for converting between JSON and comma
delimited lists.

* `Cookie.java:` Cookie provides support for converting between JSON and cookies.

* `CookieList.java:` CookieList provides support for converting between JSON and
cookie lists.

* `HTTP.java:` HTTP provides support for converting between JSON and HTTP headers.

* `HTTPTokener.java:` HTTPTokener extends JSONTokener for parsing HTTP headers.

* `XML.java:` XML provides support for converting between JSON and XML.

* `JSONML.java:` JSONML provides support for converting between JSONML and XML.

* `XMLTokener.java:` XMLTokener extends JSONTokener for parsing XML text.

Usage
-----

Different ways to construct a JSON Object :

    ```Java
        // METHOD 1
        JSONObject JSON1 = new JSONObject();
        JSON1.append("items", "noisedriver");
        JSON1.append("items", "left passage");
        System.out.println("JSON1\n" + JSON1.toString());
        
        // METHOD 2
        JSONObject JSON2 = new JSONObject();
        JSONArray ja = new JSONArray();
        ja.put("noisedriver");
        ja.put("left passage");
        JSON2.put("items", ja);
        System.out.println("JSON2\n" + JSON2.toString());
        
        // METHOD 3
        JSONObject JSON3 = new JSONObject();
        JSON3.put("items", new JSONArray());
        JSON3.put("items", "noisedriver");
        JSON3.put("items", "left passage");
        System.out.println("JSON3\n" + JSON2.toString());
    ```

Output:

    JSON1
    {"items":["noisedriver","left passage"]}
    JSON2
    {"items":["noisedriver","left passage"]}
    JSON3
    {"items":["noisedriver","left passage"]}