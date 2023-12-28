package anthill;
import java.security.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;


// Global/Instance vars HERE

/**
 * Constructor for Encryptor class.
 */
public class Encryptor {

    SecureRandom secRand = new SecureRandom();
    private final HashMap<byte[], Queue<String>> pathStorage = new HashMap<>();

    public Encryptor() {
    }
    public byte[] getNewPathId(){
        byte[] randBytes = new byte[256];
        secRand.nextBytes(randBytes);
        return randBytes;
    }

    protected boolean storePath(byte[] hash, String successor){
        //because nodes can be visited multiple times, multiples are stored Fifo in queue.
        if(pathStorage.containsKey(hash)){
            pathStorage.get(hash).add(successor);
        }
        else {
            Queue<String> tempStack = new LinkedList<>();
            tempStack.add(successor);
            pathStorage.put(hash, tempStack);
        }
        return true;
    }

    protected String getPath(byte[] key){
        return pathStorage.get(key).poll();
    }

    //So generate keypair
    //Maybe Message Authentication Code? Yes. To tell if message is corrupted or altered.
    // Also maybe randomize whether is encrypted at different levels. decreased overhead
    // So Hmac must be passed with the message on the way back.
    // Have to read more.

}

