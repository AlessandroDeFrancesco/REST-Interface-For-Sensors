package main;

/** Classe che incapsula le informazioni riguardanti il tipo di sensore e il livello di batteria associato
 * per quando avviene una elezione*/
public class NodeStateElection {
	private SensorType type;
	private float batteryLevel;
	
	public NodeStateElection(SensorType type, float batteryLevel){
		this.type = type;
		this.batteryLevel = batteryLevel;
	}

	public SensorType getType() {
		return type;
	}

	public void setType(SensorType type) {
		this.type = type;
	}

	public float getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(float batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
}
