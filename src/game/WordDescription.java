package game;

/**
 * A class pairing a describing word with the number of times
 * it's been used.
 * @author talm
 *
 */
public class WordDescription {
	/**
	 * The describing word.
	 */
	public String word;
	/**
	 * The number of games in which the word was used as a descriptor.
	 */
	public int count;

	/**
	 * Constructor
	 * @param word
	 * @param count
	 */
	public WordDescription(String word, int count) {
		this.word = word;
		this.count = count;
	}

	
	@Override
	public int hashCode() {
		return word.hashCode();
	}
	
	/**
	 * Allow comparison to another WordDescription or to a String.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof WordDescription)
			return word.equals(((WordDescription)o).word);
		else
			return word.equals(o);
	}
	
}