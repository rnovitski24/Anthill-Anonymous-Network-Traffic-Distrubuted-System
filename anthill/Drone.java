package anthill;

import anthill.util.Response;
import anthill.util.RequestParam;

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

/**
 * General Class That Runs A Node Operating in the AntHill Framework
 */
public class Drone {
    // Initialize Logger
    private static final Logger LOGGER = Logger.getLogger(Drone.class.getName());
    private static int LOG_PORT = 8888;

    // Global Server Variables
    private static XmlRpcServer droneRpcServer;
    private static XmlRpcServer xmlRpcServer;
    private static XmlRpcClient globalClient;
    private static XmlRpcClientConfigImpl globalConfig;
    // Random Class for Pass Request
    public static Random rand = new Random();

    // Constants
    private static int PORT = 8560;
    private static final int COL_SIZE = 4;

    // Variable that holds next link in chain
    private static String successor;

    // Local IP Storage
    private static String localIP;

    // List storing any down nodes currently in colonyTable
    private static final List<String> downDrones = new ArrayList<>();

    /*
     * Colony Table
     * Index 0: 2^0 nodes around ring
     * Index 1: 2^1 nodes around ring
     * Index 2: 2^2 nodes around ring
     * Index 3: 2^3 nodes around ring
     * Index 4: 2^4 nodes around ring
     */
    private static String[] colonyTable = new String[COL_SIZE];

    /**
     * Constructor for Drone class
     */
    public Drone() {
        // default
    }

    /* ~~~~~~~~~~HELPER FUNCTIONS:~~~~~~~~~~ */

    /**
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
                    LOGGER.log(Level.WARNING, "Colony Member at Index " + i + " is Down.", ex);
                }
            }
        }
        LOGGER.log(Level.SEVERE, "All Nodes Invalid. Reinitalize Network.");
        System.exit(0);
        return null;
    }

    /**
     * Scans each node in colony table and returns any changes in status
     */
    protected void scanTable() {
        for (int i = 0; i < colonyTable.length; i++) {
            try {
                doExecute(colonyTable[i], "Drone.ping", new Object[]{});
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not ping Colony Member at " + (colonyTable[i]));
                // if last node in colonyTable, begin update sequence
                if (i == (COL_SIZE - 1)) {
                    LOGGER.log(Level.INFO, "Replacing Member " + (colonyTable[i]));
                    colonyTable = syncTables(colonyTable[i], i, colonyTable);
                    // reset down nodes
                    downDrones.clear();
                } else {
                    // If Down and Not Last, Add to list of Down
                    downDrones.add(colonyTable[i]);
                }
            }
        }
    }

    /**
     * Generalized wrapper to send XML-RPC requests between nodes.
     **/
    private synchronized Object doExecute(String IP, String method, Object[] params) throws Exception {
        LOGGER.log(Level.FINEST, "Executing " + method + " At " + IP);
        if (IP.equals(localIP)) {
            IP = "localhost";
        }
        LOGGER.log(Level.FINEST, "Sending request to localhost");
        try {
            globalConfig.setServerURL(new URL("http://" + IP + ":" + PORT));
            globalClient.setConfig(globalConfig);
            Object response = globalClient.execute(method, params);
            return response;

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to Execute " + method + " At " + IP);

            throw ex;
        }

    }

    /**
     * Remake colony if a node is down or if a new node joins
     */
    private String findLiveDrone(String url) {
        boolean alive = false;
        List<String> liveNodes = Arrays.asList(colonyTable);
        while (!alive) {
            if (downDrones.contains(url)) {
                liveNodes.remove(url);
                url = liveNodes.get(rand.nextInt(liveNodes.size()));
            } else {
                return url;
            }
        }
        return null;
    }

    /**
     * Initiates sending a request through the AntHill Network
     */
    private synchronized Response sendRequest(int pathLength, String url, String method, HashMap<String, String> parameters) {
        LOGGER.log(Level.INFO, "Sending Request to " + url);
        RequestParam request = new RequestParam(pathLength, url, method, parameters);
        url = colonyTable[rand.nextInt(COL_SIZE)];
        if (downDrones.contains(url)) {
            url = findLiveDrone(url);
        }
        util.Response response = null;
        try {
            // forward the request
            response = (Response) doExecute(url, "Drone.passRequest", new Object[]{request});
            // if the response is "skip me"
            while (response.code == 308) {
                //Send to responder IP
                response = (Response) doExecute(response.url, "Drone.passRequest", new Object[]{request});
            }
            return response;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in sendRequest", e);
        }
        return response;
    }

    /**
     * Dumps colonyTable values
     */
    void dumpColony() {
        LOGGER.log(Level.INFO, "Dumped Colony");
        int nodeNumber = 1;
        for (int i = 0; i < COL_SIZE; i++) {
            System.out.println("Node: " + nodeNumber + ":" + colonyTable[i]);
            nodeNumber *= 2;
        }
    }


    /* ~~~~~~~~~~XML-RPC FUNCTIONS~~~~~~~~~~ */

    /**
     * Receives request and either responds with the response in form Response or returns IP that request should be sent to
     * instead. When the request is actually processed, the server can either pass the request or fullfill it depending
     * on path length
     *
     * @param request
     * @return Response
     */
    public synchronized Response passRequest(RequestParam request) {
        LOGGER.log(Level.INFO, "Passing Request");
        String url = "";
        //Calculate whether node should skip
        if (rand.nextInt() > 0.5) {
            //Then select random next in table to return
            url = colonyTable[rand.nextInt(COL_SIZE)];
            if (downDrones.contains(url)) {
                url = findLiveDrone(url);
            }
            return new Response(308, url, "text/IP", null);
        }
        //if there is still path length
        if (request.pathLength > 0) {
            //Decriment it
            request.pathLength -= 1;
            //Select random successor
            url = colonyTable[rand.nextInt(COL_SIZE)];
            if (downDrones.contains(url)) {
                url = findLiveDrone(url);
            }
            try {
                // forward the request
                Response response = (Response) doExecute(url, "Drone.passRequest", new Object[]{request});
                // if the response is "skip me"
                while (response.code == 308) {
                    //Send to responder IP
                    response = (Response) doExecute(response.url, "Drone.passRequest", new Object[]{request});
                }
                return response;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to Forward Request", e);
            }
        } else {
            try {
                return util.fullfillHttpReq(request);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to Complete Request", e);
            }
        }
        return null;
    }

    /**
     * Server up confirmation function.
     */
    public boolean ping() {
        return true;
    }

    /**
     *
     */
    public synchronized String[] addNode(String senderIP) throws Exception {
        String[] newCol = new String[COL_SIZE];
        if(Arrays.stream(colonyTable).distinct().count() <= 1){
            colonyTable[0] = senderIP;
            Arrays.fill(newCol, senderIP);
            newCol[0] = localIP;
            return newCol;
        }

        try{
            //Get IP of Predecessor of end of col table
            String tailIP = (String) doExecute(colonyTable[2], "Drone.getColonyMember", new Object[]{COL_SIZE-2});
            //take colony from the final member
            for(int i = 0; i < COL_SIZE; i++){
                newCol[i] = (String) doExecute(tailIP, "Drone.getColonyMember",
                        new Object[]{i});
            }
        } catch(Exception e){
            LOGGER.log(Level.SEVERE, "Final Node of BS Down", e);
            System.exit(1);
        }
        String[] replace = new String[COL_SIZE];
        Arrays.fill(replace, "");


        //This should immediately begin the replacement policy
        replaceNode((int) Math.pow(2, colonyTable.length), replace, colonyTable[COL_SIZE - 1], senderIP);
        return newCol;



    }

    public synchronized boolean replaceNode(int iter, Object[] rep, String current, String replacement) throws Exception {
        String[] replace = Arrays.stream(rep).toArray(String[]::new);
        String[] oldCol = colonyTable.clone();
        LOGGER.log(Level.INFO, "Propagating node replacement " + replacement + " iter: " + iter );
        // If it's the end of propagation, make sure it is correct;
        if(iter == 0) {
            LOGGER.log(Level.INFO, "Finished Prop");
            return true;
           /*if (localIP.equals(replacement)) {
                LOGGER.log(Level.INFO, "Finished Prop");
                return true;
            }else{
                LOGGER.log(Level.SEVERE, "Propagation Error. FATAL");
                dumpColony();
                System.exit(1);
            }*/
        }
        for( int i = 0; i<COL_SIZE; i++){
            // If its supposed to be replaced swap the value
            if(!replace[i].isEmpty()){
                LOGGER.info("Replacing node "+ colonyTable[i] + " with " + replace[i]);
                String temp = colonyTable[i];
                colonyTable[i] = replace[i];
                replace[i] = temp;
            }
            // If it's the node being replaced and make sure its only one at a time
            else if(colonyTable[i].equals(current) && iter == Math.pow(2, i)){
                LOGGER.info("Starting the replacing node "+ colonyTable[i] + " with " + replace[i]);
                replace[i] = colonyTable[i];
                colonyTable[i] = replacement;
            }
        }
        //dumpColony();
        try{
            return (boolean) doExecute(colonyTable[0], "Drone.replaceNode", new Object[]{iter-1, replace, current, replacement});
        } catch(Exception e){
            LOGGER.log(Level.SEVERE, "Unable to propagate replacement further at node:" + oldCol[0]);
            throw e;

        }


    }

    /**
     * Gets the colonyTable value at specified index.
     */
    public synchronized String getColonyMember(int index) {
        LOGGER.log(Level.FINEST, "Gave Colony Member at Index " + index);
        // returns member of colony table at index
        return colonyTable[index];
    }

    /**
     * Recursive Function That Propagates Out The Dead Node
     *
     * @param downIP
     * @param downIndex
     * @param cTable
     * @return
     */
    public String[] syncTables(String downIP, int downIndex, String[] cTable) {
        // Section of updated table that previous node needs
        // DownIndex of -1 means not in table
        String[] newTable = null;
        // DownIndex of -1 means not in table
        // If the down node is in the table.
        if (!(downIndex == -1)) {
            // Create the new table to replace colony
            newTable = new String[COL_SIZE - downIndex];
            // if the down node is the first successor
            if (downIndex == 0) {
                // copy the second successor
                newTable[0] = colonyTable[1];
                // Populate the rest of the successors
                for (int i = 1; i < COL_SIZE; i++) {
                    try {
                        // new ith successor is the current ith successor's i-1th successor
                        newTable[i] = (String) doExecute(colonyTable[i], "Drone.getColonyMember", new Object[]{i - 1});
                    } catch (Exception e) {
                        // Add to list of downed drones
                        downDrones.add(colonyTable[i]);
                        LOGGER.log(Level.WARNING, "Node down when when repopulating syncing table", e);
                        // if its not the second successor
                        if (i > 1) {
                            try {
                                // Try to contact another node to get the same address
                                String interIP = (String) doExecute(colonyTable[i - 2], "Drone.getColonyMember", new Object[]{i - 1});
                                newTable[i] = (String) doExecute(interIP, "Drone.getColonyMember", new Object[]{i - 2});
                            } catch (Exception f) {
                                LOGGER.log(Level.SEVERE, "FATAL: Backup Node down when when repopulating syncing table", f);
                                System.exit(1);
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "FATAL: Second Node down when when repopulating syncing table", e);
                            System.exit(1);
                        }
                    }
                }

            } else {
                // Otherwise transfer the remainder of the table
                if (COL_SIZE - downIndex >= 0)
                    System.arraycopy(colonyTable, 0 + downIndex, newTable, 0, COL_SIZE - downIndex);

            }
        }
        // For nodes that are not in the predecessors table
        else {
            // checks to see if any of its nodes are the down IP
            for (int i = 0; i < COL_SIZE; i++) {
                String[] response = null;
                // If down node is in table:
                if (colonyTable[i].equals(downIP)) {
                    // sends message to its first successor from where it is down
                    try {
                        response = (String[]) doExecute(colonyTable[0], "Drone.syncTables", new Object[]{downIP, i, colonyTable});
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "FATAL: Recursive call to syncTable did not work as expected ", e);
                        System.exit(1);
                    }
                    if (response != null) {
                        int inc = 0;
                        // repopulate table with new values from successor
                        for (int s = i; s < COL_SIZE; s++) {
                            colonyTable[s] = response[inc];
                            inc++;
                        }
                        return null;
                    }
                }
            }

            // If down node is not in table, check each value for duplicates and replace with first successor if needed
            for (int j = 0; j < COL_SIZE; j++) {
                String newValue = null;
                if (colonyTable[j].equals(cTable[j])) {
                    try {
                        newValue = (String) doExecute(colonyTable[j], "Drone.getFirst", new Object[]{});
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Could not find node in colonyTable ", e);
                    }
                    colonyTable[j] = newValue;
                }
            }
            try {
                doExecute(colonyTable[0], "Drone.syncTables", new Object[]{downIP, -1, colonyTable});
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Recursive call to syncTable did not work as expected ", e);
            }


        }
        return newTable;
    }

    /**
     * A method to grab the first value in colonyTable without pinging (to allow synchronicity of down node sweeping)
     */
    public String getFirst() {
        return colonyTable[0];
    }


    /* ~~~~~~~~~~INITIALIZATION FUNCTIONS:~~~~~~~~~~ */


    private boolean startClient(){
        globalClient = new XmlRpcClient();
        globalConfig = new XmlRpcClientConfigImpl();
        globalConfig.setEnabledForExtensions(true);
        globalClient.setConfig(globalConfig);
        return true;
    }
    private boolean startServer(){
        try {
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            WebServer server = new WebServer(PORT); // may have to change port
            xmlRpcServer = server.getXmlRpcServer();
            XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
            serverConfig.setEnabledForExtensions(true);
            phm.addHandler("Drone", Drone.class);
            xmlRpcServer.setHandlerMapping(phm);
            server.start();
            return true;

        } catch (Exception e) {
            // Handle any exceptions during server setup
            LOGGER.log(Level.SEVERE, "Server Initialization Exception", e);
            System.exit(1);
            return false;
        }
    }
    /**
     * Starts server when system has no current clients.
     */
    private boolean initializeNetwork() {
        // Load successor and colony table with own IP addr
        LOGGER.log(Level.INFO, "Initializing Network");
        String IP = null;
        try {
            IP = util.getPublicIP();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to Obtain Local IP", e);
            System.exit(1);
        }
        successor = IP;
        Arrays.fill(colonyTable, IP);
        return startServer() && startClient();
    }

    /**
     * Starts server and populates colonyTable when system has > 1 client.
     */
    public boolean joinNetwork(String bootstrapIP) {
        LOGGER.log(Level.FINE, "Joining Network at " + bootstrapIP);
        try {
            startClient();
            startServer();
            globalConfig.setServerURL(new URL("http://" + bootstrapIP + ":" + PORT));
            Object[] temp = (Object[]) doExecute(bootstrapIP, "Drone.addNode", new Object[]{localIP});
            colonyTable = Arrays.stream(temp).toArray(String[]::new);
            LOGGER.log(Level.FINE, "Got Successor " + colonyTable[0]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Bootstrap Client Exception", e);
            System.exit(1);
        }
        // Start up xml server.
        return true;
    }



    /* ~~~~~~~~~~MAIN METHOD:~~~~~~~~~~ */

    public static void main(String[] args) {
        Drone ant = new Drone();
        // parse input
        boolean init = false;
        boolean join = false;
        boolean log = false;
        boolean background = false;
        String boot = "";
        String logIP = "";
        Level logLevel = Level.ALL;
        // Parse command line args
        for (int i = 0; i < args.length; i++) {
            if ("--initialize".equals(args[i])) {
                init = true;
            } else if ("--join".equals(args[i])) {
                join = true;
                boot = args[i + 1];
                i++;
                // assuming the second argument is the IP address to join
            } else if ("--ping".equals(args[i])) {
                ant.ping();
            } else if ("--log".equals(args[i])) {
                log = true;
                logIP = args[i + 1];
                i++;
            } else if ("--background".equals(args[i])) {
                background = true;
            }else if ("-p".equals(args[i])){
                PORT = Integer.parseInt(args[i+1]);
                i++;
            } else {
                System.out.println("Invalid Parameters");
                System.exit(0);
            }
        }

        // Setup Logging
        Handler fileHandler = null;
        Handler socketHandler = null;

        try {
            fileHandler = new FileHandler("logs/Drone%u.log", 0, 10);
            //System.out.println("Connecting to logServer on port 8809");

        } catch (Exception e) {
            e.printStackTrace();
        }
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(logLevel);
        LOGGER.addHandler(fileHandler);

        if (log) {
            try {
                socketHandler = new SocketHandler(logIP, LOG_PORT);
                System.out.println("Connecting to logServer at " + logIP + "on port 8809");

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            socketHandler.setFormatter(new SimpleFormatter());
            socketHandler.setLevel(logLevel);
            LOGGER.addHandler(socketHandler);
            LOGGER.setUseParentHandlers(false);
        }
        LOGGER.setLevel(logLevel);
        if (background) {
            LOGGER.setUseParentHandlers(false);
        }


        try {
            localIP = util.getPublicIP();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to Get Local IP", e);
            System.exit(1);
        }

        if (init) {
            ant.initializeNetwork();
        }
        if (join) {
            ant.joinNetwork(boot);
        }
        if (!background) {
            ant.dumpColony();
        }
        // Starts background updater
        util.Updater updater = new util.Updater(ant);
        new Thread(updater).start();

        Scanner scan = new Scanner(System.in);
        while (!background) {
            System.out.println("Send, Info, or Quit");
            String command;


            try {
                command = scan.nextLine();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Running in Background");
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception y) {
                        y.printStackTrace();
                    }
                }
            }
            switch(command) {
                case "q":
                    System.exit(0);
                    break;
                case "i":
                    ant.dumpColony();
                    break;
                case "s":
                    System.out.println("Send Request of Format: [path len], [url], " +
                            "[method], [parameter], [value], [parameter], [value]");
                    try {
                        command = scan.nextLine();
                    } catch (Exception e) {
                     //do nothing
                    }
                    String[] reqArr = command.split(",");
                    int path = Integer.parseInt(reqArr[0]);
                    String url = reqArr[1];
                    String method = reqArr[2];
                    int i = 3;
                    HashMap<String, String> params = new HashMap<>();
                    while (i < reqArr.length) {
                        params.put(args[i], args[i + 1]);
                        i += 2;
                    }
                    Response rep = ant.sendRequest(path, url, method, params);
                    try {
                        String name = rep.url.substring(rep.url.lastIndexOf('/') + 1);
                        PageDisplay.savePhoto(rep.dataType, name, rep.data);
                    } catch (Exception e) {
                        LOGGER.info("Photo Save Failure");
                        //do nothing
                    }
                    break;
            }
        }
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception y) {
                y.printStackTrace();
            }
        }

    }
}

