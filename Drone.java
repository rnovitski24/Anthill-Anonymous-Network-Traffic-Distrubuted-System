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

   // Global Server Variables
   private static XmlRpcServer droneRpcServer;
   private static XmlRpcServer xmlRpcServer;
   private static WebServer server;
   private static XmlRpcClient globalClient;
   private static XmlRpcClientConfigImpl globalConfig;

   // Debugging Boolean
   private static boolean debug;

   // Constants
   private static final int PORT = 2054;
   private static final int COL_SIZE = 5;

   // Variable that holds next link in chain
   private static String successor;

   // Local IP Storage
   private static String localIP;

   /*
    * Colony Table
    * Index 0: 2^0 nodes around ring
    * Index 1: 2^1 nodes around ring
    * Index 2: 2^2 nodes around ring
    * Index 3: 2^3 nodes around ring
    * Index 4: 2^4 nodes around ring
    */
   private static String[] colonyTable = new String[COL_SIZE];

   /*
    * Constructor for Drone class
    */
   public Drone() {
      // default
   }

   /* ~~~~~~~~~~HELPER FUNCTIONS:~~~~~~~~~~ */

   /*
    * Checks if machine at specified colony table index is available
    * RETURNS IP address if available, and null if unavailable
    */
   private String getNextLiveSuccessor() {
      for (int i = 0; i < COL_SIZE; i++) {
         String potentialSuccessor = colonyTable[i];
         if (potentialSuccessor != null) {
            if (potentialSuccessor.equals(colonyTable[0])) {
               return colonyTable[0];
            }
            try {
               doExecute(potentialSuccessor, "Drone.ping", new Object[] {});
               // If ping is successful, update successor
               return potentialSuccessor;
            } catch (Exception ex) {
               if (debug) {
                  System.out.println("Colony Member at Index " + i + " is Down. Got Error: " + ex.toString());
               }
            }
         }
      }
      System.out.println("All Nodes Invalid. Reinitalize Network.");
      return null;
   }

   /*
    * Returns the private IP of current machine, or null if error is encountered.
    */
   private static String getPrivateIP() {
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

   private static void updateColony() {
      for (int i = 0; i < COL_SIZE - 1; i++) {
         colonyTable[i + 1] = (String) doExecute(colonyTable[i], "Drone.getColonyMember", new Object[] { i });
      }
   }

   /*
    * Generalized wrapper to send XML-RPC requests.
    */
   private static Object doExecute(String IP, String method, Object[] params) {
      System.out.println(IP);
      if (IP.equals(localIP)) {
         IP = "localhost";
      }
      System.out.println(IP);
      try {
         globalConfig.setServerURL(new URL("http://" + IP + ":" + PORT));
         globalClient.setConfig(globalConfig);
         Object response = globalClient.execute(method, params);
         if (debug) {
            System.out.println("Response:" + response);
         }
         return response;

      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return null;
   }

   /*
    * Dumps colonyTable values
    */
   private static void dumpColony() {
      int nodeNumber = 1;
      for (int i = 0; i < colonyTable.length; i++) {
         System.out.println("Node " + nodeNumber + ":" + colonyTable[i]);
         nodeNumber *= 2;
      }
   }

   /* ~~~~~~~~~~XML-RPC FUNCTIONS~~~~~~~~~~ */

   /*
    * Server up confirmation function.
    */
   public boolean ping() {
      return true;
   }

   /*
    * Gets first successor to respond and stores sender's IP as its own first
    * successor.
    */
   public String getSuccessor(String senderIP) {
      String nextLiveSuccessor = getNextLiveSuccessor();
      colonyTable[0] = senderIP;
      return nextLiveSuccessor;
   }

   /*
    * Gets the colonyTable value at specified index.
    */
   public String getColonyMember(int index) {
      // returns member of colony table at index
      System.out.println("Sent IP:" + colonyTable[index]);
      return colonyTable[index];
   }

   /* ~~~~~~~~~~INITIALIZATION FUNCTIONS:~~~~~~~~~~ */

   /*
    * Starts server when system has no current clients.
    */
   private static boolean initializeNetwork() {
      // Load successor and colony table with own IP addr

      String IP = getPrivateIP();
      successor = IP;
      System.out.println(IP);
      for (int i = 0; i < colonyTable.length; i++) {
         colonyTable[i] = IP;
      }
      globalClient = new XmlRpcClient();
      globalConfig = new XmlRpcClientConfigImpl();
      globalConfig.setEnabledForExtensions(true);
      globalClient.setConfig(globalConfig);
      try {
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         WebServer server = new WebServer(PORT); // may have to change port
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

   /*
    * Starts server and populates colonyTable when system has > 1 client.
    */
   public static boolean joinNetwork(String bootstrapIP) {
      try {
         globalClient = new XmlRpcClient();
         globalConfig = new XmlRpcClientConfigImpl();
         globalConfig.setEnabledForExtensions(true);
         globalConfig.setServerURL(new URL("http://" + bootstrapIP + ":" + PORT));
         globalClient.setConfig(globalConfig);
         successor = (String) doExecute(bootstrapIP, "Drone.getSuccessor", new Object[] { localIP });
         System.out.print("I got to joinNetwork\n");
         System.out.print(successor);
         colonyTable[0] = successor;
      } catch (Exception e) {
         if (debug) {
            System.err.println("Bootstrap Client exception: " + e);
         } else {
            System.out.println("Client Initalization Error");
         }
         System.exit(1);
      }
      // populate colonyTable with bootstrap's table
      for (int i = 1; i < COL_SIZE; i++) {
         colonyTable[i] = (String) doExecute(bootstrapIP, "Drone.getColonyMember", new Object[] { i });
      }

      // ask successor for their succesor (Index 0)
      // ask Index 0 for their 2 successor (Index 1)
      if (bootstrapIP == null) {
         initializeNetwork();
      } else {
         // Start up xml server.
         try {
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            WebServer server = new WebServer(PORT); // may have to change port
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

   /* ~~~~~~~~~~MAIN METHOD:~~~~~~~~~~ */

   public static void main(String[] args) {
      localIP = getPrivateIP();
      debug = true;
      Scanner usrIn = new Scanner(System.in);
      System.out.println("Join or Initialize Network");
      String mode = usrIn.nextLine();
      if (mode.toLowerCase().equals("join")) {
         System.out.println("Enter Bootstrap IP");
         String boot_ip = usrIn.nextLine();
         joinNetwork(boot_ip);
      } else if (mode.toLowerCase().equals("initialize")) {
         initializeNetwork();
      } else {
         System.out.println("Invalid command exiting");
         System.exit(1);
      }
      dumpColony();
      updateColony();
      dumpColony();
      System.out.println(localIP);
      try {
         doExecute(localIP, "Drone.ping", new Object[] {});
      } catch (Exception ex) {
         System.out.println("Ping doesn't work on itself " + ex.toString());
         ex.printStackTrace();
      }
      System.out.println("Network joined!");

      while (true) {
         try {
            Thread.sleep(10000);
         } catch (Exception e) {
         }
         dumpColony();
         updateColony();
      }

   }

}
