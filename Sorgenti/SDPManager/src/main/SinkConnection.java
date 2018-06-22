package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/** Thread che si occupa di ricevere i messaggi del Sink correntemente collegato */
public class SinkConnection implements Runnable{

	private Socket connectionSocket;
	private BufferedReader inputStream;

	public SinkConnection(Socket socket) {
		connectionSocket = socket;
		try {
			inputStream = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** riceve un messaggio dal sink e lo fa gestire da un Thread */
	public void run(){
		try{
			while(true){
				// ascolto per un messaggio
				String mess = inputStream.readLine();
				// faccio gestire il messaggio
				MessageManager manager = new MessageManager(mess);
				manager.start();
			}
		} catch(IOException e){
			System.out.println("The Sink has DROPPED.");
		}
	}

}
