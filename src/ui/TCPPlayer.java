package ui;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

import game.Player;

public interface TCPPlayer extends Player {
	/**
	 * Return the TCP Socket associated with the player.
	 * @return
	 */
	public Socket getPlayerSocket();
	
	/**
	 * Return a BufferedReader from which player input may be read. 
	 * @return
	 */
	public BufferedReader getPlayerInput();

	/**
	 * Return a PrintStream to which player output may be written.
	 * @return
	 */
	public PrintStream getPlayerOutput();
}
