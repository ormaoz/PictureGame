package standalone_net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class HelloClient {

	Socket clientSocket;

	public static final int COUNT = 10;

	/**
	 * Connect to a remote host using TCP/IP and set {@link #clientSocket} to be the
	 * resulting socket object.
	 * 
	 * @param host remote host to connect to.
	 * @param port remote port to connect to.
	 * @throws IOException
	 */
	public void connect(String host, int port) throws IOException {

		clientSocket = new Socket(host, port);
      
	}

	/**
	 * Perform the following actions {@link #COUNT} times in a row:
	 * 1. Connect to the remote server (host:port). 
	 * 2. Write the string in myname (followed by newline) to the server
	 * 3. Read the response from the server, write it to sysout
	 * 4. Close the socket.
	 * 
	 * Then do the following (only once):
	 * 1. send {@link HelloServer#BYE_MESSAGE} to the server.
	 * 2. Read the response from the server, write it to sysout
	 * 
	 * If there are any IO Errors during the execution (or if when waiting for a response
	 * we get an end of stream), output {@link HelloServer#ERR_MESSAGE} to sysout. If the error is 
	 * inside the loop, continue to the next interaction of the loop. Otherwise exit the method. 
	 * 
	 * @param sysout
	 * @param host
	 * @param port
	 * @param myname
	 */
	public void run(PrintStream sysout, String host, int port, String myname) {
		
		// Initialize I/O Streams 
		PrintWriter out = null;
	    BufferedReader in = null;
		
	    // Connect for COUNT times
		for (int i = 0; i < COUNT; i++) {
			try {
				
				// Initilize new socket
				connect(host, port);
				
				// Set up the streams
				out = new PrintWriter(clientSocket.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	           
	            // Print out myname
	            out.println(myname);
	            
	            // and read input
	            sysout.print(in.readLine() + " ");
	            
	            // close socket
	            clientSocket.close();
			
			// If error accrued, send err message
			} catch (IOException e) {
				sysout.print(" " + HelloServer.ERR_MESSAGE);
			
			// Close socket
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					
				}
			}
		}
		try {
			
			// Connect to server again
			connect(host, port);

			// Set up the streams again
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			// Send the server the "bye message"
			out.println(HelloServer.BYE_MESSAGE);
			
			// Read and print response
			sysout.println(in.readLine());
			
		} catch (IOException e) {
			return;
		}
	}
}
