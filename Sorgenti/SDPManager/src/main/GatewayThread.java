package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Thread che si occupa di rimanere in ascolto e accettare i Sink della rete di sensori */
public class GatewayThread implements Runnable {
	
	private static final int PORT = 3500;
	
	private SinkConnection sinkConnection;
	private ServerSocket welcomeSocket;
	
	public GatewayThread() {
		try {
			System.out.println("Listening for the Sink..");
			welcomeSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Rimane in ascolto per il Sink, o nel caso in cui questo cambi*/
	public void run() {
		while(true){
			try {
				Socket sinkSocket = welcomeSocket.accept();
				sinkConnection = new SinkConnection(sinkSocket);
				Thread thread = new Thread(sinkConnection);
				thread.start();
				System.out.println("\nNEW Sink connected.");
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
}
