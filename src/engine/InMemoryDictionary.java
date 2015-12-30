package engine;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;



/**
 * Implements a persistent dictionary that can be held entirely in memory.
 * When flushed, it writes the entire dictionary back to a file.
 * 
 * The file format has one keyword per line:
 * <pre>word:def1:def2:def3,...</pre>
 * 
 * Note that an empty definition list is allowed (in which case the entry would 
 * have the form: <pre>word:</pre> 
 * 
 * @author talm
 *
 */
public class InMemoryDictionary extends TreeMap<String,String> implements 
PersistentDictionary  {
	
	// (because we're extending a serializable class)
	private static final long serialVersionUID = 1L; 

	// holds the file
	private File savedFile = null;
	
	/**
	 * A constructor. receives as input a File.
	 * 
	 * * @param dictFile the dictionary file.
	 * 
	 */
	public InMemoryDictionary(File dictFile) {
		savedFile = dictFile;
	}
	
	@Override
	public void open() throws IOException { 
		RandomAccessFile in = null;
		if (!this.savedFile.exists()) {
			return;
		}
		try {
			
			// Initializing random file access
			in = new RandomAccessFile(savedFile, "rw");
			
			// Reads the first line in the file
			String line = in.readLine();
			
			// as long as there are lines available
			while (line != null) {
				
				// Made to separate key and value
				int colon = line.indexOf(':');
				
				// adding key only if there is no definition.
				if (line.substring(colon + 1) == null) {
					this.put(line.substring(0, colon), null);

				// adding key and value if there is a definition.
				} else {
					this.put(line.substring(0, colon),
							 line.substring(colon + 1, line.length()));
				}
				
				// read the next line
				line = in.readLine();
			}
		} catch (Exception e) {
			
		} finally {
			
			// close random file access
			in.close();
		}
	}

	@Override
	public void close() throws IOException {
		FileWriter out = null;
		
		try {
			
			// initialize a writer
			out = new FileWriter(savedFile);
			
			// for each key in this dictionary
			for (String key : this.keySet()) {
				
				// write the key and value to the file
				out.write(key + ":" + this.get(key) + "\n");
			}
			
			// flush the cache
			out.flush();
		} finally {
			if (out != null) {
				
				// close buffered writer 
				out.close();
			}
		}
	}
}
