package main;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import rest.Technician;
import rest.Technicians;

import com.google.gson.Gson;

/** Thread che gestisce un messaggio ricevuto */
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
		System.out.print("\nMessage received : " + mess.getType());
		switch(mess.getType()){
			case MEASURES_REQUEST:
				receivedMeasures(mess);
				break;
			case NODE_DROPPED:
				sendMessageToTechnicians(mess);
				System.out.print( " " + mess.getArgs().get(0) + " node is dropped.");
				break;
			case SENSOR_NETWORK_NOT_AVAIABLE:
				sendMessageToTechnicians(mess);
				break;
			default:
				System.out.println("\nMessage received not valid");
		}
	}

	/** Gestisce un messaggio di tipo MEASURE_REQUEST salvando tutte le misurazioni
	 * ricevute nella struttura dati condivisa */
	private void receivedMeasures(Message mess) {
		ArrayList<String> measures = mess.getArgs();
		
		for(String measuresList : measures){
			Misurazione measure = gson.fromJson(measuresList, Misurazione.class);
			Measures.getInstance().addMeasure(measure);
		}
	}
	
	private void sendMessageToTechnicians(Message mess){
		List<Technician> list = Technicians.getInstance().getTechnicianList();
		String text = gson.toJson(mess);
		for(Technician tech : list){
			try {
				Socket socket = new Socket(tech.getIP(), tech.getPort());
				DataOutputStream toTech = new DataOutputStream(socket.getOutputStream());
				toTech.writeBytes(text + "\n");
				toTech.close();
				socket.close();
			} catch (Exception e) {
				System.out.println("\nCannot connect to the tech: " + tech.getName());
			}
		}
	}
}