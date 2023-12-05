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

   /**
    * Saves an Array of bytes to local memory as an html file
    * @param filename
    * @param data
    * @return
    * @throws Exception
    */
   public static boolean saveHtml(String filename, byte[] data) throws Exception{
      Path filePath = Paths.get("anthill/" + filename);
      Files.write(filePath, data);
      return true;
   }

   /**
    * Saves array of bytes to memory as photo of type dataType
    * @param datatype
    * @param filename
    * @param data
    * @return
    * @throws Exception
    */
   public static boolean savePhoto(String datatype, String filename, byte[] data) throws Exception{

      ByteArrayInputStream bis = new ByteArrayInputStream(data);

         BufferedImage bImage2 = ImageIO.read(bis);
         datatype = datatype.substring(datatype.indexOf('/')+1);
         ImageIO.write(bImage2, datatype, new File(filename));
         return true;

   }

   /**
    * Displays html page with title url based on html file fileName
    * @param url
    * @param fileName
    * @throws Exception
    */
   public static void createWindow(String url, String fileName) throws Exception {
      System.out.println(fileName);
      JFrame frame = new JFrame(url);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      createUI(frame, fileName);
      frame.setSize(560, 450);      
      frame.setLocationRelativeTo(null);  
      frame.setVisible(true);
   }

   /**
    * Helper method of create window that actually creates the window
    * @param frame
    * @param fileName
    * @throws Exception
    */
   private static void createUI(final JFrame frame, String fileName) throws Exception{
      JPanel panel = new JPanel();
      LayoutManager layout = new FlowLayout();  
      panel.setLayout(layout);       

      JEditorPane jEditorPane = new JEditorPane();
      jEditorPane.setEditable(false);
      //Could try to make links clickable within framework
      // URLs are also not just for webpages apparently, can point to files
      URL url = null;
      url = new File("anthill/" + fileName).toURL();
      //System.out.println(url.toString());
      // Set url call assigns html file as input for the output UI
      jEditorPane.setPage(url);
         //jEditorPane.setContentType("text/html");
         //jEditorPane.setText("<html>Page not found.</html>");

      JScrollPane jScrollPane = new JScrollPane(jEditorPane);
      jScrollPane.setPreferredSize(new Dimension(540,400));      

      panel.add(jScrollPane);
      frame.getContentPane().add(panel, BorderLayout.CENTER);    
   }  
} 
