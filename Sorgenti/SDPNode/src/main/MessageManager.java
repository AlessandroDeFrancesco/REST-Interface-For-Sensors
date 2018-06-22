package main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import simulators.Misurazione;

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

	public void manageMessage(String m){
		Message mess = gson.fromJson(m, Message.class);
		if(mess != null && NodeState.getInstance().isSensorNetworkAvailable()){
			System.out.println("Managing message : " + mess.getType());
			switch(mess.getType()){
			case SENSOR_NETWORK_INITIALIZATION:
				initializationMessage(mess);
				break;
			case MEASURES_REQUEST:
				measuresRequested(mess);
				break;
			case NODE_DROPPED:
				nodeDropped(mess);
				break;
			case SINK_ELECTION:
				sinkElection(mess);
				break;
			case NEW_SINK:
				newSink(mess);
				break;
			case SENSOR_NETWORK_NOT_AVAIABLE:
				sensorNetworkNotAvailable(mess);
				break;
			default:
				System.out.println("Message received NOT VALID!");
			}
		}
	}

	/** Gestisce un messaggio di tipo SENSOR_NETWORK_NOT_AVAIABLE, se ho il livello di batteria piu' alto di tutti invio al gateway
	 * che la rete non e' piu' disponibile */
	private void sensorNetworkNotAvailable(Message mess) {
		NodeState nodeState = NodeState.getInstance();
		// imposto che la rete di sensori non e' piu' disponibile
		nodeState.setSensorNetworkAvailable(false);

		if(nodeState.imSink()){
			// il messaggio a fatto tutto il giro
			if(iHaveHigestBatteryLevel(mess)){
				nodeState.getSink().sendToGateway(mess);
			}
			nodeState.getSink().stopMe();
			nodeState.setSink(null);
		} else{
			// se ho il livello di batteria piu' alto di tutti invio io il messaggio al Gateway
			if(iHaveHigestBatteryLevel(mess)){
				Sink sink = new Sink();
				sink.sendToGateway(mess);
				sink.stopMe();
			}
			mess.addAlreadyRead(NodeState.getInstance().getType());
			nodeState.getSender().sendMessage(mess);
		}
	}

	/** Gestisce un messaggio di tipo NEW_SINK, se si e' sink chiude il proprio thread Sink perche' e' stato lui ha indire l'elezione
	 * e il nuovo sink ha gia' ricevuto il messaggio di essere Sink
	 * Se non si e' sink controlla se si ha il livello di batteria piu' alto e diventa il nuovo Sink */
	private void newSink(Message mess) {
		NodeState nodeState = NodeState.getInstance();

		if(nodeState.imSink()){
			// il messaggio a fatto tutto il giro
			nodeState.getSink().stopMe();
			nodeState.setSink(null);
		} else{
			// se sono il nodo con il livello piu' alto divento sink
			if(iHaveHigestBatteryLevel(mess)){
				Sink sink = new Sink();
				nodeState.setSink(sink);
				System.out.println("I'M THE NEW SINK.");
			}
			// mando il messaggio al prossimo nodo
			mess.addAlreadyRead(NodeState.getInstance().getType());
			nodeState.getSender().sendMessage(mess);
		}
	}

	/** Gestisce un messaggio di tipo NODE_DROPPED, se si e' sink lo inoltra al gateway */
	private void nodeDropped(Message mess) {
		NodeState nodeState = NodeState.getInstance();

		if(nodeState.imSink()){
			nodeState.getSink().sendToGateway(mess);
		} else {
			// manda il messaggio al nodo successivo
			mess.addAlreadyRead(NodeState.getInstance().getType());
			NodeState.getInstance().getSender().sendMessage(mess);
		}
	}

	/** Gestisce un messaggio di tipo SENSOR_NETWORK_INITIALIZATION
	 * collegandosi e inviando lo stesso messaggio al prossimo nodo dell'anello */
	private void initializationMessage(Message mess) {
		NodeState node = NodeState.getInstance();

		if(!node.imSink()){
			try {
				Socket socket = new Socket(Node.IP, node.getType().getMySucc());
				node.setSender(new NodeSender(socket));
				mess.addAlreadyRead(NodeState.getInstance().getType());
				// manda il messaggio al nodo successivo
				node.getSender().sendMessage(mess);
			} catch (IOException e) {
				// Errore nell'inizializzazione della rete di sensori
				e.printStackTrace();
			}
		}
	}

	/** Gestisce un messaggio di tipo MEASURES, svuotando il buffer e appendendo tutte le misurazioni
	 * al messaggio che verra' inviato al nodo successivo
	 * Se e' il sink vuol dire che il messaggio ha fatto tutto il giro e lo puo' inviare al gateway */
	private void measuresRequested(Message mess) {
		NodeState node = NodeState.getInstance();
		
		// legge le misurazioni solo se e' un messaggio che non ha gia' visto
		if(haventSeenThisMessage(mess)){
			List<Misurazione> measures = LimitedBuffer.getInstance().leggi();

			// aggiunge tutte le sue misurazioni sotto forma di json al messaggio
			for(Misurazione measure : measures){
				mess.addArg(gson.toJson(measure));
			}
		}
		if(node.imSink()){
			node.getSink().sendToGateway(mess);
		} else {
			// manda il messaggio al nodo successivo
			mess.addAlreadyRead(NodeState.getInstance().getType());
			node.getSender().sendMessage(mess);
		}
	}

	/** Gestisce un messaggio di tipo SINK_ELECTION, se non si e' sink aggiunge il proprio tipo di sensore e il proprio livello di batteria e lo manda al prossimo nodo.
	 *  Se si e' sink controlla i livelli di batteria, se nessuno e' elegibile invia un messaggio di tipo SENSOR_NETWORK_NOT_AVAIABLE al prossimo nodo
	 *  altrimenti cambia il tipo di messaggio in NEW_SINK e lo manda al prossimo nodo */
	private void sinkElection(Message mess) {
		if(NodeState.getInstance().imSink()){
			// il messaggio ha fatto tutto il giro
			if(checkBatteryLevels(mess)){
				// c'e' almeno un nodo con il liv di batteria >25
				mess.setType(MessageType.NEW_SINK);
				mess.setAlreadyRead(new ArrayList<SensorType>());
				mess.addAlreadyRead(NodeState.getInstance().getType());
				NodeState.getInstance().getSender().sendMessage(mess);
			} else {
				// nessun nodo e' elegibile
				mess.setType(MessageType.SENSOR_NETWORK_NOT_AVAIABLE);
				mess.setAlreadyRead(new ArrayList<SensorType>());
				mess.addAlreadyRead(NodeState.getInstance().getType());
				NodeState.getInstance().getSender().sendMessage(mess);
			}
		} else {
			// e' un nodo normale, aggiunge il suo liv di batteria e il tipo
			NodeStateElection nse = new NodeStateElection( NodeState.getInstance().getType(), Battery.getInstance().getLevel());
			mess.addArg(gson.toJson(nse));
			mess.addAlreadyRead(NodeState.getInstance().getType());
			// manda il messaggio al nodo successivo
			NodeState.getInstance().getSender().sendMessage(mess);
		}
	}

	/** Controlla i livelli di batteria, true se c'e' almeno un nodo che ha il liv di batteria > 25% */
	private boolean checkBatteryLevels(Message mess) {
		for(String string : mess.getArgs()){
			NodeStateElection nse = gson.fromJson(string, NodeStateElection.class);
			if(nse.getBatteryLevel() > 0.25f)
				return true;
		}

		return false;
	}

	/** Ritorna true se si ha il livello di batteria piu' alto*/
	private boolean iHaveHigestBatteryLevel(Message mess) {
		SensorType myType = NodeState.getInstance().getType();

		ArrayList<String> list = mess.getArgs();
		NodeStateElection highestBattery = gson.fromJson(list.get(0), NodeStateElection.class);
		for(int i = 1; i<list.size(); i++){
			NodeStateElection nse = gson.fromJson(list.get(i), NodeStateElection.class);
			if(nse.getBatteryLevel() > highestBattery.getBatteryLevel())
				highestBattery = nse;
		}

		if(highestBattery.getType() == myType)
			return true;
		else		
			return false;
	}
	
	/** ritorna vero se il messaggio non e' stato ancora letto da questo nodo */
	private boolean haventSeenThisMessage(Message mess) {
		if(mess.getAlreadyRead().contains(NodeState.getInstance().getType()))
			return false;
		return true;
	}
}
