package standalone_net;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket clientSocket = new Socket();
		clientSocket = serverSocket.accept();
		PrintStream bla = new PrintStream("bla"); 
		HelloClient client = new HelloClient();
		PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
		
		client.run(bla, "Or-Laptop", 8080, "Hello");

	}

}
