import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.net.MalformedURLException;
import javax.servlet.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;

import java.net.NetworkInterface;
import java.util.Enumeration;


public class Drone {
   //Global Server Variables
   private static XmlRpcServer droneRpcServer;
   private static XmlRpcServer xmlRpcServer;
   private static WebServer server;
   private static XmlRpcClient globalClient;
   private static XmlRpcClientConfigImpl globalConfig;

   //Debugging Boolean
   private static boolean debug;
   //Constants
   private static final int PORT = 2054;
   private static final int COL_SIZE = 5;
   //Variable that holds next link in chain
   private static String successor;
   
   private static String localIP;
   /*
    *Colony Table
    *Index 0: 2^0 nodes around ring
    *Index 1: 2^1 nodes around ring
    *Index 2: 2^2 nodes around ring
    *Index 3: 2^3 nodes around ring
    *Index 4: 2^4 nodes around ring
    */
    private static String[] colonyTable = new String[COL_SIZE];

    public Drone(){
       // default constructor
    }

   /*
    * Server up confirmation function
    */
    public boolean ping() {
       return true;
    }

    
    private String getNextLiveSuccessor() {
       // Checks if machine at specified colony table index is available
       // RETURNS IP address if available, and null if unavailable
       for(int i = 0; i < COL_SIZE; i++){
          String potentialSuccessor = colonyTable[i];
          if (potentialSuccessor != null && !potentialSuccessor.equals(successor)) {
             try {
                doExecute(potentialSuccessor, "Drone.ping", new Object[]{});
		// If ping is successful, update successor
                return potentialSuccessor;
             } catch (Exception ex) {
                if(debug){
		   System.out.println("Colony Member at Index " + i + " is Down. Got Error: " + ex.toString());
		} 
             }  
          }
       }
       System.out.println("All Nodes Invalid. Reinitalize Network.");
       return null;
   } 

   public String getSuccessor(String senderIP) {
      successor = senderIP;
      return getNextLiveSuccessor();
   }
   
   public String getColonyMember(int index) {
      //returns member of colony table at index
      return colonyTable[index];
   }


   private static boolean initializeNetwork(){
      //Load successor and colony table with own IP addr
     
      String IP = getPrivateIP();
      successor = IP;
      System.out.println(IP);
      for(int i = 0; i <  colonyTable.length; i++){
         colonyTable[i]= IP;
      }
      try{
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         WebServer server  = new WebServer(PORT); // may have to change port
         xmlRpcServer = server.getXmlRpcServer();
         XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
         serverConfig.setEnabledForExtensions(true);
         phm.addHandler("Drone", Drone.class);
         xmlRpcServer.setHandlerMapping(phm);
         server.start();

      } catch (Exception e) {
         // Handle any exceptions during server setup
         System.err.println("Server Initialization Exception: " + e.toString());
         e.printStackTrace();
         System.exit(1);
      }

      return false;
   }


   private static String getPublicIP(){
      String urlString = "http://checkip.amazonaws.com/";
      try {
	 URL url = new URL(urlString);
         BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
	 return br.readLine();
      }catch(Exception e){
         System.out.print("Unable to contact IP server");
	 return null;
      }
   }

   public static String getPrivateIP() {
      try {
         Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
         while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
               InetAddress ia = inetAddresses.nextElement();
               if (!ia.isLoopbackAddress() && ia.isSiteLocalAddress()) {
                  return ia.getHostAddress();
               }
            }
         }
      } catch (Exception e) {
         System.err.println("Error getting private IP: " + e.getMessage());
      }
      return null;
    }
  
  

  public static boolean joinNetwork(String bootstrapIP){
     //Start server
     //Connect to specefied peer or bootstrap
     //Set successor
     //Generate colony table
    //Connect to BS peer
    try{
      globalClient = new XmlRpcClient();
      globalConfig = new XmlRpcClientConfigImpl();
      globalConfig.setEnabledForExtensions(true);
      globalConfig.setServerURL(new URL("http://" + bootstrapIP + ":" + PORT));
      globalClient.setConfig(globalConfig);
      //not sure how xml rpc calls work without params
      successor = (String) doExecute(bootstrapIP, "Drone.getSuccessor", new Object[]{localIP});
      System.out.print("I got to joinNetwork\n");
      System.out.print(successor);
      colonyTable[0] = successor;      
    } catch (Exception e) {
      if(debug){
         System.err.println("Bootstrap Client exception: " + e);
      }else{
	 System.out.println("Client Initalization Error");
      }
      System.exit(1);
    }
    //populate Colony with bs table
    for(int i = 0; i < COL_SIZE; i++){
	    colonyTable[i] = (String) doExecute(bootstrapIP, "Drone.getColonyMember", new Object[]{i});
    }


    //ask successor for their succesor (Index 0)
    //ask Index 0 for their 2 successor (Index 1)
    if(bootstrapIP == null){
       initializeNetwork();
    }
    else{
      //Start up xml server. 
    try{
      //Setup for server. Still needs to be fixed. 
      //Don't know how the servlet architecture maps on XML Rpc
      //But must be used to get the client IP
      //See DroneServlet.java
      //FIXME
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      WebServer server  = new WebServer(PORT); // may have to change port
      droneRpcServer = server.getXmlRpcServer();
      XmlRpcServerConfigImpl droneServerConfig = (XmlRpcServerConfigImpl) droneRpcServer.getConfig();
      droneServerConfig.setEnabledForExtensions(true);
      phm.addHandler("Drone", Drone.class);
      droneRpcServer.setHandlerMapping(phm);
      server.start();
    } catch (Exception e) {
      // Handle any exceptions during server setup
      System.err.println("Server Initialization Exception: " + e);
      System.exit(1);
    }
	    updateColony();
    }
             
    return true;
  }







  private static void updateColony(){
    for( int i = 0; i < COL_SIZE-1; i++){
       colonyTable[i+1] = (String) doExecute(colonyTable[i], "Drone.getColonyMember", new Object[]{i});
    }
	  
	  
	  
	  
	  
	  
	  
	  /* try{
     globalClient = new XmlRpcClient();
     globalConfig = new XmlRpcClientConfigImpl();
     globalConfig.setEnabledForExtensions(true);
     globalClient.setConfig(globalConfig);
     for(int i = 0; i < colonyTable.length - 1; i++){
       try{
          colonyTable[i+1] = (String) doExecute(colonyTable[i],"Drone.getColonyMember", new Object[]{i});
       } catch (Exception e){
          if(i>0){
             System.out.println("Error: Unable to Find Colony for Node " + i + "\nAttempt 1 Failed");
             if(debug){
		     e.printStackTrace();
	     }
	     //Ask successor to determine node after dead node
             try{
               colonyTable[i+1] = (String) doExecute(colonyTable[0],"Drone.getColonyMember", new Object[]{i});
             } catch (Exception f){
                System.out.println("Attempt 2 Failed");
		if(debug){
		   f.printStackTrace();
		}
               //  }
                //If this fails, ask successor's successor for the second dead node     
                //globalConfig.setServerURL(new URL("http://" + colonyTable[1] + ":" + PORT));
                try{
                  colonyTable[i+1] = (String) doExecute(colonyTable[1],"Drone.getColonyMember", new Object[]{i});
                } catch(Exception g){
		  if(debug){
		     g.printStackTrace();
		  }	     
                  if (i < colonyTable.length - 1) {
                     colonyTable[i+1] = null;
                  }
                   //If all fails, terminate or could jump to next valid node is succesor's colony (can add later)
                  //  System.out.println("Attempt 3 Failed. Aborting... \n Unable to build colony. Try initializing connection with another seed/peer");
                  //  if(debug){
                  //     System.out.print("Debug:\n"+g.toString());
                  //  }
                  //  System.exit(0);
                }
             }
          }
          else{
             //i.e. it is the successor that is invalid
             //could cut the third node out because we actually cannot contact it


	  }    
       }
 
  }} catch(Exception e){
	  System.out.println("Malformed URL");
  }*/
  }


  private static Object doExecute( String IP, String method, Object[] params){
	  if(IP.equals(localIP)){
		  IP = "localhost";
	  }
	  try{
             globalConfig.setServerURL(new URL("http://" + IP + ":" + PORT));
             return globalClient.execute(method, params);

          } catch (Exception ex){
             ex.printStackTrace();
	  }
	  return "Error";
  }
	     
  private boolean doPing(String dest_IP){
	  if(dest_IP.equals(localIP)){
	     return true;
	  }
	  try{
	     globalConfig.setServerURL(new URL("http://" + colonyTable[1] + ":" + PORT));
             globalClient.execute("Drone.execute", new Object[]{});
  
	  } catch (Exception ex){
             return false;
	  }
	  return true;
  }

  private static void dumpColony(){
     int nodeNumber = 1;
     for(int i = 0; i< colonyTable.length; i++){
	System.out.println("Node " + nodeNumber  + ":" + colonyTable[i]);
        nodeNumber*=2;
     }
  }

  public static void main(String[] args) {
    localIP = getPrivateIP();
    debug = true;
    Scanner usrIn = new Scanner(System.in);
    System.out.println("Join or Initialize Network");
    String mode = usrIn.nextLine();
    if(mode.toLowerCase().equals("join")){
       System.out.println("Enter Bootstrap IP");
       String boot_ip = usrIn.nextLine();
       joinNetwork(boot_ip);
    } else if(mode.toLowerCase().equals("initialize")){
       initializeNetwork();
    } else{
       System.out.println("Invalid command exiting");
       System.exit(1);
    }
    dumpColony();
    updateColony();
    dumpColony();
    System.out.println(localIP);
    try{
	    doExecute(localIP, "Drone.ping", new Object[]{});
    } catch(Exception ex){
	    System.out.println("Ping doesn't work on itself " + ex.toString());
	    ex.printStackTrace(); 
    }
    //joinNetwork("172.31.40.145");
    System.out.println("Network joined!");
    //dumpColony();

  }

}
