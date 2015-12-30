package game;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import engine.PersistentDictionary;
import game.VerbosityGame.VerbosityPlayerStatus.ROLE;


/**
 * The verbosity game 
 * @author talm
 *
 */
public class VerbosityGame extends AbstractGame {
	
	public static class VerbosityPlayerStatus extends PlayerStatus {
		/**
		 * The player's role in the game.
		 * @author talm
		 *
		 */
		public enum ROLE {
			DESCRIBER,
			GUESSER
		}
		
		public ROLE role; 
	}
	
	/**
	 * The secret word that must be guessed.
	 */
	String secretWord;
	
	/**
	 * The set of words used to describe the secret word, with counters.
	 * The map maps each description word to the corresponding WordDescription structure.
	 */
	Map<String,WordDescription> descriptions;
	
	/**
	 * The maximum number of different players that can use a descriptor before it becomes taboo.
	 */
	int tabooThreshold;
	
	/**
	 * The set of description words it is no longer permissible to use.
	 * We keep a separate set for this to make lookup quick.
	 */
	Set<String> tabooWords;
	
	/**
	 * The dictionary containing a mapping from words to the word descriptions.
	 */
	PersistentDictionary dict;
	
	/**
	 * The set of active players that are currently in a describer role.
	 */
	Set<VerbosityPlayerStatus> describers;
	
	/**
	 * The player whose turn it is to play next.
	 */
	Player nextPlayer;
	
	/**
	 * Constructor
	 * @param tabooThreshold
	 * @param secretWord
	 * @param dict
	 * @throws IOException
	 */
	public VerbosityGame(int tabooThreshold, String secretWord, PersistentDictionary dict) throws IOException {
		// super(); // No need to call explicitly, this is done by default
		this.secretWord = secretWord;
		this.tabooThreshold = tabooThreshold;
		this.dict = dict;
		this.nextPlayer = null;
		describers = new HashSet<VerbosityGame.VerbosityPlayerStatus>();
		tabooWords = new HashSet<String>();
		descriptions = new HashMap<String, WordDescription>();
		dict.open();
		
		String descriptionString = dict.get(secretWord);
		
		if (descriptionString != null && descriptionString.length() > 0)
			deserializeDescription(descriptionString);
		
		// Go over all describing words and add to the taboo list those
		// that have been used more than the threshold.
		for (WordDescription desc : descriptions.values()) {
			if (desc.count > tabooThreshold)
				tabooWords.add(desc.word);
		}
	}
	
	@Override
	public VerbosityPlayerStatus newPlayerStatus() {
		return new VerbosityPlayerStatus();
	}
	
	@Override
	public VerbosityPlayerStatus getPlayerStatus(Player player) {
		return (VerbosityPlayerStatus) super.getPlayerStatus(player);
	}
	
	@Override
	public VerbosityPlayerStatus addPlayer(Player player) {
		VerbosityPlayerStatus info = (VerbosityPlayerStatus) super.addPlayer(player);
		// The first player is the describer, the other players 
		// are guessers.
		if (describers.size() > 0) {
			info.role = ROLE.GUESSER;
		} else {
			info.role = ROLE.DESCRIBER;
			describers.add(info);
		}
		return info;
	}
	
	
	/**
	 * Skip to the next active player.
	 */
	protected void nextPlayer() {
		if (nextPlayer == null) {
			nextPlayer = activePlayers.get(0);
			return;
		}
		
		Iterator<Player> it = activePlayers.iterator();
		while (it.hasNext() && it.next() != nextPlayer)
			// iterate until we reach player or the end of the list
			;
		
		if (it.hasNext())
			nextPlayer = it.next();
		else // we reached the end of the list, we'll take the first player
			nextPlayer = activePlayers.get(0);
	}
	

	@Override
	protected PlayerStatus abortPlayer(Player player) {
		if (player == nextPlayer) {
			// the next player aborted, so we skip to the next active player
			nextPlayer();
		}
		PlayerStatus status = super.abortPlayer(player); 
		if (activePlayers.size() < 2)
			// We don't allow 1-player games
			gameEnded = true;
		
		return status; 
	}
	

	/**
	 * Execute a move in the game.
	 * <p>Only the player whose turn it is should be allowed to play. The method should return error messages (directed at the 
	 * player who made the move) in the following cases:
	 * <ul>
	 * <li>If the game has already ended, the status update should contain the message 
	 * 		"The game has already ended!" 
	 * <li>If the player is invalid (never joined the game), the status update should contain the message 
	 * 		"You have not yet joined the game!" 
	 * <li>If the player is inactive (joined the game but then aborted), the status update should contain the message 
	 * 		"You are no longer an active player!".
	 * <li>If the player is active but not the next player, the status update should contain the message 
	 * 		"You are not the next player!".
	 * </ul>
	 * In all these cases, the next player should not change.</p>
	 * <p>Active Players can be either GUESSERs or DESCRIBERs; the response to their move depends on the role of the player:
	 * <ul>
	 * <li>DESCRIBER: A describer may play any non-taboo word except for the secret word itself 
	 * (a word is taboo if it is in the set {@link #tabooWords}). 
	 * <p>If the describer attempts to play an invalid word <i>xxxx</i>, the StatusUpdate should contain a private message to
	 * that describer with the contents: "<i>xxxx</i> is a taboo word!"; If she attempts to play the secret word,
	 * the StatusUpdate should contain a private message to that describer with the contents: 
	 * "You may not play the secret word itself!". These errors should not count as a turn (the player will remain
	 * the next player).</p>
	 * 
	 * <p>When the describer plays a word, the {@link #descriptions} map should be updated: if this word is not in the map, it should
	 * be added with a count of 1. If it is already in the map, the count should be incremented.</p>
	 * <li>GUESSER: A guesser may play any word. If the guesser did not guess the secret word, the guesser should not receive any 
	 * messages in the status update. Everyone else should receive the message:
	 * <i>playerName</i> guessed <i>xxxx</i>, where <i>playerName</i> is the player's name and <i>xxxx</i> is the word.
	 * <p>If the guesser did guess the secret word, the guesser and all current describers should get add a point to their score.
	 * The guesser then becomes a describer. In this case, the guesser should receive the message "You are correct!".  
	 * 		<ul>
	 * 			<li>If there are no more guessers left, the game ends. In this case the guesser should receive the message
	 * 				"You were the last GUESSER. The game is now over." The remaining active players should receive the message
	 * 				"Player <i>playerName</i>" has guessed correctly and has ended the game.".
	 * 			<li>If there is at least one other guesser left, the current player should receive the message:
	 * 				"You have now become a describer!". In addition, the current player should receive information about
	 * 				the list of taboo words (use the method {@link #addDescriberInfo(StatusUpdate, VerbosityPlayerStatus)}).
	 * 				 The remaining active players should receive the message
	 * 				"Player <i>playerName</i> has guessed correctly and is now a DESCRIBER.".
	 * 		</ul>
	 * </ul>
	 * 
	 * <p>If a valid move was made by a player, that completes their turn (the next active player is now eligible to move).
	 * </p>
	 * <p>Note: words are <b>case-insensitive</b>: if the secret word is "dog", then a describer is not allowed to play "DOG".</p> 
	 *  
	 */
	@Override
	public StatusUpdate playerMove(Player player, String word, long time) {
	
		// Creates a Status Update to be returned.
		StatusUpdate status = new StatusUpdate();
		
		// Deals with the case a player tries to play when the game is ended
		if (this.gameEnded) {
			status.addMessage(player, "The game has already ended!");
			return status;
		}
		
		// Deals with the case a player try to play when he hasn't joined the game
		boolean playerIsValid = false;
		for (Player validPlayer : this.getPlayers()) {
			if (validPlayer == player) {
				playerIsValid = true;
				break;
			}
		}
		if (!playerIsValid) {
			status.addMessage(player, "You have not yet joined the game!");
			return status;
		}
		
		// Deals with the case a player try to play when he's not an active player
		boolean playerIsActive = false;
		for (Player activePlayer : activePlayers) {
			if (activePlayer == player) {
				playerIsActive = true;
				break;
			}
		}
		if (!playerIsActive) {
			status.addMessage(player, "You are no longer an active player!");
			return status;
		}
		
		// Deals with the case a player try to play when he's not the next player
		if (getNextPlayers().iterator().next() != player) {
			status.addMessage(player, "You are not the next player!");
			return status;
		}
			
		// Deals with the case the player is a describer
		if (this.getPlayerStatus(player).role == ROLE.DESCRIBER) {
			
			// Keeps track if the word is valid
			Boolean validWord = true;
			
			// In case the word is part of the taboo words
			for (String tabooWord : tabooWords) {
				if (word.equalsIgnoreCase(tabooWord)) {
					status.addMessage(player, word + " is a taboo word!");
					validWord = false;
				}
			}
			
			// In case the word is the secret word
			if (word.equalsIgnoreCase(secretWord)) {
				status.addMessage(player, "You may not play the secret word itself!");
				validWord = false;
			}
			
			// If the word is already in the descriptions, count +1
			if (descriptions.containsKey(word)) {
				descriptions.get(word).count++;
			
			// Otherwise add it to the descriptions with count 1
			} else {
				descriptions.put(word, new WordDescription(word, 1));
			}
			
			// Check if count is over taboo trash hold and if it does, put it in tabooWords
			if (descriptions.get(word).count > tabooThreshold) {
				tabooWords.add(word);
			}
			
			// In case the word is valid
			if (validWord) {
				status.addMessage(player, "");
				status.addMessage("Player " + player.getName() + " hints: " + word);
				
				// change the turn to the next player
				nextPlayer();
			}
		}

		// Deals with the case the player is a guesser
		if (this.getPlayerStatus(player).role == ROLE.GUESSER) {
			
			// In case the guess was wrong
			if (!word.equalsIgnoreCase(secretWord)) {
				status.addMessage(player.getName() + " guessed " + word);
				status.addMessage(player, "");
				nextPlayer();
			
			// In case the guess was true
			} else {
				
				// Give points to guesser and describers
				getPlayerStatus(player).score++;
				for (VerbosityPlayerStatus describer : describers) {
					describer.score++;
				}
				
				// Change the guesser to a describer
				getPlayerStatus(player).role = ROLE.DESCRIBER;
				this.describers.add(this.getPlayerStatus(player));
				
				// Give the player the winning message
				status.addMessage(player, "You are correct!");
								
				// checks if there a remaining guessers
				boolean remainGuessers = false;
				
				// Goes over the active player list and check if there is a guesser among them
				for (Player remainPlayer : this.getActivePlayers()) {
					if (getPlayerStatus(remainPlayer).role == ROLE.GUESSER) {
						remainGuessers = true;
						break;
					}
				}
				
				// In case there are no guessers left, give the following messages and end game
				if (!remainGuessers) {
					status.addMessage(player, "You were the last GUESSER. The game is now over.");
					status.addMessage("Player " + player.getName() + 
							" has guessed correctly and has ended the game.");
					this.gameEnded = true;
				}
				
				// In case there are guessers left, give the following message
				else if (remainGuessers) {
					status.addMessage(player, "You have now become a describer!");
					
					// Show the taboo words to the player
					addDescriberInfo(status, getPlayerStatus(player));
					
					// Message all the players about the guess
					status.addMessage("Player " + player.getName() + 
							" has guessed correctly and is now a DESCRIBER.");
					nextPlayer();
				}
			}
		}
		return status;
	}

	@Override
	public StatusUpdate playerJoin(Player player, long time) {
		VerbosityPlayerStatus status = addPlayer(player);
		
		StringBuilder defaultMsg = new StringBuilder("Player " + player.getName() + " has joined as a " + status.role);
		
		StatusUpdate update = new StatusUpdate(defaultMsg.toString());

		update.addMessage(player, "Welcome " + player.getName() + "! You are in the " + status.role + " role.");
		if (status.role == ROLE.DESCRIBER) {
			addDescriberInfo(update, status);
		} else {
			addGuesserInfo(update, status);
		}
		
		if (activePlayers.size() < 2) {
			// This is the only player
			update.addMessage(player, "You are the first player.");
		} 
		
		for (Player otherPlayer : activePlayers) {
			if (otherPlayer != player) {
				VerbosityPlayerStatus otherStatus = getPlayerStatus(otherPlayer);
				update.addMessage(player,"Player " + otherStatus.player.getName() + " is a " + otherStatus.role);
			}
		}
		
		return update;
	}

	@Override
	public StatusUpdate playerAbort(Player player, long time) {
		VerbosityPlayerStatus info = (VerbosityPlayerStatus) abortPlayer(player);
		StatusUpdate update = new StatusUpdate("Player " + player.getName() + " has aborted!");

		if (info != null) {
			if (info.role == ROLE.DESCRIBER) {
				// We need to remove it from the describers set
				describers.remove(info);
				if (describers.size() < 1 && !hasEnded()) {
					// No describers are left - we need to turn a guesser into a describer.
					// Since the game hasn't ended yet, we know there are at least 2 active players
					Player firstPlayer = activePlayers.get(0);
					VerbosityPlayerStatus firstPlayerStatus = getPlayerStatus(firstPlayer);
					
					firstPlayerStatus.role = ROLE.DESCRIBER;
					describers.add(firstPlayerStatus);
					update.addMessage(firstPlayer, "Player " + player.getName() + 
							" has aborted and was the last " + info.role);
					update.addMessage(firstPlayer, "You have been selected to become a " + firstPlayerStatus.role);
					addDescriberInfo(update, firstPlayerStatus);
				}
			} else {
				// info.role == ROLE.GUESSER
				if (describers.size() == activePlayers.size()) {
					// All active players are describers -- the game can no longer continue.
					gameEnded = true;
					update.addMessage("The last " + info.role + " has aborted!"); 
				}
			}
			if (hasEnded()) {
				// Game is over
				update.addMessage("Game over!");
			}
		}
		
		return update;
	}

	/**
	 * Add messages with the information a describer needs to know to the StatusUpdate. 
	 * This information should be given to a new describer.
	 * @param update The StatusUpdate object
	 * @param info the info about the player who has the describer role
	 * @return update
	 */
	StatusUpdate addDescriberInfo(StatusUpdate update, VerbosityPlayerStatus info) {
		update.addMessage(info.player, "The secret word you must describe is: " + secretWord);
		if (!tabooWords.isEmpty()) {
			StringBuilder taboos = new StringBuilder("The following list of words may not be used: " );
			Iterator<String> tabooIt = tabooWords.iterator();
			
			taboos.append(tabooIt.next());
			while (tabooIt.hasNext()) {
				taboos.append(',');
				taboos.append(tabooIt.next());
			}
			update.addMessage(info.player, taboos.toString());
		}
		return update;
	}
	
	/**
	 * Add messages with the information a guesser needs to know to the StatusUpdate. 
	 * @param update The StatusUpdate object
	 * @param info the info about the player who has the guesser role
	 * @return update
	 * @return
	 */
	StatusUpdate addGuesserInfo(StatusUpdate update, VerbosityPlayerStatus info) {
		// Currently do nothing
		return update;
	}

	
	
	/**
	 * Serialize the descriptions into a single string of the form "word|counter,word|counter,...".
	 * @return
	 */
	String serializeDescriptions() {
		StringBuilder str = new StringBuilder();
		
		for (WordDescription desc : descriptions.values()) {
			str.append(desc.word);
			str.append('|');
			str.append(Integer.toString(desc.count));
			str.append(',');
		}
		return str.toString();
	}
	
	/**
	 * Parse a description string (as stored in the dictionary) to a list of 
	 * word:counter pairs and generate the corresponding {@link WordDescription} structures.
	 * Look at the {@link #serializeDescriptions()} method to see the precise format. This method
	 * should do the exact opposite of  {@link #serializeDescriptions()}: given a string that was
	 * generated by the serialize method, it should create the same map that existed in {@link #descriptions}.
	 *   
	 * @param description The format of this string is "word|counter,word|counter,..."
	 */
	void deserializeDescription(String description) {
		
		// The strings which will be added to the map later.
		StringBuilder tempWord = new StringBuilder();
		StringBuilder tempCounter = new StringBuilder();
		
		// Go over the whole string
		for (int i = 0; i < description.length(); i++) {
			
			// Store the current character
			char current = description.charAt(i);
			
			// As long it isn't the end of the word, append the char to the string
			if (current != '|' && current != ',') {
				tempWord.append(current);
				
			// If it's the end of the word start appending the numbers
			} else if (current == '|') {
				i++;
				current = description.charAt(i);
				
				// If the number isn't over yet, append the digit to the counter string 
				while ((current != ',') && (i < description.length()))  {
					tempCounter.append(current);
					i++;
					if (i < description.length()) {
						current = description.charAt(i);
					}
				}
				
				// after the two strings were created, add them to a WordDescrription
				// and put it in the descriptions map
				descriptions.put(tempWord.toString(), 
						new WordDescription(tempWord.toString(), Integer.parseInt(tempCounter.toString())));
				
				// delete the temporary strings so they'll be ready for the next ones
				tempWord.delete(0, tempWord.length());
				tempCounter.delete(0, tempCounter.length());
			}
		}
	}

	/**
	 * Save persistent game state and close.
	 */
	public void close() throws IOException {
		String descriptionString = serializeDescriptions();
		dict.put(secretWord, descriptionString);
		dict.close();
	}

	@Override
	public Collection<Player> getNextPlayers() {
		if (nextPlayer == null)
			nextPlayer();
		
		// Return a collection that contains only a single element.
		// We use the singleton method to avoid creating a new object every time.
		return Collections.singleton(nextPlayer);
	}
	
}
