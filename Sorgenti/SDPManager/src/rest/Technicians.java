package rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Classe che contiene in un HashMap tutti i tecnici correntemente loggati */
public class Technicians {

	private static Technicians instance;
	private HashMap<String, Technician> technicians;
	
	private Technicians(){
		technicians = new HashMap<String, Technician>();
	}
	
	public static synchronized Technicians getInstance(){
		if(instance == null)
			instance = new Technicians();
		return instance;
	}
	
	/** Aggiunge un tecnico e restituisce true se il nome non esiste gia'
	 * altrimenti restituisce false */
	public synchronized boolean addTechnician(Technician tech){
		if(!technicians.containsKey(tech.getName())){
			technicians.put(tech.getName(), tech);
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized List<Technician> getTechnicianList(){
		return new ArrayList<Technician>(technicians.values());
	}

	public synchronized boolean deleteTechnician(String techName) {
		if(technicians.containsKey(techName)){
			technicians.remove(techName);
			return true;
		} else {
			return false;
		}
	}

}
