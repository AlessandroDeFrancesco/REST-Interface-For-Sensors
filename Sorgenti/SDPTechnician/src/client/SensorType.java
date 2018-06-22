package client;

/** Enumerativo per descrivere i tipi di sensori e le porte associate ad essi */
public enum SensorType{
	PIR_EST(3001), PIR_OVEST(3002), LIGHT(3003), TEMPERATURE(3004);
	private int port;
	
	SensorType(int p){
		port = p;
	}
	
	/** la porta del nodo */
	public int getPort(){
		return port;
	}
	
	/** la porta del nodo successivo a questo tipo di nodo */
	public int getMySucc(){
		if(port == 3004)
			return 3001;
		
		return port + 1;
	}
	
	/** la porta del nodo successivo, per topologia ad anello */
	public static int getSuccPort(int p){
		if(p == 3004)
			return 3001;
		
		return p + 1;
	}

	/** Data una porta restituisce il tipo di sensore associato */
	public static String getType(int port) {
		for(SensorType sensor : SensorType.values()){
			if(sensor.getPort() == port)
				return sensor.toString();
		}

		return "No sensor associated with this port";
	}
}