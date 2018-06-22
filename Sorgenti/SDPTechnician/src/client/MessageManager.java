package client;

import com.google.gson.Gson;

/** Thread che si occupa di gestire un messaggio ricevuto */
public class MessageManager extends Thread{

	private String message;
	private Gson gson;

	public MessageManager(String mess){
		message = mess;
		gson = new Gson();
	}

	public void run(){
		manageMessage(message);
	}

	/** gestisce i messaggi ricevuti dal sink */
	public void manageMessage(String m){
		Message mess = gson.fromJson(m, Message.class);
		System.out.print("\nNew Message from the Gateway :");
		switch(mess.getType()){
		case SENSOR_NETWORK_NOT_AVAIABLE:
			sensorsNotAvailable();
			break;
		case NODE_DROPPED:
			nodeDropped(mess);
			break;
		default:
			System.out.println("\nMessage received not valid");
		}
	}

	/** Gestisce un messaggio di tipo SENSOR_NETWORK_NOT_AVAIABLE comunicando al tecnico
	 * che la rete di sensori ha smesso di funzionare */
	private void sensorsNotAvailable() {
		System.out.println("Sensors network has stopped.");
	}

	/** Gestisce un messaggio di tipo NODE_DROPPED comunicando al tecnico
	 *  che quel tipo di nodo e' caduto */
	private void nodeDropped(Message mess) {
		System.out.println(mess.getArgs().get(0) + " node is dropped.");
	}

}