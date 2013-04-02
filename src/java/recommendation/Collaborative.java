/**
 * @author Jesse Davis Simple skeleton class for building a collaborative
 * filtering system based on the Pearson correlation. (c) Jesse Davis, Feb 15,
 * 2013
 *
 */
package recommendation;

import java.util.*;
import java.io.*;

public class Collaborative {

    // Stores for each movies, the users that rated it
    private HashMap<Integer, HashSet<Integer>> movieUserMap;
    // For each movie, gives its title
    private HashMap<Integer, String> movieIdToName;
    // pearson correlation between each user in the DB
    private double[][] pearson;
    // Information about each user in the database
    private User[] users;
    // The number of users in the database
    // Note that numUsers < users.length
    private int numUsers = 0;

    /**
     * @param initSize is the initial user capacity of the system
     */
    public Collaborative(int initSize) {
        // Initial capacity of the
        pearson = new double[initSize][initSize];
        for (int ix = 0; ix < pearson.length - 1; ix++) {
            for (int jx = ix; jx < pearson.length; jx++) {
                pearson[ix][jx] = 0;
                pearson[jx][ix] = 0;
            }
        }
        users = new User[initSize];
        for (int ix = 0; ix < initSize; ix++) {
            users[ix] = null;
        }
        movieIdToName = new HashMap<Integer, String>();
        movieUserMap = new HashMap<Integer, HashSet<Integer>>();
    }

    /**
     * If the user capacity is exceed, double it
     */
    private void doubleSizes() {
        int initSize = users.length * 2;
        double[][] nPearson = new double[initSize][initSize];
        User[] nUsers = new User[initSize];
        System.arraycopy(users, 0, nUsers, 0, users.length);
        for (int ix = 0; ix < pearson.length; ix++) {
            System.arraycopy(pearson[ix], 0, nPearson[ix], 0, pearson.length);
        }
        pearson = nPearson;
        users = nUsers;
    }// end of constructor Collaborative

    /**
     * Reads in information that maps movie ids to movie names
     *
     * @param name of the item information file
     */
    public void readMovieInfo(String name) throws IOException {
        RandomAccessFile in = new RandomAccessFile(name, "r");
        String line = in.readLine();
        while (line != null) {
            String[] split = line.split("\\|");
            // System.out.println(line + "<>" + split[0]);
            int uid = Integer.parseInt(split[0]);
            movieIdToName.put(uid, split[1]);
            line = in.readLine();
        }
        in.close();
    }// end readMovieInfo

    /**
     * Reads in the data of user movie ratings triples
     *
     * @param name of the database file
     */
    public void readData(String name) throws IOException {
        RandomAccessFile in = new RandomAccessFile(name, "r");
        String line = in.readLine();
        while (line != null) {
            String[] split = line.split("\t");
            int uid = Integer.parseInt(split[0]);
            int mid = Integer.parseInt(split[1]);
            int rating = Integer.parseInt(split[2]);
            if (uid > users.length) {
                doubleSizes();
            }
            if (users[uid - 1] == null) {
                users[uid - 1] = new User(uid);
            }
            users[uid - 1].addMovie(mid, rating);
            if (uid > numUsers) {
                numUsers = uid;
            }
            if (!movieUserMap.containsKey(mid)) {
                HashSet<Integer> hs = new HashSet<Integer>();
                hs.add(uid);
                movieUserMap.put(mid, hs);
            } else {
                HashSet<Integer> hs = movieUserMap.get(mid);
                hs.add(uid);
            }
            line = in.readLine();
        }
        in.close();
    }// end readData

    /**
     * Reads in the data for the active user. This consists of movie_id, ratings
     * doubles.
     *
     * @param name of the active user file
     */
    public User readActive(String name) throws IOException {
        User active = new User(numUsers + 1);
        RandomAccessFile in = new RandomAccessFile(name, "r");
        String line = in.readLine();
        while (line != null) {
            line.trim();
            String[] split = line.split("\t");
            // System.out.println(line + "<>" + split[0] + "<>" + split[1]);
            int mid = Integer.parseInt(split[0]);
            int rating = Integer.parseInt(split[1]);
            if (movieIdToName.containsKey(mid)) {
                active.addMovie(mid, rating);
            } else {
                System.out.println("Invalid movie id: " + mid);
            }
            line = in.readLine();
        }
        in.close();
        return active;
    }// end readAcitve

    /**
     * Reads in a list of movies for which predictions will be made
     *
     * @param name of the active user file
     */
    public int[] readMoviesToPredict(String name) throws IOException {
        LinkedList<Integer> list = new LinkedList<Integer>();
        RandomAccessFile in = new RandomAccessFile(name, "r");
        String line = in.readLine();
        while (line != null) {
            int mid = Integer.parseInt(line);
            list.add(mid);
            line = in.readLine();
        }
        in.close();
        int[] movies = new int[list.size()];
        for (int ix = 0; ix < movies.length; ix++) {
            movies[ix] = list.removeFirst();
        }
        return movies;
    }// end readMoviesToPredict

    /**
     * This method predicts a rating for each movieId in movies for a specific
     * user (active).
     *
     * @param active the user for who we will make predictions
     * @param movies list of movies for which to make a predictioin
     */
    public void predictMultipleMovies(User active, int[] movies) {
        double[] w = new double[numUsers];
        for (int ix = 0; ix < w.length; ix++) {
            w[ix] = pearson(active, users[ix]);
        }
        for (int ix = 0; ix < movies.length; ix++) {
            if (movieIdToName.containsKey(movies[ix])) {
                predictSingleMovie(active, movies[ix], w);
            } else {
                System.out.println("Invalid movie id: " + movies[ix]);
            }
        }
    }// end predictMultipleMovies

    /**
     * This method computes the pearson correlation between two users.
     *
     * @param u1 the first user
     * @param u2 the second user
     * @return pearson correlation between u1 and u2
     */
    public double pearson(User u1, User u2) {

        List<Integer> keysIntersection = new ArrayList<Integer>();

        for (Integer key1 : u1.getMoviesRated().keySet()) {
            for (Integer key2 : u2.getMoviesRated().keySet()) {
                if (key1.equals(key2)) {
                    keysIntersection.add(key1);
                }
            }
        }

        double sum_numerator = 0;
        double avg1 = u1.getAvgRating();
        double avg2 = u2.getAvgRating();

        for (Integer key : keysIntersection) {
            sum_numerator += (u1.getMoviesRated().get(key).doubleValue() - avg1)
                    * (u2.getMoviesRated().get(key).doubleValue() - avg2);
        }

        // DENOMINATOR
        double sum_denominator1 = 0;

        for (Integer key : keysIntersection) {
            sum_denominator1 = Math.pow((u1.getMoviesRated().get(key)
                    .doubleValue() - avg1), 2);
        }

        double sum_denominator2 = 0;
        for (Integer key : keysIntersection) {
            sum_denominator2 = Math.pow((u2.getMoviesRated().get(key)
                    .doubleValue() - avg2), 2);
        }

        double sum_total_denominator = Math.pow(sum_denominator1
                * sum_denominator2, 0.5);

        return sum_numerator / sum_total_denominator;
    }

    /**
     * This method predicts active's rating for movie
     *
     * @param active is the active user
     * @param movie is the movie id for which the prediction will be made
     * @param w is an array of the pearson correlations between active and each
     * other user in the database
     */
    public void predictSingleMovie(User active, int movie, double[] w) {
        double prediction = 0;
        double alpha = 0;

        for (int i = 0; i < w.length; i++) {
            alpha += Math.abs(w[i]);
        }
        alpha = 1 / alpha;

        List<User> userIntesection = new ArrayList<User>();

        for (User u : users) {
            if (!(u == null)) {
                for (Integer key1 : u.getMoviesRated().keySet()) {

                    if (key1.equals(movie)) {
                        userIntesection.add(u);
                    }

                }
            }
        }
        double avg = active.getAvgRating();
        double total = 0;
        for (User u : userIntesection) {
            total += (u.getMoviesRated().get(movie) - avg) * pearson(active, u);
        }
        total = active.getAvgRating() + (total * alpha);

        prediction = total;

        System.out.println("Predict " + prediction + " for "
                + movieIdToName.get(movie));
    }
}