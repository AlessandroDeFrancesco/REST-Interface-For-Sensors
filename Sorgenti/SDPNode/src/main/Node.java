package main;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import simulators.Buffer;
import simulators.LightSimulator;
import simulators.Misurazione;
import simulators.PIR1Simulator;
import simulators.PIR2Simulator;
import simulators.Simulator;
import simulators.TemperatureSimulator;

public class Node {
	/** localhost */
	public final static String IP = "localhost";
	/** Il simulatore */
	private static Simulator sim;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Buffer<Misurazione> buffer = LimitedBuffer.getInstance();

		SensorType type;
		Battery battery = Battery.getInstance();
		boolean isSink;
		int sinkFrequency;

		try{
			// recupero gli argomenti
			if(args.length == 4){
				type = takeType(args[0]);
				battery.setLevel(takeBatteryLevel(args[1]));
				isSink = takeSink(args[2]);
				sinkFrequency = takeSinkFrequency(args[3]);			
			} else {
				throw new Exception("Error in the number of input arguments");
			}

			// istanzio il tipo di simulatore
			switch(type){
			case LIGHT:
				sim = new LightSimulator(buffer);
				break;
			case PIR_EST:
				sim = new PIR1Simulator(buffer);
				break;
			case PIR_OVEST:
				sim = new PIR2Simulator(buffer);
				break;
			case TEMPERATURE:
				sim = new TemperatureSimulator(buffer);
				break;
			default:
				throw new Exception("Unexpected Error while instanciating the simulator type");
			}

		} catch (Exception e){
			System.out.println(e.getMessage());
			return;
		}
		
		Sink.setFrequency(sinkFrequency);
		
		NodeState.getInstance().setType(type);

		System.out.println("TYPE: " + type.toString() + "\nBATTERY: " + battery.getLevel()*100 + "%\nSINK: " + isSink + "\nSINK FREQUENCY: " +sinkFrequency/1000 +" seconds\n\n");

		// start del thread di simulazione
		NodeState.getInstance().setSimulator(sim);
		
		// creo il thread per accettare nuove connessioni
		NodeServer server = new NodeServer(type);
		NodeState.getInstance().setServer(server);
		
		// se sink faccio partire il thread del sink
		if(isSink){
			Sink sink = new Sink();
			NodeState.getInstance().setSink(sink);
			// si collega al nodo successore e invia il messaggio di inizializzazione della rete, inviando la propria porta
			// poiche' e' una rete ad anello il messaggio fara' il giro di tutto i nodi
			NodeSender sender = new NodeSender(new Socket(IP, SensorType.getSuccPort(type.getPort())));
			NodeState.getInstance().setSender(sender);
			sender.sendMessage(new Message(MessageType.SENSOR_NETWORK_INITIALIZATION, ""+type.getPort()));
		}
	}

	/** Metodi per ottenere gli argomenti */
	private static int takeSinkFrequency(String freq)  throws Exception {
		int f = 0;

		try{
			f = Integer.parseInt(freq);
		}catch(Exception e){
			throw new Exception("Error in the sink frequency argument " + freq);
		}
		return f*1000;
	}

	private static boolean takeSink(String sink) throws Exception {
		boolean b = false;

		if(sink.equalsIgnoreCase("yes")){
			b = true;
		} else if(sink.equalsIgnoreCase("no")){
			b = false;
		} else {
			throw new Exception("Error in the sink argument " + sink);
		}
		return b;
	}

	private static int takeBatteryLevel(String lvl) throws Exception {
		int l = 0;

		try{
			l = Integer.parseInt(lvl);
		}catch(Exception e){
			throw new Exception("Error in the battery level argument " + lvl);
		}
		return l;
	}

	private static SensorType takeType( String type) throws Exception {
		for(SensorType t:SensorType.values()){
			if(t.name().equalsIgnoreCase(type))
				return t;
		}
		throw new Exception("Error in the type argument " + type);
	}

}
