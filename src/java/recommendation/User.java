package recommendation;

/**
   @author Jesse Davis
   Class that holds information about users. It contains a list of movies and rating assigned to them for a user. 
   (c) Jesse Davis, Feb 15, 2013

*/
import java.util.*;
public class User{

    //The user id
    private int userId;
    //A map of movie ids - ratings
    private HashMap<Integer,Integer> movies;
    //The average rating this user assigned to movies
    private double averageRating = 0;
    //Running total so that the average can easily modified when a new movie is added
    private double runningTotal = 0;

    /**
       @param userId the user's id
     */
    public User(int userId){
	this.userId = userId;
	movies = new HashMap<Integer,Integer>();
    }//end of constructor User

    /**
       Adds a rating for a movie to this collection. Note: It does not overwrite a previously assigned rating.  
      
       @param movieId is the id of the rated movie
       @param rating is the rating assigned to the movie
       @return true if this movie has not been previously rated, false otherwise
     */
    public boolean addMovie(int movieId, int rating){
	boolean success = false;
	if (!movies.containsKey(movieId)){
	    movies.put(movieId,rating);
	    success = true;
	    runningTotal += rating; 
	    averageRating = runningTotal / movies.size();;
	}
	return success;
    }//end addMovie
    
    /**
       @return the user's id
     */
    public int getUserId(){
	return userId;
    }//end getUserId

    /**
       @return the average ratings of this user
    */
    public double getAvgRating(){
	return averageRating;
    }//end getAvgRating

    /**
       @return A map of movies and rating assigned to them for this user. 
     */
    public HashMap<Integer,Integer> getMoviesRated(){
	return movies;
    }//end getMoviesRated

    /**
       @return the number of movies this user has rated
     */
    public int numMoviesRated(){
	return movies.size();
    }//end numMoviesRated
}