package client;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;

/** Classe che si occupa di gestire tutte le operazioni REST con il server */
public class RestOperations {
	
	private static JerseyClient client;
	private static JerseyWebTarget service;
	
	public RestOperations(){		
		client = JerseyClientBuilder.createClient();
		service = client.target(getBaseURI());
	}
	
	/** Prova a fare il Log In del tecnico
	 * restituisce true se e' avvenuto, false altrimenti*/
	public boolean logInTechnician(Technician tech) {
		Response response = service.path("technicians").request().post(Entity.entity(tech,MediaType.APPLICATION_XML));
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			return true;
		else
			return false;
	}
	
	/** Prova a fare il Log Out del tecnico
	 * restituisce true se e' avvenuto, false altrimenti*/
	public boolean logOutTechnician(Technician tech) {
		Response response = service.path("technicians").path(tech.getName()).request().delete();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			return true;
		else
			return false;
	}
	
	/** Restituisce la temperatura piu' recente a seconda del tipo passato */
	public void mostRecent(String type){
		JerseyWebTarget ser = service.path("query").path(type).path("most_recent");
		Response response = ser.request().get();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			System.out.println("\n" + response.readEntity(String.class));
		else
			System.out.println(type + " measures not collected yet.");
	}
	
	/** Restituisce la media dei valori in un intervallo di tempo a seconda del tipo passato */
	public void meanValue(String type, long start, long end){
		String path = "mean?" + start + "-" + end;
		JerseyWebTarget ser = service.path("query").path(type).path(path);
		Response response = ser.request().get();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			System.out.println("\n" + response.readEntity(String.class));
		else if (status == Status.NO_CONTENT)
			System.out.println("No " + type + " measures in the interval "+ start + " - " + end);
		else if (status == Status.BAD_REQUEST)
			System.out.println("Time interval not valid.");
	}
	
	/** Restituisce il minimo e il massimo tra i valori in un intervallo di tempo a seconda del tipo passato */
	public void minMax(String type, long start, long end){
		String path = "min_max?" + start + "-" + end;
		JerseyWebTarget ser = service.path("query").path(type).path(path);
		Response response = ser.request().get();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			System.out.println("\n" + response.readEntity(String.class));
		else if (status == Status.NO_CONTENT)
			System.out.println("No " + type + " measures in the interval "+ start + " - " + end);
		else if (status == Status.BAD_REQUEST)
			System.out.println("Time interval not valid.");
	}
	
	public void pirPresences(int numPir, long start, long end){
		String path = "count?" + start + "-" + end;
		String pirType = "PIR " + ((numPir == 1) ? "WEST" : "EAST");
		JerseyWebTarget ser = service.path("query").path("pir"+numPir).path(path);
		Response response = ser.request().get();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			System.out.println("\n" + response.readEntity(String.class));
		else if (status == Status.NO_CONTENT)
			System.out.println("No " + pirType + " precences in the interval "+ start + " - " + end);
		else if (status == Status.BAD_REQUEST)
			System.out.println("Time interval not valid.");
	}
	
	public void pirMeanPresences(long start, long end){
		String path = "pir_mean_count?" + start + "-" + end;
		JerseyWebTarget ser = service.path("query").path(path);
		Response response = ser.request().get();
		
		response.getStatusInfo();
		Status status = Status.fromStatusCode(response.getStatus());
		if(status == Status.OK)
			System.out.println("\n" + response.readEntity(String.class));
		else if (status == Status.NO_CONTENT)
			System.out.println("No precences in the interval "+ start + " - " + end);
		else if (status == Status.BAD_REQUEST)
			System.out.println("Time interval not valid.");
	}
	
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/SDPManager/rest").build();
	}

}
