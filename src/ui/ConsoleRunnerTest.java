package ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import game.Player;
import game.StatusUpdate;

public class ConsoleRunnerTest {
	ConsoleRunner console;
	StatusUpdate status;
	ByteArrayOutputStream byteStream;
	PrintStream stream;
	TreeSet<String> expected;
	TreeSet<String> actual;
	
	
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
	 * initialize before every test.
	 */
	@Before
	public void before() {
		console = new ConsoleRunner(null);
		byteStream = new ByteArrayOutputStream();
		stream = new PrintStream(byteStream);
		status = new StatusUpdate();
		expected = new TreeSet<String>();
		actual = new TreeSet<String>();
		
	}

	/**
	 * Close after each test.
	 */
	@After
	public void after() {
		// Close the print stream
		stream.close();
		
		// Help garbage collector
		stream = null;
		console = null;
		stream = null;
		status = null;
		expected= null;
		actual = null;
		
	}	
	
	/**
	 * Test the method with one message and one player
	 * no default message
	 */
	@Test
	public void OneMessageOnePlayer() {
		
		// Initialize a player for the test
		Player player1 = new MockPlayer("1", "Or");
		
		// Add a message to the game status (for the player we've just created)
		status.addMessage(player1, "Test number 1");
		
		// Create the expected string as described by the method API.
		String expected = ("** Messages for: Or **" + System.lineSeparator() + 
				"Test number 1" + System.lineSeparator() + System.lineSeparator());
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// check that the result is as expected.
		assertEquals(expected, byteStream.toString());
	}
	
	/**
	 * Test the method with one messages for each of two players
	 * no default message
	 */
	@Test
	public void OneMessageTwoPlayers() {
		
		// Create two players and add each a message.
		Player player1 = new MockPlayer("1", "Or");
		Player player2 = new MockPlayer("2", "Tal");
		status.addMessage(player1, "Test number 1");
		status.addMessage(player2, "Test number 2");
		
		// Since the order between the messages is undetermined: we'll add all
		// the expected messages to a tree set, separately.
		expected.add("** Messages for: Or **" + System.lineSeparator() + 
				"Test number 1");
		expected.add("** Messages for: Tal **" + System.lineSeparator() + 
				"Test number 2");
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// And now we'll separate the byteStream string into separated messages
		// using split option for strings (every time there is a line separation
		// between blocks.
		actual.addAll(Arrays.asList(byteStream.toString().split(System.lineSeparator() + 
				System.lineSeparator())));
		
		// check that the result is as expected.
		assertEquals(expected, actual);
	}
	
	/**
	 * Test the method with one messages for multiple players and default message
	 * 
	 */
	@Test
	public void multiplePlayersAndDefualtMessage() {
		
		// Create two players and add each a message.
		Player player1 = new MockPlayer("1", "Or");
		Player player2 = new MockPlayer("2", "Tal");
		Player player3 = new MockPlayer("3", "Yair");
		status.addMessage(player1, "Test number 1");
		status.addMessage(player2, "Test number 2");
		status.addMessage(player3, "Test number 3");
		status.addMessage("Test number 4");
		
		// Since the order between the messages is undetermined: we'll add all
		// the expected messages to a tree set, separately.
		expected.add("** Messages for: Or **" + System.lineSeparator() + 
				"Test number 1");
		expected.add("** Messages for: Tal **" + System.lineSeparator() + 
				"Test number 2");
		expected.add("** Messages for: Yair **" + System.lineSeparator() + 
				"Test number 3");
		expected.add("** Messages for everyone else **" + System.lineSeparator() + 
				"Test number 4");
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// And now we'll separate the byteStream string into separated messages
		// using split option for strings (every time there is a line separation
		// between blocks.
		actual.addAll(Arrays.asList(byteStream.toString().split(System.lineSeparator() + 
				System.lineSeparator())));
		
		// check that the result is as expected.
		assertEquals(expected, actual);
		
		// check that the default message arrives at the end
		int indexOfDefualtHeader = 126;
		assertEquals(indexOfDefualtHeader, byteStream.toString().indexOf
				("** Messages for everyone else **" + System.lineSeparator() + "Test number 4"));
	}
	
	/**
	 * Test the method with a few message for one player
	 * no default message
	 */
	@Test
	public void FewMessagesonePlayers() {
		
		// Create two players and add each a message.
		Player player1 = new MockPlayer("1", "Or");
		status.addMessage(player1, "Test number 1");
		status.addMessage(player1, "Test number 2");
		status.addMessage(player1, "Test number 3");
		status.addMessage(player1, "Test number 4");
		
		// Since the order between the messages is undetermined: we'll add all
		// the expected messages to a tree set, separately.
		expected.add("** Messages for: Or **");
		expected.add("Test number 1");
		expected.add("Test number 2");
		expected.add("Test number 3");
		expected.add("Test number 4");
	
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// And now we'll separate the byteStream string into separated messages
		// using split option for strings (every time there is a line separation
		// between blocks.
		actual.addAll(Arrays.asList(byteStream.toString().split(System.lineSeparator())));
		
		// check that the result is as expected.
		assertEquals(expected, actual);
		
		// check that the header message arrives at the start
				int indexOfHeader = 0;
				assertEquals(indexOfHeader, 
						byteStream.toString().indexOf("** Messages for: Or **"));
	}

	/**
	 * Test the method with default message and no players
	 */
	@Test
	public void DefaultMessageNoPlayers() {
		
		// Add a default message to the game status
		status.addMessage("Test number 1");
		
		// Create the expected string.
		String expected = ("** Messages for everyone else **" + 
		System.lineSeparator() + "Test number 1" + System.lineSeparator() + 
		System.lineSeparator());
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// check that the result is as expected.
		assertEquals(expected, byteStream.toString());
	}
	
	/**
	 * Test the method with empty message for one player and empty default message
	 */
	@Test
	public void EmptyMessageOnePlayer() {
		
		// Initialize a player for the test
		Player player1 = new MockPlayer("1", "Or");
				
		// Add an empty message to the player
		status.addMessage(player1, "");
		status.addMessage("");
		
		// Create the expected string as it should be.
		String expected = ("** Messages for: Or **" + System.lineSeparator() + 
		System.lineSeparator() + System.lineSeparator() + 
		"** Messages for everyone else **" + System.lineSeparator() +
		System.lineSeparator() + System.lineSeparator());
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// check that the result is as expected.
		assertEquals(expected, byteStream.toString());
	}
	
	/**
	 * Test the method with two messages for the of two players
	 * no default message
	 */
	@Test
	public void TwoMessagesTwoPlayers() {
		
		// Create two players and add each a message.
		Player player1 = new MockPlayer("1", "Or");
		Player player2 = new MockPlayer("2", "Tal");
		status.addMessage(player1, "Test number 1");
		status.addMessage(player1, "Test number 2");
		status.addMessage(player2, "Test number 3");
		status.addMessage(player2, "Test number 4");
		
		// Since the order between the messages is undetermined: we'll add all
		// the expected messages to a tree set, separately.
		expected.add("** Messages for: Or **");
		expected.add("** Messages for: Tal **");
		expected.add("Test number 1");
		expected.add("Test number 2");
		expected.add("Test number 3");
		expected.add("Test number 4");
		expected.add(""); // representing the line separation between the blocks
		
		// Generate the method
		console.outputStatusMessages(status, stream);
		
		// And now we'll separate the byteStream string into separated messages
		// using split option for strings (every time there is a line separation
		// between blocks.
		actual.addAll(Arrays.asList(byteStream.toString().split(System.lineSeparator())));
		
		// check that the result is as expected.
		assertEquals(expected, actual);
		
		// Collect the indexes of all the messages
		int indexOfOrHeader = byteStream.toString().indexOf("** Messages for: Or **");
		int indexOfTalHeader = byteStream.toString().indexOf("** Messages for: Tal **");
		int indexOfOrMessageOne = byteStream.toString().indexOf("Test number 1");
		int indexOfOrMessageTwo = byteStream.toString().indexOf("Test number 2");
		int indexOfTalMessageOne = byteStream.toString().indexOf("Test number 3");
		int indexOfTalMessageTwo = byteStream.toString().indexOf("Test number 4");
		
		// Check that messages comes after the right header
		if (indexOfOrHeader > indexOfTalHeader) {
			assertTrue((indexOfOrMessageOne > indexOfTalMessageOne) && 
					(indexOfOrMessageOne > indexOfTalMessageTwo) &&
					(indexOfOrMessageOne > indexOfTalHeader) &&
					(indexOfOrMessageTwo > indexOfTalMessageOne) &&
					(indexOfOrMessageTwo > indexOfTalMessageTwo) &&
					(indexOfOrMessageTwo > indexOfTalHeader));
		} else {
			assertTrue((indexOfOrMessageOne < indexOfTalMessageOne) && 
					(indexOfOrMessageOne < indexOfTalMessageTwo) &&
					(indexOfOrMessageOne < indexOfTalHeader) &&
					(indexOfOrMessageTwo < indexOfTalMessageOne) &&
					(indexOfOrMessageTwo < indexOfTalMessageTwo) &&
					(indexOfOrMessageTwo < indexOfTalHeader));
		}
	}
}
