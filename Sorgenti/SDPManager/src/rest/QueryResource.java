package rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import main.Measures;
import main.Misurazione;

/** Classe che espone le interfacce rest per le operazioni di lettura dei dati dei sensori */
@Path("/query")
public class QueryResource {

	@GET
	@Path("/temperature/most_recent")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMostRecentTemperature() {
		return getMostRecent("Temperature");
	}
	
	@GET
	@Path("/light/most_recent")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMostRecentLight() {
		return getMostRecent("Light");
	}

	@GET
	@Path("/temperature/mean?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMeanTemperature(@PathParam("interval") String interval) {
		return getMean("Temperature", interval);
	}
	
	@GET
	@Path("/light/mean?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMeanLight(@PathParam("interval") String interval) {
		return getMean("Light", interval);
	}
	
	@GET
	@Path("/temperature/min_max?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMinMaxTemperature(@PathParam("interval") String interval) {
		return getMinMax("Temperature", interval);
	}
	
	@GET
	@Path("/light/min_max?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getMinMaxLight(@PathParam("interval") String interval) {
		return getMinMax("Light", interval);
	}
	
	@GET
	@Path("/pir1/count?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getPirEastCount(@PathParam("interval") String interval) {
		return getPirCount("PIR1", interval);
	}
	
	@GET
	@Path("/pir2/count?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getPirWestCount(@PathParam("interval") String interval) {
		return getPirCount("PIR2", interval);
	}
	
	@GET
	@Path("/pir_mean_count?{interval}")
	@Produces({ MediaType.TEXT_PLAIN})
	public Response getPirMeanCount(@PathParam("interval") String interval) {
		double sum = getCount("PIR1", interval) + getCount("PIR2", interval);
		if(sum > 0){
			double mean = sum / 2;
			String text = "Mean number of Precences: " + mean;
			return Response.ok(text).build();
		} else {
			// restituisce errore perche' nessuna misurazione e' nell'intervallo
			return Response.status(Status.NO_CONTENT).build();
		}
	}
	
	private Response getPirCount(String type, String interval) {
		TimeInterval timeInterval;
		try{
			timeInterval = new TimeInterval(interval);
		} catch (NumberFormatException e){
			return Response.status(Status.BAD_REQUEST).build();
		}
		// Prendo la lista
		List<Misurazione> measures = Measures.getInstance().getMeasures(type);
		// controllo che sia stata creata la lista, se si allora c'e' almeno un elemento
		if(measures != null){
			double sum = 0;
			for(Misurazione measure : measures){
				if(checkInInterval(timeInterval, measure)){
					sum ++;
				}
			}
			if(sum > 0){
				String text = "Number of Precences: " + sum;
				return Response.ok(text).build();
			} else {
				// restituisce errore perche' nessuna misurazione e' nell'intervallo
				return Response.status(Status.NO_CONTENT).build();
			}
		} else {
			// restituisce errore se la lista di misurazioni e' vuota
			return Response.status(Status.NO_CONTENT).build();
		}
	}
	
	private double getCount(String type, String interval) {
		TimeInterval timeInterval;
		try{
			timeInterval = new TimeInterval(interval);
		} catch (NumberFormatException e){
			e.printStackTrace();
			return 0;
		}
		double sum = 0;
		List<Misurazione> measures = Measures.getInstance().getMeasures(type);
		if(measures != null){
			for(Misurazione measure : measures){
				if(checkInInterval(timeInterval, measure)){
					sum ++;
				}
			}
		}
		return sum;
	}
	
	private Response getMostRecent(String type) {
		// Prendo e ordino la lista
		List<Misurazione> measures = Measures.getInstance().getMeasures(type);
		// controllo che sia stata creata la lista, se si allora c'e' almeno un elemento
		if(measures != null){
			Collections.sort(measures);
			// Poiche' la lista e' ordinata l'ultimo elemento e' il piu' recente
			Misurazione measure = measures.get(measures.size() - 1);
			String text = "Value: " + measure.getValue() + "\nTimestamp: " + measure.getTimestamp();
			return Response.ok(text).build();
		} else {
			// restituisce errore se la lista di misurazioni e' vuota
			return Response.status(Status.NO_CONTENT).build();
		}
	}
	
	private Response getMean(String type, String interval) {
		TimeInterval timeInterval;
		try{
			timeInterval = new TimeInterval(interval);
		} catch (NumberFormatException e){
			return Response.status(Status.BAD_REQUEST).build();
		}
		// Prendo la lista
		List<Misurazione> measures = Measures.getInstance().getMeasures(type);
		// controllo che sia stata creata la lista, se si allora c'e' almeno un elemento
		if(measures != null){
			// calcolo la media
			double sum = 0;
			double measuresInInterval = 0;
			for(Misurazione measure : measures){
				if(checkInInterval(timeInterval, measure)){
					sum += Double.parseDouble(measure.getValue());
					measuresInInterval++;
				}
			}
			if(measuresInInterval > 0){
				double mean = sum/measuresInInterval;

				String text = "Mean: " + mean;
				return Response.ok(text).build();
			} else {
				// restituisce errore perche' nessuna misurazione e' nell'intervallo
				return Response.status(Status.NO_CONTENT).build();
			}
		} else {
			// restituisce errore se la lista di misurazioni e' vuota
			return Response.status(Status.NO_CONTENT).build();
		}
	}
	
	private Response getMinMax(String type, String interval) {
		TimeInterval timeInterval;
		try{
			timeInterval = new TimeInterval(interval);
		} catch (NumberFormatException e){
			return Response.status(Status.BAD_REQUEST).build();
		}
		// Prendo la lista
		List<Misurazione> measures = Measures.getInstance().getMeasures(type);
		// controllo che sia stata creata la lista, se si allora c'e' almeno un elemento
		if(measures != null){
			// trovo il min e il max
			ArrayList<Double> inInterval = new ArrayList<Double>();
			
			for(Misurazione measure : measures){
				if(checkInInterval(timeInterval, measure)){
					inInterval.add(Double.parseDouble(measure.getValue()));
				}
			}
			
			if(!inInterval.isEmpty()){
				double min = Collections.min(inInterval);
				double max = Collections.max(inInterval);
				
				String text = "Min: " + min + "; Max: " + max;
				return Response.ok(text).build();
			} else {
				// restituisce errore perche' nessuna misurazione e' nell'intervallo
				return Response.status(Status.NO_CONTENT).build();
			}
		} else {
			// restituisce errore se la lista di misurazioni e' vuota
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	/** Controlla che una misurazione sia nell'intervallo di tempo indicato */
	private static boolean checkInInterval(TimeInterval timeInterval, Misurazione measure) {
		return timeInterval.getStart() <= measure.getTimestamp()/1000 && measure.getTimestamp()/1000 <= timeInterval.getEnd();
	}

}
