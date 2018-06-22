package main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import main.Battery.Cost;

import com.google.gson.Gson;

/** Thread che si occupa di inviare un messaggio al nodo successivo */
public class NodeSender extends Thread {

	private boolean stop;
	/** la lista di messaggi da inviare */
	private MessagesBuffer messagesBuffer;
	/** lo stream con il nodo successivo */
	private DataOutputStream outToServer;
	/** la socket verso il nodo successivo */
	private Socket socket;
	private Gson gson;

	public NodeSender(Socket socket) {
		this.stop = false;
		this.gson = new Gson();
		this.messagesBuffer = new MessagesBuffer();
		this.socket = socket;	

		try {
			this.outToServer =  new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Invia il messaggio al nodo successivo */
	public void sendMessage(Message mess){
		// creo il json del messaggio
		String m = gson.toJson(mess);
		// aggiungo il messaggio al buffer
		messagesBuffer.put(m);

		System.out.println("Sending to Next Node: " + mess.getType());
	}

	/** finche' ci sono messaggi da inviare li invia
	 * se avviene un errore prova a cercare un nuovo nodo successivo a cui inviare il messaggio */
	public void run(){
		while(!needToStop()){
			// prende il messaggio dal buffer
			String mess = messagesBuffer.take();
			try {
				outToServer.writeBytes(mess + "\n");
				Battery.getInstance().reduceLevel(Cost.TRANSMISSION);
			} catch (IOException e) {
				// il nodo successivo e' caduto
				succNodeHasDropped(mess);
			}
		}
		// chiudo le connessioni
		try {
			outToServer.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** se non riesce ad inviarlo vuol dire che il nodo successivo
	 * ha chiuso le connessioni in entrata o e' caduto, lo comunica agli altri e 
	 * prova a collegarsi al nodo successivo e riprovare ad inviare il messaggio */
	private void succNodeHasDropped(String mess) {
		sendMessage(new Message(MessageType.NODE_DROPPED, SensorType.getType(socket.getPort()) ));
		System.out.println("The successor node has dropped, trying the next one.");
		try {
			socket = NodeState.getInstance().getNewSuccNode();
			// se c'e' un nuovo successore a cui inviare reinvia il messaggio
			if(socket != null){
				outToServer = new DataOutputStream(socket.getOutputStream());
				outToServer.writeBytes(mess + "\n");
			}
			else{
				// se non ha trovato nessun altro nodo successore chiude il thread
				stopMe();
			}
		}
		catch (Exception e){
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
