package anthill;
import anthill.util.Response;
import anthill.util.RequestParam;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.*;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.util.*;
import java.net.URL;



import static anthill.util.getPublicIP;


public class Drone {

    private static final Logger LOGGER = Logger.getLogger(Drone.class.getName());

    // Global Server Variables
    private static XmlRpcServer droneRpcServer;
    private static XmlRpcServer xmlRpcServer;
    private static WebServer server;
    private static XmlRpcClient globalClient;
    private static XmlRpcClientConfigImpl globalConfig;

    // Debugging Boolean
    public static boolean debug;

    public static Random rand = new Random();

    // Constants
    private static final int PORT = 8560;
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
                    doExecute(potentialSuccessor, "Drone.ping", new Object[]{});
                    // If ping is successful, update successor
                    return potentialSuccessor;
                } catch (Exception ex) {
                    if (debug) {
                        LOGGER.log(Level.WARNING, "Colony Member at Index " + i + " is Down.", ex);

                    }
                }
            }
        }
        if (debug) {
            LOGGER.log(Level.SEVERE, "All Nodes Invalid. Reinitalize Network.");
            System.exit(0);
        }
        return null;
    }



    private synchronized void updateColony() {
        LOGGER.log(Level.INFO, "Updating Colony");
        for (int i = 0; i < COL_SIZE - 1; i++) {
            try {
                colonyTable[i + 1] = (String) doExecute(colonyTable[i], "Drone.getColonyMember", new Object[]{i});
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error Updating Colony Member " + (i + 1), e);
                // Do replacement
            }

            LOGGER.log(Level.FINEST, "Updated Colony Table:" + colonyTable.toString());
        }
    }

    /*
     * Generalized wrapper to send XML-RPC requests between nodes.
     */
    private Object doExecute(String IP, String method, Object[] params) throws Exception {
        LOGGER.log(Level.FINE, "Executing " + method + " At " + IP);
        if (IP.equals(localIP)) {
            IP = "localhost";
        }
        LOGGER.log(Level.FINE, "Sending request to localhost");
        try {
            globalConfig.setServerURL(new URL("http://" + IP + ":" + PORT));
            globalClient.setConfig(globalConfig);
            Object response = globalClient.execute(method, params);
            return response;

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to Execute "+ method + " At " + IP, ex);
            throw ex;
        }

    }

    /*
     * 
     */
    private util.Response sendRequest(int pathLength, String url, String method, HashMap<String, String> parameters){
        LOGGER.log(Level.INFO, "Sending Request to " + url);
        util.RequestParam request = new util.RequestParam(pathLength, url, method, parameters);
        url = colonyTable[rand.nextInt(COL_SIZE)];
        util.Response response = null;
        try{
            // forward the request
            response = (Response) doExecute(url, "Drone.passRequest", new Object[]{request});
            // if the response is "skip me"
            while (response.code == 308) {
                //Send to responder IP
                response = (Response) doExecute(response.url, "Drone.passRequest", new Object[]{request});
            }
            return response;
        } catch( Exception e){
            LOGGER.log(Level.SEVERE, "Error in sendRequest", e);
        }
       return response;
    }



    /*
     * Dumps colonyTable values
     */
    private void dumpColony() {
        LOGGER.log(Level.FINE, "Dumped Colony");
        int nodeNumber = 1;
        for (int i = 0; i < colonyTable.length; i++) {
            System.out.println("Node " + nodeNumber + ":" + colonyTable[i]);
            nodeNumber *= 2;
        }
    }

    /* ~~~~~~~~~~XML-RPC FUNCTIONS~~~~~~~~~~ */


    public Response passRequest(RequestParam request){
        LOGGER.log(Level.INFO, "Passing Request");
        String url = "";
        //Calculate whether node should skip
        if(rand.nextInt() > 0.5){
            //Then select random next in table to return
            url = colonyTable[rand.nextInt(COL_SIZE)];
            return new Response(308, url, "text/IP", null);
        }
        //if there is still path length
        if(request.pathLength > 0){
            //Decriment it
            request.pathLength -= 1;
            //Select random successor
            url = colonyTable[rand.nextInt(COL_SIZE)];
            try{
                // forward the request
                Response response = (Response) doExecute(url, "Drone.passRequest", new Object[]{request});
                // if the response is "skip me"
                while (response.code == 308) {
                    //Send to responder IP
                    response = (Response) doExecute(response.url, "Drone.passRequest", new Object[]{request});
                }
                return response;
            } catch( Exception e){
                LOGGER.log(Level.WARNING, "Unable to Forward Request", e);
            }
        }
        else{
            try {
                return util.fullfillHttpReq(request);
            } catch(Exception e){
                LOGGER.log(Level.WARNING, "Unable to Complete Request", e);
            }
        }
        return null;
    }
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
        LOGGER.log(Level.FINE , "Gave Successor To" + senderIP);
        String nextLiveSuccessor = getNextLiveSuccessor();
        colonyTable[0] = senderIP;
        return nextLiveSuccessor;
    }

    /*
     * Gets the colonyTable value at specified index.
     */
    public String getColonyMember(int index) {
        LOGGER.log(Level.FINE , "Gave Colony Member at Index " + index);
        // returns member of colony table at index
        return colonyTable[index];
    }

    /*
     * Updates bootstrap node whenever a new node joins. Only called via queen
     */
    public synchronized void updateQueenTable(String newDroneIP) {
        if (newDroneIP.equals(localIP)) {
            updateColony();
            System.out.println("Boostrap colony updated.");
        }
        else {
            System.out.println("Given IP was not the Queen node.");
        }
    }

    /* ~~~~~~~~~~INITIALIZATION FUNCTIONS:~~~~~~~~~~ */

    /*
     * Starts server when system has no current clients.
     */
    private boolean initializeNetwork() {
        // Load successor and colony table with own IP addr
        LOGGER.log(Level.FINE , "Initializing Network");
        String IP = null;
        try{
            IP = util.getPublicIP();
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Unable to Obtain Local IP", e);
            System.exit(1);
        }
        successor = IP;
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
            LOGGER.log(Level.SEVERE, "Server Initialization Exception", e);
            System.exit(1);
        }

        return false;
    }

    /*
     * Starts server and populates colonyTable when system has > 1 client.
     */
    public boolean joinNetwork(String bootstrapIP) {
        LOGGER.log(Level.FINE , "Joining Network at " + bootstrapIP);

        if (!bootstrapIP.equals(localIP)) {
            try {
                doExecute(bootstrapIP, "Drone.updateQueenTable", new Object[]{localIP});
            }catch (Exception e){
                LOGGER.log(Level.SEVERE, "Unable to Update Queen Table", e);
            }
        }

        try {
            globalClient = new XmlRpcClient();
            globalConfig = new XmlRpcClientConfigImpl();
            globalConfig.setEnabledForExtensions(true);
            globalConfig.setServerURL(new URL("http://" + bootstrapIP + ":" + PORT));
            globalClient.setConfig(globalConfig);
            successor = (String) doExecute(bootstrapIP, "Drone.getSuccessor", new Object[]{localIP});
            LOGGER.log(Level.FINE , "Got Successor " + successor);
            colonyTable[0] = successor;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Bootstrap Client Exception" , e);
            System.exit(1);
        }
        // populate colonyTable with bootstrap's table
        try{
            for (int i = 1; i < COL_SIZE; i++) {
                colonyTable[i] = (String) doExecute(bootstrapIP, "Drone.getColonyMember", new Object[]{i});
            }
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Bootstrap Transfer Error", e );
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
                LOGGER.log(Level.SEVERE, "Server Initialization Exception", e);
                System.exit(1);
            }
            updateColony();
        }

        return true;
    }



    /* ~~~~~~~~~~MAIN METHOD:~~~~~~~~~~ */

    public static void main(String[] args) {
        Handler fileHandler = null;
        try {
             fileHandler = new FileHandler("logs/Drone%u.log", 0,1);
        } catch(Exception e ){
            e.printStackTrace();
        }
        fileHandler.setFormatter(new SimpleFormatter());
        //setting custom filter for FileHandler
        fileHandler.setLevel(Level.ALL);

        LOGGER.addHandler(fileHandler);
        LOGGER.setUseParentHandlers(false);


        Drone ant = new Drone();
        try {
            localIP = util.getPublicIP();
        } catch(Exception e){
            LOGGER.log(Level.WARNING, "Unable to Get Local IP", e);
        }
        if (args.length > 0) {
            if ("--initialize".equals(args[0])) {
                ant.initializeNetwork();
            } else if ("--join".equals(args[0]) && args.length > 1) {
                ant.joinNetwork(args[1]); // assuming the second argument is the IP address to join
            } else if ("--ping".equals(args[0])) {
                ant.ping();
            }
        } else {
            System.exit(0);
        }

        // ant.initializeNetwork();

        ant.dumpColony();
        //Response response = ant.sendRequest(6, "https://cds.cern.ch/record/2725767/files/dimuons.png",
                //"get", new HashMap<String, String>());
        //System.out.println(response.dataType);

        //String filename = response.url.substring(response.url.lastIndexOf('/') + 1);

        //System.out.println(PageDisplay.savePhoto(response.dataType, filename, response.data));
        //System.exit(0);
        while(true){
            try {
                Thread.sleep(10000);
            } catch (Exception e){
                System.out.println("No Sleep");
            }
            ant.updateColony();
            ant.dumpColony();
        }
    }

}
