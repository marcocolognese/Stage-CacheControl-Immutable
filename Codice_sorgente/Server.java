import java.io.*;
import java.util.*;
import java.net.*;

class Server {
	public static void main(String args[]) throws Exception {
		//System.out.println("Creazione ServerSocket");
		ServerSocket connection = new ServerSocket( 11111 );
		System.out.println("\nServer in funzione\n(per terminare premere ctrl+c)");
		while(true)
			new server_thread(connection.accept());
	}
}

class server_thread implements Runnable {
	Socket c;
	server_thread(Socket c) {
		this.c = c;
		new Thread(this).start();
	}

	public void run(){
		httpPacket pacchetto = new httpPacket();
		String risposta;
		PrintStream os = null;
		char s1[];
		//inizio colloquio col client
		try {
			BufferedOutputStream ob = new BufferedOutputStream(c.getOutputStream());
			os = new PrintStream(ob, false);
			InputStream iss = c.getInputStream();
			ObjectInputStream in = new ObjectInputStream(iss);

			while((pacchetto = (httpPacket) in.readObject()) != null){ //ricezione del pacchetto http del client
				if(pacchetto.getHeader_only()){
					os.println("304 - Not Modified");
					//System.out.println("Dati_inviati a " + pacchetto.getIdClient() + ":\t" + pacchetto.getDati_inviati() + " byte" + "\t\t...risorsa non modificata");
				}
				else{
					//invio della rSisorsa al client
					s1 = new char[pacchetto.getDati_inviati()]; //genera una stringa di lunghezza = Dati_inviati
					Arrays.fill(s1, 'a');
					risposta=String.copyValueOf(s1);
					os.println(risposta);
					//System.out.println("Dati_inviati a " + pacchetto.getIdClient() + ":\t" + pacchetto.getDati_inviati() + " byte" + "\t\t...risorsa inviata");
				}
				os.flush();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato");
			os.println("File non trovato");
			os.flush();
		} catch (Exception e) {
			System.out.println(e +" INFO: "+pacchetto.getIdClient()+" "+ pacchetto.getTimestamp()+" "+pacchetto.getobjID()+" "+pacchetto.getSize()+" "+pacchetto.getDati_inviati()+" "+pacchetto.getHeader_only()+" "+pacchetto.getIs_fragment());
		}
		os.close();
		try {
			c.close();
		} catch (IOException e) {
			System.out.println(e+" Errore chiusura Socket");
		}
	}
}