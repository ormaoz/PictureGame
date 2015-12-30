package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A status update for a game.
 * @author talm
 *
 */
public class StatusUpdate {
	/**
	 * The default messages to give to all players that have no specific messages.
	 * null means there is no default message.
	 */
	List<String> defaultMessages;
	
	/**
	 * Messages that are targeted at specific players.
	 */
	Map<Player,List<String>> playerMessages;


	/**
	 * Empty default constructor (no messages for anyone)
	 */
	public StatusUpdate() {

	}
	
	/**
	 * Construct a StatusUpdate that only contains a default message. 
	 * @param defaultMessage
	 */
	public StatusUpdate(String defaultMessage) {
		defaultMessages = new ArrayList<String>(1);
		defaultMessages.add(defaultMessage);
	}
	

	/**
	 * Return a new status for a player, or null if there is no new status. 
	 * @param player
	 * @return
	 */
	public List<String> getMessages(Player player) {
		if (playerMessages != null && playerMessages.containsKey(player))
			return playerMessages.get(player);
		else 
			return defaultMessages;
	}

	public Map<Player, List<String>> getSpecificMessages() {
		return playerMessages;
	}
	
	/**
	 * Get the default message.
	 * @return
	 */
	public List<String> getMessages() {
		return defaultMessages;
	}

	/**
	 * Set the default message.
	 * @param message
	 */
	public void addMessage(String message) {
		if (defaultMessages == null)
			defaultMessages = new ArrayList<String>(1);
		defaultMessages.add(message);
	}
	
	public void addMessage(Player player, String message) {
		if (playerMessages == null)
			playerMessages = new HashMap<Player, List<String>>();
		if (message == null) {
			// "empty" message
			playerMessages.put(player, null);
		} else {
			List<String> messages = playerMessages.get(player);
			if (messages == null) {
				messages = new ArrayList<String>(1);
				playerMessages.put(player, messages);
			}
			
			messages.add(message);
		}
	}
	
}
