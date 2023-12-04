import anthill.Drone;

import java.util.logging.Level;
import java.util.logging.*;



public class LogBot {

    private static final Logger LOGGER = Logger.getLogger(Drone.class.getName());
    public static void main(String[] args) throws InterruptedException {


        Handler socketHandler = null;

        try {

            socketHandler = new SocketHandler("hopper.bowdoin.edu", 8809);
            System.out.println("Connecting to logServer on port 8809");

        } catch(Exception e ){
            e.printStackTrace();
        }

        socketHandler.setFormatter(new SimpleFormatter());

        socketHandler.setLevel(Level.ALL);

        LOGGER.addHandler(socketHandler);
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
        while(true){
            Thread.sleep(3000);
            LOGGER.log(Level.FINE, "TEST!!!");
        }

    }
}

