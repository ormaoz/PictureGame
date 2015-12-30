package game;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implments an abstract multi-player word game.
 * In a word game, a player's move consists of sending a single word. Moves are never simultaneous.
 * Games can be cooperative or competitive.  
 * 
 * This class doesn't implement communication between players; instead it defines a "action" methods: 
 * {@link #playerJoin(Player, long)}, {@link #playerMove(Player, String, long)} and {@link #playerAbort(Player, long)} 
 * that an external class will call to signify a player has taken an action.
 * 
 * @author talm
 *
 */
public abstract class AbstractGame implements Game {
	
	/**
	 * Players participating in the game.
	 * Map each player to their corresponding status. 
	 */
	Map<Player,PlayerStatus> players;
	
	/**
	 * Players that remain active.
	 */
	List<Player> activePlayers;

	/**
	 * has the game ended?
	 */
	boolean gameEnded;
	
	/**
	 * Default Constructor.
	 */
	AbstractGame() {
		players = new LinkedHashMap<Player, PlayerStatus>();
		activePlayers = new LinkedList<Player>();
		gameEnded = false;
	}
	
	/**
	 * This method is called to create a new {@link PlayerStatus} object. 
	 * We use a method rather than directly calling "new" so that subclasses of 
	 * {@link AbstractGame} can define subclasses of {@link PlayerStatus} by overriding
	 * this method.
	 * @return a new {@link PlayerStatus} object
	 */
	protected PlayerStatus newPlayerStatus() {
		return new PlayerStatus();
	}
	
	/**
	 * Add a new player to the game data structures. This is an internal method. 
	 * The external interface for adding new players is  
	 * @param player
	 * @return
	 */
	protected PlayerStatus addPlayer(Player player) {
		PlayerStatus status = newPlayerStatus();
		status.player = player;
		status.score = 0;
		status.active = true;
		status.numMoves = 0;
		
		players.put(player,status);
		activePlayers.add(player);
		return status;
	}
	
	/**
	 * Deal with an aborting player.
	 * @param player The aborting player. A null value indicates that all players have aborted. 
	 * @return the info of the aborting player (null if not found or if all players have aborted).
	 */
	protected PlayerStatus abortPlayer(Player player) {
		if (player == null) {
			// All players have aborted.
			for (Player activePlayer : activePlayers) {
				PlayerStatus status = players.get(activePlayer);
				status.active = false;
			}
			activePlayers.clear();
			gameEnded = true;
			return null;
		} else {
			PlayerStatus status = players.get(player);
			if (status != null) {
				// this is a valid player
				status.active = false;
				activePlayers.remove(player);
				if (activePlayers.size() == 0)
					gameEnded = true;
			}
			return status;
		}
	}

	
	/**
	 * A new player has joined the game.
	 * This should increase by 1 the number of players in the game.
	 * @param player
	 * @param time
	 * @return
	 */
	public abstract StatusUpdate playerJoin(Player player, long time);
	
	/**
	 * A move by one of the players. 
	 * @param player The player who made the move
	 * @param word The word played
	 * @param time The time at which the move was made (in milliseconds from Jan 1, 1970).
	 * @return A status update (can be aimed at specific players or at all of them).
	 */
	public abstract StatusUpdate playerMove(Player player, String word, long time);

	
	/**
	 * A player has aborted.  
	 * This can happen due to network failure, client crashes, etc.
	 * If an active player aborts, the number of players in the game should decrease by 1.
	 * If all players have aborted, the game should end.
	 * @param player the aborting player. null means all players aborted simultaneously.
	 * @return
	 */
	public abstract StatusUpdate playerAbort(Player player, long time);
	

	@Override
	public boolean hasEnded() {
		// We consider the game ended if there is only a single player remaining (or none).
		// subclasses should add additional conditions.
		return gameEnded;
	}

	
	/**
	 * Get the status of a player. 
	 * @return a set of PlayerInfo
	 */
	public PlayerStatus getPlayerStatus(Player player) {
		return players.get(player);
	}

	@Override
	public Collection<Player> getActivePlayers() {
		return activePlayers;
	}
	
	@Override
	public Collection<Player> getPlayers() {
		return players.keySet();
	}

	/**
	 * Return the number of players currently in the game.
	 * @return
	 */
	@Override
	public int getNumPlayers() {
		return players.size();
	}
	
	@Override
	public int getNumActivePlayers() {
		return activePlayers.size();
	}
}
