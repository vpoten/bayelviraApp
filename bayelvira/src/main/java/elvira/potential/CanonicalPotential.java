package elvira.potential;

import java.io.*;
import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.text.*;

import elvira.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author francisco
 * @version 1.0
 */

public class CanonicalPotential extends PotentialFunction {

  /* FIELDS */

  protected String henrionVSdiez;
  private PotentialTable theCPT;

  /* CONSTRUCTORS */

  public CanonicalPotential () {
    //super();
    variables = new Vector();
    arguments = new Vector();
    strArg = new Vector();
    theCPT=null;
    henrionVSdiez="Diez";
  }

  public CanonicalPotential (NodeList vars) {
    //super(vars);
    variables = (Vector)vars.getNodes().clone();
    arguments = new Vector();
    strArg = new Vector();
    theCPT=null;
    henrionVSdiez="Diez";
  }

  public CanonicalPotential (Vector vars) {
    //super(vars);
    variables = (Vector)vars.clone();
    arguments = new Vector();
    strArg = new Vector();
    theCPT=null;
    henrionVSdiez="Diez";
  }

  /* METHODS */

  public void setHenrionVSDiez(String isIt) {
    henrionVSdiez = new String(isIt);
  }

  public String isHenrionVSDiez() {
    return henrionVSdiez;
  }

  public boolean isCPTcalculated() {
    if (theCPT == null) {
      return false;
    }
    else {
      return true;
    }
  }

  /* Set the CPT from a PotentialTable */

  public void setCPT(PotentialTable potCPT) {
    theCPT = new PotentialTable();
    theCPT.setVariables(variables);
    theCPT.setValues(potCPT.getValues());
  }

  /* Get the CPT */

  public PotentialTable getCPT() {
    if (theCPT != null) {
      return theCPT;
    }
    else {
      return getCPTbyColumns();
    }
  }

  /* Update CPT */

  public PotentialTable updateCPT() {
    return getCPTbyColumns();
  }

  /* If CPT is not stored in theCPT, it is constructed by columns according to
   * the "function" field */

  public PotentialTable getCPTbyColumns() {

    if ((getFunction().getName().equals("Or") || getFunction().getName().equals("CausalMax")) && isHenrionVSDiez().equals("Henrion")) {
       ((CanonicalMaxFunction) getFunction()).transform2Diez(arguments, ((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates());
    }

    Relation relation = new Relation(getVariables());
    relation.setValues(new CanonicalPotential(getVariables()));
    ((CanonicalPotential) relation.getValues()).setFunction(getFunction());
    for (int i=0; i<arguments.size(); i++) {
      ((CanonicalPotential) relation.getValues()).addArgument(strArg.elementAt(i).toString());
      ((CanonicalPotential) relation.getValues()).setArgumentAt((Potential) arguments.elementAt(i),i);
    }

    int cols = 1; /* Number of cols of the CPT */
    int rows = ((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates(); /* Number of rows of the CPT */
    Vector parentNodes = new Vector();
    Vector theNodes = new Vector();

    theNodes.addElement((FiniteStates) /*relation.*/getVariables().elementAt(0));

    for (int i=1; i<relation.getVariables().size(); i++) {
      cols=((FiniteStates) /*relation.*/getVariables().elementAt(i)).getNumStates() * cols;
      parentNodes.addElement(((FiniteStates) /*relation.*/getVariables().elementAt(i)));
      theNodes.addElement((FiniteStates) /*relation.*/getVariables().elementAt(i));
    }

    PotentialTable pt = new PotentialTable(theNodes);

    Configuration config = new Configuration(parentNodes);

    pt.setValues(new double[cols*((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates()]);

    for (int i=0; i<cols; i++) {
      double theColumn[];
      theColumn = getCPTColumn(config);

      for (int j=0; j<((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates(); j++) {
        pt.setValue(i+cols*j,theColumn[j]);
      }

      config.nextConfiguration();
    }

    setCPT(pt);

    if ((getFunction().getName().equals("Or") || getFunction().getName().equals("CausalMax")) && isHenrionVSDiez().equals("Henrion")) {
       ((CanonicalMaxFunction) getFunction()).transform2Henrion(arguments, ((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates());
    }

    return pt;
  }

  /**
   * Get the CPT column given by the parent's Configuration config
   */

  public double[] getCPTColumn(Configuration config) {

    Relation relation = new Relation(getVariables());
    relation.setValues(new CanonicalPotential(getVariables()));
    ((CanonicalPotential) relation.getValues()).setFunction(getFunction());
    for (int i=0; i<arguments.size(); i++) {
      ((CanonicalPotential) relation.getValues()).addArgument(strArg.elementAt(i).toString());
      ((CanonicalPotential) relation.getValues()).setArgumentAt((Potential) arguments.elementAt(i),i);
    }

    int dataSize;

    if (getFunction().getName().equals("Xor")) {
      dataSize = (/*relation.*/getVariables().size()-1)*((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates();
    }
    else {
      dataSize = (/*relation.*/getVariables().size())*((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates();
    }

    double[] theData = new double[dataSize];

    double [] theColumn;
    int total = 0;
    for (int j=0; j</*relation.*/getVariables().size()-1; j++) {
      int column = config.getValue(config.getVariable(j));

      for (int k=0; k<((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates(); k++) {
	theData[total+k]=((PotentialTable) ((CanonicalPotential) relation.getValues()).getArgumentAt(j)).getValue(column+k*((FiniteStates) relation.getVariables().elementAt(j+1)).getNumStates());
      }

      total=total+((FiniteStates) relation.getVariables().elementAt(0)).getNumStates();
    }

    if (!getFunction().getName().equals("Xor")) {
      for (int k=0; k<((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates(); k++) {
	theData[dataSize-((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates()+k]=
	((PotentialTable) ((CanonicalPotential) relation.getValues()).getArgumentAt(relation.getVariables().size()-1)).getValue(k);
      }
    }

    theColumn = ((CanonicalFunction) function).PotValues(theData, ((FiniteStates) /*relation.*/getVariables().elementAt(0)).getNumStates());

    return theColumn;
  }

  public void diez2Henrion(int states) {
    ((CanonicalMaxFunction) getFunction()).transform2Henrion(arguments,states);
  }

  public void henrion2Diez(int states) {
    ((CanonicalMaxFunction) getFunction()).transform2Diez(arguments,states);
  }

  /* ++++++++++++++++++++TO REDEFINE++++++++++++++++++++ */

  /**
   * Methods in PotentialTable. Added here to know which are to be implemented to reuse
   * the propagation methods.
   */

  public PotentialTable add(PotentialTable p, boolean byname) {
    if (!isCPTcalculated()) {
      System.out.println("Error: add(PotentialTable,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.add(p,byname);
    }
  }

  public PotentialTable Add(PotentialTable p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: Add(PotentialTable) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.Add(p);
    }
  }

  public Potential addition(Potential p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: addition(Potential) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.addition(p);
    }
  }

  public Potential addVariable(FiniteStates var) {
    if (!isCPTcalculated()) {
      System.out.println("Error: addVariable(FiniteStates) not implemented when the CPT is null in CanonicalPotential!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.addVariable(var);
    }
  }

  /* THIS CANNOT BE REDEFINED HERE: INHERITED FROM PotentialFunction... */
  //public PotentialTable addVariable(Vector vars) {
    //System.out.println("Error: addVariables(Vector) not implemented in CanonicalPotential!!!!");
    //return new PotentialTable();
  //}

  public Potential combine(Potential p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: combine(Potential) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.combine(p);
    }
  }

  public PotentialTable combine(PotentialTable p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: combine(PotentialTable) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.combine(p);
    }
  }

  public PotentialTable combine(PotentialTree p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: combine(PotentialTree) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.combine(p);
    }
  }

  public void combineWithSubset(Potential p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: combineWithSubset(Potential) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.combineWithSubset(p);
    }
  }

  public /*static*/ PotentialTable convertToPotentialTable(Potential pot) {
    if (!isCPTcalculated()) {
      System.out.println("Error: convertToPotentialTable(Potential) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.convertToPotentialTable(pot);
    }
  }

  public Potential copy() {
    if (!isCPTcalculated()) {
      //System.out.println("Error: copy() not implemented in CanonicalPotential when the CPT is null!!!!");
      //return new PotentialTable();
      return getCPTbyColumns().copy();
    }
    else {
      //return theCPT.copy();
      return getCPTbyColumns().copy();
    }
  }

  public double crossEntropyPotential() {
    if (!isCPTcalculated()) {
      System.out.println("Error: crossEntropyPotential() not implemented in CanonicalPotential when the CPT is null!!!!");
      return 0.0;
    }
    else {
      return theCPT.crossEntropyPotential();
    }
  }

  public Potential divide(Potential p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: divide(Potential) not implemented in CanonicalPotential!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.divide(p);
    }
  }

  public PotentialTable divide(PotentialTable p) {
    if (!isCPTcalculated()) {
      System.out.println("Error: divide(PotentialTable) not implemented in CanonicalPotential!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.divide(p);
    }
  }

  public double entropyPotential() {
    if (!isCPTcalculated()) {
      System.out.println("Error: entropyPotential() not implemented in CanonicalPotential!!!!");
      return 0.0;
    }
    else {
      return theCPT.entropyPotential();
    }
  }

  public double entropyPotential(Configuration conf) {
    if (!isCPTcalculated()) {
      System.out.println("Error: entropyPotential(Configuration) not implemented in CanonicalPotential!!!!");
      return 0.0;
    }
    else {
      return theCPT.entropyPotential(conf);
    }
  }

  public String getClassName() {
    if (!isCPTcalculated()) {
      return new String("CanonicalPotential");
    }
    else {
      return new String("CanonicalPotential");
    }
  }

  public Configuration getMaxConfiguration(Configuration subconf) {
    if (!isCPTcalculated()) {
      System.out.println("Error: getMaxConfiguration(Configuration) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new Configuration();
    }
    else {
      return theCPT.getMaxConfiguration(subconf);
    }
  }

  public long getSize() {
    if (!isCPTcalculated()) {
      long theSize = 1;
      for (int i=0; i<variables.size(); i++) {
	theSize = theSize*((FiniteStates) variables.elementAt(i)).getNumStates();
      }

      return theSize;
    }
    else {
      return theCPT.getSize();
    }
  }

  public double getValue(Configuration conf) {

    if (!isCPTcalculated()) {
      Vector theParents = new Vector();

      for (int i=1; i<conf.getVariables().size(); i++) {
        theParents.addElement(((FiniteStates) conf.getVariable(i)));
      }

      Configuration parentsConfig = new Configuration(theParents);

      for (int i=0; i<parentsConfig.getVariables().size(); i++) {
        parentsConfig.putValue(parentsConfig.getVariable(i),conf.getValue(parentsConfig.getVariable(i)));
      }

      Relation relation = new Relation(getVariables());
      relation.setValues(new CanonicalPotential(getVariables()));
      ((CanonicalPotential) relation.getValues()).setFunction(getFunction());

      for (int i=0; i<arguments.size(); i++) {
	((CanonicalPotential) relation.getValues()).addArgument(strArg.elementAt(i).toString());
        ((CanonicalPotential) relation.getValues()).setArgumentAt((Potential) arguments.elementAt(i),i);
      }

      double arg[] = getCPTColumn(parentsConfig);

      //System.out.println("El valor es "+arg[conf.getValue((FiniteStates) conf.getVariable(0))]);

      return arg[conf.getValue((FiniteStates) conf.getVariable(0))];
    }
    else {
      return theCPT.getValue(conf);
    }

  }

  public double getValue(Hashtable positions, int[] conf) {
    if (!isCPTcalculated()) {
      System.out.println("Error: getValue(Hashtable,int[]) not implemented in CanonicalPotential!!!!");
      return 0.0;
    }
    else {
      return theCPT.getValue(positions,conf);
    }
  }

  public double getValue(int index) {
    if (!isCPTcalculated()) {
      Configuration auxConf = new Configuration(variables);
      for (int i=0; i<getSize(); i++) {
	if (auxConf.getIndexInTable() == i) {
	  break;
	}
	auxConf.nextConfiguration();
      }

      return getValue(auxConf);
    }
    else {
      return theCPT.getValue(index);
    }
  }

  public Vector getVariables() {
    return(variables);
  }

  public void incValue(int i, double increment) {
    if (!isCPTcalculated()) {
      System.out.println("Error: incValue(int,double) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.incValue(i,increment);
    }
  }

  /*public void instantiateEvidence(Configuration evid) {
    if (!isCPTcalculated()) {
      System.out.println("Error: instantiateEvidence(Configuration) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      instantiateEvidence(evid);
    }
  }*/

  /*public void linearPool(Vector pv) {
    if (!isCPTcalculated()) {
      System.out.println("Error: linearPool(Vector) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.linearPool(pv);
    }
  }*/

  /*public void logarithmicPool (Vector pv) {
    if (!isCPTcalculated()) {
      System.out.println("Error: logarithmicPool(Vector) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.logarithmicPool(pv);
    }
  }*/

  /*public void LPNormalize() {
    if (!isCPTcalculated()) {
      System.out.println("Error: LPNormalize() not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.LPNormalize();
    }
  }*/

  public Potential marginalizePotential(Vector vars) {
    if (!isCPTcalculated()) {
      System.out.println("Error: marginalizePotential(Vector) not implemented in CanonicalPotential!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.marginalizePotential(vars);
    }
  }

  public Potential maxMarginalizePotential(Vector vars) {
    if (!isCPTcalculated()) {
      System.out.println("Error: maxMarginalizePotential(Vector) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.maxMarginalizePotential(vars);
    }
  }

  public PotentialTable multiply(PotentialTable p, boolean byname) {
    if (!isCPTcalculated()) {
      System.out.println("Error: multiply(PotentialTable,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      return theCPT.multiply(p,byname);
    }
  }

  /*public void noisyORPool(Vector pv, boolean loose) {
    if (!isCPTcalculated()) {
      System.out.println("Error: noisyORPool(Vector,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.noisyORPool(pv,loose);
    }
  }*/

  /*public void normalize() {
    if (!isCPTcalculated()) {
      System.out.println("Error: normalize() not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.normalize();
    }
  }*/

  /*public void normalizeOver(FiniteStates v) {
    if (!isCPTcalculated()) {
      System.out.println("Error: normalizeOver(FiniteStates) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.normalizeOver(v);
    }
  }*/

  public Potential restrictVariable(Configuration conf) {
    if (!isCPTcalculated()) {
      Configuration auxConf;
      Vector aux;
      FiniteStates temp;
      PotentialTable pot;
      int i;

      // Creates a configuration preserving the values in conf.
      auxConf = new Configuration(variables,conf);

      // Computes the list of variables of the new Potential.
      aux = new Vector();
      for (i=0 ; i<variables.size() ; i++) {
	temp = (FiniteStates)variables.elementAt(i);
	if (conf.indexOf(temp) == -1) {
	  aux.addElement(temp);
	}
      }

      pot = new PotentialTable(aux);

      for (i=0 ; i<pot.getValues().length ; i++) {
	pot.setValue(i,getValue(auxConf));
	auxConf.nextConfiguration(conf);
      }

      return pot;
    }
    else {
      return theCPT.restrictVariable(conf);
    }
  }

  public void save(PrintWriter p) {
    int i;
    p.print("values= function  \n");
    if(function!=null)
      p.print("          "+ function.getName());
    else
      p.print("          Unknown");
    p.print("(");
    for (i=0;i<arguments.size();i++)
    {
      if(arguments.elementAt(i).getClass()==Double.class)
        p.print(((Double)(arguments.elementAt(i))).doubleValue());
      else if(arguments.elementAt(i) instanceof Potential)
        p.print(strArg.elementAt(i));
      else
        System.out.println("Error in CanonicalPotential.save(PrintWriter p): "+
			  "I do not know this kind of argument");
      if (i+1!=arguments.size()) p.print(",");
    }
    p.print(");");
    p.print("\n\n");
    if (getFunction().getName().equals("Or") || getFunction().getName().equals("CausalMax")) {
      p.print("henrionVSdiez = \""+henrionVSdiez+"\";\n");
    }
  }

  /*public void saveAsTable(PrintWriter p) {
    System.out.println("Error: saveAsTable(PrintWriter) not implemented in CanonicalPotential!!!!");
  }

  public void saveMaxResult(PrintWriter p) {
    System.out.println("Error: saveMaxResult(PrintWriter) not implemented in CanonicalPotential!!!!");
  }

  public void saveResult(PrintWriter p) {
    System.out.println("Error: saveResult(PrintWriter) not implemented in CanonicalPotential!!!!");
  }*/

  /*public void setTreeFromTable(ProbabilityTree tree, Configuration conf, Vector vars) {
    if (!isCPTcalculated()) {
      System.out.println("Error: setTreeFromTable(ProbabilityTree,Configuration,Vector) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      theCPT.setTreeFromTable(tree,conf,vars);
    }
  }*/

  public void setValue(Configuration conf, double value) {
    if (!isCPTcalculated()) {
      System.out.println("Error: setValue(Configuration,double) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      System.out.println("Error: setValue(Configuration,double) not implemented in CanonicalPotential when the CPT is null!!!!");
      //theCPT.setValue(conf,value);
    }
  }

  public void setValue(double value) {
    if (!isCPTcalculated()) {
      System.out.println("Error: setValue(double) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      System.out.println("Error: setValue(double) not implemented in CanonicalPotential when the CPT is null!!!!");
      //theCPT.setValue(value);
    }
  }

  public void setValue(int index, double value) {
    if (!isCPTcalculated()) {
      System.out.println("Error: setValue(int,double) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      System.out.println("Error: setValue(int,double) not implemented in CanonicalPotential when the CPT is null!!!!");
      //theCPT.setValue(index,value);
    }
  }

  public void setValues(double[] v) {
    if (!isCPTcalculated()) {
      System.out.println("Error: setValues(double[]) not implemented in CanonicalPotential when the CPT is null!!!!");
    }
    else {
      System.out.println("Error: setValues(double[]) not implemented in CanonicalPotential when the CPT is null!!!!");
      //theCPT.setValues(v);
    }
  }

  public double totalPotential() {
    if (!isCPTcalculated()) {
      System.out.println("Error: totalPotential() not implemented in CanonicalPotential when the CPT is null!!!!");
      return 0.0;
    }
    else {
      return theCPT.totalPotential();
    }
  }

  public double totalPotential(Configuration conf) {
    if (!isCPTcalculated()) {
      System.out.println("Error: totalPotential(Configuration) not implemented in CanonicalPotential when the CPT is null!!!!");
      return 0.0;
    }
    else {
      return theCPT.totalPotential(conf);
    }
  }

  public PotentialTree toTree() {
    if (!isCPTcalculated()) {
      System.out.println("Error: toTree() not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTree();
    }
    else {
      return theCPT.toTree();
    }
  }

  private PotentialTable binNoisyOr(Vector vp, boolean loose) {
    if (!isCPTcalculated()) {
      System.out.println("Error: binNoisyOr(Vector,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      //return theCPT.binNoisyOr(vp,loose);
      System.out.println("Error: binNoisyOr(Vector,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
  }

  private PotentialTable mulNoisyOr(Vector vp, boolean loose) {
    if (!isCPTcalculated()) {
      System.out.println("Error: mulNoisyOr(Vector,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
    else {
      //return theCPT.mulNoisyOr(vp,loose);
      System.out.println("Error: mulNoisyOr(Vector,boolean) not implemented in CanonicalPotential when the CPT is null!!!!");
      return new PotentialTable();
    }
  }

  /**
   * New method to reorder strArg
   */

  public void setStrArgumentAt(String strVal, int position) {
    strArg.setElementAt(strVal,position);
  }

}