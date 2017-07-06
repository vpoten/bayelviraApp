package elvira.potential;

import java.util.Vector;
import java.io.*;
import elvira.*;

/**
 * Class : PotentialConvexSet
 * Description: Potential to represent a convex set with its extrem
 * points, extending from PotentialTable.
 * There is a transparent variable in this PotentialTable with as
 * many cases as number of extrem points of the convex set.
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 */

public class PotentialConvexSet extends PotentialTable implements CredalSet{
  public PotentialConvexSet() {
  }
  
    /**
   * Constructs a new PotentialConvexSet from a CredalSet
   * @param pot an instance of a Potential implementing CredalSet
   */
  public PotentialConvexSet(CredalSet pot){
    Vector vTrans,varsCopy,varsCopy2;
    Configuration confNewPConvexSet,confPTreeCredalSet;
    int nextremes,nv;
    FiniteStates newTrans;
    double v;
    
    variables=pot.getListNonTransparents();
    vTrans=pot.getListTransparents();
    varsCopy=new Vector(variables);
    varsCopy2=new Vector(variables);
    varsCopy.addAll(vTrans);
    confPTreeCredalSet=new Configuration(varsCopy);

    nextremes=(int)FiniteStates.getSize(vTrans);

    newTrans=appendTransparentVariable(nextremes);
    varsCopy2.add(newTrans);
    confNewPConvexSet=new Configuration(varsCopy2);   
    
    nv = (int)FiniteStates.getSize(variables);
    setValues(new double[nv]);
        
    for(int i=0;i<nv;i++){
      v=pot.getValue(confPTreeCredalSet);
      setValue(confNewPConvexSet, v);
      confPTreeCredalSet.nextConfiguration();
      confNewPConvexSet.nextConfiguration();
    }    
  }
  
  /**
   * Constructs a new PotentialConvexSet for a list of
   * variables and a vector of potentials (PotentialTable).
   * @param vars is the list of variables
   * @param C is the vector of potentials
   */
  public PotentialConvexSet(NodeList vars,Vector C) {
    super(vars);
    FiniteStates trans_node;
    int nv,i,j;
    Configuration conf; // Used to move by each one of the potentials
    Configuration conf2; // Used to move by the PotentialConvexSet
    
    trans_node=appendTransparentVariable(C.size());
    /*trans_node=new FiniteStates(C.size());
    trans_node.setTransparency(FiniteStates.TRANSPARENT);
    trans_node.setName("Transparent");
    variables.addElement(trans_node);*/
   
    nv = (int)FiniteStates.getSize(variables);
    setValues(new double[nv]);
    nv=nv/trans_node.getNumStates();
    for(i=0;i<trans_node.getNumStates();i++){
      conf=new Configuration(((Potential)C.elementAt(i)).getVariables());
      for(j=0;j<nv;j++){
        conf2=new Configuration(getVariables(),conf);
        conf2.putValue(trans_node,i);
        setValue(conf2,((Potential)C.elementAt(i)).getValue(conf));
        conf.nextConfiguration();
      }
    }
  }

  /**
   * Constructs a new PotentialConvexSet for a list of
   * variables and a a number showing the values for a transparent
   * variable
   * @param vars vector with the variables
   * @param states number of states for the transparent variable
   * to create
   */
  public PotentialConvexSet(Vector vars, int states) {
    super(vars);
    FiniteStates trans_node;
    
    // Make a transparent variable, with the given number of states
    trans_node=appendTransparentVariable(states);
    
    // Get the number of values
    int nv=(int)FiniteStates.getSize(variables);
    setValues(new double[nv]);
  }

  /**
   * Saves this potential to a file. The values are written as convex-set.
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void save(PrintWriter p) {   
    int i,j, total;
    Vector listTrans;
    Configuration conf;
    int nExtremes;
    PotentialTable potRestricted;
    double array[];
    
    p.print("values= convex-set (\n");   
    listTrans=getListTransparents();
    conf=new Configuration(listTrans);
    nExtremes=conf.possibleValues();
    total = (int)FiniteStates.getSize(variables)/nExtremes;
    for(j=0;j<nExtremes;j++){
      potRestricted=(PotentialTable)restrictVariable(conf);
      p.print("   table(");
      array=potRestricted.getValues();
      for (i=0 ; i<total ; i++) {
        p.print(array[i]+" ");
      }
      p.print(")\n");
      conf.nextConfiguration();
    }
    p.print(");\n");
  }
  
  /**
   * Normalizes the values of this potential. Each extreme point is normalized
   * independently.
   * The object is modified.
   */
  
  public void normalize() {
    int i,j,nextremes,ncases;
    double s,v;
    Configuration confTrans,confAllVars;
    Vector vTrans,vNonTrans,vAllVars;
    
    vTrans=getListTransparents();
    vNonTrans=getListNonTransparents();
    vAllVars=new Vector(vTrans);
    vAllVars.addAll(vNonTrans);
    nextremes=(int)FiniteStates.getSize(vTrans);
    ncases=(int)FiniteStates.getSize(vNonTrans);
    
    confTrans=new Configuration(vTrans);
    confAllVars=new Configuration(vAllVars);
    for(j=0;j<nextremes;j++){
      s = totalPotential(confTrans);
      for (i=0 ; i<ncases ; i++){
        v=getValue(confAllVars);
        setValue(confAllVars,v/s);
        confAllVars.nextConfiguration();
      }
      confTrans.nextConfiguration();
    }
  }


  public String getClassName() {
    return new String("PotentialConvexSet");
  }
}