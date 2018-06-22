package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/** Thread che si occupa di ricevere un messaggio dal nodo precedente */
public class NodeListener extends Thread{

	private boolean stop;
	private Socket connectionSocket;
	private BufferedReader inputStream;

	public NodeListener(Socket socket) {
		this.stop = false;
		this.connectionSocket = socket;
		try {
			this.inputStream = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** riceve un messaggio dal nodo precedente e lo fa gestire da un Thread */
	public void run(){

		while(!needToStop()){
			try{
				// ascolto per un messaggio
				String mess = inputStream.readLine();
				// faccio gestire il messaggio
				MessageManager manager = new MessageManager(mess);
				manager.start();
			} catch(IOException e){
				System.out.println("Connection with the precedent node closed.");
				stopMe();
			}
		}
	}

	public synchronized void stopMe() {
		if(stop == false){
			stop = true;
			try {
				// chiudo le connessioni
				connectionSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected synchronized boolean needToStop() {
		return stop;
	}

}
