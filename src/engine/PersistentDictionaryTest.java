package engine;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test a persistent dictionary.
 * 
 * This test is abstract, and must be subclassed to test for a concrete 
 * PersistentDictionary. 
 * @author talm
 *
 */
public abstract class PersistentDictionaryTest {

	/**
	 * Return a persistent dictionary. If one doesn't exist by that name, create it.
	 * Subclasses should override this method to return a concrete PersistentDictionary.
	 * @param name
	 * @return
	 */
	abstract PersistentDictionary getDictionary(File file) throws IOException;

	File dictFile;
	PersistentDictionary dict;
	TreeMap<String, String> expected;

	public PersistentDictionaryTest() {
		try {
			dictFile = File.createTempFile("DictTest", ".tmp");
		} catch (IOException e) {
			Assert.fail("Couldn't create temporary file for testing");
		}
	}

	String[] testWords = {
			"Second",
			"First",
			"CAB",
			"BBA",
			"",
			"1",
			"4"
	};

	String[] testDefs = {
			"Some definition",
			"another definition",
			"",
			"yet another definition",
			"lorem ipsum dolor",
			"",
			"- ? d  ddd cccc e"
	};

	String[] addWords = {
			"Second",
			"Third",
			"AAAA",
			"CAB"
	};


	String[] addDefs = {
			"A completely different definition",
			"1 2 3",
			"BBBB",
			"yahoo!",
	};
	

	
	@Before
	/** Before each test, fill file with keys and values.
	 * 
	 */
	public void initializing() throws Exception {
		
		dictFile.delete();
		dict = getDictionary(dictFile);
		assertNotNull(dict);
	
		// fill expected map
		expected = new TreeMap<String, String>();
		fillMap(expected, testWords, testDefs);
		
		// fill test dictionary
		dict.open();
		fillMap(dict, testWords, testDefs);
	}
	
	/**
	 * After any test - close the dictionary, delete the file.
	 */
	@After
	public void closing() throws IOException {
		dict.close();
		dictFile.delete();
	}
	
	/**
	 * Fill a dictionary with words.
	 * Any class with a "put(String,String)" method is supported as a dictionary.
	 * 
	 * @param dict the dictionary to be filled
	 * @param words the array of words that will fill it
	 * @param defs the definitions corresponding to the words (must have the same length as words).
	 * @throws Exception may throw a reflection-related exception if dict 
	 * 	is not a valid dictionary or an IOException if dict.put does.
	 */
	void fillMap(Object dict, String[] words, String[] defs)  throws Exception {
		Class<?> dictClass = dict.getClass();

		Method put = null;
		
		// Go over all the methods of dict and see if there is a put(*,*) method
		// for which both arguments can be assigned Strings. (this matches (put(Object,Object) and put(String,String)).
		for (Method m : dictClass.getMethods()) {
			if (!m.getName().equals("put"))
				continue;
			 Class<?>[] params = m.getParameterTypes();
			 if (params.length == 2 && params[0].isAssignableFrom(String.class) && params[1].isAssignableFrom(String.class)) {
				put = m;
			 	break;
			 }
		}
		if (put == null)
			throw new NoSuchMethodException();
		
		for (int i = 0; i < words.length; ++i) {
			put.invoke(dict, words[i], defs[i]);
		}
	}
	
	/**
	 * Compare a Java map to a dictionary. Checks that the sizes are the same, then iterates
	 * over all keys in the map and compares the values.
	 * @param map
	 * @param dict
	 * @return
	 * @throws IOException
	 */
	void testEquality(String msg, Map<String,String> map, PersistentDictionary dict) throws IOException {
		assertEquals(msg + ": Map and dictionary have different sizes", map.size(), dict.size());
		
		for (String key : map.keySet()) {
			assertEquals(msg + ": Recall failed for key " + key, map.get(key), dict.get(key));
		}
	}

	/**
	 * Test the dictionary inside the memory
	 * @throws Exception
	 */
	@Test
	public void recallInMemoryTest() throws Exception {
		
		// checks equality between maps
		testEquality("In-memory recall", expected, dict);
	}
	
	/**
	 * Test the dictionary after it saved to a file and reopened
	 * @throws Exception
	 */
	@Test
	public void recallPersistenceTest() throws Exception {
		
		// close dictionary
		dict.close();
		
		// and then reopen
		dict = getDictionary(dictFile);
		dict.open();
		
		// checks equality between maps
		testEquality("Persistent recall", expected, dict);
	}
	
	/**
	 * Test after dictionary being closed and reopened and words added to it
	 * @throws Exception
	 */
	@Test
	public void recallCombinedTest() throws Exception {
		
		// add more words
		fillMap(expected, addWords, addDefs);
		fillMap(dict, addWords, addDefs);
		
		// checks equality between maps
		testEquality("Combined recall", expected, dict);
	}

	/**
	 * Check that the dictionary enumerates words in alphabetical order.
	 * In memory test.
	 * @throws Exception may throw either an IOException or a reflection-related exception.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void enumerationOrderInMemoryTest() throws Exception  {
		
		// checks for keySet method
		try {
			dict.getClass().getMethod("keySet");
		} catch (NoSuchMethodException e) {
			fail("Key Iteration not supported for " + dict.getClass().getSimpleName());
		}
		
		// get dictionary key iterator
		Iterator<String> dictKeys = ((Set<String>) dict.getClass().getMethod("keySet").invoke(dict)).iterator();
		
		// test keys equality
		for (String key : expected.keySet()) {
			assertTrue("In-memory enumeration test failed: dict ran out of keys!", dictKeys.hasNext());
			assertEquals("In-memory enumeration test failed", dictKeys.next(), key);
		}
	}
	
	/**
	 * Check that the dictionary enumerates words in alphabetical order.
	 * Persistence test.
	 * @throws Exception may throw either an IOException or a reflection-related exception.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void enumerationOrderPersistenceTest() throws Exception  {
		
		// checks for keySet method
		try {
			dict.getClass().getMethod("keySet");
		} catch (NoSuchMethodException e) {
			fail("Key Iteration not supported for " + dict.getClass().getSimpleName());
		}
		
		// get dictionary key iterator
		Iterator<String> dictKeys = ((Set<String>) dict.getClass().getMethod("keySet").invoke(dict)).iterator();
		
		// close the dictionary
		dict.close();
		
		// reopen dictionary
		dict = getDictionary(dictFile);
		dict.open();
		
		// test keys equality
		for (String key : expected.keySet()) {
			assertTrue("In-memory enumeration test failed: dict ran out of keys!", dictKeys.hasNext());
			assertEquals("In-memory enumeration test failed", dictKeys.next(), key);
		}
	}

	/**
	 * Check that the dictionary enumerates words in alphabetical order.
	 * Combined test.
	 * @throws Exception may throw either an IOException or a reflection-related exception.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void enumerationOrderCombinedTest() throws Exception  {
		
		// checks for keySet method
		try {
			dict.getClass().getMethod("keySet");
		} catch (NoSuchMethodException e) {
			fail("Key Iteration not supported for " + dict.getClass().getSimpleName());
		}
		
		// add more words to maps
		fillMap(expected, addWords, addDefs);
		fillMap(dict, addWords, addDefs);
		
		// get dictionary key iterator
		Iterator<String> dictKeys = ((Set<String>) dict.getClass().getMethod("keySet").invoke(dict)).iterator();
		
		// test keys equality
		for (String key : expected.keySet()) {
			assertTrue("In-memory enumeration test failed: dict ran out of keys!", dictKeys.hasNext());
			assertEquals("In-memory enumeration test failed", dictKeys.next(), key);
		}
	}

	/**
	 * Check that elements disappear after we remove them.
	 * In memory test
	 * @throws Exception
	 */
	@Test
	public void removeElementsInMemoryTest() throws Exception  {
		for (String word : addWords) {
			expected.remove(word);
			dict.remove(word);
		}
		testEquality("In-memory remove", expected, dict);
	}

	/**
	 * Check that elements disappear after we remove them.
	 * Persistence test
	 * @throws Exception
	 */
	@Test
	public void removeElementsPersistentTest() throws Exception  {
		
		// close and reopen dictionary
		dict.close();
		dict = getDictionary(dictFile);
		dict.open();

		for (String word : addWords) {
			expected.remove(word);
			dict.remove(word);
		}
		
		// close and reopen dictionary
		dict.close();
		dict = getDictionary(dictFile);
		dict.open();

		testEquality("Persistent remove", expected, dict);
	}

	@Test
	public void clearInMemoryTest() throws Exception  {
		dict.close();

		// Filled dictionary and closed, now we open and clear.
		dict = getDictionary(dictFile);
		dict.open();

		dict.clear();
		expected.clear();

		fillMap(expected, addWords, addDefs);
		fillMap(dict, addWords, addDefs);

		// Check that in-memory everything looks good.
		testEquality("In-memory comparison after clear()", expected, dict);

	}
	@Test
	public void clearPersistenceTest() throws Exception  {

		// close the dictionary and reopen
		dict.close();
		dict = getDictionary(dictFile);
		dict.open();

		// clear words
		dict.clear();
		expected.clear();

		// fill with words
		fillMap(expected, addWords, addDefs);
		fillMap(dict, addWords, addDefs);

		// close the dictionary and reopen
		dict.close();
		dict = getDictionary(dictFile);
		dict.open();
		
		// Check that in-memory everything looks good.
		testEquality("Persistent comparison after clear()", expected, dict);
	}

}
