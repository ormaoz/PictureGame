package game;

/**
 * A class holding status information about a player in regards to a specific game. 
 * Concrete game implementations may create a subclass of PlayerStatus. In this case, they
 * should override the {@link AbstractGame#newPlayerStatus()} method. 
 * @author talm
 *
 */
public class PlayerStatus {
	
	public Player player;
	
	/**
	 * The player's current score.
	 */
	public int	   score;
	
	/**
	 * Is the player currently active?
	 */
	public boolean active;
	
	/**
	 * The number of moves made so far by this player in this game.
	 */
	public int numMoves;

	@Override
	public int hashCode() {
		return player.hashCode();
	}
	
	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof PlayerStatus))
			return false;
		return player.equals(((PlayerStatus)o).player);
	}
}