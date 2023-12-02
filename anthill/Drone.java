package anthill;
import anthill.util.Response;
import anthill.util.RequestParam;
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
                        System.out.println("Colony Member at Index " + i + " is Down. Got Error: " + ex.toString());
                    }
                }
            }
        }
        System.out.println("All Nodes Invalid. Reinitalize Network.");
        return null;
    }



    private void updateColony() {
        for (int i = 0; i < COL_SIZE - 1; i++) {
            colonyTable[i + 1] = (String) doExecute(colonyTable[i], "Drone.getColonyMember", new Object[]{i});
        }
    }

    /*
     * Generalized wrapper to send XML-RPC requests between nodes.
     */
    private Object doExecute(String IP, String method, Object[] params) {
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
     * 
     */
    private Response sendRequest(int pathLength, String url, String method, HashMap<String, String> parameters){
       RequestParam request = new RequestParam(pathLength, url, method, parameters);
        url = colonyTable[rand.nextInt(COL_SIZE)];
        Response response = null;
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
            e.printStackTrace();
        }
       return response;
    }



    /*
     * Dumps colonyTable values
     */
    private void dumpColony() {
        int nodeNumber = 1;
        for (int i = 0; i < colonyTable.length; i++) {
            System.out.println("Node " + nodeNumber + ":" + colonyTable[i]);
            nodeNumber *= 2;
        }
    }

    /* ~~~~~~~~~~XML-RPC FUNCTIONS~~~~~~~~~~ */


    public Response passRequest(RequestParam request){
        System.out.println("Passing request");
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
                e.printStackTrace();
            }
        }
        else{
            try {
                return util.fullfillHttpReq(request);
            } catch(Exception e){
                e.printStackTrace();
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
    private boolean initializeNetwork() {
        // Load successor and colony table with own IP addr

        String IP = util.getPublicIP();
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
    public boolean joinNetwork(String bootstrapIP) {
        try {
            globalClient = new XmlRpcClient();
            globalConfig = new XmlRpcClientConfigImpl();
            globalConfig.setEnabledForExtensions(true);
            globalConfig.setServerURL(new URL("http://" + bootstrapIP + ":" + PORT));
            globalClient.setConfig(globalConfig);
            successor = (String) doExecute(bootstrapIP, "Drone.getSuccessor", new Object[]{localIP});
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
            colonyTable[i] = (String) doExecute(bootstrapIP, "Drone.getColonyMember", new Object[]{i});
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
        Drone ant = new Drone();

        localIP = util.getPublicIP();
        if (args.length > 0) {
            if ("--initialize".equals(args[0])) {
                ant.initializeNetwork();
            } else if ("--join".equals(args[0]) && args.length > 1) {
                ant.joinNetwork(args[1]); // assuming the second argument is the IP address to join
            }
        }
        debug = true;

        // ant.initializeNetwork();
        System.out.println(util.getPublicIP());
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
