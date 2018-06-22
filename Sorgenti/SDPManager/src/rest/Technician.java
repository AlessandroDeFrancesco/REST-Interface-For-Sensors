package rest;

import javax.xml.bind.annotation.XmlRootElement;

/** Classe che incapsula tutte le informazioni riguardanti un tecnico */
@XmlRootElement(name="technician")
public class Technician{
	
	private String name;
	private String IP;
	private int port;
	
	public Technician(){
		
	}
	
	public Technician(String name, String IP, int port) {
		this.name = name;
		this.IP = IP;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
