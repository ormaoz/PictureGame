package standalone_net;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HelloServerTest {
	HelloServer server;
	
	@Before
	public void setup() {
		server = new HelloServer();
	}
	
	@After
	public void tearDown() throws IOException {
		if (server.serverSocket != null && !server.serverSocket.isClosed())
			server.serverSocket.close();
	}

	@Test
	public void testListen() throws IOException {
		int port = server.listen();
		
		assertTrue(server.serverSocket.isBound());
		assertTrue(server.serverSocket.getLocalPort() == port);
	}

	/**
	 * Verify that listen uses a different port each time.
	 * @throws IOException
	 */
	@Test
	public void testListenMultiPort() throws IOException {
		final int COUNT = 10;
		HelloServer servers[] = new HelloServer[COUNT];
		
		Set<Integer> ports = new HashSet<Integer>();
		
		for (int i = 0; i < COUNT; ++i) {
			servers[i] = new HelloServer();
			int port = servers[i].listen();
			// We should get the same port twice
			assertFalse(ports.contains(port));
			ports.add(port);
		}
		
		// Close everything
		for (int i = 0; i < COUNT; ++i) {
			servers[i].serverSocket.close();
		}
	}
	
	void testMessage(int port, String msg, String expectedResponse) throws IOException {
		Socket s = new Socket(InetAddress.getLocalHost(), port);
		
		PrintStream sout = new PrintStream(s.getOutputStream());
		BufferedReader sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
		
		sout.println(msg);
		sout.flush();
		
		String resp = sin.readLine();
		assertEquals(expectedResponse, resp);
		assertNull(sin.readLine()); // Make sure socket was closed
		
		s.close();
	}
	
	
	@Test(timeout=1500)
	public void testRunBasic() throws IOException, InterruptedException {
		PipedInputStream pin = new PipedInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(pin));
		PipedOutputStream pout = new PipedOutputStream(pin);
		final PrintStream out = new PrintStream(pout);
		
		Thread runner = new Thread() {
			public void run() {
				server.run(out);
			}
		};
		
		runner.start();
		
		String listenString = in.readLine();
		assertTrue(listenString.startsWith(HelloServer.LISTEN_MESSAGE));
		
		int port = Integer.parseInt(listenString.substring(HelloServer.LISTEN_MESSAGE.length()));
		
		assertEquals(server.serverSocket.getLocalPort(), port);
		
		testMessage(port, "name", HelloServer.HELLO_MESSAGE + "name");

		testMessage(port, "bye", "bye");
		
		runner.join();
	}
}
