package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.Battery.Cost;
import simulators.Buffer;
import simulators.Misurazione;

/** Implementazione del buffer con capacita' limitata */
public class LimitedBuffer implements Buffer<Misurazione> {

	/** La capacita' massima del buffer */
	private static final int MAX_SIZE = 10;
	/** Pattern singleton */
	private static LimitedBuffer instance;
	/** Array che contiene le misurazioni */
	private ArrayList<Misurazione> measures;

	public static synchronized LimitedBuffer getInstance(){
		if(instance == null)
			instance = new LimitedBuffer();
		return instance;
	}

	private LimitedBuffer() {
		measures = new ArrayList<Misurazione>();
	}

	/**
	 * Aggiunge una misurazione al buffer, nel caso in cui il buffer sia pieno
	 * rimpiazza la misura piu' vecchia con quella ricevuta
	 */
	@Override
	public synchronized void aggiungi(Misurazione measure) {
		if(Battery.getInstance().getLevel() > 0){
			if(this.isFull()){
				System.out.println("Buffer full, replacing oldest");
				replaceOldest(measure);
			} else {
				measures.add(measure);
			}

			Collections.sort(measures);
			// decremento il livello di batteria
			System.out.println("READ MEASURE: " + measure.getValue());
			Battery.getInstance().reduceLevel(Cost.READ_MEASURE);

			notify();
		}
	}

	/**
	 * Rimpiazza la misura piu' vecchia dell'array measures con quella ricevuta come parametro.
	 * La misurazione piu' vecchia e' sempre la prima, perche' la lista e' sempre ordinata.
	 * Nel caso in cui quella ricevuta sia piu' vecchia di quella piu' vecchia gia' presente nell'array measures
	 * questa viene ignorata.
	 */
	private void replaceOldest( Misurazione measure) {
		if(measure.compareTo(measures.get(0)) >= 0){
			measures.set(0, measure);
		}
	}

	/** Indica vero se il buffer e' pieno */
	private boolean isFull() {
		return measures.size() >= MAX_SIZE;
	}

	/**
	 * Svuota il buffer e restituisce la lista delle misure.
	 * Se il buffer e' vuoto mette in attesa il thread chiamante finche' non c'è almeno una misurazione.
	 */
	@Override
	public synchronized List<Misurazione> leggi() {
		List<Misurazione> list = new ArrayList<Misurazione>();

		while(measures.size() <= 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Something went wrong while waiting for reading the buffer");
			}
		}

		// se c'e' qualcosa da leggere nel buffer lo restituisco
		if(measures.size() > 0){
			list = new ArrayList<Misurazione>(measures);
			measures.clear();
		}

		return list;
	}

}
