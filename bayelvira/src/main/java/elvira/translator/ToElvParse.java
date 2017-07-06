package elvira.translator;

import java.util.Vector;


import elvira.FiniteStates;
import elvira.Continuous;
import elvira.NodeList;
import elvira.Link;
import elvira.LinkList;
import elvira.Bnet;
import elvira.IDiagram;
import elvira.Relation;
import elvira.potential.PotentialTable;

/** 
 * This class is the superclass of the translators to Elvira format
 */

public abstract class ToElvParse{
  public FiniteStates DefaultFinite;
  public Continuous DefaultContinuous;
  public Relation DefaultRelation;
  public NodeList Nodes;
  public LinkList Links;
  public Vector Relations;    
  public String Name;
  public String Title;
  public String Comment;
  public String Author;
  public String WhoChanged;
  public String WhenChanged;
  public String version;

  public void initialize() {
    String DefaultStates[]= {"absent","present"};
    DefaultFinite = new FiniteStates("Default",DefaultStates);
    DefaultFinite.setTitle("");
    DefaultFinite.setComment("");
    DefaultContinuous = new Continuous();
    DefaultContinuous.setTitle("");
    DefaultContinuous.setComment("");
    Name =  new String("");
    Title =  new String("");
    Comment =  new String("");
    Author =  new String("");
    WhoChanged =  new String("");
    WhenChanged =  new String("");
    Nodes = new NodeList();
    Links = new LinkList();
    Relations = new Vector();
    DefaultRelation = new Relation();
    DefaultRelation.setKind(Relation.CONDITIONAL_PROB);
    

    version =  new String("1.0");
    //networkPropertyList=new Hashtable();
    //Consistency = new Vector();

    //DefaultLink = new Link(true);
    //defaultnodetype=1;
  }
}