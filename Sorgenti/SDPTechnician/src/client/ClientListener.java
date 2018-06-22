package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/** Thread che si occupa di ascoltare i messaggi dal server */
public class ClientListener extends Thread{
	private ServerSocket serverSocket;
	private BufferedReader inputStream;
	private boolean stop;

	public ClientListener(ServerSocket socket) {
		this.stop = false;
		this.serverSocket = socket;
	}

	/** Ascolta i messaggi dal server e li stampa a video */
	public void run(){
		try{
			while(!needToStop()){
				Socket socket = serverSocket.accept();
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String mess = inputStream.readLine();
				// faccio gestire il messaggio
				MessageManager manager = new MessageManager(mess);
				manager.start();
				socket.close();
			}
			// se esce dal while il thread e' stato terminato
			inputStream.close();
			serverSocket.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public synchronized void stopMe() {
		stop = true;
	}

	protected synchronized boolean needToStop() {
		return stop;
	}

}
