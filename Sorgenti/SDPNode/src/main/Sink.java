package main;

import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;

/** Thread che si occupa di gestire tutte le operazioni riguardanti il sink */
public class Sink extends Thread {	
	private static final String GATEWAY_IP = "localhost";
	private static final int GATEWAY_PORT = 3500;

	private boolean stop;
	private static long frequency;
	private Gson gson;
	private SinkSender sinkSender;
	private boolean electionStarted;

	public Sink(){
		this.stop = false;
		this.electionStarted = false;
		this.gson = new Gson();

		try {
			Socket socketGateway = new Socket(GATEWAY_IP, GATEWAY_PORT);
			this.sinkSender = new SinkSender(socketGateway);
			this.sinkSender.start();
			
			System.out.println("Connected to Gateway");
		} catch (IOException e) {
			// problemi nel collegarsi al gateway
			e.printStackTrace();
		}
	}
	
	public static void setFrequency(long freq){
		frequency = freq;
	}

	@Override
	public void run() {
		while(!needToStop()){
			try {
				Thread.sleep(frequency);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!needToStop() && !electionStarted)
				requestMeasuresToNodes();
		}
		sinkSender.stopMe();
	}

	/** manda un messaggio, che fara' il giro dell'anello
	 * richiedendo le misurazioni a tutti i nodi 
	 */
	private void requestMeasuresToNodes() {
		Message mess = new Message(MessageType.MEASURES_REQUEST);
		System.out.println("Requesting Measures.");
		// invia il messaggio
		NodeState.getInstance().getSender().sendMessage(mess);
	}
	
	public void sendToGateway(Message mess){
		String m = gson.toJson(mess);
		sinkSender.sendToGateway(m);
		
		System.out.println("Sending to Gateway: " + mess.getType());
	}

    public synchronized void stopMe() {
        stop = true;
    }

    protected synchronized boolean needToStop() {
        return stop;
    }
    
    /** Invia il messaggio di elezione di un nuovo Sink, aggiungendo il proprio livello di batteria e il proprio tipo di sensore */
	public synchronized void startElection() {
		if(!electionStarted){
			electionStarted = true;
			NodeStateElection nse = new NodeStateElection( NodeState.getInstance().getType(), Battery.getInstance().getLevel());
			Message mess = new Message(MessageType.SINK_ELECTION, gson.toJson(nse));
			NodeState.getInstance().getSender().sendMessage(mess);
		}		
	}

}
