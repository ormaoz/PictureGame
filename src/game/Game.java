package game;

import java.util.Collection;

public interface Game {
	/**
	 * A new player has joined the game.
	 * @param player the player who has joined the game
	 * @param time the time at which the player joined.
	 * @return A status update (can be aimed at specific players or at all of them).
	 */
	public StatusUpdate playerJoin(Player player, long time);
	
	/**
	 * A move by one of the players. 
	 * @param player The player who made the move
	 * @param word The word played
	 * @param time The time at which the move was made (in milliseconds from Jan 1, 1970).
	 * @return A status update (can be aimed at specific players or at all of them).
	 */
	public StatusUpdate playerMove(Player player, String word, long time);
	
	/**
	 * A player has aborted.  
	 * This can happen due to network failure, client crashes, etc.
	 * @param player the aborting player. null means all players aborted simultaneously.
	 * @param time the time at which the player aborted.
	 * @return A status update (can be aimed at specific players or at all of them).
	 */
	public StatusUpdate playerAbort(Player player, long time);
	
	/**
	 * Check if the game has ended.
	 * This should always be true if all players have aborted.
	 * @return true iff the game is over.
	 */
	public boolean hasEnded();
	
	
	/**
	 * Return the number of players currently in the game (including players that have aborted).
	 * @return the number of players currently in the game.
	 */
	public int getNumPlayers();
	
	/**
	 * Return a collection of the players in the game.
	 * @return a collection of all the players in the game.
	 */
	public Collection<Player> getPlayers();
	
	/**
	 * Return the number of <i>active</i> players in the game.
	 * A player is active if that player may still make moves.
	 * @return the number of active players in the game.
	 */
	public int getNumActivePlayers();
	
	/**
	 * Return a collection of the active players in the game.
	 * @return a collection of the active players in the game.
	 */
	public Collection<Player> getActivePlayers();
	
	/**
	 * Return a collection of players that are eligible to move.
	 * This can consist of a single player (if the game is turn-based)
	 * or multiple players (including the set of all players, if any player can
	 * play at any time).
	 * @return A collection of eligible players.
	 */
	public Collection<Player> getNextPlayers();

}
