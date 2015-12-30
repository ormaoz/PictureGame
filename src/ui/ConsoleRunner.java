package ui;

import game.Game;
import game.Player;
import game.StatusUpdate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConsoleRunner {
	Game game;
	
	public ConsoleRunner(Game game) {
		this.game = game;
	}

	public void runGame(Collection<StatusUpdate> initialUpdates) {
		for (StatusUpdate update : initialUpdates) {
			outputStatusMessages(update, System.out);
		}
		
		// Create a buffered reader from standard input stream so we can read entire lines.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (!game.hasEnded()) {
				Collection<Player> nextPlayers = game.getNextPlayers();
				// We only take the first player in the collection (since the console doesn't allow simultaneous play).
				Player player = nextPlayers.iterator().next(); 
				System.out.print("Next input for player " + player.getName() + "-> ");
				String word = in.readLine();
				if (word == null) {
					// input was closed
					throw new IOException("Input stream closed unexpectedly");
				}
				long time = System.currentTimeMillis();
				StatusUpdate newStatus = game.playerMove(player, word, time);
				outputStatusMessages(newStatus, System.out);
			}
		} catch (IOException e) {
			// Handle IO Exception. This should only happen when the standard input is closed, 
			// in which case we will declare the game over (all players have aborted).
			
			game.playerAbort(null, System.currentTimeMillis()); 
		}
	}

	/**
	 * Output a status update.
	 * The expected format is as follows:
	 * First, if there are any player-specific messages then the method should output, for each player:
	 * <pre>
	 * ** Messages for: playername **
	 * </pre>
	 * and then print each message on a separate line.
	 * Default messages should then be printed with the header:
	 * <pre>
	 * ** Messages for everyone else **
	 * </pre>
	 * followed by each message on a separate line.
	 * 
	 * @param status the update to be output
	 * @param out the stream to which output should be written
	 */
	void outputStatusMessages(StatusUpdate status, PrintStream out) {
		Map<Player,List<String>> specifics = status.getSpecificMessages();
		if (specifics != null) {
			for (Player player : specifics.keySet()) {
				List<String> messages = specifics.get(player);
				if (messages != null) {
					out.println("** Messages for: " + player.getName() + " **");

					for (String msg : messages) {
						out.println(msg);
					}
					out.println();
				}
			}
		}
		
		List<String> defaultMessages = status.getMessages(); 
		if (defaultMessages != null) {
			out.println("** Messages for everyone else **");
					for (String msg : defaultMessages) {
				out.println(msg);
			}
			out.println();
		}
	}
 
}
