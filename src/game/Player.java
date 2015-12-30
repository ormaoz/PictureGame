package game;

/**
 * Information about a player.
 * The information this class holds should be true regardless of the 
 * specific game (or games) the player is participating in.
 * 
 * @author talm
 *
 */
public interface Player {
	/**
	 * Get the Id of a player. 
	 * The player's Id should be unique (not shared with other players) and consistent across games.
	 * An email address would be a good example of an id.
	 * @return the player's id.
	 */
	public String getId();
	
	/**
	 * The player's name.
	 * The name can be used in the user interface to refer to the player. 
	 * Two players with the same name are allowed (as long as they have different Ids).
	 * 
	 * @return the name of the player.
	 */
	public String getName();
}
