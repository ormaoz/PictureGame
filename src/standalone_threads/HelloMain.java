package standalone_threads;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HelloMain {
	/**
	 * A flag specifying that consumer threads are/should be running.
	 * Consumer threads may access this field to check if they should continue to run.
	 */
	boolean isRunning;
	
	/**
	 * A map that gives for each name, the number of times it appeared in a hello message.
	 */
	Map<String,Integer> helloCounters;
	
	/**
	 * A queue of hello messages that should be handled by consumers.
	 */
	Queue<Hello> helloQueue;
	
	/**
	 * Default constructor
	 */
	HelloMain() {
		helloCounters = new HashMap<String,Integer>();
		helloQueue = new LinkedList<Hello>();
		isRunning = false;
	}

	/**
	 * Block until a hello message becomes available, and return it.
	 *   
	 * @return the next hello message, or null to signal that consumers should exit.
	 */
	public Hello getNextHelloMessage() {
		// TODO: Implement
		return null;
	}
	
	/**
	 * This method is called by an external class when a new {@link Hello} was produced.
	 * (your code should not call this method)
	 * @param hi the new Hello that was produced.
	 */
	public void helloWasProduced(Hello hi) {
		 // TODO: Implement
	}
	
	/**
	 * This method should be called by a {@link HelloConsumer} when it is finished parsing a {@link Hello}.
	 * @param name the name appearing in the hello message.
	 */
	public void parsedHelloMessage(String name) {
		// TODO: Implement
	}
	
	/**
	 * Return the number of hello messages a name appeared in.
	 * @param name
	 * @return
	 */
	public int getHelloCount(String name) {
		// TODO: Implement
		return 0;
	}
	
	/**
	 * Create and start consumer threads. Each consumer thread should run a separate instance
	 * of {@link HelloConsumer}, constructed with the current instance of {@link HelloMain} 
	 * as its source.
	 * @param numConsumers the number of consumer threads to create.
	 */
	public void go(int numConsumers) {
		// TODO: Implement
	}
	
	/**
	 * Return an array containing the {@link Thread} objects corresponding
	 * to all the running consumer threads. 
	 * 
	 * This method can return null if {@link #go(int)} hasn't been called or
	 * if {@link #stop()} has been called.
	 * @return
	 */
	public Thread[] getRunningConsumerThreads() {
		// TODO: Implement
		return null;
	}
	
	/**
	 * Stop all running consumer threads. This method may <b>not</b> use {@link Thread#stop()}! 
	 * It must notify all running consumer threads and wait until they've stopped themselves
	 * (use {@link Thread#join()} to wait). 
	 */
	public void stop() {
		// TODO: Implement
	}
	
	
	
}
