package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;

/** Classe che espone le interfacce rest per le operazioni di login e logout dei tecnici */
@Path("/technicians")
public class TechnicianResource {

	/** Operazione Rest per far loggare un tecnico
	 * restituisce BAD_REQUEST se il nome scelto e' gia' presente */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response createTechnician(JAXBElement<Technician> technician) {
		
		System.out.print("\nRequested to create Tech " + technician.getValue().getName());
		
		if(Technicians.getInstance().addTechnician(technician.getValue()))
			return Response.ok().build();
		else
			return Response.status(Status.BAD_REQUEST).build();
	}

	/** Operazione Rest per far sloggare un tecnico
	 * restituisce BAD_REQUEST se il nome non e' presente tra quelli correntemente loggati*/
	@DELETE
	@Path("/{techName}")
	public Response deleteTechnician(@PathParam("techName") String techName) {
		
		System.out.print("\nRequested to delete Tech " + techName);
		
		if(Technicians.getInstance().deleteTechnician(techName))
			return Response.ok().build();
		else
			return Response.status(Status.BAD_REQUEST).build();
	}
	
} 