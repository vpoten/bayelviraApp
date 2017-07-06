/* ElviraConversor */

package elvira.gui;

import elvira.Elvira;

import java.util.*;
import javax.swing.UIManager;
import java.awt.*;



/**
 * Class ElviraConversor.
 * Implements the conversion from csv files to dbc files, and from
 * dbc files to csv files.
 *
 * @author fsoler@ual.es
 * @authoe avrofe@ual.es
 * @version 1.6
 * @since 13/05/2004
 */


public class ElviraConversor {

    private ResourceBundle dialogBundle;

    boolean packFrame = false;


    /* CONSTRUCTOR */


    /**
     * Creates a new object: importWindow, if option is equal to 0, or exportDialog, if option is equal to 1
     *
     * @param option an integer for choose import or export
     */

    public ElviraConversor(int option) {

        Dimension screenSize;
        Dimension frameSize;


        switch (option){

            case 0:

                //   IMPORTATION


                ImportDialog frame = new ImportDialog(null, "Import Dialog", true);

                //Validate frames that have preset sizes
                //Pack frames that have useful preferred size info, e.g. from their layout

                if (packFrame) {
                    frame.pack();
                }
                else {
                    frame.validate();

                }

                //Center the window
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                frameSize = frame.getSize();

                if (frameSize.height > screenSize.height) {
                    frameSize.height = screenSize.height;
                }
                if (frameSize.width > screenSize.width) {
                    frameSize.width = screenSize.width;
                }
                frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

                frame.setVisible(true);



                break;

            case 1:

                //   EXPORTATION

                dialogBundle = Elvira.getElviraFrame().getDialogBundle();

                String title =  Elvira.localize(dialogBundle,"Export.title");

                ExportDialog frame2 = new ExportDialog(null, title, true);

                //Validate frames that have preset sizes
                //Pack frames that have useful preferred size info, e.g. from their layout

                if (packFrame) {
                    frame2.pack();
                }
                else {
                    frame2.validate();

                }

                //Center the window
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                frameSize = frame2.getSize();

                if (frameSize.height > screenSize.height) {
                    frameSize.height = screenSize.height;
                }
                if (frameSize.width > screenSize.width) {
                    frameSize.width = screenSize.width;
                }
                frame2.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

                frame2.setVisible(true);

                break;

        }

    }


    /**
     * Program for convert cvs files to dbc files.
     * The arguments are none.
     */

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        new ElviraConversor(0);
    }
}
