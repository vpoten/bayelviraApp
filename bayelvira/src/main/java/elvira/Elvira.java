/* Elvira.java */

package elvira;

import java.applet.Applet;
import java.awt.Frame;
import java.util.Vector;
import elvira.gui.ElviraFrame;
import elvira.gui.ProgressDialog;
import java.util.ResourceBundle;
import java.util.MissingResourceException;   
import javax.swing.UIManager;

/**
 * Main class: we will not create or use instances of it.
 * For this reason, all the variables and some of the methods
 * (the ones we invoke from other classes) are static.
 */

public class Elvira extends Applet {
   
  /** 
   * Elvira can be run as an application or as an applet 
   */
  private static boolean runningAsApplet;
  
  /** 
   * Contains the language that will be used in Elvira 
   */
  private static int language;
  
  public static final int SPANISH = 0;
  public static final int AMERICAN = 1;
  private static final char LANGUAGE_FLAG = 'l';
  private static final String SPANISH_FLAG = "sp";
  private static final String AMERICAN_FLAG = "ae";
  
  private static ElviraFrame elviraFrame;
  private static ProgressDialog progressDialog;
  private static ResourceBundle dialogBundle;
   
  /**
   * Main method for Elvira when running as an application.
   *
   * @param argv List of arguments passed to the program
   */
   
  public static void main(String args[]) {
    String languaje_code = new String();
    int parametersCounter = 0, i;
    Vector names = new Vector();
    
    runningAsApplet = false;
    
    /* Future: read arguments with flags */
    /* Future: there might be several networks to be opened */
    if (args.length > 0) {
      if (getFlag(args[0]) == LANGUAGE_FLAG) {         
         if (args[1].equals(SPANISH_FLAG)) 
            language = SPANISH;
         else if (args[1].equals(AMERICAN_FLAG))
            language = AMERICAN;
         else {
            System.out.println("Unknown parameter: "+args[1]);
            //ElviraHelpMessages.show (ElviraHelpMessages.syntax);
            System.exit(0);
         }
         parametersCounter=2;
      }
      for (i=parametersCounter; i<args.length; i++) {
         if (isElviraFile(args[i]))
            names.addElement(args[i]);
         else {            
            System.out.println ("Unknown parameter: "+args[i]);
            //ElviraHelpMessages.show (ElviraHelpMessages.syntax);
            System.exit(0);
         }
      }
    }
    else  { // there is no arguments. Get the language for their own
       languaje_code = System.getProperty("user.language");
       if (languaje_code.equals("es") || languaje_code.equals("sp"))
          language = SPANISH;
       else 
          language = AMERICAN;
    }
    
    if (language == SPANISH)
       dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");
    else
       dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");
    
    try {
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {};
    
    progressDialog = new ProgressDialog();
    progressDialog.show();
    elviraFrame = new ElviraFrame();
    progressDialog.setVisible(false);
    progressDialog.dispose();
    elviraFrame.show();
    if (names.size()>0)
       elviraFrame.openVariousFiles(names);
    
  }

  /**
   * Methods for Elvira when running as an applet.
   */

  public void init() {
    runningAsApplet = true;
    /* Future: read applet arguments */
    ElviraFrame f = new ElviraFrame();
    displayInitialMessages(f);
  	 setLayout(null);
	 setSize(430,270);	 
}

  /**
   * Class methods
   */

  public static int getLanguaje () {
    return language;
  }
  
  public static ElviraFrame getElviraFrame() {
    return elviraFrame;
  }
  
  public static ResourceBundle getDialogBundle() {
     return dialogBundle;
  }

  public static void displayInitialMessages(Frame f) {
    //hacer
  }
 

  
/* ** AUXILIARY METHODS FOR GETTING THE PARAMETERS ** */

  /**
   * Get the flag given in parameter.
   *
   * @param parameter Argument get from the command line
   */
  
  private static char getFlag (String parameter) {
    char flag = ' ';
    
    if (parameter.charAt(0)=='-') {
       flag = parameter.charAt(1);
       return flag;
    }
    else        
       return flag;
  }
  
  
  /**
   * Used to know if the file gives as parameter is an Elvira file (have an .elv
   * extension
   */
  
  private static boolean isElviraFile (String fileName) {
     String extension;     
     
     if (fileName.length()>4) {     
        extension=fileName.substring(fileName.length()-4,fileName.length());
        
        if (extension.equalsIgnoreCase(".elv"))
           return true;
        else
           return false;
     }
     else
        return false;
        
  }
  
  
  public static void println (String text) {
     if (elviraFrame != null) 
        elviraFrame.appendText(text);
     else
        System.out.println(text);
   
  }
    

   /**
    * Method for get the string from the resource bundle 
    *
    * @param bundle Where the string is
    * @param name Variable in the bundle that contains the string
    *             that this method returns
    */
    
   public static String localize (ResourceBundle bundle, String name) {
      
      try {
         return bundle.getString(name);
      }
      catch (MissingResourceException e) {
         int first = name.indexOf(".");
         int last = name.lastIndexOf(".");
         if (first == last) 
            first = 0;
         if (last == -1)
         	last = 0;
         return name.substring(first, last);         
      }            
   } 
   
   public static Vector getDefaultStates () {
      Vector v = new Vector();
      v.add(localize(dialogBundle,"States.present"));
      v.add(localize(dialogBundle,"States.absent"));
      return v;
   }
   
   public static void incrementProgressBar (String s) {
      progressDialog.incrementProgressBar(s);      
   }
	
} // end of Elvira class
