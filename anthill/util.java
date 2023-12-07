package anthill;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.Serializable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;

public class util {
    /**
     * Class designed to assist with the passing of both response data and important typing
     */
    public static class Response implements Serializable {
        public final String dataType;
        public final String url;
        public final byte[] data;
        public final int code;

        public Response(int code, String url,  String dataType, byte[] data) {
            this.dataType = dataType;
            this.url = url;
            this.data = data;
            this.code = code;
        }
    }

    /**
     * Class to help pass requests from server to server.
     */
    public static class RequestParam implements Serializable{
        public int pathLength;
        public final String url;
        public final String method;
        public final HashMap<String, String> parameters;




        /**
         * RequestParam constructor sets the request's path length, url, method, and parameters
         */
        public RequestParam(int pathLength, String url, String method, HashMap<String, String> parameters){
            this.pathLength = pathLength;
            this.url = url;
            this.method = method;
            this.parameters = parameters;
        }
    }
    static class Updater implements Runnable {
        private final Drone ant;
        public Updater(Drone ant) {
            this.ant = ant;
        }

        /**
         * Updates the colony table every 30 secs
         */
        public void run() {
            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (Exception e) {
                    System.out.println("No Sleep");
                }
                // Check for down nodes and update
                ant.scanTable();
                //Check for new nodes
                ant.updateColony();

            }
        }
    }

    /**
     * Fullfills request and returns full Response object
     */
    public static Response fullfillHttpReq(RequestParam requestParam) throws Exception {
        //byte[] finalData = null;
        //final String dataType;
        Response responseData;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // Assign correct method
            ClassicRequestBuilder requestBuild;
            if (requestParam.method.equals("get")) {
                requestBuild = ClassicRequestBuilder.get(requestParam.url);
            } else if (requestParam.method.equals("post")) {
                requestBuild = ClassicRequestBuilder.post(requestParam.url);
            } else if (requestParam.method.equals("head")) {
                requestBuild = ClassicRequestBuilder.head(requestParam.url);
            } else if (requestParam.method.equals("delete")) {
                requestBuild = ClassicRequestBuilder.delete(requestParam.url);
            } else if (requestParam.method.equals("put")) {
                requestBuild = ClassicRequestBuilder.put(requestParam.url);
            } else if (requestParam.method.equals("patch")) {
                requestBuild = ClassicRequestBuilder.patch(requestParam.url);
            } else {
                throw new MethodNotSupportedException("Method " + requestParam.method + " is not supported.");
            }
            //Add Parameters
            for (String param : requestParam.parameters.keySet()) {
                requestBuild.addParameter(param, requestParam.parameters.get(param));
            }
            //Assemble request
            ClassicHttpRequest request = requestBuild.build();
            //Execute request
            responseData = httpclient.execute(request, response -> {
                String dataType = response.getHeader("content-type").getValue();
                int code = response.getCode();
                final HttpEntity entity1 = response.getEntity();
                // Write response data to string, then create response object with data
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                entity1.writeTo(stream);
                byte[] inter = stream.toByteArray();
                EntityUtils.consume(entity1);
                //System.out.println(finalString);
                return new Response (code, requestParam.url,  dataType, inter);
            });
        }

        return responseData;
    }
    /**
     * Returns the private IP of current machine, or null if error is encountered.
     */
    public static String getPrivateIP() throws Exception {

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
        return null;
    }

    /**
     * Returns public IP of current machine
     * @return
     * @throws Exception
     */
    public static String getPublicIP() throws Exception{
        String urlString = "http://checkip.amazonaws.com/";
        URL url = new URL(urlString);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        return br.readLine();

    }
    public static int[][] alternateDef(int COL_SIZE){
        int[][] defs = new int[COL_SIZE][4];
        defs[0] = new int[]{0,1,-1,-1};
        for(int i=2; i < COL_SIZE; i++){
            defs[i] = new int[]{0, i, i-1, i-1};
        }
        return defs;

    }
}
