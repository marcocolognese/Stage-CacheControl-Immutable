import java.io.*;
import java.net.*;

class Client {
	public static void main(String args[]) throws IOException, InterruptedException { //input: percorso del file; IP del server; numero di thread da creare; 0 - 1 (senza immutable - con immutable)
		if(!args[3].equals("0") && !args[3].equals("1")){
			//System.out.print("\nCodice immutable non valido. Terminazione");
			System.exit(1);
		}
		
		BufferedReader is = null; //lettura file
		is = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))); //legge il percorso del file dagli argomenti inseriti
		int i = 0; 
		int num = Integer.parseInt(args[2]); //numero di client in int
		long tempo; //tempo di inizio
		tempo = System.currentTimeMillis();
		//creo le thread
		while(i < num){
			System.out.println("Thread " + (i+1) + ": creata. Attendere terminazione...");
			new client_thread(args[0], args[1], i++, is, tempo, args[3]);
		}
	}
}

class SynchronizedCounter {
	public static int c = 1;
	public synchronized static int value() {
		return c++;
	}
}

class client_thread extends Thread implements Runnable  {
	String percorsoFile;
	String indirizzoIP;
	int id_Thread; 
	long time;
	String immutable;
	BufferedReader is; //lettura file

	//costruttore
	client_thread(String file, String ip, int i, BufferedReader is1, long tempo, String imm) {
		percorsoFile = file;
		indirizzoIP = ip;
		id_Thread = i;
		is = is1;
		time=tempo;
		immutable=imm;
		new Thread(this).start();
	}

	public void run(){
		Socket s = null;
		BufferedReader b = null; 	//lettura risposta server
		OutputStream oss = null;
		ObjectOutputStream out = null;
		String r = "";				//stringa per leggere il file
		String[] r1 = null;			//array di stringhe per suddividere i campi dell'header http
		String risposta = "";
		httpPacket pacchetto=null;
		//int c;						//Numero richiesta

		try {
			s = new Socket(indirizzoIP, 11111); // IP e porta del server
			b = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		try {
			oss = s.getOutputStream();
			out = new ObjectOutputStream(oss);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		try{
			while ((r = is.readLine()) != null) {
				//creazione richiesta da inviare al server
				//System.out.println("Richiesta " + SynchronizedCounter.value() + " della thread " + id_Thread + ": " + r);
				r1 = r.split(" "); //divide la stringa letta in base agli spazi nei vari campi dell'header http
				//c=SynchronizedCounter.value();
				if(Integer.parseInt(String.valueOf(r1[3])) > 268435453){
					//System.out.println("Fail su richiesta numero: " + c);
					continue;
				}
				if(r1[4].equals("1")){ //controllo bit di header_only
					r1[4] = "true";
					if(immutable.equals("1")){ //se è settato il token immuable, i pacchetti con header_only=1 non vengono inviati
						//System.out.println("\tPacchetto immutable: richiesta non inviata");
						continue;
					}
				}
				else if( r1[4].equals("0"))
					r1[4] = "false";
				else{
					//System.out.println("\t\t\tRichiesta non valida");
					continue;
				}
				
				if(r1[5].equals("1")) //controllo bit di is_fragment
					r1[5] = "true";
				else if( r1[5].equals("0"))
					r1[5] = "false";
				else{
					//System.out.println("\t\t\tRichiesta non valida");
					continue;
				}

				pacchetto = new httpPacket(id_Thread, r1[0], Integer.parseInt(String.valueOf(r1[1])), Integer.parseInt(String.valueOf(r1[2])), Integer.parseInt(String.valueOf(r1[3])), Boolean.parseBoolean(r1[4]), Boolean.parseBoolean(r1[5]));
				//controllo validità pacchetto
				if(pacchetto.getSize()<0 || pacchetto.getDati_inviati()<0)
					System.out.println("\t\t\tRichiesta non valida");
				else{
					//invio richiesta http al server
					out.writeObject((Object) pacchetto);
					out.flush();
					//ricezione risposta del server
					risposta = b.readLine();
					/*if(risposta.contains("304"))
						//System.out.println("\t\tMessaggio ricevuto dalla thread "+ id_Thread + ": risorsa ancora valida");
					else
						System.out.println("\tMessaggio ricevuto dalla thread "+ id_Thread + "--> Lunghezza: " + risposta.length() + " byte");*/
				}
				/*if((c%500000) == 0) //stampa per verificare che il programma funzioni correttamente
					System.out.println("Richiesta: " + c);*/
			}
			out.writeObject(null); //invia "null" per indicare al server che le richieste sono terminate
			out.flush();
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato");
			System.exit(1);
		} catch (IOException e) {
			System.out.println(e.getMessage() +" INFO: "+pacchetto.getIdClient()+" "+ pacchetto.getTimestamp()+" "+pacchetto.getobjID()+" "+pacchetto.getSize()+" "+pacchetto.getDati_inviati()+" "+pacchetto.getHeader_only()+" "+pacchetto.getIs_fragment());
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Tempo Thread " + (id_Thread+1) + ": " + time + " millisecondi");
		try {
			b.close();
			oss.close();
			s.close();
		} catch (IOException e) {
			System.out.println(e.getMessage() + " Errore chiusure b oss s");
		}
	}
}