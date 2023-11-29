import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class PageDisplay {
   public static void main(String[] args) {
   }

   public static void createWindow(String url, String fileName) {    
      JFrame frame = new JFrame(url);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      createUI(frame, fileName);
      frame.setSize(560, 450);      
      frame.setLocationRelativeTo(null);  
      frame.setVisible(true);
   }

   private static void createUI(final JFrame frame, String fileName){  
      JPanel panel = new JPanel();
      LayoutManager layout = new FlowLayout();  
      panel.setLayout(layout);       

      JEditorPane jEditorPane = new JEditorPane();
      jEditorPane.setEditable(false);   
      URL url= PageDisplay.class.getResource(fileName);
      try {
         System.out.println(url.toString());   
         jEditorPane.setPage(url);
      } catch (IOException e) { 
         jEditorPane.setContentType("text/html");
         jEditorPane.setText("<html>Page not found.</html>");
      }

      JScrollPane jScrollPane = new JScrollPane(jEditorPane);
      jScrollPane.setPreferredSize(new Dimension(540,400));      

      panel.add(jScrollPane);
      frame.getContentPane().add(panel, BorderLayout.CENTER);    
   }  
} 
