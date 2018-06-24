import java.io.Serializable;

public class httpPacket implements Serializable{
	private int idClient;
	private String timestamp;
	private int objID;
	private int size;
	private int dati_inviati;
	private boolean header_only;
	private boolean is_fragment;
	
	//costruttori
	public httpPacket(int client, String t, int id, int s, int d, boolean h,  boolean f){
		idClient = client;
		timestamp = t;
		objID = id;
		size = s;
		dati_inviati = d;
		header_only = h;
		is_fragment = f;
	}
	
	public httpPacket(){
	}
	
	//set
	public void setIdClient(int id){
		idClient = id;
	}
	
	public void setTimestamp(String x){
		timestamp = x;
	}
	
	public void setobjID(int x){
		objID = x;
	}
	
	public void setSize(int x){
		size = x;
	}
	
	public void setDati_inviati(int x){
		dati_inviati = x;
	}
	
	public void setHeader_only(boolean x){
		header_only = x;
	}

	public void setIs_fragment(boolean x){
		is_fragment = x;
	}
	
	//get
	public int getIdClient(){
		return idClient;
	}
	
	public String getTimestamp(){
		return timestamp;
	}
	
	public int getobjID(){
		return objID;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getDati_inviati(){
		return dati_inviati;
	}
	
	public boolean getHeader_only(){
		return header_only;
	}
	
	public boolean getIs_fragment(){
		return is_fragment;
	}
}