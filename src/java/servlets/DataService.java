package servlets;

import be.kuleuven.cs.som.annotate.Model;
import lastfm.Artist;
import lastfm.Authenticator;
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
    
    public static final String API_KEY              = "828c109e6a54fffedad5177b194f7107";
    public static final String API_SECRET           = "7c2f09e6eb84e8a6183c59e0bc574f70";
    public static final String CONTENT_TYPE         = "application/json";
    public static final int LIMIT_NEIGHBOURS        = 10;
    public static final int LIMIT_SIMILAR_ARTISTS   = 5;
    public static final int THRESHOLD               = 0;
    
    private String session_key;
    private String user;
    
    /**
     * @param token 
     */
    @Model
    private void setSessionKey(String sessionKey) {
        this.session_key = sessionKey;
    }
    
    /**
     * @param user 
     */
    @Model
    private void setUser(String user) {
        this.user = user;
    }

    public String getSessionKey() {
        return session_key;
    }

    public String getUser() {
        return user;
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
        //this.setToken(request.getParameter("token"));
        this.setUser(request.getParameter("user"));
        this.setSessionKey(request.getParameter("key"));
        try {
            Caller.getInstance().setUserAgent("tst");
            Caller.getInstance().setDebugMode(true);
            Session session = Session.createSession(API_KEY, API_SECRET, getSessionKey());
            out.println(toStringDatastructure(collectData(session)));
        } finally {
            out.close();
        }
    }
    
    private String toStringDatastructure(Map<Artist, Collection<User>> data) {
        String string = "";
        Set<Artist> artists = data.keySet();
        string += "[\n";
        for (Artist a : artists) {
            string += "{\n";
            string += "\t\"name\" : " + a.getName() + ",\n";
            string += "\towners : [\n";
            for (User u : data.get(a)) {
                string += "\t\t" + u.getName() + ",\n";
            }
            string += "\n";
            string += "\n]";
            string += "\n},";
        }
        string += "]\n";
        return string;
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
    private Map<Artist, Collection<User>> collectData(Session session) {
        Map<Artist, Collection<User>> datastructure = new HashMap<Artist, Collection<User>>();
        
        PaginatedResult<Artist> recommendations = User.getRecommendedArtists(session);
        Collection<User> neighbours = User.getNeighbours(getUser(), LIMIT_NEIGHBOURS, API_KEY);
        for (Artist recommendation : recommendations) {
            Collection<Artist> similarArtists = Artist.getSimilar(recommendation.getName(), LIMIT_SIMILAR_ARTISTS, API_KEY);
            List<Artist> list = new ArrayList<Artist>(similarArtists);
            list.add(recommendation);
            for (User neighbour : neighbours) {
                Tasteometer.ComparisonResult result = Tasteometer.compare(Tasteometer.InputType.USER, neighbour.getName(),
                        Tasteometer.InputType.ARTISTS, createArtistString(list), API_KEY);
                if (result.getScore() > THRESHOLD) {
                    if (datastructure.get(recommendation) != null) { datastructure.put(recommendation, new ArrayList<User>()); }
                    datastructure.get(recommendation).add(neighbour);
                }
            }
        }
        
        return datastructure;
    }
    
    /**
     * @param artists
     * @return 
     */
    private String createArtistString(List<Artist> artists) {
        StringBuilder stringBuilder = new StringBuilder();
        int N = artists.size() - 1;
        for (int i = 0; i < N; i++) {
            stringBuilder.append(artists.get(i).getName());
            stringBuilder.append(",");
        }
        return stringBuilder.toString() + artists.get(N);
    }
    
    /**
     * 
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
     * @param data
     * @return 
     */
    private JSONObject createJSON(Map<Artist, Collection<User>> data) {
        JSONObject json = new JSONObject();
        
        
        
        return json;
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
