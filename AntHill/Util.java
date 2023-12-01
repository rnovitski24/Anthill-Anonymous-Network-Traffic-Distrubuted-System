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


        public Response(int code, String url,  String dataType, byte[] data) {
            this.dataType = dataType;
            this.url = url;
            this.data = data;
            this.code = code;
        }
    }

    public static class RequestParam{
        public int pathLength;
        public final String url;
        public final String method;
        public final HashMap<String, String> parameters;




        /*
         * RequestParam constructor sets the request's path length, url, method, and parameters
         */
        public RequestParam(int pathLength, String url, String method, HashMap<String, String> parameters){
            this.pathLength = pathLength;
            this.url = url;
            this.method = method;
            this.parameters = parameters;
        }
    }

    /*
     * Fullfills request and returns html text or binary of response
     */
    public static Response fullfillHttpReq(RequestParam requestParam) throws Exception {
        //byte[] finalData = null;
        //final String dataType;
        Response responseData;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
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
                return new Response (code, requestParam.url,  dataType, inter);
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
