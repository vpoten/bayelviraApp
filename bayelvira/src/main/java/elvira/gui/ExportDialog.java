/* Class ExportDialog */

package elvira.gui;

import elvira.*;
import elvira.gui.*;
import elvira.parser.ParseException;
import elvira.database.DataBaseCases;
import elvira.Elvira;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;





/**
 * This class exports cases file (.dbc files) to .csv files.
 * .csv files are text files with fields separated by ";", and that uses
 * "." for real numbers. Variables are in the first line, and cases, in the others.
 *
 * @author avrofe@ual.es
 * @version 1.2
 * @since 13/05/04
 */


public class ExportDialog extends JDialog {


    //Visual component

    JPanel exportPanel = new JPanel();
    JLabel dbcFileLabel = new JLabel();
    JLabel outFileLabel = new JLabel();
    JTextField dbcFileText = new JTextField();
    JTextField outFileText = new JTextField();
    JButton dbcFileButton = new JButton();
    JButton outFileButton = new JButton();
    JButton exportButton = new JButton();
    JButton cancelButton = new JButton();


    //Panel to show error and success messages

    JOptionPane jOPanel=new JOptionPane();


    //The paths of properties files

    private ResourceBundle menusBundle;
    private ResourceBundle messagesBundle;
    private ResourceBundle dialogBundle;


    //File Chooser to choose dbc and out files

    private ElviraFileChooser dbcFileDialog = new ElviraFileChooser(System.getProperty("user.dir"));
    private JFileChooser outFileDialog = new JFileChooser(System.getProperty("user.dir"));




    //CONSTRUCTORS

    public ExportDialog(Frame frame, String title, boolean modal) {

        super(frame, title, modal);

        try {
            jbInit();
            pack();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public ExportDialog() {
        this(null, "", false);
    }




    /**
     * Initialices all the dialog components
     */

    private void jbInit() throws Exception {


        //select the path of the properties files

        dialogBundle = Elvira.getElviraFrame().getDialogBundle();
        menusBundle = Elvira.getElviraFrame().getMenuBundle();
        switch (Elvira.getLanguaje()) {
            case Elvira.AMERICAN:
                messagesBundle = ResourceBundle.getBundle("elvira/localize/Messages");
                break;
            case Elvira.SPANISH:
                messagesBundle = ResourceBundle.getBundle("elvira/localize/Messages_sp");
                break;
        } // End of switch


        //Inicialize the window

        this.setSize(new Dimension(485, 210));

        exportPanel.setPreferredSize(new Dimension(485, 210));
        exportPanel.setLayout(null);
        exportPanel.setMaximumSize(new Dimension(600, 300));
        exportPanel.setMinimumSize(new Dimension(1, 1));


        dbcFileLabel.setText(Elvira.localize(dialogBundle,"Export.DbcFile.label"));
        dbcFileLabel.setBounds(new Rectangle(19, 15, 292, 15));

        outFileLabel.setBounds(new Rectangle(18, 86, 292, 15));
        outFileLabel.setText(Elvira.localize(dialogBundle,"Export.OutFile.label"));

        dbcFileText.setText("[none]");
        dbcFileText.setBounds(new Rectangle(19, 43, 310, 25));

        outFileText.setBounds(new Rectangle(18, 110, 310, 25));
        outFileText.setText("[none]");

        dbcFileButton.setBounds(new Rectangle(358, 42, 105, 25));
        dbcFileButton.setText(Elvira.localize(dialogBundle,"Export.Browse.button"));
        dbcFileButton.setMnemonic((int)(Elvira.localize(dialogBundle,"Export.Browse.button")).charAt(0));
        dbcFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dbcFileButton_actionPerformed(e);
            }
        });

        outFileButton.setText(Elvira.localize(dialogBundle,"Export.Browse.button"));
        outFileButton.setBounds(new Rectangle(358, 110, 105, 25));
        outFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outFileButton_actionPerformed(e);
            }
        });

        exportButton.setBounds(new Rectangle(90, 154, 123, 36));
        exportButton.setText(Elvira.localize(dialogBundle,"Export.Export.button"));
        exportButton.setMnemonic((int)(Elvira.localize(dialogBundle,"Export.Export.button")).charAt(0));
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportButton_actionPerformed(e);
            }
        });

        cancelButton.setText(Elvira.localize(dialogBundle,"Cancel.Label"));
        cancelButton.setMnemonic((int)(Elvira.localize(dialogBundle,"Cancel.Label")).charAt(0));
        cancelButton.setBounds(new Rectangle(264, 154, 123, 36));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton_actionPerformed(e);
            }
        });


    exportPanel.add(exportButton, null);
    exportPanel.add(cancelButton, null);
    exportPanel.add(dbcFileLabel, null);
    exportPanel.add(dbcFileText, null);
    exportPanel.add(dbcFileButton, null);
    exportPanel.add(outFileText, null);
    exportPanel.add(outFileButton, null);
    exportPanel.add(outFileLabel, null);

        this.getContentPane().add(exportPanel, BorderLayout.CENTER);
    }




    /**
     * Returns the active dialog, use this method in order to invoke any specific method of
     * the dialog
     *
     * @return  the active dialog
     */


    public JDialog getActiveDialog() {
        return this;
    }//end getActiveDialog()




    /*****    COMPONENTS'S ACTION PERFORMED  *****/


    /**
     * Selects the cases file, that is, the imput file.
     *
     */

    void dbcFileButton_actionPerformed(ActionEvent e) {

        //Opens an open dialog to choose the .dbc file, using the elvira files filter
        dbcFileDialog.setDataBaseFilter();
        dbcFileDialog.rescanCurrentDirectory();
        dbcFileDialog.setDialogType(dbcFileDialog.OPEN_DIALOG);
        dbcFileDialog.setDialogTitle(Elvira.localize(dialogBundle,"Export.DbcFileChooser.title"));
        dbcFileDialog.setSelectedFile(new File(""));

        //If cancel option has been selected, show an error message
        int state = dbcFileDialog.showDialog(this.getActiveDialog(),Elvira.localize(dialogBundle,"Export.Browse.button"));
        if (state == dbcFileDialog.CANCEL_OPTION) {
            dbcFileDialog.setSelectedFile(null);
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoDbcFileSelected"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
            dbcFileText.setText("[none]");
        }
        else{
            //Inserts .dbc file name in .dbc file text
            dbcFileText.setText(dbcFileDialog.getSelectedFile().getPath());
        }

    }


    /**
     * Selects the .csv file, that is, the out file.
     *
     */

    void outFileButton_actionPerformed(ActionEvent e) {


        //Set a file filter for .csv files
        outFileDialog.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File file) {
                String filename = file.getName();
                return (filename.endsWith(".csv") || file.isDirectory());
            }
            //Shows a description in the file dialog
            public String getDescription() {
                return ".csv Files (*.csv)";
            }
        });


        //Opens an save dialog to choose the .csv file
        outFileDialog.setDialogType(outFileDialog.SAVE_DIALOG);
        outFileDialog.setDialogTitle(Elvira.localize(dialogBundle,"Export.OutFileChooser.title"));
        outFileDialog.setSelectedFile(new File(""));

        //If cancel option has been selected, show an error message
        int state = outFileDialog.showDialog(this.getActiveDialog(),Elvira.localize(dialogBundle,"Export.Browse.button"));
        if (state == outFileDialog.CANCEL_OPTION) {
            outFileDialog.setSelectedFile(null);
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoOutFileSelected"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
            //en el campo de texto del nombre del fichero de salida
            outFileText.setText("[none]");
        }
        else{
            //Inserts .csv file name in .csv file text
            outFileText.setText(outFileDialog.getSelectedFile().getPath());
        }

    }

    /*
     * Exit of the export dialog, without performs the exportation
     *
     */

    void cancelButton_actionPerformed(ActionEvent e) {
        //    this.setVisible(false);
        this.dispose();

    }


    /*
     * Exit of the export dialog, performing the exportation, that is:
     * turning the .dbc file into .csv file, and saving the changes
     *
     */

    void exportButton_actionPerformed(ActionEvent e) {


        try { //first try

            //Checks the out file has been selected, and is a .csv file

            if ((outFileText.getText()!="[none]") && (outFileText.getText().endsWith(".csv"))) {

                FileWriter fw = new FileWriter(new File(outFileText.getText()));
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter exit = new PrintWriter(bw);


                try { //second try

                    String dbcName = dbcFileText.getText();

                    //Verify that input file has been selected, and is a .dbc file
                    if ((dbcFileText.getText()!="[none]") && (dbcFileText.getText().endsWith(".dbc"))) {

                        FileInputStream dbcFile = new FileInputStream(dbcFileDialog.getSelectedFile());

                        //Get the data base cases of .dbc file
                        DataBaseCases dbCases = new DataBaseCases(dbcFile);
                        CaseListMem cases ;
                        Configuration config;
                        NodeList varsList;
                        Node var;
                        FiniteStates finiteVar = new FiniteStates();
                        Continuous contVar;

                        Vector vars, values;
                        String separator = ";";
                        String variable, stringValue;
                        int numCases, numVars, i, j, k, intValue;
                        double value, undefValue, doubleValue;

                        numVars = dbCases.getVariables().size();
                        numCases = dbCases.getCases().getNumberOfCases();

                        vars = dbCases.getVariables().getNodes();

                        cases = (CaseListMem)dbCases.getCases();

                        varsList = dbCases.getVariables();


                        //Print in .csv file variables' name and values of these for each case
                        //separated by ";"

                        //Print the names of the variables at the first line
                        Enumeration enumerator = vars.elements();
                        while (enumerator.hasMoreElements()){
                            exit.print( ((Node) enumerator.nextElement()).getName());
                            //In the end, continue in the next line
                            if(enumerator.hasMoreElements())
                                exit.print(separator);
                            else
                                exit.println();
                        }

                        Configuration currentCaseAux = new Configuration();

                        for (i = 0; i < numCases; i++){

                            if (cases.get(i)==null){

                                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.GettingCaseError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);

                            }
                            else {

                                for (j = 0; j < numVars-1; j++){

                                    var = dbCases.getVariables().elementAt(j);
                                    undefValue = var.undefValue();

                                    //Distinct if the variable is finite states o continuous
                                    //If it's finite states, values are string
                                    if (var.getTypeOfVariable()==Node.CONTINUOUS){
                                        contVar = (Continuous) var;
                                        doubleValue = cases.getValue(i,j);
                                        if (doubleValue==undefValue)
                                            exit.print("?");
                                        else{
                                            //If it's integer, print as integer
                                            if(((new Double(doubleValue)).intValue())==doubleValue)
                                                exit.print(((new Double(doubleValue)).intValue()));
                                            else
                                                exit.print(doubleValue);
                                        }
                                    }//end if of continuous
                                    else
                                        if (var.getTypeOfVariable()==Node.FINITE_STATES) {
                                            finiteVar = (FiniteStates) var;
                                            intValue = (int) cases.getValue(i,j);
                                            if (((double)intValue)==undefValue)
                                                exit.print("?");
                                            else{
                                                stringValue = finiteVar.getState(intValue);
                                                exit.print(stringValue);
                                            }
                                        }//end if of finite states
                                    //after a value, print separator
                                    exit.print(separator);
                                }//end j

                                //In the end of the line

                                var = dbCases.getVariables().elementAt(numVars-1);
                                undefValue = var.undefValue();

                                if (var.getTypeOfVariable()==Node.CONTINUOUS){
                                    contVar = (Continuous) var;
                                    doubleValue = cases.getValue(i, numVars-1);
                                    if (doubleValue==undefValue)
                                        exit.println("?");
                                    else {
                                        if(((new Double(doubleValue)).intValue())==doubleValue)
                                            exit.println(((new Double(doubleValue)).intValue()));
                                        else
                                            exit.println(doubleValue);
                                    }
                                }
                                else
                                    if (var.getTypeOfVariable()==Node.FINITE_STATES){
                                        finiteVar = (FiniteStates) var;
                                        intValue = (int) cases.getValue(i,numVars-1);
                                        if (((double) intValue)==undefValue)
                                            exit.println("?");
                                        else{
                                            stringValue = finiteVar.getState(intValue);
                                            exit.println(stringValue);
                                        }
                                    }
                            }//end else
                        }//end i

                        exit.close();
                        jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.SuccessfulExport"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.PLAIN_MESSAGE);
                        setVisible(false);

                    }//end if is .dbc file
                    else{

                        if (dbcFileText.getText().compareTo("[none]") == 0){
                            //Cases file has not been selected
                            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoDbcFileSelected"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
                        }else

                            if (!(dbcFileText.getText().endsWith(".dbc"))){
                                //Cases file is not a .dbc file
                                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoDbcFileError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);

                            }

                    }
                }//of second try

                catch (java.io.FileNotFoundException e2){
                    //first this exception, because IOException is the superclass
                    exit.close();
                    //Error opening .dbc file
                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.DbcFileError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
                }
                catch (java.io.IOException e4){
                    exit.close();
                    //Error opening .dbc file
                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.DbcFileError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);

                }
                catch (elvira.parser.ParseException e5){
                    exit.close();
                    System.out.println(e.toString());
                    //Error when parse errors are encountered in the data base cases
                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.ParseError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
                }
            }//end if is .csv file
            else{
                if (outFileText.getText().compareTo("[none]") == 0){
                    //Out file has not been selected
                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoOutFileSelected"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
                }else

                    if (!(outFileText.getText().endsWith(".csv"))){
                        //Out file is not a .dbc file
                        jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.NoOutFileError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);

                    }
            }


        }//of first try
        catch (java.io.IOException e3) {
            //Error opening out file
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Export.OutFileError"),Elvira.localize(dialogBundle,"Export.Elvira.label"), jOPanel.ERROR_MESSAGE);
        }

    }/////End exportButton_actionPerformed

}//End ExportDialog class





