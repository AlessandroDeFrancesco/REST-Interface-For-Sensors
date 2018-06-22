package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class Main {	
	/** Indica se bisogna chiudere l'applicazione */
	private static boolean closeApplication = false;
	/** Il thread per ascoltare gli errori provenienti dalla rete di sensori */
	private static ClientListener listener;
	/** L'input dell'utente */
	private static BufferedReader input;
	/** Classe per le operazioni rest */
	private static RestOperations restOperations;
	/** Il tecnico che si e' loggato */
	private static Technician tech;

	public static void main(String[] args) {
		System.out.println("########## WELCOME ON TECH CLIENT ##########"
				+ "\nEnter your credential:\n");
		// classe per effettuare operazioni rest
		restOperations = new RestOperations();

		try {
			// Richiesta di input dall'utente
			input = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("IP: ");
			String sIP = input.readLine();
			System.out.println("PORT: ");
			int sPORT = -1;
			while(sPORT == -1){
				sPORT = requestPortNumber();
			}

			// finche' non sceglie un nome disponibile riprova a fare il Log In
			System.out.println("Your name: ");
			String name = input.readLine();

			tech = new Technician(name, sIP, sPORT);
			// log in del tecnico
			boolean logged = restOperations.logInTechnician(tech);
			while(!logged){
				System.out.println("Name already exists, enter another name: ");
				name = input.readLine();

				tech = new Technician(name, sIP, sPORT);
				// log in del tecnico
				logged = restOperations.logInTechnician(tech);
			}
			System.out.println("\nLOGGED IN.\n");

			// inizio il thread per ascoltare i messaggi dal server
			ServerSocket connectionSocket = new ServerSocket(sPORT);
			listener = new ClientListener(connectionSocket);
			listener.start();
			System.out.println("Listening from the server...");

			// rimane in attesa finche' l'utente non decide che fare
			while(!closeApplication){
				System.out.println("\nPress ENTER to continue..\n");
				input.readLine();
				waitForUserInput();
			}
			
			System.out.println("\n\nPress ENTER to close..");
			input.readLine();
			input.close();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Problems occurred during the connection with the server.");
		}
	}

	/** Richiede all'utente un numero di porta finche' non ne inserisce una valida */
	private static int requestPortNumber() throws IOException {
		int sPORT = -1;
		String stringPort = input.readLine();
		try{
			sPORT = Integer.parseInt(stringPort);
			if(sPORT < 1025 || sPORT > 65535){
				System.out.println("The PORT must be between 1025 and 65535.\nTry again..");
				sPORT = -1;
			}
		} catch( NumberFormatException e ){
			System.out.println("PORT not valid try again..");
			sPORT = -1;
		}
		
		return sPORT;
	}

	/** Richiede in loop cosa fare all'utente */
	private static void waitForUserInput() throws IOException {
		printChoices();
		String in = input.readLine();
		long start = 0;
		long end = 0;
		
		switch (in){
		case "0":
			shutdown(input);
			break;
		case "1":
			restOperations.mostRecent("temperature");
			break;
		case "2":
			restOperations.mostRecent("light");
			break;
		case "3":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.meanValue("temperature", start, end);
			break;
		case "4":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.meanValue("light", start, end);
			break;
		case "5":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.minMax("temperature", start, end);
			break;
		case "6":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.minMax("light", start, end);
			break;
		case "7":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.pirPresences(1, start, end);
			break;
		case "8":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.pirPresences(2, start, end);
			break;
		case "9":
			start = requestTime("Insert the start time: ");
			end = requestTime("Insert the end time: ");
			restOperations.pirMeanPresences(start, end);
			break;
		default:
			System.out.println("Input not valid. Try again..");
			break;
		}
	}

	/** Fa il log out e chiude l'applicazione */
	private static void shutdown(BufferedReader input) throws IOException {
		System.out.println("Closing the application..");
		if(restOperations.logOutTechnician(tech)){
			listener.stopMe();
			closeApplication = true;
			System.out.println("LOGGED OUT.");
		} else {
			System.out.println("Something went wrong while logging out!");
		}
	}

	private static void printChoices() {
		System.out.println("Choose one:"
				+ "\n0: Log Out and Close the Application"
				+ "\n1: Most Recent Temperature"
				+ "\n2: Most Recent Light"
				+ "\n3: Temperature Mean in a Time Interval"
				+ "\n4: Light Mean in a Time Interval"
				+ "\n5: Temperature Min/Max in a Time Interval"
				+ "\n6: Light Min/Max in a Time Interval"
				+ "\n7: East Zone Number of Presences in a Time Interval"
				+ "\n8: Ovest Zone Number of Presences in a Time Interval"
				+ "\n9: Mean Number of Presences in a Time Interval between Ovest and Est");
	}

	/** Richiede all'utente un tempo, se sbaglia lo fa ripetere */
	private static long requestTime(String text) throws IOException {
		long time = -1;
		while(time == -1){
			System.out.println(text);
			String in = input.readLine();
			try{
				time = Long.parseLong(in);
			} catch (NumberFormatException e){
				System.out.println("Input not valid. Try again..");
				time = -1;
			}
		}

		return time;
	}

}
