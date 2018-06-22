package main;

/** Classe che si occupa di gestire la batteria del nodo */
public class Battery {

	/** Costanti */
	private static final int MAX_LEVEL = 1000;	
	/** Singleton */
	private static Battery instance;
	/** Livello batteria */
	private int batteryLevel;
	
	private Battery(){
		batteryLevel = MAX_LEVEL;
	}
	
	public synchronized static Battery getInstance() {
		if(instance == null)
			instance = new Battery();
		return instance;
	}
	
	/** Setta il livello della batteria */
	public synchronized void setLevel(int level){
		if(level < MAX_LEVEL)
			batteryLevel = level;
		if(level < 0)
			batteryLevel = 0;
		if(level > MAX_LEVEL)
			batteryLevel = MAX_LEVEL;
	}

	/** Il livello della batteria, normalizzato tra 0 e 1 */
	public synchronized float getLevel(){
		return (float)batteryLevel / (float)MAX_LEVEL;
	}
	
	/** Decrementa il livello della batteria
	 * se questo diventa 0 chiude l'applicazione */
	public synchronized void reduceLevel( Cost c){
		NodeState node = NodeState.getInstance();
		setLevel( batteryLevel - c.getValue());
		System.out.println("New Battery Level: "+ getLevel()*100 + "%");
		
		if(getLevel() <= 0 )
			NodeState.getInstance().shutdown();
		else if(getLevel() <= 0.25f && node.imSink()){
			// inizia l'elezione se la batteria e' meno del 25% e se si e' il sink
			node.getSink().startElection();
		}
	}
	
	/** Enumerativo per il costo del consumo della batteria */
	public enum Cost{
		READ_MEASURE(2),
		TRANSMISSION(5),
		TRANSMISSION_TO_GATEWAY(TRANSMISSION.getValue() * 4);
		
		private int value;
		Cost(int c){
			value = c;
		}
		public int getValue(){
			return value;
		}
	};
}
