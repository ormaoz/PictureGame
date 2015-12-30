package ui;

import static org.junit.Assert.assertEquals;
import engine.InMemoryDictionary;
import engine.PersistentDictionary;
import game.Game;
import game.Player;
import game.StatusUpdate;
import game.VerbosityGame;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import javax.crypto.spec.PSource;

public class TCPRunnerTest1 {
	final static int BASEPORT = 8080;
	
	TCPRunner runner;
	
	// For now, we use an anonymous inner class with an empty implementation (Game isn't used in our tests at this point) 
	static final Game game = new Game() {
		@Override public StatusUpdate playerMove(Player player, String word, long time) { return null;	}
		@Override public StatusUpdate playerJoin(Player player, long time) { return null; 	}
		@Override public StatusUpdate playerAbort(Player player, long time) {	return null;	}
		@Override public boolean hasEnded() {	return false;	}
		@Override public Collection<Player> getPlayers() { return null; }
		@Override public int getNumPlayers() { return 0; }
		@Override public int getNumActivePlayers() { return 0; }
		@Override public Collection<Player> getNextPlayers() {	return null; }
		@Override public Collection<Player> getActivePlayers() { return null; }
	};
	
	@Before
	public void setup() {
		runner = new TCPRunner(game, BASEPORT);
	}
	
	@After
	public void tearDown() {
		runner = null;
	}

	/**
	 * Check basic operation of the startListening method.
	 * @throws IOException
	 */
	@Test
	public void testStartListeningBasic() throws IOException {
		runner.startListening();
		int port = runner.getServerPort();
		
		Socket sock = new Socket("localhost", port);
		sock.close();
		runner.stopListening();
	}
	
	/**
	 * Check that the startListening method skips unavailable ports.
	 * @throws IOException
	 */
	@Test
	public void testStartListeningSkip() throws IOException {
		// "block" half the ports to see if startListening still works.
		ServerSocket[] blocks = new ServerSocket[TCPRunner.NETWORK_TRIES / 2];
		for (int i = 0; i < blocks.length; ++i)
			try {
				blocks[i] = new ServerSocket(BASEPORT + i);
			} catch (BindException e) {
				// Ignore; this port is already bound.
			}
		
		runner.startListening();
		int port = runner.getServerPort();
		assertEquals(BASEPORT + blocks.length, port);
		
		for (int i = 0; i < blocks.length; ++i)
			try {
				if (blocks[i] != null)
					blocks[i].close();
			} catch (IOException e) {
				// Ignore exceptions in close.
			}

		runner.stopListening();
	}

	@Test
	public void testGetPlayerFromSocket() throws IOException {
		// TODO: Implement
	}
	
	@ Test
	public void runServer() throws IOException {
		File dict = new File("C:\\maze\\bla");
		PersistentDictionary persistentDictionary = new InMemoryDictionary(dict);
		VerbosityGame verbosityGame = new VerbosityGame(5, "secret", persistentDictionary);
		runner = new TCPRunner(verbosityGame, BASEPORT);
		
		Player p1 = new MockPlayer("Or", "1");
		Player p2 = new MockPlayer("Tal", "2");
		Player p3 = new MockPlayer("Yair", "3");
		
		// Join them to the game
		verbosityGame.playerJoin(p1, 1);
		verbosityGame.playerJoin(p2, 1);
		verbosityGame.playerJoin(p3, 1);
		
		StatusUpdate update = new StatusUpdate("Welcome to the amazing game");
		Collection<StatusUpdate> initialUpdates = new ArrayList<StatusUpdate>();
		initialUpdates.add(update);
		runner.runGame(initialUpdates);
	}
	

	public class MockPlayer implements Player {
		private String name;
		private String id;
		
		public MockPlayer(String name, String id) {
			this.name = name;
			this.id = id;
		}
		
		@Override
		public String getId() {
			
			return this.id;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
