/* ElviraFileChooser */



package elvira.gui;



import javax.swing.*;

import java.beans.*;

import java.io.*;

import java.awt.*;

import java.net.URL;

import java.awt.event.*;



/**

 * This class implements a file chooser that contain three

 * filters: one for Elvira files, another for Elvira evidence

 * files and other for Elvira case (of evidence) files. 

 * This file chooser contains a file previewer for looking

 * the files' contains.*/ 



public class ElviraFileChooser extends JFileChooser {

    

	ElviraEvidenceFilter evidenceFilter;

	ElviraFilter elviraFilter;
        
        XBIFFilter xbifFilter;
       
	DataBaseFilter databaseFilter;
	
	CaseFilter caseFilter;

	

	/**

	 * An object of this class contains the previewer and set

	 * all the visual elements necessary for it. It will be 

	 * instanciated in ElviraFileChooser, so this class is inside

	 * of that.

	 */



	class PreviewPanel extends JPanel {

		public PreviewPanel() {

			JLabel label = new JLabel("Text Previewer",

									   SwingConstants.CENTER);

			setPreferredSize(new Dimension(300,0));

			setBorder(BorderFactory.createEtchedBorder());



			setLayout(new BorderLayout());



			label.setBorder(BorderFactory.createEtchedBorder());

			add(label, BorderLayout.NORTH);

		}

	}

	

	

	/**

	 * Creates a ElviraFileChooser without a type

	 */

	

	public ElviraFileChooser() {
                //super(".");
		evidenceFilter = new ElviraEvidenceFilter();

		addChoosableFileFilter(evidenceFilter);
		
		CaseFilter caseFilter = new CaseFilter();
		
		addChoosableFileFilter(caseFilter);				

		databaseFilter = new DataBaseFilter();

		addChoosableFileFilter(databaseFilter);		

		ElviraFilter elviraFilter = new ElviraFilter();

		addChoosableFileFilter(elviraFilter);		
                
                XBIFFilter xbifFilter = new XBIFFilter();

		addChoosableFileFilter(xbifFilter);		
		
		addActionListener(new ElviraFileChooserListener());

	}

	/**

	 * Creates a ElviraFileChooser without a type

	 */

	

	public ElviraFileChooser(String path) {
                super(path);
		evidenceFilter = new ElviraEvidenceFilter();

		addChoosableFileFilter(evidenceFilter);
		
		CaseFilter caseFilter = new CaseFilter();
		
		addChoosableFileFilter(caseFilter);				

		databaseFilter = new DataBaseFilter();

		addChoosableFileFilter(databaseFilter);		

		ElviraFilter elviraFilter = new ElviraFilter();

		addChoosableFileFilter(elviraFilter);	
                
                XBIFFilter xbifFilter = new XBIFFilter();

		addChoosableFileFilter(xbifFilter);	
                
		
		addActionListener (new ElviraFileChooserListener());

	}


	

	public void setElviraFilter() {
           
	   setFileFilter(elviraFilter);	   

	}

	public void setXBIFFilter() {

	   setFileFilter(xbifFilter);	   

	}

	public void setEvidenceFilter() {

	   setFileFilter(evidenceFilter);

	}

	

	public void setDataBaseFilter() {

	   setFileFilter(databaseFilter);

	}


    public void setCaseFilter() {

	   setFileFilter(caseFilter);

	}


   /**

    * This class' objects are created when a new item is added to

    * the File Menu as a last reference item (remember that the last 

    * reference list contains the last files that had been opened in 

    * Elvira).

    *

	 * @author ..., fjdiez, ratienza, clacave, ...

	 * @version 0.1

	 * @since 18/10/99    

    */



   public class ElviraFileChooserListener implements ActionListener {      

    

      public ElviraFileChooserListener(){

         super();

      }

      

      public void actionPerformed (ActionEvent e) {

         String state = (String) e.getActionCommand();		            		      

         

		   if (getDialogType()==SAVE_DIALOG)

		      if (state.equals(APPROVE_SELECTION))  {		         

		         

		         File file = getSelectedFile();

		         String fileName = file.getPath();//.getName();

		         

                            if (fileName.indexOf('.',0) == -1) {

                                file = new File(fileName+".elv");

                                setSelectedFile(file);

                            }		      

		         

                            if (file.exists()) {

                                Object[] names = {fileName};

                                int value = ShowMessages.showConfirmDialogPlus (

		                           ShowMessages.OVERWRITE,

                                 JOptionPane.WARNING_MESSAGE, 

                                 JOptionPane.YES_NO_OPTION, names);

		            switch (value) {

		               case JOptionPane.CLOSED_OPTION:		                  

		                  cancelSelection(); break;

		               case JOptionPane.NO_OPTION:		                  

		                  cancelSelection(); break;

		               case JOptionPane.YES_OPTION:

		                  ; break;

                                }

                            }

                        }

                    }

            }

	

}







/**

 * Abstract filter that accepts all directories and provides

 * a method that returns a file name suffix, given a file

 * This is an abstract class because it leaves the getDescription

 * method for subclasses to implement

 */



abstract class SuffixAwareFilter extends javax.swing.filechooser.FileFilter {

	

	/**

	 * Get the file's extension of the file given as parameter

	 * @return The extension of the file

	 */

	 

	public String getSuffix(File f) {

		String s = f.getPath(), suffix = null;

	    int i = s.lastIndexOf('.');



	    if(i > 0 &&  i < s.length() - 1)

	    	suffix = s.substring(i+1).toLowerCase();



		return suffix;

	}

	

	public boolean accept(File f) {

		return f.isDirectory();

	}

}



/**

 * This class let displaying in the fileChooser

 * only the jpg and gif images.

 */

 

// For using in the future



class ImageFilter extends SuffixAwareFilter {

   public boolean accept(File f) {

		boolean accept = super.accept(f);



		if( ! accept) {

			String suffix = getSuffix(f);



			if(suffix != null)

				accept = super.accept(f) || suffix.equals("jpg")

				|| suffix.equals("gif");

		}

		return accept;

	}

	public String getDescription() {

		return "Image formats (*.jpg, *.gif)";

	}

}





/**

 * This class let displaying in the fileChooser

 * only the files with the Elvira format.

 */



class ElviraFilter extends SuffixAwareFilter {

    public boolean accept(File f) {

		boolean accept = super.accept(f);



		if( ! accept) {

			String suffix = getSuffix(f);



			if(suffix != null)

				accept = super.accept(f) || suffix.equals("elv");

		}

		return accept;

	}

	public String getDescription() {

		return "Elvira files(*.elv)";

	}

}

/**

 * This class let displaying in the fileChooser

 * only the files with the XBIF format.

 */

class XBIFFilter extends SuffixAwareFilter {

    public boolean accept(File f) {

		boolean accept = super.accept(f);



		if( ! accept) {

			String suffix = getSuffix(f);



			if(suffix != null)

				accept = super.accept(f) || suffix.equals("xbif");

		}

		return accept;

	}

	public String getDescription() {

		return "XBIF format files(*.xbif)";

	}

}




/**

 * This class let displaying in the fileChooser

 * only the files with the Elvira evidence format.

 */



class ElviraEvidenceFilter extends SuffixAwareFilter {

    public boolean accept(File f) {

      boolean accept = super.accept(f);
      if(! accept)
      {
        String suffix = getSuffix(f);
        if(suffix != null)
          accept = super.accept(f) || suffix.equals("evi");
      }
      return accept;
    }

	public String getDescription() {

		return "Elvira Evidence Format(*.evi)";

	}

}



class CaseFilter extends SuffixAwareFilter {

    public boolean accept(File f) {

      boolean accept = super.accept(f);
      if(! accept)
      {
        String suffix = getSuffix(f);
        if(suffix != null)
          accept = super.accept(f) || suffix.equals("cas");
      }
      return accept;
    }

	public String getDescription() {

		return "Elvira Case Format(*.cas)";

	}

}



/**

 * This class let displaying in the fileChooser

 * only the files with the Elvira database format.

 */



class DataBaseFilter extends SuffixAwareFilter {

    public boolean accept(File f) {

      boolean accept = super.accept(f);
      if(! accept)
      {
        String suffix = getSuffix(f);
        if(suffix != null)
          accept = super.accept(f) || suffix.equals("dbc");
      }
      return accept;
    }

	public String getDescription() {

		return "Elvira Database Format(*.dbc)";

	}

}



