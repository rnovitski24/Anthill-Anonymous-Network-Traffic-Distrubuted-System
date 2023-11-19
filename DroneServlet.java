package AntHill;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.webserver.XmlRpcServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.*;

public class DroneServlet extends XmlRpcServlet {
    
    private static ThreadLocal clientIpAddress = new ThreadLocal();

    public static String getClientIpAddress() {
        return (String) clientIpAddress.get();
    }

    public void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse)
            throws IOException, ServletException {
        clientIpAddress.set(pRequest.getRemoteAddr());
        super.doPost(pRequest, pResponse);
    }
}
