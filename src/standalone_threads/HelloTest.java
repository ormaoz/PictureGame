package standalone_threads;

import static org.junit.Assert.*;

import org.junit.Test;

public class HelloTest {
	final static int NUM_CONSUMERS = 4;
	
	static class Hello implements standalone_threads.Hello {
		String msg;
		
		Hello(String msg) { this.msg = msg; }
		@Override
		public String getHelloMessage() { return msg; }
		
	}
	
	@Test
	public void test() {
		HelloMain main = new HelloMain();
		
		main.go(NUM_CONSUMERS);
		
		Thread[] consumerThreads = main.getRunningConsumerThreads();
		assertEquals("Expecting a different number of consumers", NUM_CONSUMERS, consumerThreads.length);
		
		for (Thread thread : consumerThreads) {
			assertTrue(thread.isAlive());
		}
		
		main.helloWasProduced(new Hello("Hi first"));
		main.helloWasProduced(new Hello("Hello   first"));
		main.helloWasProduced(new Hello("Hi second"));
		main.helloWasProduced(new Hello("Isn't a hi message third"));
		main.helloWasProduced(new Hello("hello third"));
		main.helloWasProduced(new Hello("Hello second"));
		main.helloWasProduced(new Hello("Not a hello second"));
		
		main.stop();

		for (Thread thread : consumerThreads) {
			assertFalse("Expecting thread to be stopped", thread.isAlive());
		}
		
		assertEquals(2, main.getHelloCount("first"));
		assertEquals(2, main.getHelloCount("second"));
		assertEquals(1, main.getHelloCount("third"));
	}

}
