package anthill;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.imageio.ImageIO;


public class PageDisplay {


   public static boolean saveHtml(String filename, byte[] data ){
      Path filePath = Paths.get("anthill/" + filename);
      try {
         Files.write(filePath, data);
         return true;
      }catch(Exception e){
         if(Drone.debug) {
            e.printStackTrace();
         }
         return false;
      }
   }
   public static boolean savePhoto(String datatype, String filename, byte[] data) throws Exception{

      ByteArrayInputStream bis = new ByteArrayInputStream(data);

         BufferedImage bImage2 = ImageIO.read(bis);
         datatype = datatype.substring(datatype.indexOf('/')+1);
         ImageIO.write(bImage2, datatype, new File(filename));
         return true;

   }
   public static void createWindow(String url, String fileName) throws Exception {
      System.out.println(fileName);
      JFrame frame = new JFrame(url);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      createUI(frame, fileName);
      frame.setSize(560, 450);      
      frame.setLocationRelativeTo(null);  
      frame.setVisible(true);
   }

   private static void createUI(final JFrame frame, String fileName) throws Exception{
      JPanel panel = new JPanel();
      LayoutManager layout = new FlowLayout();  
      panel.setLayout(layout);       

      JEditorPane jEditorPane = new JEditorPane();
      jEditorPane.setEditable(false);
      //Could try to make links clickable within framework
      URL url = null;

         url = new File("anthill/" + fileName).toURL();



         System.out.println(url.toString());   
         jEditorPane.setPage(url);

         jEditorPane.setContentType("text/html");
         jEditorPane.setText("<html>Page not found.</html>");


      JScrollPane jScrollPane = new JScrollPane(jEditorPane);
      jScrollPane.setPreferredSize(new Dimension(540,400));      

      panel.add(jScrollPane);
      frame.getContentPane().add(panel, BorderLayout.CENTER);    
   }  
} 
