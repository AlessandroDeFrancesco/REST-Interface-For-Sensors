package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Thread che rimane in ascolto per eventuali nuove connessioni, che avverranno
 * solo nell'inizializzazione della rete o se il nodo precedente cade
 */
public class NodeServer extends Thread {

	private ServerSocket welcomeSocket;
	private boolean stop;

	NodeServer(SensorType type){
		this.stop = false;
		System.out.println("Listening port: " + type.getPort());
		try {
			this.welcomeSocket = new ServerSocket(type.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run(){
		while(!needToStop()) {
			try {
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("The precedent node has connected: " + connectionSocket.getPort());
				// Creazione di un thread e passaggio del thread allo stato del nodo
				NodeListener listener = new NodeListener(connectionSocket);
				NodeState.getInstance().setListener(listener);
			} catch (SocketException e){
				// serverSocket closed
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void stopMe() {
		stop = true;
		try {
			welcomeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected synchronized boolean needToStop() {
		return stop;
	}

}
