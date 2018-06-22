package main;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import main.Battery.Cost;

/** Thread che si occupa di comunicare con il gateway */
public class SinkSender extends Thread{	
	private MessagesBuffer messagesBuffer;
	private Socket socketGateway;
	private DataOutputStream toGateway;
	private boolean stop;

	public SinkSender(Socket socketGateway) throws IOException{
		this.messagesBuffer = new MessagesBuffer();
		this.stop = false;
		this.socketGateway = socketGateway;
		this.toGateway = new DataOutputStream(socketGateway.getOutputStream());
	}

	/** Invia i messaggi al gateway */
	@Override
	public void run() {
		while(!needToStop()){
			String mess = messagesBuffer.take();
			try {
				toGateway.writeBytes(mess + "\n");
			} catch (IOException e) {
				// problemi nell'inviare al gateway
				e.printStackTrace();
			}

			Battery.getInstance().reduceLevel(Cost.TRANSMISSION_TO_GATEWAY);
		}
		// chiudo le connessioni
		try {
			toGateway.close();
			socketGateway.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** invia un messaggio al gateway */
	public void sendToGateway(String m){
		messagesBuffer.put(m);
	}

	public synchronized void stopMe() {
		stop = true;
	}

	protected synchronized boolean needToStop() {
		return stop;
	}

}
