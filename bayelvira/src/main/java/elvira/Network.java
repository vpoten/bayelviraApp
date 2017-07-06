/* Network.java */

package elvira;

import java.io.*;
import java.net.URL;
import java.util.*;
import elvira.parser.*;
import elvira.translator.ToElvParse;
import elvira.translator.bif2elv.*;
import elvira.translator.xbif2elv.*;
import elvira.translator.hugin2elv.*;
import elvira.translator.genie2elv.*;
import elvira.potential.*;



/**
 * The <code>Network</code> class is defined as a <code>Graph</code>
 * (that contains a set of nodesand a set of links) and a set of relations.
 *
 * @see Graph
 * @author ..., fjdiez, ratienza, ...
 * @version 0.1
 * @since 01/06/2004
 */

public class Network extends Graph implements Serializable{

static final long serialVersionUID = 6724143352100430282L;

/**
 * Title of the netwotk.
 */
private String title = "";

/**
 * A comment about the network.
 */
private String comment = "";

/**
 * Author of the network.
 */
private String author = "";

/**
 * Author of the last modification.
 */
private String whoChanged = "";

/**
 * Date of the last modification.
 */
private String whenChanged = "";

/**
 * <code>true</code> if modifications are not allowed, <code>false</code>
 * otherwise. By default, <code>false</code>.
 */
private boolean locked = false;

/**
 * Version.
 */
private float version = (float) 1.0;

/**
 * The list of properties for this network
 */
private Hashtable networkPropertyList;

/**
 * A vector woth the default names for the states of a
 * <code>FiniteStates</code> variable.
 */
private Vector FSDefaultStates;

/**
 * A vector with the default names for the states of a
 * <code>FiniteStates</code> decision variable.
 */
private Vector FSDecisionDefaultStates;

/**
 * A constant defining default states "present" and "absent",
 * very frequently used.
 */
//public String FiniteStatesDefaultStates[]={localize(Elvira.getElviraFrame().getDialogBundle(),"States.present"),localize(Elvira.getElviraFrame().getDialogBundle(),"States.absent")};
public String FiniteStatesDefaultStates[]={"\"present\"","\"absent\""};

/**
 * A constant defining default states "yes" and "no",
 * very frequently used.
 */
//public String FiniteStatesDecisionDefaultStates[]={localize(Elvira.getElviraFrame().getDialogBundle(),"States.yes"),localize(Elvira.getElviraFrame().getDialogBundle(),"States.no")};
public String FiniteStatesDecisionDefaultStates[]={"\"yes\"","\"no\""};

/**
 * Contains the list of relations in the network.
 */
private Vector relationList;

/**
 * Contains the list of relations after compiling the network.
 */
private Vector compiledPotentialList;

/**
 * <code>true</code> if the network has been modified in edit mode,
 * <code>false</code> otherwise.
 */
private boolean isModified = false;

/**
 * Sets the precision as seen in the expanded nodes
 */

private String VisualPrecision = "0.00";


/**
 * Used in Elvira Frame for define default chance node .
 */
private int defaultChanceNode = Node.FINITE_STATES;



/**
 * Program for probing reading and writing of networks from the
 * command line.
 * The command line arguments are as follows:
 * <ol>
 * <li> Input file: the network.
 * <li> Output file.
 * </ol>
 */

public static void main(String args[]) throws elvira.parser.ParseException, elvira.translator.xbif2elv.ParseException,IOException {

  Network b;

  if (args.length < 1)
    System.out.println("Too few arguments. Arguments are: ElviraFile [OutputElviraFile]");
  else {
    b = Network.read(args[0]);
    if (args.length == 2) {
      b.save(args[1]);
    }
  }
}


/**
 * Creates an empty Network.
 */

public Network() {

  super(); // Calls a constructor of <code>Graph</code>.
  setFSDefaultStates (FiniteStatesDefaultStates);
  setFSDecisionDefaultStates (FiniteStatesDecisionDefaultStates);
  relationList = new Vector();
  networkPropertyList=new Hashtable();
}


/**
 * Creates a new random network with a given number of nodes.
 * @param generator the random numbers generator.
 * @param numberOfNodes the number of nodes.
 * @param nParents average of parents for each node.
 * @param con <code>true</code> if we want a connected graph.
 */

public Network(Random generator, int numberOfNodes, double nParents,
	       boolean con) {

  super(generator,numberOfNodes,nParents,con);
  relationList = new Vector();
  networkPropertyList=new Hashtable();
}

/**
 * Used with the bundles. Find the string given as parameter in
 * the bundle specified
 */

/*public String localize (ResourceBundle bundle, String name) {
  return Elvira.getElviraFrame().localize(bundle, name);
}*/


/* ********** Access methods ************ */

/**
 * Sets the precision as seen in the expanded nodes.
 *
 */
public void setVisualPrecision(String VP)
{
  if (VP == null)
    VisualPrecision="0.00";
  else
    VisualPrecision=VP;

  return;
}

/**
 * Gets the value of VisualPrecision.
 */

public String getVisualPrecision()
{
  return VisualPrecision;
}


/**
 * Gets the title of the network.
 * @return the title.
 */

public String getTitle() {

  return title;
}


/**
 * Gets the comment.
 * @return the comment.
 */

public String getComment() {

  return comment;
}


/**
 * Gets the author.
 * @return the author.
 */

public String getAuthor() {

  return author;
}


/**
 * Gets the author of the last modification.
 * @return the author of the last modification.
 */

public String getWhoChanged() {

  return whoChanged;
}


/**
 * Gets the date of the last modification.
 * @return the date of the last modification.
 */

public String getWhenChanged() {

  return whenChanged;
}


/**
 * Determines whether modifications are allowed.
 * @return <code>true</code> if modifications are not allowed,
 * <code>false</code> otherwise.
 */

public boolean getLocked() {

  return locked;
}


/**
 * Gets the version.
 * @return the version.
 */

public float getVersion() {

  return version;
}


/**
 * Gets the default names for the states of <code>FiniteStates</code>
 * variables.
 * @return a <code>Vector</code> with the names.
 */

public Vector getFSDefaultStates(int kind) {

  if (kind != Node.DECISION) {
    return FSDefaultStates;
  }
  else {
    return FSDecisionDefaultStates;
  }

}


/**
 * Gets the relations in the network.
 * @return a list with the relations in the network.
 */

public Vector getRelationList() {

  return relationList;
}





/**
 * Gets the relations in the network after compiling.
 * @return a <code>Vector</code> with the relations in the network
 * after compiling.
 */

public Vector getCompiledPotentialList() {

  return compiledPotentialList;
}


/**
 * Determined whether the network has been edited or not.
 * @return <code>true</code> if the network has been modified in edit mode,
 * <code>false</code> otherwise.
 */

public boolean getIsModified() {

  return isModified;
}


/* ********* Modifiers ********* */


/**
 * Sets the title of the network.
 * @param s the new title.
 */

public void setTitle(String s) {

  title = new String(s);
}


/**
 * Sets the comment of the network.
 * @param s the new comment.
 */

public void setComment(String s) {

  comment = new String(s);
}


/**
 * Sets the author of the network.
 * @param s the new author.
 */

public void setAuthor(String s) {

  author = new String(s);
}


/**
 * Sets the author of the last modification.
 * @param s the author.
 */

public void setWhoChanged(String s) {

  whoChanged = new String(s);
}


/**
 * Sets the date of the last modification.
 * @param s the date.
 */

public void setWhenChanged(String s) {

  whenChanged = new String(s);
}


/**
 * Locks/unlocks the network for modifications.
 * @param b <code>true</code> if modifications are not allowed,
 * <code>false</code> otherwise.
 */

public void setLocked(boolean b) {

  locked = b;
}


/**
 * Sets the version.
 * @param v the new version.
 */

public void setVersion(float v) {

  version  =  v;
}


/**
 * Sets the version.
 * @param v the new version.
 */

public void setVersion(Float v) {

  version = v.floatValue();
}


/**
 * Sets the default states for <code>FiniteStates</code> variables.
 * @param c a <code>Vector</code> with the names of the states.
 */

public void setFSDefaultStates(Vector c) {

  FSDefaultStates = c;
}

/**
 * Sets the default states for <code>FiniteStates</code> decision variables.
 * @param c a <code>Vector</code> with the names of the states.
 */

public void setFSDecisionDefaultStates(Vector c) {

  FSDecisionDefaultStates = c;
}

/**
 * Sets the default states for <code>FiniteStates</code> variables.
 * @param numChance an array of <code>String</code> with the names of the states.
 */

public void setFSDefaultStates(String s[]) {

  int i;

  FSDefaultStates = new Vector();

  for (i=0 ; i<s.length ; i++)
    FSDefaultStates.addElement(s[i]);
}

/**
 * Sets the default states for <code>FiniteStates</code> decision variables.
 * @param numChance an array of <code>String</code> with the names of the states.
 */

public void setFSDecisionDefaultStates(String s[]) {

  int i;

  FSDecisionDefaultStates = new Vector();

  for (i=0 ; i<s.length ; i++)
    FSDecisionDefaultStates.addElement(s[i]);
}


/**
 * Sets the relations in the network.
 * @param r a <code>Vector</code> of relations.
 */

public void setRelationList(Vector r) {

  relationList = r;
}


/**
 * Sets the relations in the network after compiling.
 * @param r a <code>Vector</code> of relations.
 */

public void setCompiledPotentialList(Vector r) {

  compiledPotentialList = r;
}


/**
 * Marks the network as edited or not edited.
 * @param b <code>true</code> if the network has been modified in edit mode,
 * <code>false</code> otherwise.
 */

public void setIsModified(boolean b) {

  isModified = b;
}

/**
 * Sets the property list for this network
 *  @param pList the new property list for this network
 */
void setPropertyList(Hashtable pList){
  networkPropertyList=new Hashtable(pList);
}


/**
 * Graphical Method. Sets all the nodes with the correct axis values.
 * @param byTitle <code>true</code> if the node is displayed by title,
 * <code>false</code> if by name.
 */

/*public void setStrings(boolean byTitle) {

  for (int i=0 ; i<nodeList.size() ; i++) {
    Node n = (Node) nodeList.elementAt(i);
    VisualNode.setAxis (n,byTitle);
  }
}*/



/* **** Methods for saving the data structures in files **** */

/**
 * Saves the list of relations of the network using the text output
 * stream given as parameter.
 * @param p text output stream where the list will be saved.
 */

public void saveRelationList(PrintWriter p) {

  p.print("//Network Relationships: \n\n");

  for (int i=0 ; i<getRelationList().size() ; i++) {
    ((Relation)getRelationList().elementAt(i)).save(p);}
}


/**
 * Saves all the variables of the network using the text output
 * stream given as parameter. The network is saved with the Elvira
 * format.
 * @param p <code>PrintWriter</code> where the network is saved.
 * @see saveNodeList
 * @see saveLinkList
 * @see saveRelationList
 */

public void save(PrintWriter p) throws IOException {

  saveHead(p);

  p.print("// Network Properties\n\n");

  p.print("kindofgraph = \""+getKindOfGraphAsString()+"\";\n");

  if (!getTitle().equals(""))
    p.print("title = \""+ getTitle()+"\";\n");

  if (!getAuthor().equals(""))
    p.print("author = \""+ getAuthor()+"\";\n");

  if (!getWhoChanged().equals(""))
    p.print("whochanged = \""+ getWhoChanged()+"\";\n");

  if (!getWhenChanged().equals(""))
    p.print("whenchanged = \""+ getWhenChanged()+"\";\n");

  if (!getComment().equals(""))
    p.print("comment = \""+ getComment()+"\";\n");

  if (getLocked())
    p.print("locked = true;\n");

  p.print("visualprecision = \""+ getVisualPrecision()+"\";\n");

  p.print ("version = "   +getVersion() +";\n");
  p.print ("default node states = (");

  for (int i=0 ; i<FSDefaultStates.size()-1 ; i++)
    p.print(FSDefaultStates.elementAt(i)+" , ");

  p.print(FSDefaultStates.lastElement()+");\n\n");

  String key;
  for(Enumeration pKeys=networkPropertyList.keys();pKeys.hasMoreElements();){
    key=(String)pKeys.nextElement();
    p.print(key + "=" + networkPropertyList.get(key) + ";\n");
  }
  saveNodeList(p);
  saveLinkList(p);
  saveRelationList(p);
  p.print("}\n");
}


/**
 * Saves the header of the network.
 * @param p <code>PrintWriter</code> where the network is saved.
 */

public void saveHead(PrintWriter p) throws IOException {

  p.print("// Network\n");
  p.print("// Elvira format \n\n");
  p.print("network  \""+getName()+"\" { \n\n");
}


/**
 * Reads a network from a file.
 * @param nameOfFile the name of the file.
 * @return the read network.
 */

public static Network read(String nameOfFile) throws elvira.parser.ParseException ,IOException {

  FileInputStream f;
  Network network = null;
  f = new FileInputStream(nameOfFile);
  
     BayesNetParse parser = new BayesNetParse(f);
     parser.initialize();    
     parser.CompilationUnit();
     if (parser.Type.equals("network"))
       network = new Network();
     else if(parser.Type.equals("bnet"))
       network = new Bnet();
     else if (parser.Type.equals("iDiagram"))
       network = new IDiagram();
     else if (parser.Type.equals("iDWithSVNodes"))
       network = new IDWithSVNodes();
     else if (parser.Type.equals("dan"))
               network = new Dan();
     else if (parser.Type.equals("uid"))
               network = new UID();
     else if (parser.Type.equals("graph"))
       network = new Network();
     else {
       System.out.println("Error in Network.read(String nameOfFile): Type of"+
                          " network \""+parser.Type+"\" not reconigzed");
       System.exit(1);
     }
     network.translate(parser);
  
  f.close();
  return network;
}

/**
 * Reads a network from a Xbif file.
 * @param nameOfFile the name of the file.
 * @return the read network.
 */

public static Network readXbif(String nameOfFile) throws elvira.translator.xbif2elv.ParseException, IOException {
   
  FileInputStream f;
  Network network = null;

  f = new FileInputStream(nameOfFile);
  
  XBif2ElvParse parser = new XBif2ElvParse(f);
  parser.initialize();

  parser.CompilationUnit();
 
  parser.saveEvidence(nameOfFile);
  network=new Bnet();     
  network.translate(parser);
    
  f.close();  
  return network;
}

public static Network read(String nameOfFile, boolean quantitative) throws elvira.parser.ParseException ,IOException {

    
  FileInputStream f;
  Network network = null;
  f = new FileInputStream(nameOfFile);
  
  BayesNetParse parser = new BayesNetParse(f);
  parser.initialize();

  parser.CompilationUnit();
  if (parser.Type.equals("network"))
    network = new Network();
  else if(parser.Type.equals("bnet"))
    network = new Bnet();
  else if (parser.Type.equals("iDiagram"))
    network = new IDiagram();
  else if (parser.Type.equals("iDWithSVNodes"))
    network = new IDWithSVNodes();
  else if (parser.Type.equals("dan"))
	    network = new Dan();
  else {
    System.out.println("Error in Network.read(String nameOfFile): Type of"+
		       " network \""+parser.Type+"\" not reconigzed");
    System.exit(1);
  }
  network.translate(parser,quantitative);
  f.close();
  return network;
}


/* ******************* Other methods ********************** */

/**
 * Gets the value for all the instance variables of the network
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(BayesNetParse parser) {

  //call the graph.translate methos that gets the name, the nodes and the links of the Network
  super.translate(parser);

  //get the network attributes
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setVisualPrecision(parser.VisualPrecision);
  setVersion(new Float(parser.version));
  setPropertyList(parser.networkPropertyList);


  //Gets the relations form the parser
  setRelationList(parser.Relations);
  setFSDefaultStates(parser.DefaultFinite.getStates());
  RelationList relList=new RelationList();
  relList.setRelations(getRelationList());
  relList.repairPotFunctions();
  setRelationList(relList.getRelations());
}

/**
 * Gets the value for all the instance variables of the network
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 * @param quantitative says if relations must be stored
 */

public void translate(BayesNetParse parser,boolean quantitative) {

  //call the graph.translate methos that gets the name, the nodes and the links of the Network
  super.translate(parser);

  //get the network attributes
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setVersion(new Float(parser.version));
  setPropertyList(parser.networkPropertyList);

  /*
   * This is done if quantitative information is required
   */
    setFSDefaultStates(parser.DefaultFinite.getStates());

  /*
   * This is required only for quantitative information
   */
  if (quantitative == true) {
    RelationList relList=new RelationList();
    relList.setRelations(parser.Relations);
    relList.repairPotFunctions();
    setRelationList(relList.getRelations());
  }
}

/**
 * Gets the value for all the instance variables of the network
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(Hugin2ElvParse parser) {

  setName(parser.Name);
  setTitle(parser.Title);
  setPropertyList(parser.networkPropertyList);


  try{
    nodeList = parser.Nodes;

    /**
     * The parser contains the list of links and the list of nodes but not
     * the list of links of each node that acts as parent, child or
     * sibling. Therefore the graph that represents the network with all
     * this information must be built now. It is done creating a new link for
     * each one of the list of links in the parser and inserting it
     * in the graph calling to the method createLink defined in class Graph.
    */
    Link link;
    Enumeration links = parser.Links.elements();
    while (links.hasMoreElements()) {
      link = (Link) links.nextElement();
      createLink(link.getTail(), link.getHead(), link.getDirected());
    }
    setRelationList (parser.Relations);
    setFSDefaultStates (parser.DefaultFinite.getStates());
  }
  catch (InvalidEditException iee){
    System.out.println("The bnet can't be translated");
  }
}

/**
 * Gives a valid name to a variable. The names created by this function
 * are like a..z, a1..z1, a2..z2, etc.
 * The name is generated using the given parameter that is
 * assigned to the variable when it is created.
 * @param variableNumber contains the number of the variable whose name
 * is going to be generated.
 * @return the new name of the variable.
 */

public String generateName(int variableNumber) {

  Node node;

  // generate names of the form a..z, a1..z1, a2..z2, etc.
  char namec = (char) ((int) 'A' + variableNumber % 26);
  int suffix = variableNumber / 26;
  String name;

  if (suffix > 0)
    name = new String("" + namec + suffix);
  else
    name = new String("" + namec);

  // check whether there is a variable with this name
  for (Enumeration e = getNodeList().elements() ; e.hasMoreElements() ; ) {
    node = (Node)(e.nextElement());
    if (node.getName().equalsIgnoreCase(name))
      return (generateName(variableNumber+1));
  }

  System.out.println(name);
  return name;
}

/**
 * Gets the value for all the instance variables of the network
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(XBif2ElvParse parser) {
  setName(parser.Name);
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setVersion(new Float(parser.version));

  try{
    nodeList = parser.Nodes;
    /**
     * The parser contains the list of links and the list of nodes but not
     * the list of links of each node that acts as parent, child or
     * sibling. Therefore the graph that represents the network with all
     * this information must be built now. It is done creating a new link for
     * each one of the list of links in the parser and inserting it
     * in the graph calling to the method createLink defined in class Graph.
    */
    Link link;
    Enumeration links = parser.Links.elements();
    while (links.hasMoreElements()) {
      link = (Link) links.nextElement();
      createLink(link.getTail(), link.getHead(), link.getDirected());
    }
    setRelationList (parser.Relations);
    setFSDefaultStates (parser.DefaultFinite.getStates());
  }
  catch (InvalidEditException iee){
    System.out.println("The bnet can't be translated");
  }
}


/**
 * Gets the value for all the instance variables of the network
 * from the parser.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(Bif2ElvParse parser) {
  setName(parser.Name);
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setVersion(new Float(parser.version));

  try{
    nodeList = parser.Nodes;
    /**
     * The parser contains the list of links and the list of nodes but not
     * the list of links of each node that acts as parent, child or
     * sibling. Therefore the graph that represents the network with all
     * this information must be built now. It is done creating a new link for
     * each one of the list of links in the parser and inserting it
     * in the graph calling to the method createLink defined in class Graph.
    */
    Link link;
    Enumeration links = parser.Links.elements();
    while (links.hasMoreElements()) {
      link = (Link) links.nextElement();
      createLink(link.getTail(), link.getHead(), link.getDirected());
    }
    setRelationList (parser.Relations);
    setFSDefaultStates (parser.DefaultFinite.getStates());
  }
  catch (InvalidEditException iee){
    System.out.println("The bnet can't be translated");
  }
}

/**
 * Gets the value for all the instance variables of the network
 * from the generic translator.
 * @param parser contains all the information about the
 * network (read from a file).
 */

public void translate(ToElvParse parser) {
  setName(parser.Name);
  setTitle(parser.Title);
  setComment(parser.Comment);
  setAuthor(parser.Author);
  setWhoChanged(parser.WhoChanged);
  setWhenChanged(parser.WhenChanged);
  setVersion(new Float(parser.version));

  try{
    nodeList = parser.Nodes;
    /**
     * The parser contains the list of links and the list of nodes but not
     * the list of links of each node that acts as parent, child or
     * sibling. Therefore the graph that represents the network with all
     * this information must be built now. It is done creating a new link for
     * each one of the list of links in the parser and inserting it
     * in the graph calling to the method createLink defined in class Graph.
    */
    Link link;
    Enumeration links = parser.Links.elements();
    while (links.hasMoreElements()) {
      link = (Link) links.nextElement();
      createLink(link.getTail(), link.getHead(), link.getDirected());
    }
    setRelationList (parser.Relations);
    setFSDefaultStates (parser.DefaultFinite.getStates());
  }
  catch (InvalidEditException iee){
    System.out.println("The bnet can't be translated");
  }
}

/**
 * Generates node names with a special prefix. Normally,
 * it can be used for generating utility nodes names -in this
 * case the prefix can be u- or for generating decision names
 * -with the prefix d for example-.
 * @param prefix the prefix.
 * @param variableNumber contains the number of the variable whose name
 * is going to be generated.
 * @return the generated name.
 */

public String generateSpecialName(String prefix, int variableNumber) {

  Node node;
  String name = new String(prefix);
  int suffix = variableNumber - 1;

  if (suffix >0)
    name = name + suffix;

  if (checkName(name) == null)
    return (generateSpecialName(prefix,variableNumber+1));

  System.out.println(name);
  return name;
}


/**
 * Gets the position of a node in the nodelist using the name
 * of the node. This method doesn't distinguish between upper and
 * lower case.
 * @param name the name of the node to search.
 * @return the position of the node. -1 if it is not found.
 */

public int getNodePosition (String name) {

  int position;

  for (position=0 ; position<getNodeList().size() ; position++)
    if (name.equalsIgnoreCase( ((Node) getNodeList().elementAt(position)).getName()))
      return position;

  return (-1);
}


/**
 * Gets the node with the name given as parameter.
 * @param name the name of a node.
 * @see getNodePosition
 * @return the node with the name given or error if this name
 *         can't be found in the list of nodes.
 */

public Node getNode(String name) {

  return ((Node) getNodeList().elementAt(getNodePosition(name)));
}


/**
 * Obtains a link given the names of the nodes given as parameter.
 * This method doesn't distinguish between upper and lower case.
 * @param nameNode1 the name of the first node.
 * @param nameNode2 the name of the second node.
 * @return a link from <code>nameNode1</code> to <code>nameNode2</code> or an
 * undirected link between <code>nameNode1</code> and <code>nameNode2</code>.
*/

public Link getLink (String nameNode1, String nameNode2) {

  Node node1 = getNode (nameNode1);
  Node node2 = getNode (nameNode2);
  return getLink(node1, node2);
}


/**
 * Creates a new node in the network. This node will be set in
 * the position (x,y).
 * @param x position in the x axis.
 * @param y position in the y axis.
 */

public void createNode(int x, int y) {

  Node node;

  try {
    String n = generateName(getNodeList().size());
    if (defaultChanceNode==Node.FINITE_STATES)
        node = new FiniteStates(n,x,y,getFSDefaultStates(Node.CHANCE));
    else
        node = new Continuous(n,x,y,null);
    addNode(node);
    addRelation(node);
  }
  catch (InvalidEditException iee) {
    System.out.println("The node can't be created");
  }
}


/**
 * Creates a new node in the network. This node will be set in
 * the position (x,y) with the specified font.
 * @param x position in the x axis.
 * @param y position in the y axis.
 * @param fm the font.
 * @param name the name of the node.
 * @param kind the kind of node.
 */

public void createNode(int x, int y, String fm, String name, int kind) {

  Node node;

  try {
    if ((kind == Node.UTILITY)||(kind==Node.SUPER_VALUE))
      node = new Continuous (name,x,y,fm);
    else if (defaultChanceNode==Node.FINITE_STATES)
      node = new FiniteStates(name,x,y,getFSDefaultStates(kind),fm);
    else
      node = new Continuous(name,x,y,fm);

    node.setKindOfNode(kind);
    addNode(node);
    if(node.getKindOfNode()!=Node.DECISION)
      addRelation(node);
  }
  catch (InvalidEditException iee) {
    System.out.println("The node can't be created");
  }
}


/**
 * Dettach a node from the network (without destroying it).
 * @param node the node to remove.
 */

public void removeNode (Node node) {

  Enumeration e;
  Node parent, child;
  int i;
  NodeList children = children(node);


  // First remove the node itself
  try{
    super.removeNode(node); /* Seems to be correct at the end */

    if (hasRelation(node) == true) {
      // Removes the relations of the delete node
      removeRelation(node);

      // Remove the relations of all the node's children

      for (e=children.elements() ; e.hasMoreElements() ; ) {
        child = (Node) e.nextElement();
        removeRelation(child);
      }

      // Add the new relations among the children of the node to remove
      for (e=children.elements() ; e.hasMoreElements() ; ) {
        child = (Node) e.nextElement();
        if (child.getKindOfNode()!=Node.DECISION){
        	addRelation(child);
        }
      }
    }

    //super.removeNode(node);
  }
  catch (InvalidEditException iee){}
}


/**
 * Creates a new link between head and tail in the network.
 * @param tail first node in the new link.
 * @param head second node in the new link.
 */

public void createLink(Node tail, Node head) throws InvalidEditException {

  Vector V = new Vector();
  NodeList parents;
  Node node;
  int i;
  
  // Create a new link
  super.createLink(tail,head);

 if (this.getClass()!=IDWithSVNodes.class){
 
  if(head.getKindOfNode()!=Node.DECISION){
    removeRelation(head);
  // Now create the new relation and add it
    addRelation(head);
  }
 }
}


/**
 * Deletes the link (head,tail) of the network, removing the relation
 * associated with it. This method creates the new relation that appears
 * when the link is removed.
 * @param tail tail node of the arc to remove.
 * @param head head node of the arc to remove.
 */

public void removeLink (Node tail, Node head) throws InvalidEditException {

  Enumeration e;
  Node parent, child;
  int p;

  // Remove the link
  p = getLinkList().getID(tail.getName(),head.getName());
  removeLink(p);

  // now take the old relation from head
  // first remove the old one
  removeRelation(head);

  // now add the new relation
  addRelation(head);
}


/**
 * Deletes the given link from this network, without making
 * anything with the relations. It just calls method
 * <code>removeLink</code> of <code>Graph</code>.
 * @param link link to be removed.
 */

public void removeLinkOnly (Link link) {

  // Remove the link

  try{
    super.removeLink(link);
  }catch (InvalidEditException iee){;}
}


/**
 * Dettaches a node from the network (without destroying it).
 * It deletes the node without deleting the relations of <CODE>node</CODE>.
 * @param node node to remove.
 */

public void removeNodeOnly (Node node) {

  removeRelation(node);
  try{
    super.removeNode(node);
  } catch (InvalidEditException iee) {;}
}


/**
 * Gets the nodes in the network as an <code>Enumeration</code> object.
 * @return the list of nodes as an <code>Enumeration</code> object.
 */

public Enumeration enumerateNodes() {

  return(getNodeList().elements());
}


/**
 * Determines whether or not a name is valid and/or repeated.
 * @param name contains the name to check.
 * @return the name checked if it is valid or null in other case.
 */

public String checkName(String name) {

  Node node;
  String newName = validateValue(name);

  for (Enumeration e = getNodeList().elements() ; e.hasMoreElements() ; ) {
    node = (Node)(e.nextElement());
    if (node.getName().equals(newName))
      return null;
  }
  return newName;
}


/**
 * Gets a valid name from the string given as parameter.
 * This method changes blanks by '_'.
 * @param value contains the string to check.
 * @return the string checked and corrected.
 */

public String validateValue(String value) {

  StringBuffer str = new StringBuffer(value);

  for (int i=0 ; i < str.length() ; i++) {
    if (str.charAt(i) == ' ')
      str.setCharAt(i, '_');
  }

  return str.toString();
}


/**
 * Changes the values of a variable. Note that, if the number
 * of new values is different from the number of current values,
 * this operation resets the probability values of the variable
 * and all its children.
 * @param node node whose states are going to be set.
 * @param values contains the new states of the node.
 */

public void changeValues(FiniteStates node, String values[]) {

  Node cnode;
  NodeList children;
  Enumeration e;

  if (node.getNumStates() == values.length) {
    node.setStates(values);
    return;
  }

  node.setStates(values);

  /* Now all the probabilty distributions must be reset
     for the actual node and for all its children */

  // For current node

  removeRelation(node);
  addRelation(node);

  // For all its children

  children = children (node);

  for (e=children.elements() ; e.hasMoreElements() ; ) {
    cnode = (Node) e.nextElement();
    removeRelation(cnode);
    addRelation(cnode);
  }
}


/**
 * Removes from the list of relations the relation that contains the node
 * given as parameter.
 * @param node node whose relation is going to be removed.
 */

public void removeRelation(Node node) {

  int i;
  Relation r;

  for (i=0 ; i<getRelationList().size() ; i++) {
    r = (Relation) getRelationList().elementAt(i);

    if ((((Node)r.getVariables().elementAt(0)).getName()).equals(node.getName())) {
      if (r.getValues() != null && r.getValues().getClass() == CanonicalPotential.class) {
	for (int j=0; j<((CanonicalPotential) r.getValues()).getArguments().size(); j++)
	{
	  String relName = ((CanonicalPotential) r.getValues()).getStrArgument(j);
	  for (int k=0; k<getRelationList().size(); k++) {
	    if (((Relation) getRelationList().elementAt(k)).getName().equals(relName)) {
	      getRelationList().removeElementAt(k);
	    }
	  }
	}
      }

      getRelationList().removeElementAt(i);
    }
  }
}

/**
 * To check if there is any relation for a given node
 * @param node
 * @return boolean
 */

private boolean hasRelation(Node node) {
  int i;
  Relation r;

  for (i=0 ; i<getRelationList().size() ; i++) {
    r = (Relation) getRelationList().elementAt(i);

    if ((((Node)r.getVariables().elementAt(0)).getName()).equals(node.getName())) {
      return(true);
    }
  }

  // No relations for this node

  return(false);
}


/**
 * Creates a relation for the given node and adds it to the list of
 * relations in the network. The new relation will contain a
 * probability table depending on its parents in the graph.
 * @param node the node for which the relation will be created.
 */

public void addRelation(Node node) {

  Vector v = new Vector();
  NodeList parents;
  int i;
  Relation r;

  v.addElement(node);
  parents = parents(node);
  if (parents != null) {
    for (i=0 ; i<parents.size() ; i++)
      v.addElement((Node) parents.elementAt(i));
  }

  r = new Relation(v);
  Relation newRel = r.copy();
  
  if ((parents != null) && (parents.size() > 0) && (node.getKindOfNode() == Node.UTILITY))
  {
		newRel.setVariables(r.getVariables());
		NodeList copyVarsRel = newRel.getVariables().copy();
		copyVarsRel.removeNode(node);
		//PotentialTable pot = new PotentialTable(newRel.getVariables());
		PotentialTable pot = new PotentialTable(copyVarsRel);
		r.setValues(pot);
		r.setKind(Relation.UTILITY);
  }
  else if(node.getKindOfNode()==Node.CHANCE){
    r.setKind(Relation.CONDITIONAL_PROB);
  }

    Enumeration E = parents.elements();
    boolean isContinuo=false;
    for (;E.hasMoreElements();){
               Object next=E.nextElement();
               if ((next.getClass()==Continuous.class)
               &&(((Node)next).getKindOfNode()!=Node.UTILITY)&&(((Node)next).getKindOfNode()!=Node.SUPER_VALUE))
                            isContinuo=true;
    }

    if (isContinuo){

        PotentialContinuousPT pot =new PotentialContinuousPT();
        r.setValues(pot);
        r.setKind(Relation.POTENTIAL);
        //System.out.println("Link Continuous Created");

    }

  getRelationList().addElement(r);
}


/**
 * Adds a relation to the list of relations.
 * @param a relation to add.
 */

public void addRelation(Relation r) {

  getRelationList().addElement(r);
}


/**
 * Gives the relation whose first variable is the same that
 * the given node.
 * @param nodenNode to find.
 * @return the relation found.
 */

public Relation getRelation(Node node) {

  int i;
  Relation r;
  for (i=0 ; i<getRelationList().size() ; i++) {
    r = (Relation) getRelationList().elementAt(i);
    if (node.getKindOfNode() == Node.CHANCE) {
      if (((((Node)r.getVariables().elementAt(0)).getName()).equals(node.getName())) &&
          (r.getActive()))
	return r;
    }
    else
      for (int j=0 ; j<r.getVariables().size() ; j++) {
      if (((((Node)r.getVariables().elementAt(j)).getName()).equals(node.getName())) &&
          (r.getActive()))
	return r;
    }
  }
  return null;
}


/**
 * Gets a copy of the relations present in this network.
 * @return a copy of the relations in this network.
 */

public RelationList getInitialRelations() {
  Relation rNew;
  RelationList ir;
  int i;

  ir = new RelationList();
  for (i=0 ; i<relationList.size() ; i++) {
    rNew = (Relation)((Relation)(relationList.elementAt(i))).copy();
    ir.insertRelation(rNew);
  }
  return ir;
}

/**
 *Set the default node chance.
 * @param i a int value representing the new kind.
 */

public void setDefaultChanceNode(int i){

    defaultChanceNode=i;
}

/**
 *get the default node chance.
 */

public int getDefaultChanceNode(){

    return defaultChanceNode;
}


/**
 * Get all the nodes of a kind
 * @param kind the kind of node (Node.CHANCE, Node.DECISION,...)
 */
public NodeList getNodesOfKind(int kind) {
	int i;
	NodeList nl;
	Node n;
	NodeList nodes;

	nl = getNodeList();
	nodes = new NodeList();

	for (i = 0; i < nl.size(); i++) {

		n = nl.elementAt(i);
		if (n.getKindOfNode() == kind) {
			nodes.insertNode(n);
		}
	}

	return nodes;

}


/**
 * Get all the nodes of a kind in a list of kinds of nodes specified by 'kinds'
 * @param kind the kind of node (Node.CHANCE, Node.DECISION,...)
 */
public NodeList getNodesOfKind(Integer...kinds) {
	int i;
	NodeList nl;
	Node n;
	NodeList nodes;
	boolean isOfKind=false;

	nl = getNodeList();
	nodes = new NodeList();

	for (i = 0; i < nl.size(); i++) {

		n = nl.elementAt(i);
		for (int indexKind=0;(indexKind<kinds.length)&&!isOfKind;indexKind++){
			Integer kind = kinds[indexKind];
			isOfKind = (n.getKindOfNode() == kind);
			
		}
		if (isOfKind){
			nodes.insertNode(n);
		}
		
	}

	return nodes;

}

/**
 * Get the number of nodes of a kind
 * @param kind the kind of node (Node.CHANCE, Node.DECISION,...)
 */
public int getNumNodesOfKind(int kind) {
	
	return (getNodesOfKind(kind).size());
}

}  // End of class
