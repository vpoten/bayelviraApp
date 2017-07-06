/*
 * NodoPaintList.java
 *
 * Created on 20 de octubre de 2003, 12:22
 */

package elvira.gui.continuousEdit;



import java.util.Vector;
import java.awt.Graphics;
import elvira.gui.continuousEdit.*;



/**
 *
 * @author  andrew
 */
public class NodoPaintList extends java.lang.Object implements Cloneable{
    
    public Vector nodes;
    
    /** Creates a new instance of NodoPaintList */
    public NodoPaintList() {
        nodes=new Vector();
    }
    
    public void paintNodoPaintList(Graphics g){
        
        int i;
        NodoPaint NP;
        for (i=0; i<nodes.size(); i++){
             NP = (NodoPaint) nodes.elementAt(i);
            NP.paintNodo(g);
        }
    }
}
