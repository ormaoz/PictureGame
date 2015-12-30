package standalone_net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.security.ntlm.Client;

public class HelloServer {

	public static final String ERR_MESSAGE = "IO Error!";
	public static final String LISTEN_MESSAGE = "Listening on port: ";
	public static final String HELLO_MESSAGE = "hello ";
	public static final String BYE_MESSAGE = "bye"; 

	ServerSocket serverSocket;
	Socket clientSocket;
	/**
	 * Listen on an available port.
	 * @return The port number chosen.
	 */
	public int listen() throws IOException {
		
		serverSocket = null;
		
		// Open a ServerSocket to listen for client connections      
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + serverSocket.getLocalPort());
            System.exit(1);
        }
        
       	return serverSocket.getLocalPort();
	}


	/**
	 * 1. Start listening on an open port. Write {@link #LISTEN_MESSAGE} followed by the port number (and a newline) to sysout.
	 * 	  If there's an IOException at this stage, exit the method.
	 * 
	 * 2. Run in a loop; 
	 * in each iteration of the loop, wait for a client to connect,
	 * then read a line of text from the client. If the text is {@link #BYE_MESSAGE}, 
	 * send {@link #BYE_MESSAGE} to the client and exit the loop. Otherwise, send {@link #HELLO_MESSAGE} 
	 * to the client, followed by the string sent by the client (and a newline)
	 * After sending the hello message, close the client connection and wait for the next client to connect.
	 * 
	 * If there's an IOException while in the loop, or if the client closes the connection before sending a line of text,
	 * send the text {@link #ERR_MESSAGE} to sysout, but continue to the next iteration of the loop.
	 * 
	 * Note: in any case, before exiting the method you must close the server socket. 
	 *  
	 * @param sysout a {@link PrintStream} to which the console messages are sent.
	 * 
	 * 
	 */
	public void run(PrintStream sysout) {
	
		try {
			
			// Print info to sysout
			sysout.println(LISTEN_MESSAGE + listen());
			
		} catch (IOException e) {
			return;
		}
		
		// Initialize the socket
		clientSocket = null;
		
		// Initialize the streams
		PrintWriter out = null;
		BufferedReader in = null;
		
		// Checks if client disconnected connection
		boolean toDisconnect = false;
		
		// Listen in a loop as long as the client didn't send a bye message
		while (!toDisconnect) {
			try {
				
				// Wait for connection from client
				clientSocket = serverSocket.accept();
				
				// Setup socket reader and writer
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				
				// read from the client
				String clientMessage = in.readLine();
				
				// In case no message came
				if (clientMessage == null) {
					sysout.println(ERR_MESSAGE);	
				
				// In case of bye message
				} else if (clientMessage.equals(BYE_MESSAGE)) {
					out.println(BYE_MESSAGE);
					toDisconnect = true;
				
				// In case of any other message from client
				} else {
					out.println(HELLO_MESSAGE + clientMessage);
					clientSocket.close();
				}
			} catch (IOException e) {
				sysout.println(ERR_MESSAGE);
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					
				}
			}
		}
	}

	public static void main(String args[]) {
		HelloServer server = new HelloServer();

		server.run(System.err);
	}
}
