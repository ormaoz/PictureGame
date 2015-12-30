package engine;

import java.io.File;
import java.io.IOException;

public class SortedWordfileDictionaryTest extends PersistentDictionaryTest {

	@Override
	PersistentDictionary getDictionary(File file) throws IOException {
		return new SortedWordfileDictionary(file);
	}

}
