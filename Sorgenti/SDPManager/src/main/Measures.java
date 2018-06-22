package main;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe che attraverso l'utilizzo di un HashMap conterra' una lista di misurazioni per tipo di misurazione
 * se getMeasures(type) restituisce null vuol dire che o non esiste quel tipo
 * o che non si sono ricevute ancora misurazioni di quel tipo dalla rete di sensori
 * */
public class Measures {
	
	private static Measures instance;
	private HashMap<String, ArrayList<Misurazione>> measures;
	
	private Measures(){
		measures = new HashMap<String, ArrayList<Misurazione>>();
	}
	
	public synchronized static Measures getInstance() {
		if(instance == null)
			instance = new Measures();
		return instance;
	}

	/** Restituisce la lista di misurazioni di un specifico tipo
	 * null se ancora non e' stata collezionata nessuna misurazione di quel tipo */
	public synchronized ArrayList<Misurazione> getMeasures(String type){
		if(measures.get(type) != null)
			return new ArrayList<Misurazione>(measures.get(type));
		else
			return null;
	}


	/** Aggiunge la misurazione nell'hashmap a seconda del tipo di misurazione */
	public synchronized void addMeasure(Misurazione measure) {
		// se ancora non era stata inserita nessuna misurazione di quel tipo, crea la lista per quel tipo nell'hashmap
		if(measures.get(measure.getType()) == null)
			measures.put(measure.getType(), new ArrayList<Misurazione>());
		
		measures.get(measure.getType()).add(measure);
	}
	
}
