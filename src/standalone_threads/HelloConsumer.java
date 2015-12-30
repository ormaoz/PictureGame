package standalone_threads;



public class HelloConsumer implements Runnable {
	/**
	 * The source from which hello messages should be taken.
	 */
	HelloMain main;
	
	HelloConsumer(HelloMain main) {
		this.main = main;
	}
	
	@Override
	public void run() {
		// TODO Implement
	}

}
