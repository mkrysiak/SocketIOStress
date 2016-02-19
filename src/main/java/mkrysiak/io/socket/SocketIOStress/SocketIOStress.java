package mkrysiak.io.socket.SocketIOStress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class SocketIOStress 
{
	
    private static int INCREASE_CLIENTS = 200;
    //Pause between INCREASE_CLIENTS
    private static int INCREASE_CLIENTS_INTERVAL = 10000;
    //New connection Rate
    private static int CONNECT_INTERVAL = 80;  
    
    private static String WEBSOCKETS_SCHEME = "ws";
    private static String HTTP_SCHEME = "http";
    
    public static void main( String[] args )
    {
    	
    	if (args.length < 1 || args.length > 1) {
    		System.out.println("Please supply a properties file");
    		return;
    	}
    	
    	Properties props = new Properties();
    	try {
    		props.load(new FileInputStream(new File(args[0])));
    	} catch (IOException e) {
    		System.err.println(e.getMessage());
    	}
    	
    	INCREASE_CLIENTS = Integer.parseInt(props.getProperty("increaseClients"));
    	INCREASE_CLIENTS_INTERVAL = Integer.parseInt(props.getProperty("increaseClientsInterval"));
    	CONNECT_INTERVAL = Integer.parseInt(props.getProperty("connectInterval"));
    	
        List<String> serverUrlList = new ArrayList<String>();
        for (Map.Entry<Object, Object> entry : props.entrySet())
        {
            if (((String)entry.getKey()).matches("^" + Pattern.quote("serverUrl") + "\\.\\d+$"))
            {
                serverUrlList.add((String) entry.getValue());
            }
        }   
   	
    	try { 	
    		
    		WebSocketClient ws = new WebSocketClient();
    		HttpClient hc = new HttpClient();
    		
	    	if (serverUrlList.size() > 0) {		
		    	int currentClients = 0;
	            while(true) {
	            	
	            	currentClients += INCREASE_CLIENTS;
	            	System.out.println("Starting " + currentClients + " clients.");
	            	
	            	for (int i = 0; i < INCREASE_CLIENTS; i++) {
	                    try {
	                    	for (String serverUrl : serverUrlList) {
	                    		try {
	                    			URI uri = new URI(serverUrl);
	                    			if (uri.getScheme().equals(WEBSOCKETS_SCHEME)) {
	                    				ws.open(uri);
	                    			} else if (uri.getScheme().equals(HTTP_SCHEME)) {
	                    				hc.open(uri);
	                    			}
	                    		} catch (Exception e) {
	                    			System.err.println(e.getMessage());
	                    		}
	                    	}
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                    try {
	                        Thread.sleep(CONNECT_INTERVAL);
	                    } catch (InterruptedException e) {
	                    }
	            	}
	                try {
	                	System.out.println(currentClients + " connected. Pausing...");
	                    Thread.sleep(INCREASE_CLIENTS_INTERVAL);
	                } catch (InterruptedException e) {
	                }
	            }
	    	}
	    
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
    	}
    }
}
