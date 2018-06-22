package main;

import java.net.Socket;

import simulators.Simulator;

/** Classe che incapsula lo stato corrente del nodo */
public class NodeState {
	
	private static NodeState instance;
	
	/** Il runnable del simulatore */
	private Simulator simulator;
	/** Il thread per il sink */
	private Sink sink;
	/** Il thread che rimane in ascolto per nuove connessioni dagli altri nodi */
	private NodeServer server;
	/** Il thread per ascoltare dal nodo precedente*/
	private NodeListener listener;
	/** Il thread per inviare al nodo successivo*/
	private NodeSender sender;
	/** Il tipo di sensore collegato al nodo */
	private SensorType type;
	/** Indica se la rete di sensori e' ancora disponibile e che quindi c'e' ancora un Sink attivo */
	private boolean sensorNetworkAvailable;
	/** Indica se l'applicazione si sta per chiuder */
	private boolean shutdown;

	private NodeState(){
		setSensorNetworkAvailable(true);
	}
	
	public static synchronized NodeState getInstance(){
		if(instance == null)
			instance = new NodeState();
		return instance;
	}
	
	public void setSimulator(Simulator sim) {
		this.simulator = sim;
		Thread simThread = new Thread(sim);
		simThread.start();
	}

	public Simulator getSimulator(){
		return simulator;
	}

	public synchronized Sink getSink() {
		return sink;
	}

	public synchronized void setSink(Sink sink) {
		this.sink = sink;
		if(sink != null)
			sink.start();
	}

	public synchronized NodeListener getListener() {
		return listener;
	}

	public synchronized void setListener(NodeListener listener) {
		this.listener = listener;
		listener.start();
	}

	public NodeSender getSender() {
		return sender;
	}

	public void setSender(NodeSender sender) {
		this.sender = sender;
		sender.start();
	}
	
	public void setServer(NodeServer server) {
		this.server = server;
		server.start();
	}
	
	public NodeServer getServer() {
		return server;
	}

	public SensorType getType() {
		return type;
	}

	public void setType(SensorType type) {
		this.type = type;
	}
	
	public synchronized boolean isSensorNetworkAvailable() {
		return sensorNetworkAvailable;
	}

	public synchronized void setSensorNetworkAvailable(boolean b) {
		this.sensorNetworkAvailable = b;
	}

	public synchronized boolean imSink() {
		return getSink() != null;
	}

	/** il nodo successivo e' caduto, cerco un nodo successore disponibile
	 * e restituisco la socket, null se non ne trova
	 * Se tutti gli altri nodi sono caduti si connette a se stesso se non ha chiuso le comunicazioni anche questo */
	public Socket getNewSuccNode() {
		int succPort = type.getMySucc();
		Socket socket = null;
		
		// esco solo quando ho trovato un nodo che accetta connessioni
		// o quando ho provato a connettermi a tutti i nodi e sono tornato 
		// al mio nodo
		while(socket == null && succPort != type.getPort()){
			succPort = SensorType.getSuccPort(succPort);			
			try {
				// provo ad aprire una socket verso quel nodo
				socket = new Socket(Node.IP, succPort);
			} catch (Exception e) {
				socket = null;
			}
		}
		
		// se la socket e' stata aperta allora c'e' un nodo successore
		if(socket != null){
			System.out.println("\nThe new next node is: " + succPort + "\n");
		} else {
			System.out.println("There are'nt next nodes.");
		}
		
		return socket;
	}

	/** Chiude tutte le comunicazioni e i thread e chiude l'applicazione */
	public synchronized void shutdown() {
		if(!shutdown){
			shutdown = true;
			System.out.println("SHUTTING DOWN, THE BATTERY IS EMPTY");
			
			if(imSink())
				getSink().stopMe();
			getSimulator().stopMeGently();
			System.out.println("Stopped Simulator");
			getSender().stopMe();
			System.out.println("Stopped Sender");
			getListener().stopMe();
			System.out.println("Stopped Listener");
			getServer().stopMe();
			System.out.println("Stopped Server");
			
			System.exit(0);
		}
	}
	
}
