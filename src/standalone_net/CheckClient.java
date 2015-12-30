package standalone_net;

import java.io.IOException;
import java.io.PrintStream;

public class CheckClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
			HelloClient client = new HelloClient();
			client.run(System.out, "localhost", 1234, "connection succeed");
	}

}
