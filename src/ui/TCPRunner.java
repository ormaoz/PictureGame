package ui;

import game.Game;
import game.Player;
import game.PlayerStatus;
import game.StatusUpdate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TCPRunner {
	
	/**
	 * The number of times to retry until declaring failure
	 */
	public final static	int NETWORK_TRIES = 3;

	/**
	 * A String to query a player's ID
	 */
	final static String ID_QUERY = "What is your ID?";
	
	/**
	 * A String to query a player's Name
	 */
	final static String NAME_QUERY = "What is your Name?";
	
	/**
	 * The game we're running.
	 */
	Game game;
	
	int port;
	
	ServerSocket serverSocket;
	Socket clientSocket;
	
	public TCPRunner(Game game, int basePort)  {
		this.game = game;
		this.port = basePort;
		
	}

	/**
	 * Return the port at which the server is currently listening.
	 * @return the port at which the server is currently listening.
	 */
	public int getServerPort() {
		return port;
	}
	
	/**
	 * Start listening at the port given in the constructor. 
	 * If listening fails, increment the port number and try again.
	 * If {@link #NETWORK_TRIES} attempts fail, throw an IOException.
	 *  
	 */
	public void startListening() throws IOException {
		serverSocket = null;
		boolean listening = false;
		int counts = 0;
		
		// As long as the limit of tries hasn't pass and a listening wasn't started yet
		while (counts < NETWORK_TRIES && !listening)
		try {
			// initialize the Server Socket
			serverSocket = new ServerSocket(getServerPort());
			listening = true;
		
			// If it failed, try the next port and count that try
		} catch (IOException e) {
			this.port++;
			counts++;
		}
		if (!listening) {
			throw new IOException("Astablishing connection failed in given port");
		}
		
	}
	
	
	/**
	 * Close the server port.
	 */
	public void stopListening()  {
		try {
			serverSocket.close();
		} catch (IOException e) {
		
		}
	}
	
	/**
	 * Wait and return the socket corresponding to the next connected client.
	 * If there was an IO error during the connection, wait again for the 
	 * next connection -- up to {@link #NETWORK_TRIES} attempts. If all the
	 * attempts failed, return null. 
	 * @return a socket connected to a client, or null if all attempts failed.
	 */
	public Socket getNextConnection() {
		clientSocket = null;
		int counts = 0;
		boolean isConnected = false;
		
		// As long as the number of tries hasn't pass the limit and connection
		// wasn't established yet
		while (counts < NETWORK_TRIES && !isConnected) {
			try {
				// Wait for a connection from client
				clientSocket = serverSocket.accept();
				isConnected = true;
			
			// If failed count the failure and try again
			} catch (IOException e) {
				counts++;
			}
		}
		
		// Exit method if failed after the limit attempts or return the socket
		if (!isConnected) {
			return null;
		}
		return clientSocket;
	}
	
	/**
	 * An inner class implementing TCPPlayer made so we'll be able to create an
	 * instance of TCPPlayer in the getPlayerFromSocket method.
	 *
	 */
	class TCPMockPlayer implements TCPPlayer {
		String id;
		String name;
		Socket playerSocket;
		
		/**
		 * A Basic constructor for TCPPlayer.
		 * @param id
		 * @param name
		 * @param playerSocket
		 */
		public TCPMockPlayer(String id, String name, Socket playerSocket) {
			this.id = id;
			this.name = name;
			this.playerSocket = playerSocket;
		}
		
		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Socket getPlayerSocket() {
			return this.playerSocket;
		}

		@Override
		public BufferedReader getPlayerInput() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PrintStream getPlayerOutput() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/**
	 * Return a {@link TCPPlayer} instance by reading player details from the socket.
	 * The method should send the string {@link #ID_QUERY} over the connection and wait
	 * for a response (read an entire line). The first line (not including the newline character)
	 * should be used as the ID.
	 * The method should then send the string {@link #NAME_QUERY} over the connection and 
	 * wait for a response (read an entire line). The line (not including the newline character) 
	 * should be used as the player name.
	 * @param sock the socket to read/write from.
	 * @return an instance of a class that implements TCPPlayer.
	 */
	public TCPPlayer getPlayerFromSocket(Socket sock) throws IOException {
		
		// Initialize the TCPPlayer
		TCPMockPlayer tcp = null;
		
		// Initialize the streams
		PrintWriter out = null;
		BufferedReader in = null;
		
		try {
			
			// Setup socket reader and writer
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
			
			// write ID request to client and save answer
			out.write(ID_QUERY);
			String id = in.readLine();
			
			// write name request to client and save answer
			out.write(NAME_QUERY);
			String name = in.readLine();
			
			// Create the new TCPPlayer with all data
			tcp = new TCPMockPlayer(id, name, sock);
			
		} catch (IOException e) {
			
		}
		return tcp;
	}

	public void runGame(Collection<StatusUpdate> initialUpdates) {
		
		// Initialize input and output streams
		BufferedReader in = null;
		PrintStream out = null;
		
		// Start listing
		try {
			startListening();
			System.err.print(getServerPort());
		} catch (IOException e) {
			
		}
		
		// Start Connection with client
		getNextConnection();
		
		try {
			// Create streams
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintStream(clientSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.print("An error accrued while trying to create streams");
		}
		
		for (StatusUpdate update : initialUpdates) {
			outputStatusMessages(update, out);
		}
		
		try {
			while (!game.hasEnded()) {
				Collection<Player> nextPlayers = game.getNextPlayers();
				// We only take the first player in the collection (since the console doesn't allow simultaneous play).
				Player player = nextPlayers.iterator().next(); 
				out.print("Next input for player " + player.getName() + "-> ");
				String word = in.readLine();
				if (word == null) {
					
					// input was closed
					throw new IOException("Input stream closed unexpectedly");
				}
				long time = System.currentTimeMillis();
				StatusUpdate newStatus = game.playerMove(player, word, time);
				outputStatusMessages(newStatus, out);
			}
		} catch (IOException e) {
			// Handle IO Exception. This should only happen when the standard input is closed, 
			// in which case we will declare the game over (all players have aborted).
			System.err.print("I/O Error");
			stopListening();
			game.playerAbort(null, System.currentTimeMillis()); 
			return;
		}
	}
	
	/**
	 * Output status messages to TCPPlayers. 
	 * @param status
	 */
	void outputTCPStatusMessages(StatusUpdate update) {
		// Initialize output stream
		PrintStream out = null;
		
		//
		List<StatusUpdate> abortedUpdates = new LinkedList<StatusUpdate>();
		
		// Go over the list of active players
		for (Player player : game.getActivePlayers()) {	
			if (player instanceof TCPPlayer) {
				try {
					// Create output stream
					out = new PrintStream(((TCPPlayer) player).getPlayerSocket().getOutputStream(), true);
				} catch (IOException e) {
					
					// If an I/O error occurs, close socket
					try {
						((TCPPlayer) player).getPlayerSocket().close();
					} catch (IOException e1) {

					}
					// abort player and: save the status update to a linked list
					abortedUpdates.add(game.playerAbort(player, System.currentTimeMillis()));
				}
				
				// In case there is a message for the player 
				if (update.getMessages(player) != null) {
					
					// output the player's message/s
					out.print(update.getMessages(player));
				} else if (update.getMessages() != null){
					
					// otherwise, send the player the default messages
					out.print(update.getMessages());
				}
			}	
		}
		
		// Output aborted messages
		for (StatusUpdate abortedUpdate : abortedUpdates) {
			outputTCPStatusMessages(abortedUpdate);
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
