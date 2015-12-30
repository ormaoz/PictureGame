package engine;

import java.io.File;
import java.io.IOException;

/**
 * This class implements a file-backed dictionary.
 * 
 * We do not assume the entire dictionary can fit in memory. Instead,
 * the file contains the words in a sorted list. Words can be located using a binary search.
 * Updates to the dictionary are stored entirely in memory. 
 * 
 * When the dictionary is flushed, a new file is created and the old one removed.
 * 
 * The file format is as follows:
 * <table>
 * <tr><th>Index</th><th>Length (bytes)</th><th>Desciption</th></tr>
 * <tr><td>0</td><td>4</td><td>SWPD [constant "magic" to allow easy recognition]</td></tr>
 * <tr><td>4</td><td>4</td><td>numWords (32 bit unsigned integer in big-endian order [MSB first])</td></tr>
 * <tr><td>8</td><td>numWords * 4</td><td>Word Index</td></tr>
 * <tr><td>8+numWords * 4</td><td>??</td><td>Word Entries</td></tr>
 * </table>  
 * <p>The word index is composed of pointers to word entries (in alphabetical order). Each pointer is a 32 bit unsigned
 * integer in big-endian order that holds the file index of the corresponding word entry.</p>
 * <p>A word entry is a 32 bit length, followed by that many bytes. The string has the form:
 * 		<pre>keyword:def1:def2:...</pre>
 * </p> 
 * @author talm
 *
 */
public class SortedWordfileDictionary implements PersistentDictionary {
	
	public SortedWordfileDictionary(File dictFile) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void open() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String put(String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
}
