/* Evidence.java */

package elvira;

import java.util.Vector;
import java.util.Random;
import java.io.*;
import elvira.parser.*;
import elvira.FiniteStates;
import elvira.NodeList;
import elvira.tools.DSeparation;
import elvira.Bnet;


/**
 * Class <code>Evidence</code>. Implements the observations over a network.
 * The observations are represented as a configuration of variables,
 * both discrete (<code>FiniteStates</code>) and/or
 * (<code>Continuous</code>).
 *
 * @sice 17/01/2006
 */

public class Evidence extends ContinuousConfiguration  {

/**
 * The name of the observations.
 */
String name;

/**
 * A title.
 */
String title;

/**

 */
String comment;

/**
 * The one who created the observations.
 */
String author;

/**
 * The author of the last change.
 */
String whoChanged;

/**
 * The date of the last change.
 */
String whenChanged;

/**
 * Name of the network concerning this evidence.
 */
String networkName;

/**
 * Indicates whether the evidence can be modified or not.
 */
boolean locked;

/**
 * Version of the evidence.
 */
float version;

/**
 * Default states for <code>FiniteStates</code> variables.
 */
static String FiniteStatesDefaultStates[]={"absent","present"};


/**
 * Just to check the class.
 */

public static void main(String args[]) throws ParseException, IOException { 

  Bnet bnet;
  Evidence evidence;
  FileInputStream networkFile, evI;
  FileWriter evidenceFile, evO;
  int nvar;

/*  if (args.length < 3) {
    System.out.print("Too few arguments. Arguments are: ElviraFile EvidenceFile nvar");
    System.exit(1);
  }
  
  networkFile = new FileInputStream(args[0]);
  bnet = new Bnet(networkFile);
  System.out.println("Elvira file read");
  nvar = (Integer.valueOf(args[2])).intValue();
  evidence = new Evidence(bnet.getNodeList(),nvar);
  System.out.println("Random evidence selected");
  evidenceFile = new FileWriter(args[1]);
  evidence.save(evidenceFile);
  evidenceFile.close();
  System.out.println("Evidence file saved"); */

  if (args.length < 3) {
    System.out.print("Too few arguments. Arguments are: ElviraFile EvidenceFile EvidenceFileOut\n");
    System.exit(1);
  }
  
  networkFile = new FileInputStream(args[0]);
  bnet = new Bnet(networkFile);
  System.out.println("Elvira file read");
  evI = new FileInputStream(args[1]);
//  evidence = new Evidence(evI,bnet.getNodeList());
  evidence = new Evidence(bnet,4);
  evO = new FileWriter(args[2]);
  evidence.save(evO);
  evO.close();
  System.out.println("Evidence file saved");
} 


/**
 * Constructs an empty evidence.
 */

public Evidence() {

  variables = new Vector();
  values = new Vector();
  continuousVariables = new Vector();
  continuousValues = new Vector();
  setName("NoName");
  setTitle("Untitled");
  setComment("");
  setAuthor("");
  setWhoChanged("");
  setWhenChanged("");
  setLocked(false);
  setVersion((float)1.0);
  setNetworkName("");
}


/**
 * Constructs an evidence from a configuration.
 * @param conf the configuration.
 */

public Evidence(Configuration conf) {

  variables = (Vector) conf.getVariables().clone();
  values = (Vector) conf.getValues().clone();
  if (conf instanceof ContinuousConfiguration){
    continuousVariables = (Vector) ((ContinuousConfiguration)conf).getContinuousVariables().clone();
    continuousValues = (Vector) ((ContinuousConfiguration)conf).getContinuousValues().clone();
  }  
  setName("NoName");
  setTitle("Untitled");
  setComment("");
  setAuthor("");
  setWhoChanged("");
  setWhenChanged("");
  setLocked(false);
  setVersion((float)1.0);
  setNetworkName("");
}

/**
 * Constructs an new evidence from two previous evidences
 * @param evidence1
 * @param evidence2
 */

public Evidence(Evidence evidence1, Evidence evidence2) {
  Vector variables2=(Vector)(evidence2.getVariables()).clone();
  // Create a new evidence from evidence1
  variables = (Vector)(evidence1.getVariables()).clone();
  values = (Vector)(evidence1.getValues()).clone();
  continuousVariables = (Vector)(evidence1.getContinuousVariables()).clone();
  continuousValues = (Vector)(evidence1.getContinuousValues()).clone();
  setName(evidence1.getName());
  setTitle(evidence1.getTitle());
  setComment(evidence1.getComment());
  setAuthor(evidence1.getAuthor());
  setWhoChanged(evidence1.getWhoChanged());
  setWhenChanged(evidence1.getWhenChanged());
  setLocked(evidence1.getLocked());
  setVersion(evidence1.getVersion());
  setNetworkName(evidence1.getNetworkName());
  
  // Add the variables in evidence2
  int evidence2Size=variables2.size();
  // Consider all of them
  for(int i=0; i < evidence2Size;i++) {
    // Consider every variable
    FiniteStates var2=(FiniteStates)variables2.elementAt(i);
    
    // Check if this variable is already included in variables
    boolean repeated=false;
    for(int j=0; j < variables.size(); j++) {
      FiniteStates var1=(FiniteStates)variables.elementAt(j);
      
      // Compare names
      if (var1.getName().equals(var2.getName()) == true){
        repeated=true;
        break;
      }
    }
    
    // If the variable was found, exit
    if (repeated == true) {
      System.out.println("Error in class Evidence: ");
      System.out.println("Method: Constructor Evidence (Evidence, Evidence)");
      System.out.println("The var "+var2.getName()+ " is repeated in both evidences");
      System.exit(0);
    }
    
    // Get its value in evidence2
    int val=evidence2.getValue(i);
    // Set this value for the new variable
    insert(var2,val);
  }
}

/**
 * Constructs a new evidence from the  configuration conf.
 * The evidence is done for the list of variables nlist. 
 * This method allows that conf comes from any Bnet (possibly 
 * different from the current one).
 * @param nlist the list of variables corresponding to the Bnet where
 * the evidence will be used
 * @param conf the configuration.
 * 
 */

public Evidence(NodeList nlist, Configuration conf) {
  FiniteStates var;
  int i;

  for(i=0;i<conf.size();i++){
      var=(FiniteStates)nlist.getNode(conf.getVariable(i).getName());
      insert(var,conf.getValue(i));
  }
  setName("NoName");
  setTitle("Untitled");
  setComment("");
  setAuthor("");
  setWhoChanged("");
  setWhenChanged("");
  setLocked(false);
  setVersion((float)1.0);
  setNetworkName("");
}

/**
 * Constructs a new evidence reading from a file, given a list
 * of all possible variables.
 * @param f the input file.
 * @param list the list of all possible variables.
 */

public Evidence(FileInputStream f, NodeList list)
    throws ParseException ,IOException {
			     
  EvidenceParse parser = new EvidenceParse(f);
  
  parser.initialize(list);
 
  parser.CompilationUnit();
  translate(parser);  
}

/**
 * Constructs a new evidence reading from a file, given a list
 * of all possible variables.
 * @param evidenceFile the input file.
 * @param list the list of all possible variables.
 */
public Evidence(String evidenceFile, NodeList list) throws ParseException ,IOException{
  FileInputStream f = new FileInputStream(evidenceFile);
  EvidenceParse parser = new EvidenceParse(f);
  parser.initialize(list);
  parser.CompilationUnit();
  translate(parser);  
}

/**
 * Constructs an evidence from a continuous configuration.
 * @param conf the continuous configuration.
 */

public Evidence(ContinuousConfiguration conf) {

  variables = (Vector) conf.getVariables().clone();
  values = (Vector) conf.getValues().clone();
  continuousVariables = (Vector) conf.getContinuousVariables().clone();
  continuousValues = (Vector) conf.getContinuousValues().clone();
  setName("NoName");
  setTitle("Untitled");
  setComment("");
  setAuthor("");
  setWhoChanged("");
  setWhenChanged("");
  setLocked(false);
  setVersion((float)1.0);
  setNetworkName("");
}


/**
 * Constructs a random evidence for some variables of a Bnet by
 * forward sampling. The generated evidence will therefore have
 * positive probability.
 *
 * @param bn a Bnet.
 * @param nvar the number of variables to be randomly included in the evidence
 */

public Evidence(Bnet bn, int nvar) {

    this();
    
    Evidence auxEvi;
    boolean[] selected;
    int i, r;
    Random random;
    
    auxEvi = bn.generateEvidenceByForwardSampling(bn.getNodeList());
    
    if (nvar > bn.getNodeList().size()) {
       System.out.println("Error in Evidence.Evidence(Bnet bn, int nvar): "+
                          "nvar > nodeList.size()");
    System.exit(1);
    }
    
    selected = new boolean[auxEvi.size()];
    
    for (i=0 ; i<nvar ; i++) {
      selected[i] = false;
    }
    
  random = new Random();
  for (i=0 ; i<nvar ; i++) { // Select nodes randomly
    r = random.nextInt(auxEvi.size());
    if (!selected[r]) {
      selected[r] = true;
      this.insert(auxEvi.getVariable(r),auxEvi.getValue(r));
    }
    else { // If the node was previously selected, then select another one.
      i--;
    }
  }
}


/**
 * Constructs a new random evidence for the <code>NodeList</code> object
 * with <code>nvar</code> nodes. All these nodes must be
 * <code>FiniteStates</code> nodes.
 * @param nodeList the <code>NodeList</code> associated to this 
 * <code>Evidence</code>.
 * @param nvar the number of variables in this <code>Evidence</code>
 */

public Evidence(NodeList nodeList, int nvar) {
  this();

  int i, r, s;
  boolean selected[];
  Random random;

  if (nvar > nodeList.size()) {
    System.out.println("Error in Evidence.Evidence(NodeList nodeList, int nvar): "+
                       "nvar > nodeList.size()");
    System.exit(1);
  }
  selected = new boolean[nodeList.size()];
  for (i=0 ; i<nvar ; i++) {
    selected[i] = false;
  }
  random = new Random();
  for (i=0 ; i<nvar ; i++) { // Select nodes randomly
    r = random.nextInt(nodeList.size());
    if (!selected[r]) {
      selected[r] = true;
      s = random.nextInt(((FiniteStates)(nodeList.elementAt(r))).getNumStates());
      insert((FiniteStates)(nodeList.elementAt(r)),s);
    }
    else { // If the node was previously selected, then select another one.
      i--;
    }
  }
}


/**
 * Saves an evidence to a file.
 * @param f the file.
 */

public void save(FileWriter f) throws IOException {

  PrintWriter p;
  
  p = new PrintWriter(f);
  
  p.print("// Evidence case \n");
  p.print("//   Elvira format \n\n");
  
  
  p.print("evidence  "+getName()+" { \n\n");
  
  saveAux(p);
  p.print ("}\n");        
}


/**
 * Used by <code>save</code>.
 * @param p the <code>PrintWriter</code> where the evidence will be written.
 */

private void saveAux(PrintWriter p) throws IOException { 
 
  int i, j, k;
  double d;
  String n;

  p.print("// Evidence Properties\n\n");

  if (!getTitle().equals(""))
    p.print("title = \""+ getTitle()+"\";\n");

  if (!getAuthor().equals(""))
    p.print("author = \""+ getAuthor()+"\";\n");

  if (!getWhoChanged().equals(""))
    p.print("whoChanged = \""+ getWhoChanged()+"\";\n");

  if (!getWhenChanged().equals(""))
    p.print("whenChanged = \""+ getWhenChanged()+"\";\n");

  if (!getComment().equals(""))
    p.print("title = \""+ getComment()+"\";\n");

  if (!getNetworkName().equals(""))
    p.print("networkName = \""+ getNetworkName()+"\";\n");

  if (getLocked())
    p.print("locked = true;\n");

  p.print("version = "   +getVersion() +";\n\n"); 

  j = values.size();
 
  for (i=0 ; i<j ; i++) {
    p.print( ((Node) variables.elementAt(i)).getName()+" = ");
    
    n = new String((String) 
		   ((FiniteStates)variables.elementAt(i)).
		   getPrintableState(((Integer)
			     values.elementAt(i)).intValue()));
    
    p.print(n);    
    
    /*
    try {
      k = Integer.parseInt(n);
      p.print("\""+n+"\"");
    }
    catch (NumberFormatException e)
      {p.print(n);}*/
    
    p.print(",\n");	 
  }
  
  j = continuousValues.size();
 
  for (i=0 ; i<j ; i++) {
    p.print( ((Node) continuousVariables.elementAt(i)).getName()+" = ");
    
    n = Double.toString(((Double)continuousValues.elementAt(i)).doubleValue());
    try {
      d = Double.parseDouble(n);
      //p.print("\""+n+"\"");
      p.print(n);
    }
    catch (NumberFormatException e)
      {p.print(n);}
    p.print(",\n");	 
  }
}

/* ********* Access methods ************** */

/**
 * Initializes the evidence from a parser.
 * @param parser the parser with the contents of the evidence.
 */

public void translate(EvidenceParse parser) {   
  
  Float f;
  
  setName(parser.Name);
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setNetworkName(parser.NetworkName);
  setVersion(f = new Float(parser.version));
  values = parser.C.values;
  variables = parser.C.variables;
  continuousValues = parser.C.continuousValues;
  continuousVariables = parser.C.continuousVariables;
}


/**
 * Gets the name.
 * @return the name of the evidence.
 */

public String getName() {

  return name;
}


/**
 * Gets the title.
 * @return the title of the evidence.
 */

public String getTitle() {

  return title;
}


/**
 * Gets the comment.
 * @return the comment of the evidence.
 */

public String getComment() {

  return comment;
}


/**
 * Gets the author.
 * @return the author of the evidence.
 */

public String getAuthor() {

  return author;
}


/**
 * Says who made the last change.
 * @return the content of <code>whoChanged</code>.
 */

public String getWhoChanged() {

  return whoChanged;
}


/**
 * Gets the date of the last change.
 * @return the content of <code>whenChanged</code>.
 */

public String getWhenChanged() {

  return whenChanged;
}


/**
 * Gets the name of the network.
 * @return the name of the network.
 */

public String getNetworkName() {

  return networkName;
}


/**
 * Says whether the evidence is locked or not.
 * @return <code>true if the evidence can be modified or <code>false</code>
 * otherwise.
 */

public boolean getLocked() {

  return locked;
}


/**
 * Gets the version of the evidence.
 * @return the version.
 */

public float getVersion() {

  return version;
}


/* ************** Modifiers *************** */

/**
 * Ses the name.
 * @param s the name of the evidence.
 */

public void setName(String s) {

  name = new String(s);
}

/**
 * Sets the title.
 * @param s the title of the evidence.
 */

public void setTitle(String s) {
 
  title = new String(s);
}


/**
 * Sets the comment.
 * @param s the comment of the evidence.
 */

public void setComment(String s) {

  comment = new String(s);
}


/**
 * Sets the author.
 * @param s the author of the evidence.
 */

public void setAuthor(String s) {

  author = new String(s);
}


/**
 * Sets the author of the last change.
 * @param s the author of the last change.
 */

public void setWhoChanged(String s) {

  whoChanged = new String(s);
}


/**
 * Sets the date of the last change.
 * @param s the date of the last change.
 */

public void setWhenChanged(String s) {

  whenChanged = new String(s);
}


/**
 * Sets the version.
 * @param s the version.
 */

public void setVersion(float s) {

  version = s;
}

/**
 * Sets the version.
 * @param s the version (in <code>Float</code> format).
 */

public void setVersion(Float v) {

  version = v.floatValue();
}


/**
 * Locks or unlocks the evidence.
 * @param b <code>true</code> to lock or <code>false</code> to unlock.
 */

public void setLocked(boolean b) {

  locked = b;
}


/**
 * Sets the name of the network.
 * @param s the name of the network.
 */

public void setNetworkName(String s) {

  networkName = new String(s);
}

  
/**
 * Checks whether the nodes in the evidence are coherent with the nodes
 * in the network. 
 * @param bnet contains the nodes of the network to check.
 * @return <code>true</code> if the nodes in the evidence are coherent with
 * the nodes in the network, or <code>false</code> otherwise.
 */

public boolean coherentEvidence(Bnet bnet) {
  
  int sizeEvidence;
  int i, j;
  boolean result = true;
  
  sizeEvidence = ((Vector)variables).size();
  for (i=0 ; i<sizeEvidence ; i++) {
    FiniteStates fs = getVariable(i);
    if ( (j=bnet.getNodeList().getId(fs.getName())) == -1) {
      result = false;
      break;
    }
    else if ( fs.getNumStates() !=
                 ((FiniteStates)bnet.getNodeList().elementAt(j)).getNumStates()){
      result = false;
      break;
    }
  }
  
  return result;
}


/**
 * This method is used to know whether a node is observed or not.
 * @param node the <code>Node</code> we want to know whether it is
 * observed or not.
 * @return <code>true</code> if <code>node</code> is observed,
 * or <code>false</code> otherwise.
 */

public boolean isObserved(Node node) {
  
  if (node.getTypeOfVariable() == Node.FINITE_STATES) {
    if (indexOf(node) < 0)
      return false;
    else
      return true;
  }
  
  if (getIndex((Continuous)node) < 0)
      return false;
    else
      return true;
}

/**
 * This method compares two Evidence objects.
 * @return boolean, true is they contains the same evidence, 
 *                  and false if otherwise.
 */

public boolean equals(Evidence ev){

    if (this.getAuthor().compareTo(ev.getAuthor())!=0)
        return false;
    if (this.getName().compareTo(ev.getName())!=0)
        return false;

    return super.equals((ContinuousConfiguration)ev);
        
}

} // End of class
