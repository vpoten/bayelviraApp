/*
 * UtilityPotential.java
 *
 * Created on 18 de junio de 2003, 15:47
 */

package elvira.potential;

import java.io.*;
import java.util.Vector;
import elvira.*;

/**
 *
 * @author  Administrador
 */
public class UtilityPotential extends elvira.potential.PotentialFunction {
    
   public void setStrArgumentAt(String strVal, int position) {
    strArg.setElementAt(strVal,position);
  }

    /** Creates a new instance of UtilityPotential */
    public UtilityPotential() {
        super();
    }
   
    
    public UtilityPotential(Vector vars) {
        int i;
        
        variables = (Vector)vars.clone();
        arguments = new Vector();
        strArg = new Vector();
        for (i=0;i<variables.size();i++)
        {
           String iName;
           iName=((Node)(variables.elementAt(i))).getName();
           strArg.addElement(iName);
           arguments.addElement(iName);
        }
        setFunction(new SumFunction());
    } 
   

    public UtilityPotential(NodeList vars) {
            int i;
                       
            variables = (Vector)vars.getNodes().clone();
            arguments = new Vector();
            strArg = new Vector();
            for (i=0;i<variables.size();i++)
            {
                String iName;
                iName=((Node)(variables.elementAt(i))).getName();
                strArg.addElement(iName);
                arguments.addElement(iName);
            }
            setFunction(new SumFunction());
        }
  
      
    public void setStrArguments(Vector vars){
        int i;
        for(i=0;i<vars.size();i++)
        {
            addArgument(((Node)(vars.elementAt(i))).getName());
        }
   }
    

    /**
 * Copies this potential.
 * @return a copy of this <code>PotentialTable</code>.
 */

public Potential copy() {
    //Tengo que poner en los campos arguments y strarguments los nombres de los nodos de los
    //argumentos del potencial del nodo supervalor, así como copiar la función (suma o multiplicacion)
    //Tras la copia ya se encargará repairPotFunction de conseguir los potenciales de los argumentos
    //string que haya en el atributo 'arguments'

  UtilityPotential pot;
  
  pot = new UtilityPotential(variables);
  pot.setFunction(getFunction().getName());
  
  return pot;
}
	   
  
  
    
   
   
    
    public void save(PrintWriter p) {
        int i;
        p.print("values= function  \n");
        if(function!=null)
            p.print("          "+ function.getName());
        else
            p.print("          Unknown");
        p.print("(");
        /*for (i=0;i<arguments.size();i++)
        {
            if(arguments.elementAt(i).getClass()==Double.class)
                p.print(((Double)(arguments.elementAt(i))).doubleValue());
            else if(arguments.elementAt(i) instanceof Potential)
                p.print(strArg.elementAt(i));
            else
                System.out.println("Error in UtilityPotential.save(PrintWriter p): "+
			  "I do not know this kind of argument");
            if (i+1!=arguments.size()) p.print(",");
         }*/
         p.print(");");
    }
}
