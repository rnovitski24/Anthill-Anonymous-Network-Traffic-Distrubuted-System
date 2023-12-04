import javax.net.ssl.*;
import javax.net.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogServer {
    private static final int PORT_NUM = 8809;
    public static void main(String args[]) {
        ServerSocketFactory serverSocketFactory =
                ServerSocketFactory.getDefault();
        ServerSocket serverSocket = null;
        try {
            serverSocket =
                    serverSocketFactory.createServerSocket(PORT_NUM);
        } catch (IOException ignored) {
            System.err.println("Unable to create server");
            System.exit(-1);
        }
        System.out.printf("LogServer running on port: %s%n", PORT_NUM);
        while (true) {
            Socket socket = null;
            PrintWriter pw = null;
            try {
                socket = serverSocket.accept();
                //System.out.println("Got log");
                InputStream is = socket.getInputStream();
                String host = socket.getInetAddress().getHostName();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "US-ASCII"));

                FileWriter fw = new FileWriter("ServerLogs/" + host +".log", true);
                BufferedWriter writer = new BufferedWriter(fw);
                pw = new PrintWriter(writer);



                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(host + line);
                    pw.println(host + line);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                // Just handle next request.
            } finally {

                if (socket != null) {
                    try {
                        pw.close();
                        socket.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }
}