package game;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.InMemoryDictionary;
import engine.PersistentDictionary;
import game.VerbosityGame.VerbosityPlayerStatus.ROLE;

public class VerbosityGameTest {

	VerbosityGame game;
	Map<String, WordDescription> expected;
	Player player;
	Player player2;
	Player player3;
	Player player4;
	long time;
	StatusUpdate status;
	
	/**
	 * Set up class before each test.
	 * @return 
	 * @throws Exception
	 */
	@Before
	public void setUpBeforeClass() throws Exception {
		
		// Creates a file for the dictionary
		File dictFile = File.createTempFile("DictTest", ".tmp");
		dictFile.delete();
		
		// Create a dictionary with the file.
		PersistentDictionary dict = new InMemoryDictionary(dictFile);
		
		// Initialize the Verbosity game with the dictionary, the word "Encyclopedia"
		// and a random threshold I chose.
		this.game = new VerbosityGame(2, "Encyclopedia", dict);
		
		// Initialize the expected hash map (to be compared to descriptions)
		this.expected = new HashMap<String, WordDescription>();
		
		// Initialize players for PlayerMove method
		this.player = new MockPlayer("1", "Or");
		this.player2 = new MockPlayer("2", "Tal");
		this.player3 = new MockPlayer("3", "Yair");
		this.player4 = new MockPlayer("4", "Assaf");
		
		// Initialize time parameter for PlayerMove method
		this.time = 10;
		
		// Initialize status for PlayerMove method
		this.status = new StatusUpdate();
		
		// Set up game to not be over
		game.gameEnded = false;
	}

	/**
	 * clear unnecessary objects after each test.
	 * 
	 */
	@After
	public void tearDownAfterClass(){
		
		// Help garbage collector
		this.game = null;
		this.expected = null;
		this.player = null;
		this.player2 = null;
		this.player3 = null;
		this.player4 = null;
		this.status = null;
	}

	/**
	 * Test deserializeDescription method for one word description.
	 */
	@Test
	public void OneWordDescription() {
		String description = "book|15";
		expected.put("book", new WordDescription("book", 15));
		game.deserializeDescription(description);
		assertEquals(expected, game.descriptions);
	}
	
	/**
	 * Test deserializeDescription method for a few word descriptions.
	 */
	@Test
	public void FewWordDescriptions() {
		String description = "book|5,knowledge|2,infomation|5,heavy|3,wikipedia|1";
		expected.put("book", new WordDescription("book", 5));
		expected.put("knowledge", new WordDescription("knowledge", 2));
		expected.put("infomation", new WordDescription("infomation", 5));
		expected.put("heavy", new WordDescription("heavy", 3));
		expected.put("wikipedia", new WordDescription("wikipedia", 1));
		game.deserializeDescription(description);
		assertEquals(expected, game.descriptions);
	}
	
	/**
	 * Test deserializeDescription method with no words descriptions.
	 */
	@Test
	public void NoWordDescriptions() {
		String description = "";
		game.deserializeDescription(description);
		assertEquals(expected, game.descriptions);
	}
	
	/**
	 * Test deserializeDescription method with problematic words descriptions.
	 */
	@Test
	public void ProblematicWordDescriptions() {
		String description = "15|3,@#$%|0,aaaaaaaaaaaaaaaa|10000,...........|3";
		expected.put("15", new WordDescription("15", 3));
		expected.put("@#$%", new WordDescription("@#$%", 0));
		expected.put("aaaaaaaaaaaaaaaa", new WordDescription("aaaaaaaaaaaaaaaa", 10000));
		expected.put("...........", new WordDescription("...........", 3));
		game.deserializeDescription(description);
		assertEquals(expected, game.descriptions);
	}
	
	// A player made for the tests of the PlayerMove method
	private class MockPlayer implements Player {

		String id;
		String name;

		MockPlayer(String id, String name) {
			this.id = id;
			this.name = name;
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
	
	/**
	 * Test PlayerMove method for trying to make a move when the game ended
	 */
	@Test
	public void MoveWhenGameEnded() {
		game.gameEnded = true;
		game.playerJoin(player, 0);
		status.addMessage(player, "The game has already ended!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method for trying to make a move when player is invalid
	 */
	@Test
	public void MoveWhenPlayerInvalid() {
		status.addMessage(player, "You have not yet joined the game!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method for trying to make a move when player is invalid
	 */
	@Test
	public void MoveWhenPlayerInactive() {
		game.playerJoin(player, 0);
		game.playerJoin(player2, 1);
		game.playerJoin(player3, 0);
		game.playerAbort(player, 2);
		status.addMessage(player, "You are no longer an active player!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method for trying to make a move when player is not next
	 */
	@Test
	public void MoveWhenPlayerNotNext() {
		game.playerJoin(player, 0);
		game.playerJoin(player2, 1);
		game.playerJoin(player3, 0);
		status.addMessage(player2, "You are not the next player!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player3), 
				game.playerMove(player3, "computer", time).getMessages(player2));
	}
	
	/**
	 * Test PlayerMove method for trying to make a move with taboo word
	 */
	@Test
	public void MoveWithTabooWord() {
		game.playerJoin(player, 0);
		game.tabooWords.add("computer");
		status.addMessage(player, "computer is a taboo word!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
		game.tabooWords.clear();
	}
	
	/**
	 * Test PlayerMove method for trying to make a move with valid word
	 */
	@Test
	public void MoveWithValidWord() {
		game.playerJoin(player, 0);
		status.addMessage(player, "");
		status.addMessage("Player Or hints: wikipedia");
		
		// Assert message as expected
		assertEquals(status.getMessages(), 
				game.playerMove(player, "wikipedia", time).getMessages());
	}
	
	
	/**
	 * Test PlayerMove method for checking if word is added to taboo words
	 */
	@Test
	public void MoveAddingToTabooList() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.DESCRIBER;
		game.playerMove(player, "computer", time);
		game.playerMove(player, "computer", time);
		
		// The word is not taboo yet, no message for player yet
		status.addMessage(player, "");
		
		// After 2 attempts to use the word, it shoudn't be a taboo word yet
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
		
		// Initialize status for PlayerMove method
		status = null;
		this.status = new StatusUpdate();		
		
		// But after the third attempt it should become a taboo word
		game.playerMove(player, "computer", time);
		status.addMessage(player, "computer is a taboo word!");
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "computer", time).getMessages(player));
		game.tabooWords.clear();
	}
	
	/**
	 * Test PlayerMove method for trying to make a move with the secret word
	 */
	@Test
	public void MoveWhithSecretWord() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.DESCRIBER;
		status.addMessage(player, "You may not play the secret word itself!");
		
		// Assert message as expected
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "Encyclopedia", time).getMessages(player));
		
		// Same check with capital letters
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "EnCyClOpEdIa", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method for adding words to the descriptions and update counter
	 */
	@Test
	public void MoveAndUpdateDescriptions() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.DESCRIBER;
		
		// Play the word one time and check that the word was added and the count is 1
		game.playerMove(player, "computer", time);
		assertTrue(game.descriptions.containsKey("computer"));
		assertEquals(1, game.descriptions.get("computer").count);
		
		// Play the word one more time and check that count is 2
		game.playerMove(player, "computer", time);
		assertEquals(2, game.descriptions.get("computer").count);
	
		// Play the word three more times and check that count is 5
		game.playerMove(player, "computer", time);
		game.playerMove(player, "computer", time);
		game.playerMove(player, "computer", time);
		assertEquals(5, game.descriptions.get("computer").count);
	}
	
	/**
	 * Test PlayerMove method when trying to play with an empty word
	 */
	@Test
	public void MoveWithDescribeEmptyWord() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.DESCRIBER;
		game.playerMove(player, "", time);
		
		// Checks an empty string was added to descriptions and its counter was added 1
		assertTrue(game.descriptions.containsKey(""));
		assertEquals(1, game.descriptions.get("").count);
	}
	
	/**
	 * Test PlayerMove method when trying to guess wrong word
	 */
	@Test
	public void MoveWithWrongWord() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
		
		// Player should get an empty message (to block the default message)
		status.addMessage(player, "");
		status.addMessage("Or guessed cow");
		
		// Checks empty message for player
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "cow", time).getMessages(player));
		
		// Checks message for everyone else
		assertEquals(status.getMessages(), 
				game.playerMove(player, "cow", time).getMessages());
	}
	
	/**
	 * Test PlayerMove method when trying to guess the secret word and no more players
	 * Checks private messages only
	 */
	@Test
	public void MoveWithSecretWordNoMorePlayers() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
		
		// messages expected for this situation
		status.addMessage(player, "You are correct!");
		status.addMessage(player, "You were the last GUESSER. The game is now over.");
		
		// In this test we only check messages for player
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "Encyclopedia", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method when trying to guess the secret word and no more players
	 * Checks default message only
	 */
	@Test
	public void MoveWithSecretWordNoMorePlayers2() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
		
		// messages expected for this situation
		status.addMessage("Player Or has guessed correctly and has ended the game.");
		
		// In this test we only check message for everyone else
		assertEquals(status.getMessages(), 
				game.playerMove(player, "Encyclopedia", time).getMessages());
	}
	
	/**
	 * Test PlayerMove method when trying to guess the secret word with more players
	 * Checks private messages only
	 */
	@Test
	public void MoveWithSecretWordWithMorePlayers() {
		game.playerJoin(player, 0);
		game.playerJoin(player2, 1);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
		
		// Adding taboo words for the check of addDescriberInfo functionality 
		game.tabooWords.add("book");
		game.tabooWords.add("wikipedia");
		
		// messages expected for this situation
		status.addMessage(player, "You are correct!");
		status.addMessage(player, "You have now become a describer!");
		status.addMessage(player, "The secret word you must describe is: Encyclopedia");
		status.addMessage(player, "The following list of words may not be used: wikipedia,book");
		
		// In this test we only check messages for player
		assertEquals(status.getMessages(player), 
				game.playerMove(player, "Encyclopedia", time).getMessages(player));
	}
	
	/**
	 * Test PlayerMove method when trying to guess the secret word with more players
	 * Checks default message only
	 */
	@Test
	public void MoveWithSecretWordWithMorePlayers2() {
		game.playerJoin(player, 0);
		game.playerJoin(player2, 1);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
				
		status.addMessage("Player Or has guessed correctly and is now a DESCRIBER.");
		
		// In this test we only check message for everyone else
		assertEquals(status.getMessages(), 
				game.playerMove(player, "Encyclopedia", time).getMessages());
	}
	
	/**
	 * Test PlayerMove method when trying to guess an empty word
	 */
	@Test
	public void MoveWithGuessEmptyWord() {
		game.playerJoin(player, 0);
		game.getPlayerStatus(player).role = ROLE.GUESSER;
		game.playerMove(player, "", time);
		status.addMessage(player, "");
		status.addMessage("Or guessed ");
		assertEquals(status.getMessages(), 
				game.playerMove(player, "", time).getMessages());
	}
	
	/**
	 * Test PlayerMove method when trying to guess the secret word 
	 * Test the score check only
	 */
	@Test
	public void MoveWithSecretWordScoreCheck() {
		
		game.playerJoin(player, 0); // Player 1 is a describer by default
		game.playerJoin(player2, 1); // Player 2 is a guesser by default
		game.playerJoin(player3, 2); // Player 3 is a guesser by default
		game.playerJoin(player4, 3); // Player 4 is a guesser by default
		
		// a short round is played while player4 get the word right
		game.playerMove(player, "book", time);
		game.playerMove(player2, "harry potter", time);
		game.playerMove(player3, "Aye Pluto", time);
		game.playerMove(player4, "Encyclopedia", time);
		
		// Checks that only the guesser and the describer got points
		assertEquals(1, game.getPlayerStatus(player).score);
		assertEquals(0, game.getPlayerStatus(player2).score);
		assertEquals(0, game.getPlayerStatus(player3).score);
		assertEquals(1, game.getPlayerStatus(player4).score);
		
		// another short round is played while player3 get the word right
		game.playerMove(player, "info", time);
		game.playerMove(player2, "dictionary", time);
		game.playerMove(player3, "Encyclopedia", time);
		
		// Checks that only the guesser and the describers got points
		assertEquals(2, game.getPlayerStatus(player).score);
		assertEquals(0, game.getPlayerStatus(player2).score);
		assertEquals(1, game.getPlayerStatus(player3).score);
		assertEquals(2, game.getPlayerStatus(player4).score);

	}
}
