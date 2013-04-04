package servlets;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;
import lastfm.Artist;
import lastfm.PaginatedResult;
import lastfm.Session;
import lastfm.Tasteometer;
import lastfm.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import lastfm.Caller;
import org.json.JSONArray;

/**
 * <p>
 * The DataService class gathers data from the Last.fm website through the
 * Last.fm API and lastfm-java Java library. This data is the transformed
 * into a JSON output page.
 * </p>
 * <p></p>
 * @author Joris Schelfaut
 */
public class DataService extends HttpServlet {
    
    public static final String API_KEY                  = "828c109e6a54fffedad5177b194f7107";
    public static final String API_SECRET               = "7c2f09e6eb84e8a6183c59e0bc574f70";
    public static final String CONTENT_TYPE             = "application/json";
    public static final int LIMIT_NEIGHBOURS            = 10;
    public static final int LIMIT_RECOMMENDED_ARTISTS   = 10;
    public static final int LIMIT_TOP_ARTISTS           = 10;
    public static final int LIMIT_SIMILAR_ARTISTS       = 5;
    public static final double THRESHOLD                = 0.1;
    
    private String session_key;
    private String username;
    private User activeUser;
    private boolean debug;
    
    /**
     * @param token 
     */
    @Model
    private void setSessionKey(String sessionKey) {
        this.session_key = sessionKey;
        if (getDebug()) System.out.println("getSessionKey() == " + getSessionKey());
    }
    
    /**
     * @return the session key
     */
    @Basic
    public String getSessionKey() {
        return session_key;
    }

    @Model
    private void setDebug(boolean debug) {
        this.debug = debug;
        if (getDebug()) System.out.println("getDebug() == " + getDebug());
    }
    
    @Basic
    public boolean getDebug() {
        return this.debug;
    }
    
    /**
     * @param user 
     */
    @Model
    private void setUsername(String username) {
        this.username = username;
        if (getDebug()) System.out.println("getUsername() == " + getUsername());
    }
    
    /**
     * @return the username of the active user.
     */
    @Basic
    public String getUsername() {
        return username;
    }
    
    @Basic
    public User getActiveUser() {
        return activeUser;
    }
    
    /**
     * @param activeUser 
     */
    @Model
    private void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
        if (getDebug()) System.out.println("getActiveUser() == " + getActiveUser().getName());
    }
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType(CONTENT_TYPE);
        try {
            this.setUsername(request.getParameter("user"));
            this.setSessionKey(request.getParameter("key"));
            Caller.getInstance().setUserAgent("tst");
            Caller.getInstance().setDebugMode(true);
            this.setDebug(true);
            this.setActiveUser(User.getInfo(getUsername(), API_KEY));
            out.println(collectData(Session.createSession(API_KEY, API_SECRET, getSessionKey())));
        } finally {
            out.close();
        }
    }
    
    /**
     * <p>The structure of the JSON String :</p>
     * <pre>
     * {
     *      "items" :
     *      [
     *          {
     *              "name"  : "item.SOME_ARTIST",
     *              "edges" :
     *              [
     *                  "item.SOME_ARTIST.user.SOME_USER",
     *                  ...
     *              ],
     *              "owners" :
     *              [
     *                  "SOME_USER",
     *                  ...
     *              ],
     *              "description" : "SOME_DESCRIPTION"
     *          },
     *          ...
     *      ],
     *      users :
     *      [
     *          {
     *              "name"      : "SOME_USER",
     *              "active"    : BOOLEAN
     *          },
     *          ...
     *      ]
     * }
     * </pre>
     * @param data  : the data structure containing relevant artists and users
     * @param users : the list of users involved
     * @return 
     */
    @Model
    private String toJSON(Map<Artist, List<User>> artistData, Map<User, List<Artist>> userData) {
        JSONObject JSON = new JSONObject();
        Set<Artist> artists = artistData.keySet();
        
        JSON.put("items", new JSONArray());
        
        for (Artist a : artists) {
            boolean isRecommendation = true;
            JSONObject artist = new JSONObject();
            artist.put("name", "item." + encodeName(a.getName()));
            artist.put("edges", new JSONArray());
            artist.put("owners", new JSONArray());
            //artist.put("description", a.getWikiSummary());
            artist.put("description", "NA");
            for (User u : artistData.get(a)) {
                if (u.getName().equalsIgnoreCase(getUsername())) {
                    isRecommendation = false;
                }
                artist.append("edges", "item." + encodeName(a.getName()) + ".user." + encodeName(u.getName()));
                artist.append("owners", encodeName(u.getName()));
            }
            artist.put("recommendation", isRecommendation);
            JSON.append("items", artist);
        }
        
        JSON.put("users", new JSONArray());
        
        Set<User> users = userData.keySet();
        for (User u : users) {
            JSONObject user = new JSONObject();
            user.put("name", encodeName(u.getName()));
            user.put("active", u.getName().equalsIgnoreCase(getUsername()));
            user.put("owned", new JSONArray());
            for (Artist a : artists) {
                user.append("owned", "item." + encodeName(a.getName()));
            }
            JSON.append("users", user);
        }
        
        return JSON.toString();
    }
    
    /**
     * <p>Collect data uses the lastfm-java library to construct a datastructure
     * to generate the JSON file from.</p>
     * <p>Algorithm used :</p>
     * <pre>
     * recommendations	= user.getRecommendedArtists (active_user)
     * neighbours = user.getNeighbours (active_user)
     * 
     * FOR EACH recommendation IN recommendations
     *      similar_artists = artist.getSimilar(recommendation)
     *      list.push(similar_artists) 
     *      list.push(recommendation)
     *      FOR EACH neighbour IN neighbours
     *          IF tasteometer.compare (neighbour, list) > threshold THEN
     *              data[recommendation].push (neighbour)
     *          END-IF
     *      END-FOR
     * END-FOR
     * </pre>
     */
    @Model
    private String collectData(Session session) {
        // Output
        Map<Artist, List<User>> artistData = new HashMap<Artist, List<User>>();
        Map<User, List<Artist>> userData = new HashMap<User, List<Artist>>();
        
        // Get all relevant artists : "my" top artists and recommended artists
        PaginatedResult<Artist> paginatedResult = User.getRecommendedArtists(session);
        Object[] pageResults = paginatedResult.getPageResults().toArray();
        Collection<Artist> recommendations = new ArrayList<Artist>(LIMIT_RECOMMENDED_ARTISTS);
        for (int i = 0; i < LIMIT_RECOMMENDED_ARTISTS; i++) {
            recommendations.add((Artist) pageResults[i]);
        }
        System.out.println("recommendations.size() == " + recommendations.size());
        Collection<Artist> topartists =  User.getTopArtists(getActiveUser().getName(), LIMIT_TOP_ARTISTS, API_KEY);
        System.out.println("topartists.size() == " + topartists.size());
        List<Artist> artists = new ArrayList<Artist>();
        artists.addAll(topartists);
        artists.addAll(recommendations);
        
        // Get all relevant users : "my" profile and neighbouring profiles
        Collection<User> neighbours = User.getNeighbours(getUsername(), LIMIT_NEIGHBOURS, API_KEY);
        List<User> users = new ArrayList<User>(neighbours);
        users.add(getActiveUser());
        System.out.println("users.size() == " + users.size());
        
        // Initialize further
        for (Artist artist : artists) {
            artistData.put(artist, new ArrayList<User>());
        }
        for (User user : users) {
            userData.put(user, new ArrayList<Artist>());
        }
        
        // Current state of affairs :
        if (getDebug()) {
            String out = "\nartists : [";
            for (Artist artist : artists) {
                out += artist.getName() + ", ";
            }
            out += "];\n";
            out += "users : [ ";
            for (User user : users) {
                out += user.getName() + ", ";
            }
            out += "];";
            System.out.println(out);
        }
        
        for (Artist artist : artists) {
            for (User user : users) {
                try {
                    Tasteometer.ComparisonResult result = Tasteometer.compare
                            (Tasteometer.InputType.USER, user.getName(),
                            Tasteometer.InputType.ARTISTS, artist.getName(),
                            API_KEY);
                    if (result.getScore() > THRESHOLD) { 
                        artistData.get(artist).add(user);
                        userData.get(user).add(artist);
                    }
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + " : {msg : ["
                            + e.getLocalizedMessage() + "], data : [{usr : "
                            + user.getName() + "}, {artist : "
                            + artist.getName() + "}]}");
                }
            }
        }
        
        return toJSON(artistData, userData);
    }
    
    /**
     * @param artists
     * @return 
     */
    @Model
    private String createArtistString(List<Artist> artists) {
        StringBuilder stringBuilder = new StringBuilder();
        int N = artists.size() - 1;
        for (int i = 0; i < N; i++) {
            stringBuilder.append(artists.get(i).getName());
            stringBuilder.append(",");
        }
        return stringBuilder.toString() + artists.get(N).getName();
    }
    
    private String encodeName(String name) {
        return name.replaceAll(" ", "_");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
