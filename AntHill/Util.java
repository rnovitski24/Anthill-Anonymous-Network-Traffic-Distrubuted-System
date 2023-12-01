package AntHill;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;

public class Util {
    public static class Response {
        public final String dataType;

        public final String url;
        public final byte[] data;
        public final int code;

        /*
         * Empty constructor method
         */
        public Response() {
            // default
        }

        public Response(int code, String url,  String dataType, byte[] data) {
            this.dataType = dataType;
            this.url = url;
            this.data = data;
            this.code = code;
        }
    }

    public class RequestParam{
        public final int pathLength;
        public final String path;
        public final String method;
        public final HashMap<String, String> parameters;

        /*
         * Empty constructor method
         */
        public RequestParam() {
            // default
        }


        /*
         * RequestParam constructor sets the request's path length, url, method, and parameters
         */
        public RequestParam(int pathLength, String url, String method, HashMap<String, String> parameters){
            this.pathLength = pathLength;
            this.path = url;
            this.method = method;
            this.parameters = parameters;
        }
    }

    /*
     * Fullfills request and returns html text or binary of response
     */
    public static Response fullfillHttpReq(String url, String method, HashMap<String, String> parameters) throws Exception {
        //byte[] finalData = null;
        //final String dataType;
        Response responseData;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            ClassicRequestBuilder requestBuild;
            if (method.equals("get")) {
                requestBuild = ClassicRequestBuilder.get(url);
            } else if (method.equals("post")) {
                requestBuild = ClassicRequestBuilder.post(url);
            } else if (method.equals("head")) {
                requestBuild = ClassicRequestBuilder.head(url);
            } else if (method.equals("delete")) {
                requestBuild = ClassicRequestBuilder.delete(url);
            } else if (method.equals("put")) {
                requestBuild = ClassicRequestBuilder.put(url);
            } else if (method.equals("patch")) {
                requestBuild = ClassicRequestBuilder.patch(url);
            } else {
                throw new MethodNotSupportedException("Method " + method + " is not supported.");
            }
            //Add Parameters
            for (String param : parameters.keySet()) {
                requestBuild.addParameter(param, parameters.get(param));
            }
            ClassicHttpRequest request = requestBuild.build();

            responseData = httpclient.execute(request, response -> {
                //System.out.println(response.getCode() + " " + response.getReasonPhrase());
                String dataType = response.getHeader("content-type").getValue();
                int code = response.getCode();
                final HttpEntity entity1 = response.getEntity();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                entity1.writeTo(stream);
                byte[] inter = stream.toByteArray();
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity1);
                //System.out.println(finalString);
                return new Response (code, url,  dataType, inter);
            });
        }

        return responseData;
    }
    /*
     * Returns the private IP of current machine, or null if error is encountered.
     */
    public static String getPrivateIP() {
        try{
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
}
