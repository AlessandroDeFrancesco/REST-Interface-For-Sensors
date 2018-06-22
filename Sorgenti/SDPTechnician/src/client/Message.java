package client;

import java.util.ArrayList;

public class Message {

	/** Il tipo di messaggio */
	private MessageType type;
	/** Chi ha gia' letto il messaggio */
	private ArrayList<SensorType> alreadySeen;
	/** Il contenuto del messaggio */
	private ArrayList<String> args;
	
	public Message(MessageType type) {
		this.type = type;
		this.alreadySeen = new ArrayList<SensorType>();
		this.args = new ArrayList<String>();
	}
	
	public Message(MessageType type, String arg) {
		this.type = type;
		this.alreadySeen = new ArrayList<SensorType>();
		this.args = new ArrayList<String>();
		this.args.add( arg );
	}
	
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public ArrayList<String> getArgs() {
		return args;
	}
	public void setArgs(ArrayList<String> args) {
		this.args = args;
	}
	public void addArg(String arg){
		args.add(arg);
	}
	public void setAlreadyRead(ArrayList<SensorType> alreadySeen) {
		this.alreadySeen = alreadySeen;
	}
	public ArrayList<SensorType> getAlreadyRead() {
		return alreadySeen;
	}
	public void addAlreadyRead(SensorType type){
		alreadySeen.add(type);
	}	
}
