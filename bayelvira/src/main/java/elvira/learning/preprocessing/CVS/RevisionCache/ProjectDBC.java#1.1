/* ProjectDBC.java */
package elvira.learning.preprocessing;

import elvira.*;
import elvira.database.*;
//import elvira.learning.*;
//import elvira.potential.*;
//import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;
//import elvira.tools.Chi2;

import java.io.*;
//import java.lang.Runtime;
//import java.lang.Math;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 *
 * The goal of this class is to project the input databasecases over a 
 * subset of the original attribute list.
 *
 * @author Antonio.Salmeron@uclm.es
 * @author jose.gamez@uclm.es
 * @version 0.1
 * @since 18/04/2005
 */
public class ProjectDBC {

    /** The original database */
    DataBaseCases m_input = null;
 
    /** The variables to project onto */
    NodeList m_attributes = null;  
    
    /** 
     * Constructor
     */
    
    public ProjectDBC(DataBaseCases dbc, NodeList nl){
        m_input = dbc;
        m_attributes = nl;
    }
    
    /**
     * Constructor
     */
    
    public ProjectDBC(DataBaseCases dbc, Vector list, boolean inv){
        int i;
        
        m_input = dbc;
        m_attributes = new NodeList();
                
        NodeList vars = dbc.getVariables();
        for(i=0;i<vars.size();i++) 
            System.out.println(vars.elementAt(i).getName());
        int[] indexes = new int[vars.size()];
        for(i=0;i<vars.size();i++) indexes[i]=0;
        for(i=0;i<list.size();i++)
          indexes[ ((Integer)list.elementAt(i)).intValue() ] = 1;
        if (inv) 
          for(i=0; i<indexes.length; i++) 
            indexes[i]= (indexes[i]+1)%2;
        
        for(i=0; i<indexes.length; i++)
          if (indexes[i]==1)
            m_attributes.insertNode(vars.elementAt(i));
          
        
    }
    
    /**
     * Carries out the projection according to m_attributes and returns 
     * the output in a new DataBaseCases
     *
     * @returns a <code>DataBaseCases</code> containing the desired projection
     */
    
    public DataBaseCases doProjection(){
        DataBaseCases output=null;

        output = m_input.copy();
        
        NodeList newNodeList=new NodeList((Vector)output.getVariables().getNodes().clone());
        for (int i=0; i<m_attributes.size();i++)
          newNodeList.removeNode(m_attributes.elementAt(i));

        output.removeVariables(newNodeList);
        
        return output;
    }
   
    /**
     * Returns a Vector with the indexed of the attributes in list
     * @param list a <code>String</code> containing the a list of attributes,
     *              i.e.,  0,2-5,9,11
     * @returns a Vector with indexes of the selected attributes
     */
    
    public static Vector parseAttributes(String s){
        Vector list = new Vector();
        StringTokenizer st = new StringTokenizer(s,",");
        int i;
        
        for( ; st.hasMoreTokens(); ){
          String s2 = st.nextToken();
//          System.out.println("s2 es " + s2);
          if (s2.indexOf('-') == -1){ // we substract 1 to pass to 0-based 
            list.addElement(new Integer(Integer.parseInt(s2)-1));
          }else{ // this is a range
            String[] ss = s2.split("-");
            int low = Integer.parseInt(ss[0]);
            int high = Integer.parseInt(ss[1]);
            for(i=low;i<=high;i++) 
              list.addElement(new Integer(i-1)); // substacting 1 to pass to 0-based
          }
        }

        return list;
    }
    
    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * For performing tests. The arguments are:
    *    - inputFileName.dbc
    *    - a string with the attribute list (0-based)
    *    - invert selection (false|true) indicates if the attributes in list should b
    *      removed (true) or maintained (false) 
    *    - outputFileName.dbc
    */

    public static void main(String args[]) throws ParseException, IOException, 
                                                    InvalidEditException
    {
        if (args.length != 4){
            System.out.println("\n*** Error: Correct usage is: ProjectDBC");
            System.out.println("\t inputFileName.dbc (i.e. file.dbc)\n" +
                               "\t attribute-list (non-zero based, i.e., 1,3-6,8)\n" +
                               "\t invertSelection (false|true)\n" +    
                               "\t outputFileName.dbc (i.e. newfile.dbc)\n");
            System.exit(0);
        }
        
        // reading input file
        System.out.print("Reading file " + args[0] + " ....");
        FileInputStream File = new FileInputStream(args[0]);
        DataBaseCases input = new DataBaseCases(File);
        File.close();
        System.out.println("..... done\n");
        
        // processing attribute list
        Vector list = ProjectDBC.parseAttributes(args[1]); 
        
        // invert selection??
        boolean invert = Boolean.valueOf(args[2]).booleanValue();
        
        // getting the projection
        ProjectDBC projection = new ProjectDBC(input,list,invert);

        DataBaseCases output = projection.doProjection();
     
        // saving the obtained database
        FileWriter fw = new FileWriter(args[3]);
        output.saveDataBase(fw);
        fw.close();
        
        return;
    }//End main 
}
//End Grouping class


