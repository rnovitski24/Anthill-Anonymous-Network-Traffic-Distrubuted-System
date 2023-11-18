import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.util.*;

import java.net.URL;


/**
 * The order server of the online bookstore.
 * 
 * 
 */
public class Drone {

  //private static XmlRpcClient DroneClient;
  
  private XmlRpcServer xmlRpcServer;
  
  private final int Port = 2054;

  private String successor;
  
  /*
   *Colony Table
   *Index 0: 2^0 nodes around ring
   *Index 1: 2^1 nodes around ring
   *Index 2: 2^2 nodes around ring
   *Index 3: 2^3 nodes around ring
   *Index 4: 2^4 nodes around ring
   */
  private int[5] colonyTable;

  public Drone(){
    
     public String getSuccessor(){
        //check that successor is valid
	//if valid 
	   //send successor
	   //assign requestor as successor
	//else check 
	   //if next successor is valid
	     //send
	     //assign requestor as successor
	   //else
	     //try index 2 on colony, cutting 1 out of chain
	     //if this addr is invalid, exit and request new BS node
     }

     public String getColonyMember(int index){
	     //returns member of colony table at index 
     }
          
    	  
    // default
  }

  private static boolean initialize_connection(String bootstrap_IP){
     //Start server
     //Connect to specefied peer or bootstrap
     //Set successor
     //Generate colony table
    //Connect to BS peer
    try {
      XmlRpcClient boostrapClient = new XmlRpcClient();
      XmlRpcClientConfigImpl configBootstrap = new XmlRpcClientConfigImpl();
      configBootstrap.setEnabledForExtensions(true);
      bootstrapClient.setConfig(configBootstrap);
      configBootstrap.setServerURL(new URL("http://" + bootstrap_IP + ":" + Port));
      //not sure how xml rpc calls work without params
      successor = (String) bootstrapClient.execute("Drone.getSuccessor",);
      colonyTable[0] = successor;      
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
      System.exit(1);
    }
    //build finger table
    //ask successor for their succesor (Index 0)
    //ask Index 0 for their 2 successor (Index 1)
    updateColony();

    //Start up xml server. 
    try{
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      WebServer server = new WebServer(5360); // may have to change port
      xmlRpcServer = server.getXmlRpcServer();
      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
      serverConfig.setEnabledForExtensions(true);
      phm.addHandler("Drone", Drone.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();

    } catch (Exception e) {
      // Handle any exceptions during server setup
      System.err.println("Server Initialization Exception: " + e);
      System.exit(1);
    }
             

  }
  private static void updateColony(){
     XmlRpcClient UpdateClient = new XmlRpcClient();
     XmlRpcClientConfigImpl configUpdate = new XmlRpcClientConfigImpl();
     configUpdate.setEnabledForExtensions(true);
     UpdateClient.setConfig(configBootstrap);
     for(int i = 0; i < 5; i++){
       configUpdate.setServerURL(new URL("http://" + colonyTable[i] + ":" + Port));

       try{
          colonyTable[i+1] = (String) updateClient.execute("Drone.getColonyMember", i);
       } catch (Exception e){
          //Error or dead node
          //Try to contact nodes after

          //Acquisition of next node fails
          //If its not the successor
          if(i>0){
             System.out.println("Error: Unable to Find Colony for Node " + i + "\n Attempt 1 Failed );
             //Ask successor to determine node after dead node
             configUpdate.setServerURL(new URL("http://" + colonyTable[0] + ":" + Port));
             try{
                colonyTable[i+1] = colonyTable[i+1] = (String) updateClient.execute("Drone.getColonyMember", i);
             } catch (Exception f){
                System.out.println("Attempt 2 Failed");
                if(debug){
                      System.out.print("Debug:\n"+f.toString());
                }
                //If this fails, ask successor's successor for the second dead node     
                configUpdate.setServerURL(new URL("http://" + colonyTable[1] + ":" + Port));
                try{
                   colonyTable[i+1] = colonyTable[i+1] = (String) bootstrapClient.execute("Drone.getColonyMember", i);
                } catch(Exception g){
                   //If all fails, terminate or could jump to next valid node is succesor's colony (can add later)
                   System.out.println("Attempt 3 Failed. Aborting... \n Unable to build colony. Try initializing connection with another seed/peer");
                   if(debug){
                      System.out.print("Debug:\n"+g.toString());
                   }
                   System.exit(0);
                }
             }
          }
          else{
             //i.e. it is the successor that is invalid
             //could cut the third node out because we actually cannot contact it


       }
 
  }

  public static void main(String[] args) {
    System.out.println(");
    /*try {
      catalogClient = new XmlRpcClient();
      XmlRpcClientConfigImpl configCatalog = new XmlRpcClientConfigImpl();
      configCatalog.setEnabledForExtensions(true);
      catalogClient.setConfig(configCatalog);
      configCatalog.setServerURL(new URL("http://" + args[0] + ":7612")); // may have to change port
      System.out.println("Catalog client connected.");
    } catch (Exception e) {
      System.err.println("Server exception: " + e);
      System.exit(1);
    }*/

    try {
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      XmlRpcServer xmlRpcServer;
      WebServer server = new WebServer(5360); // may have to change port
      xmlRpcServer = server.getXmlRpcServer();
      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
      serverConfig.setEnabledForExtensions(true);
      phm.addHandler("OrderServer", OrderServer.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();
      System.out.println("XML-RPC OrderServer started.");
    } catch (Exception e) {
      // Handle any exceptions during server setup
      System.err.println("Server exception: " + e);
      System.exit(1);
    }


    
  }

}
