/*  ConstraintKnowledge.java */
package elvira.learning.constraints;

import elvira.Graph;
import elvira.Network;
import elvira.Bnet;

import elvira.LinkList;
import elvira.Link;
import elvira.NodeList;
import elvira.Node;

import elvira.InvalidEditException;
import elvira.parser.ParseException;
import elvira.learning.Metrics;

import java.util.Vector;
import java.util.Random;
import java.util.Enumeration;
import java.io.*;


/**
 * In this class we store the constraints knowledge in three graph classes. Each
 * graph class have the constraints for one type, the constraints types supported
 * are: existence of  arc and/or edges constraints, absence of arc and/or edges
 * constraints and partial order constraints.
 * @author J.G. Castellano (fjgc@decsai.ugr.es)
 * @author Luis M. de Campos (lci@decsai.ugr.es)
 * @author Jose M. Puerta (jpuerta@info-ab.uclm.es)
 * @since 07/01/2003
 */
public class ConstraintKnowledge {

    /* These constants are used to set the constraint type */

    /**
     * The constraint is a existence constraint
     */
    public static final int EXISTENCE= 0;

    /**
     * The constraint is a absence constraint
     */
    public static final int ABSENCE = 1;

    /**
     * The constraint is a partial order constraint
     */
    public static final int PARTIALORDER = 2;

    /**
     * Graph where we store existence of arc/edges constraints
     */
    Graph existence;

    /**
     * Graph where we store the real existence constraints. Union of existence, absence and partial order constraints
     */
    Graph realexistence;

    /**
     * Graph where we store absence of arc/edges constraints
     */
    Graph absence;

    /**
     * Graph where we store partial order constraints
     */
    Graph order;

    /**
     * Graph where we store the real partial order constraints. The order contraints plus the result 
     * of apply the transitive property
     */
    Graph realpartialorder;


    /**
     * If this boolean variable is enabled, we show warnings.
     */
    public boolean warnings;


    /*---------------------------------------------------------------*/
    /**
    *  Basic constructor with the problem where apply the constraints
    *  @param problem bayesian network with the problem
    */
    public ConstraintKnowledge(Bnet problem) {
	this.realexistence=null;
	this.realpartialorder=null;
	this.existence= new Graph(problem.getNodeList(), new LinkList(), Graph.MIXED);
	this.absence=new Graph(problem.getNodeList(), new LinkList(), Graph.MIXED);
	this.order=new Graph(problem.getNodeList(), new LinkList(), Graph.MIXED);
	this.warnings=true;
    }//end ctor. from bnet

    /*---------------------------------------------------------------*/
    /**
    *  Basic constructor from a List of Node of the problem where apply
    *  the constraints
    *  @param nodeList List of nodes of the problem
    */
    public ConstraintKnowledge(NodeList nodelist) {
	this.realexistence=null;
	this.realpartialorder=null;
	this.existence= new Graph(nodelist.duplicate(), new LinkList(), Graph.MIXED);
	this.absence=new Graph(nodelist.duplicate(), new LinkList(), Graph.MIXED);
	this.order=new Graph(nodelist.duplicate(), new LinkList(), Graph.MIXED);
	this.warnings=true;
    }//end ctor. from NodeList

    /*---------------------------------------------------------------*/
    /**
    *  Constructor from actual Graphs with constraints
    *  @param e graph with existence constraints
    *  @param a graph with absence constraints
    *  @param o graph with partial order constraints
    */
    public ConstraintKnowledge(Graph e, Graph a, Graph o) throws InvalidEditException {

	int i;
	NodeList enl=e.getNodeList().duplicate();
	NodeList anl=a.getNodeList().duplicate();
	NodeList onl=o.getNodeList().duplicate();
	this.warnings=true;

	//check if the nodes belong to the same problem
        if ( !sameProblem(e,a,o)) {
          if (warnings) System.out.println("ERROR:The constraints are for differents problems");
	  throw new SecurityException("The constraints are for differents problems");
        }

	//Build the Constraints
	this.realexistence=null;
	this.realpartialorder=null;
	this.existence= new Graph(enl, new LinkList(), Graph.MIXED);
	this.absence=new Graph(anl, new LinkList(), Graph.MIXED);
	this.order=new Graph(onl, new LinkList(), Graph.MIXED);


	//Add only consistent constraints
	LinkList ell=e.getLinkList();
	for (i=0;i < ell.size();i++) {
				  Node tail=enl.getNode(ell.elementAt(i).getTail().getName());
				  Node head=enl.getNode(ell.elementAt(i).getHead().getName());					
					Link link=new Link(tail, head,((Link)ell.elementAt(i)).getDirected() );
          if (!addConstraint(EXISTENCE, link) )
            if (warnings) System.out.println("WARNING: The Existence constraint:"+ell.elementAt(i).toString()+" cant't be added.It isn't consistent.");
	}

	LinkList all=a.getLinkList();
	for (i=0;i < all.size();i++) {
				  Node tail=anl.getNode(all.elementAt(i).getTail().getName());
				  Node head=anl.getNode(all.elementAt(i).getHead().getName());					
					Link link=new Link(tail, head,((Link)all.elementAt(i)).getDirected() );
          if (!addConstraint(ABSENCE, link))
            if (warnings) System.out.println("WARNING: The Absence constraint:"+all.elementAt(i).toString()+" cant't be added.It isn't consistent.");
	}

	LinkList oll=o.getLinkList();
	for (i=0;i < oll.size();i++) {
				  Node tail=onl.getNode(oll.elementAt(i).getTail().getName());
				  Node head=onl.getNode(oll.elementAt(i).getHead().getName());					
					Link link=new Link(tail, head,((Link)oll.elementAt(i)).getDirected() );
          if (!addConstraint(PARTIALORDER, link))
            if (warnings) System.out.println("WARNING: The Partial Order constraint:"+oll.elementAt(i).toString()+" cant't be added.It isn't consistent.");
	}
    }//end graph ctor.
    /*---------------------------------------------------------------*/
    /**
    *  Constructor that read the constraints form files
    *  @param nameOfFileExistenceConstraints the name of the file with existence constraints
    *  @param nameOfFileAbsenceConstraints the name of the file with absence constraints
    *  @param nameOfFilePartialOrderConstraints the name of the file with partial order constraints
    */
    public ConstraintKnowledge(String nameOfFileExistenceConstraints, String nameOfFileAbsenceConstraints, String nameOfFilePartialOrderConstraints) throws elvira.parser.ParseException ,IOException, InvalidEditException {

        int i;

	//Read the constraints
	Graph ne=Graph.readGraph(nameOfFileExistenceConstraints);
	Graph na=Graph.readGraph(nameOfFileAbsenceConstraints);
	Graph no=Graph.readGraph(nameOfFilePartialOrderConstraints);

	//check if the nodes belong to the same problem
        if ( !sameProblem(ne,na,no)) {
          if (warnings) System.out.println("ERROR:The constraints are for differents problems");
	  throw new SecurityException("The constraints are for differents problems");
        }

	//Build the Constraints
	this.realexistence=null;
	this.realpartialorder=null;
	this.existence= new Graph(ne.getNodeList(), new LinkList(), Graph.MIXED);
	this.absence=new Graph(na.getNodeList(), new LinkList(), Graph.MIXED);
	this.order=new Graph(no.getNodeList(), new LinkList(), Graph.MIXED);

	//Add only consistent constraints
	LinkList ell=ne.getLinkList();
	for (i=0;i < ell.size();i++)
	    addConstraint(EXISTENCE, (Link)ell.elementAt(i));

	LinkList all=na.getLinkList();
	for (i=0;i < all.size();i++)
	    addConstraint(ABSENCE, (Link)all.elementAt(i));

	LinkList oll=no.getLinkList();
	for (i=0;i < oll.size();i++)
	    addConstraint(PARTIALORDER, (Link)oll.elementAt(i));

	this.warnings=true;
    }//end file ctor.
    /*---------------------------------------------------------------*/
    /**
     *  Method that add existence constraints from a file
     *  @param nameOfFileExistenceConstraints the name of the file with existence constraints
     *  @return -1 if the file belong a different problem than the actual constraints, in the other case
     *             the number of constraints add correctly
     */
     public int loadExistenceConstraints(String nameOfFileExistenceConstraints) throws elvira.parser.ParseException ,IOException, InvalidEditException {

       int i,added=0;

       //Read the constraints
       Graph ne=Graph.readGraph(nameOfFileExistenceConstraints);

       //check if the nodes belong to the same problem
       if ( !sameProblem(ne,this.absence,this.order))
         return -1;

       //Add only consistent constraints
       LinkList ell=ne.getLinkList();
       for (i=0;i < ell.size();i++)
         if (addConstraint(EXISTENCE, (Link)ell.elementAt(i)))
           added++;

       //return the added constraints
       return added;
    }//end loadExistenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     *  Method that add absence constraints from a file
     *  @param nameOfFileAbsenceConstraints the name of the file with absence constraints
     *  @return -1 if the file belong a different problem than the actual constraints, in the other case
     *             the number of constraints add correctly
     */
    public int loadAbsenceConstraints(String nameOfFileAbsenceConstraints) throws elvira.parser.ParseException ,IOException, InvalidEditException {

      int i,added=0;

      //Read the constraints
      Graph na=Graph.readGraph(nameOfFileAbsenceConstraints);

      //check if the nodes belong to the same problem
      if ( !sameProblem(this.existence,na,this.order))
        return -1;

      //Add only consistent constraints
      LinkList all=na.getLinkList();
      for (i=0;i < all.size();i++)
        if (addConstraint(ABSENCE, (Link)all.elementAt(i)))
          added++;

      //return the added constraints
      return added;
    }//end loadAbsenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     *  Method that add partial order constraints from a file
     *  @param nameOfFilePartialOrderConstraints the name of the file with partial order constraints
     *  @return -1 if the file belong a different problem than the actual constraints, in the other case
     *             the number of constraints add correctly
     */
    public int loadPartialOrderConstraints(String nameOfFilePartialOrderConstraints) throws elvira.parser.ParseException ,IOException, InvalidEditException {

      int i,added=0;

      //Read the constraints
      Graph no=Graph.readGraph(nameOfFilePartialOrderConstraints);

      //check if the nodes belong to the same problem
      if ( !sameProblem(this.existence,this.absence ,no))
        return -1;

      //Add only consistent constraints
      LinkList oll=no.getLinkList();
      for (i=0;i < oll.size();i++)
        if (addConstraint(PARTIALORDER, (Link)oll.elementAt(i)))
          added++;

      //return the added constraints
      return added;
    }//end loadPartialOrderConstraints method
    /*---------------------------------------------------------------*/
    /**
    *  Save the contraints in 3 differente files, one file for each
    *  constraint type
    *  @param fe file where store existence constraints
    *  @param fa file where store absence constraints
    *  @param fo file where store paratial order constraints
    */
    public void save(FileWriter fe,FileWriter fa,FileWriter fo) throws IOException {

	//save existence constrints
	PrintWriter pe = new PrintWriter(fe);
	this.existence.setName("ExistenceConstraints");
	this.existence.save(pe);

	//save absence constrints
	PrintWriter pa = new PrintWriter(fa);
	this.absence.setName("AbsenceConstraints");
	this.absence.save(pa);

	//save partial order constrints
	PrintWriter po = new PrintWriter(fo);
	this.order.setName("PartialOrderConstraints");
	this.order.save(po);

    }//end save method
    /*---------------------------------------------------------------*/
    /**
     *  Save the existence contraints in a file
     *  @param fe file where store existence constraints
     */
    public void saveExistenceConstraints(FileWriter fe) throws IOException {
      //save  constraints
      PrintWriter pe = new PrintWriter(fe);
      this.existence.save(pe);
    }//end saveExistenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     *  Save the absence contraints in a file
     *  @param fa file where store absence constraints
     */
    public void saveAbsenceConstraints(FileWriter fa) throws IOException {
      //save constraints
      PrintWriter pa = new PrintWriter(fa);
      this.absence.save(pa);
    }//end saveAbsenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     *  Save the partial order contraints in a file
     *  @param fo file where store partial order constraints
     */
    public void savePartialOrderConstraints(FileWriter fo) throws IOException {
      //save constraints
      PrintWriter po = new PrintWriter(fo);
      this.order.save(po);
    }//end savePartialOrderConstraints method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if the Graph with the different constraints types
    *  belongs to same problem or, that's the same, have equals NodeList
    *  @param e graph with existence constraints
    *  @param a graph with absence constraints
    *  @param o graph with partial order constraints
    *  @return true if the constraints are for the same problem, false in other case
    */
   public boolean sameProblem(Graph e, Graph a, Graph o)  {

     NodeList enl=e.getNodeList();
     NodeList anl=a.getNodeList();
     NodeList onl=o.getNodeList();

     //check if the nodes belong to the same problem
     if ( !enl.equals(anl) || !anl.equals(onl) )
	 return false;

     //if all the nodes are equal, the 3 graphs belong to the same problema
     return true;

   }//end sameProblem method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if the stored constraints and a Bnet
    *  belongs to same problem, so the bnet nodes must be included in the 
    *  constraints nodes.
    *  @param bnet bnet to test
    *  @return true if the constraints and the bnet are for the same problem, false in other case
    */
   public boolean sameProblem(Bnet bnet) {

     NodeList bnl=bnet.getNodeList();
     NodeList enl=this.existence.getNodeList();//the 3 constraints types belongs to the same problem (it was checked before)

     //check if the bnet nodes are constraints nodes
     for (int i=0 ; i<bnl.size() ; i++) {
	 if (enl.getId(bnl.elementAt(i)) == -1)
	     return false;
     }

     //if all the bnet nodes are in the constraints, the bnet and the constraints belong to the same problem
     return true;

   }//end sameProblem for bnet method
   /*---------------------------------------------------------------*/
    /**
    *  This method return a Graph with a copy of the existence constraints
    *  @return the existence constraints
    */
    public Graph getExistenceConstraints() {
      return new Graph(this.existence);
    }
    /*---------------------------------------------------------------*/
    /**
     *  This method return a Graph with a copy of the absence constraints
     *  @return the absence constraints
     */
    public Graph getAbsenceConstraints() {
      return new Graph(this.absence);
    }
    /*---------------------------------------------------------------*/
    /**
     *  This method return a Graph with a copy of the partial order constraints
     *  @return the partial order constraints
     */
    public Graph getPartialOrderConstraints() {
    return new Graph(this.order);
    }
    /*---------------------------------------------------------------*/
    /**
     *  This method return a Graph with a copy of the existence constraints, where
     *  the cases where x--y in Existence and x->y in Absence have replaced 
     *  with y<-x, and the cases where x--y in Existence and x->y in Partial Order have
     *  replaced with x->y
     *  @param e graph with the existence constraints
     *  @return the real existence constraints
     */
      protected Graph getRealExistenceConstraints(Graph e) {
        //use a acopy in memory if we have it
	if (this.realexistence!=null)
	    return this.realexistence.duplicate();


	int i;
	Node x,y;
	Link link,xy,yx;
	Graph existencereal=e.duplicate();

	//get the links
	LinkList links=(e.getLinkList()).copy();
	for (i=0;i<links.size();i++) {
	    link=links.elementAt(i);
	    //if existence link is x--y, look if exists x->y or y<-x in absence and path fom x->..->y or a path x<- .. <-y in partial order
	    if ( !link.getDirected() ) {

		x=existencereal.getNodeList().getNode(link.getTail().getName());//link.getTail();
		y=existencereal.getNodeList().getNode(link.getHead().getName());//link.getHead();
		xy= new Link(x,y,true); //x->y
		yx= new Link(y,x,true); //y->x

		//Test Ge and Ga
		if ( (this.absence.getLinkList()).indexOf(xy)!=-1) {
		    try {
			existencereal.removeLink(x,y);//remove x--y from Existence (exists x->y in Absence)
			existencereal.createLink(y,x, true); //add y->x to Existence
		    }catch (InvalidEditException iee) {}
		    
		} else if ( (this.absence.getLinkList()).indexOf(yx)!=-1) {
		    try {
			existencereal.removeLink(x,y); //remove x--y from Existence (exists y->x in Absence)
			existencereal.createLink(x,y, true); //add x->y to Existence
		    }catch (InvalidEditException iee) {}
		} else {
		    //Test Ge and Go
		    this.order.setVisitedAll(false);//
		    if (getPath(this.order.getNodeList().getNode(x.getName()),this.order.getNodeList().getNode(y.getName()), this.order, new LinkList())) {
			//else if ( (this.order.getLinkList()).indexOf(xy)!=-1) {
			try {
			    existencereal.removeLink(x,y); //remove x--y from Existence (exists x->..->y in Partial Order)
			    existencereal.createLink(x,y, true); //add x->y to Existence
			}catch (InvalidEditException iee) {}
		    } else {
			this.order.setVisitedAll(false);//
			if (getPath(this.order.getNodeList().getNode(y.getName()),this.order.getNodeList().getNode(x.getName()), this.order, new LinkList())) {
			    //} else if ( (this.order.getLinkList()).indexOf(yx)!=-1) {
			    try {
				existencereal.removeLink(x,y); //remove x--y from Existence (exists y->..->x in Partial Order)
				existencereal.createLink(y,x, true); //add y->x to Existence
			    }catch (InvalidEditException iee) {}
			} 
		    }//
		}//

	    }//end if
	}//end for i

	this.realexistence=existencereal.duplicate();
	return existencereal;

    }//end getRealExistenceConstraint method
    /*---------------------------------------------------------------*/
    /**
     *  This method return a Graph with a copy of the partial order constraints, where
     *  the cases where if we have x->y and y->z, we add x->z (the transitive property of 
     *  partial ortder constraints)
     *  @param e graph with the partial order constraints
     *  @return the real partial order constraints
     */
      protected Graph getRealPartialOrderConstraints(Graph o) {
        //use a acopy in memory if we have it
	if (this.realpartialorder!=null)
	    return this.realpartialorder.duplicate();

	int i,j;
	Node x,y;
	Link xy,yz;
	Graph ordenreal=o.duplicate();

	boolean added=true;
	while (added) {
	    added=false;
	    for (i=0;i<ordenreal.getLinkList().size();i++) {
		xy=ordenreal.getLinkList().elementAt(i);
		for (j=0;j<ordenreal.getLinkList().size();j++)
		    if (j!=i) {
			yz=ordenreal.getLinkList().elementAt(j);
			//if we have x->y & y->z, the we add x->z
			if ( xy.getHead().equals(yz.getTail()) ) {
			    //we add x->z if it's no exist
			    if ( ordenreal.getLinkList().getID(xy.getTail().getName(),yz.getHead().getName())==-1 ) {
				added=true;
				try {
				    ordenreal.createLink(xy.getTail(),yz.getHead(),true);
				}catch (InvalidEditException iee) {}
			    }
			}
		    }
	    }//end for i
	}//end while
	
	this.realpartialorder=ordenreal.duplicate();
	return ordenreal;

    }//end getRealPartialOrderConstraint method
    /*---------------------------------------------------------------*/
    /**
    *  Determines whether the stored constraints are empty or not.
    *  @return true if it has no constraints, false otherwise.
    */
    public boolean isEmpty() {
	if ( (this.existence.getLinkList().size()==0) &&
	     (this.absence.getLinkList().size()==0) &&
	     (this.order.getLinkList().size()==0) )
	    return true;
	return false;
    }//end isEmpty method

    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the existence
    *  constraints
    *  @param bnet bayesian network to test
    *  @return if the bayesian net satisfy the existence constraints (true) or not (false)
    */
    public boolean testExistenceConstraints(Bnet bnet) {
	return testExistenceConstraints((Graph)bnet);
    }//end testExitenceConstraints bnet method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the existence
    *  constraints
    *  @param graph graph to test
    *  @return if the graph satisfy the existence constraints (true) or not (false)
    */
    public boolean testExistenceConstraints(Graph graph) {
	int i,j;
	LinkList elinks=(this.existence.getLinkList()).copy();
	LinkList graphlinks=(graph.getLinkList()).copy();

	//The links in existence constraints must exist in the graph
	for (i=0;i<elinks.size();i++) {
	    boolean exists=false;
	    Link elink=elinks.elementAt(i);
	    if ( elink.getDirected() ) {
		//if the link is directed (x->y)  must exist x-->y
		for (j=0;j<graphlinks.size();j++)
		    if ( elink.equals(graphlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    } else {
		//if the link isn't directed (x--y) , must exist x-->y or x<--y
		Link one=new Link(elink.getTail(), elink.getHead(), true); //x->y
		Link two=new Link(elink.getHead(), elink.getTail(), true); //x<-y

		for (j=0;j<graphlinks.size();j++)
		    if ( one.equals(graphlinks.elementAt(j)) || two.equals(graphlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    }

	    //If the existence constraints isn't verfied by the graph, return false
	    if (warnings) if (!exists) System.out.println("WARNING: The graph/bnet doesn't verify the existence constraint:"+elink);//
	    if (!exists) return false;
	}//end for i

	//return that the grapht verify all the existence constraints
	return true;
    }//end testExistenceConstraints graph method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the existence
    *  constraints, returning the existence constraints that aren't satisfied.
    *  @param bnet bayesian network to test
    *  @param c the constraints that the bnet doesn't satisfy.
    *  @return if the bayesian net satisfy the existence constraints (true) or not (false)
    */
    public boolean testExistenceConstraints(Bnet bnet, LinkList c) {
	return testExistenceConstraints((Graph)bnet, c);
    }//end testExistenceConstraints Bnet with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the existence
    *  constraints, returning the existence constraints that aren't satisfied.
    *  @param bnet graph to test
    *  @param c the constraints that the graph doesn't satisfy.
    *  @return if the bayesian net satisfy the existence constraints (true) or not (false)
    */
    public boolean testExistenceConstraints(Graph bnet, LinkList c) {

	int i,j;
	LinkList elinks=(this.existence.getLinkList()).copy();
	LinkList bnetlinks=(bnet.getLinkList()).copy();
	if (c.size()!=0) c=new LinkList(); //the LinkList must be empty

	//The links in existence constraints must exist in bnet
	for (i=0;i<elinks.size();i++) {
	    boolean exists=false;
	    Link elink=elinks.elementAt(i);
	    if ( elink.getDirected() ) {
		//if the link is directed (x->y)  must exist x-->y
		for (j=0;j<bnetlinks.size();j++)
		    if ( elink.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    } else {
		//if the link isn't directed (x--y) , must exist x-->y or x<--y
		Link one=new Link(elink.getTail(), elink.getHead(), true); //x->y
		Link two=new Link(elink.getHead(), elink.getTail(), true); //x<-y

		for (j=0;j<bnetlinks.size();j++)
		    if ( one.equals(bnetlinks.elementAt(j)) || two.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    }

	    //If the existence constraints isn't verfied by the bnet, add it to the vector
	    if (!exists) {
		//get the existence constraints stored in existence constraints graph
		Link elink2=this.existence.getLink(elink.getTail(),elink.getHead());
		if (elink2==null) elink2=this.existence.getLink(elink.getHead(),elink.getTail());
		c.insertLink(elink2);
	    }
	}//end for i

	//return if the bnet verify the existence constraints
	if (c.size()==0) return true;
	else return false;
    }//end testExistenceConstraints graph with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the existence
    *  constraints, returning the real existence constraints that aren't satisfied. The
    *  real existence constraints are the existence constraints that are consistent with
    *  absence and partial order constraints.
    *  @param bnet bayesian network to test
    *  @param c the real existence constraints that the bnet doesn't satisfy.
    *  @return if the bayesian net satisfy the existence constraints (true) or not (false)
    */
    public boolean testRealExistenceConstraints(Bnet bnet, LinkList c) {
	return testRealExistenceConstraints((Graph)bnet,c);
    }//end testRealExistenceConstraints Bnet with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the existence
    *  constraints, returning the real existence constraints that aren't satisfied. The
    *  real existence constraints are the existence constraints that are consistent with
    *  absence and partial order constraints.
    *  @param bnet graph to test
    *  @param c the real existence constraints that the bnet doesn't satisfy.
    *  @return if the graph satisfy the existence constraints (true) or not (false)
    */
    public boolean testRealExistenceConstraints(Graph bnet, LinkList c) {

	int i,j;
	Graph e=getRealExistenceConstraints(this.existence);
	LinkList elinks=e.getLinkList().copy();//(this.existence.getLinkList()).copy();
	LinkList bnetlinks=(bnet.getLinkList()).copy();
	if (c.size()!=0) c=new LinkList(); //the LinkList must be empty

	//The links in existence constraints must exist in bnet
	for (i=0;i<elinks.size();i++) {
	    boolean exists=false;
	    Link elink=elinks.elementAt(i);
	    if ( elink.getDirected() ) {
		//if the link is directed (x->y)  must exist x-->y
		for (j=0;j<bnetlinks.size();j++)
		    if ( elink.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    } else {
		//if the link isn't directed (x--y) , must exist x-->y or x<--y
		Link one=new Link(elink.getTail(), elink.getHead(), true); //x->y
		Link two=new Link(elink.getHead(), elink.getTail(), true); //x<-y

		for (j=0;j<bnetlinks.size();j++)
		    if ( one.equals(bnetlinks.elementAt(j)) || two.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    }

	    //If the existence constraints isn't verfied by the bnet, add it to the vector
	    if (!exists) c.insertLink(elink);
	}//end for i

	//return if the bnet verify the existence constraints
	if (c.size()==0) return true;
	else return false;
    }//end testRealExistenceConstraints Graph with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the absence
    *  constraints
    *  @param bnet bayesian network to test
    *  @return if the bayesian net satisfy the absence constraints (true) or not (false)
    */
    public boolean testAbsenceConstraints(Bnet bnet) {
	return testAbsenceConstraints((Graph) bnet);
    }//end testAbsenceConstraints bnet method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the absence constraints
    *  @param bnet graph to test
    *  @return if the graph satisfy the absence constraints (true) or not (false)
    */
    public boolean testAbsenceConstraints(Graph bnet) {

	int i,j;
	LinkList alinks=(this.absence.getLinkList()).copy();
	LinkList bnetlinks=(bnet.getLinkList()).copy();

	//The links in absence constraints must not exist in bnet
	for (i=0;i<alinks.size();i++) {
	    boolean exists=false;
	    Link alink=alinks.elementAt(i);

	    if ( alink.getDirected() ) {
		//if the link is directed (x->y)  must not exist x-->y
		for (j=0;j<bnetlinks.size();j++)
		    if ( alink.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    } else {
		//if the link isn't directed (x--y) , must not exist x-->y and x<--y
		Link one=new Link(alink.getTail(), alink.getHead(), true); //x->y
		Link two=new Link(alink.getHead(), alink.getTail(), true); //x<-y
		for (j=0;j<bnetlinks.size();j++)
		    if ( one.equals(bnetlinks.elementAt(j)) || two.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    }
	    //If the absence constraints isn't verfied by the bnet, return false
	    if (warnings) if (exists) System.out.println("WARNING: The bnet doesn't verify the absence constraint:"+alink);
	    if (exists) return false;
	}//end for i

	//return that the bnet verify all the absence constraints
	return true;
    }//end testAbsenceConstraints graph method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the absence
    *  constraints, returning the absence constraints that aren't satisfied.
    *  @param bnet bayesian network to test
    *  @param c the constraints that the bnet doesn't satisfy.
    *  @return if the bayesian net satisfy the absence constraints (true) or not (false)
    */
    public boolean testAbsenceConstraints(Bnet bnet, LinkList c) {
	return testAbsenceConstraints((Graph)bnet,c);

    }//end testAbsenceConstraints bnet with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the absence
    *  constraints, returning the absence constraints that aren't satisfied.
    *  @param bnet graph to test
    *  @param c the constraints that the graph doesn't satisfy.
    *  @return if the graph satisfy the absence constraints (true) or not (false)
    */
    public boolean testAbsenceConstraints(Graph bnet, LinkList c) {

	int i,j;
	LinkList alinks=(this.absence.getLinkList()).copy();
	LinkList bnetlinks=(bnet.getLinkList()).copy();
	if (c.size()!=0) c=new LinkList(); //the LinkList must be empty

	//The links in absence constraints must not exist in bnet
	for (i=0;i<alinks.size();i++) {
	    boolean exists=false;
	    Link alink=alinks.elementAt(i);

	    if ( alink.getDirected() ) {
		//if the link is directed (x->y)  must not exist x-->y
		for (j=0;j<bnetlinks.size();j++)
		    if ( alink.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    } else {
		//if the link isn't directed (x--y) , must not exist x-->y and x<--y
		Link one=new Link(alink.getTail(), alink.getHead(), true); //x->y
		Link two=new Link(alink.getHead(), alink.getTail(), true); //x<-y
		for (j=0;j<bnetlinks.size();j++)
		    if ( one.equals(bnetlinks.elementAt(j)) || two.equals(bnetlinks.elementAt(j)) ) {
			exists=true;
			break;
		    }
	    }
	    //If the absence constraints isn't verfied by the bnet, add it to the vector
	    if (exists) c.insertLink(alink);
	}//end for i

	//return if the bnet verify the absence constraints
	if (c.size()==0) return true;
	else return false;
    }//end testAbsenceConstraints graph with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the partial order
    *  constraints
    *  @param bnet bayesian network to test
    *  @return if the bayesian net satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testPartialOrderConstraints(Bnet bnet) {
	return testPartialOrderConstraints((Graph) bnet);
    }//end testPartialOrderConstraints bnet method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the partial order
    *  constraints
    *  @param graph graph to test
    *  @return if the graph satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testPartialOrderConstraints(Graph graph) {

	Graph ordergraph=this.order.duplicate();
	Graph uniongraph = ordergraph.union( new Graph(graph.getNodeList().duplicate(),graph.getLinkList().duplicate(),Graph.MIXED));

	//The union of the Bnet and the partial order constraints must be a DAG
	//check if the union it's a DAG
	if ( !uniongraph.isADag() )
	    return false;

	//return that the bnet verify all the Partial Order constraints
	return true;
    }//end testPartialOrderConstraints graph method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the partial order
    *  constraints, returning the partial order constraints that aren't 
    *  satisfied. 
    *  @param bnet bayesian network to test
    *  @param c the constraints that the bnet doesn't satisfy. Must be empty.
    *  @return if the bayesian net satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testPartialOrderConstraints(Bnet bnet, LinkList c) {
	return testPartialOrderConstraints((Graph)bnet, c);
    }//end testPartialOrderConstraints bnet with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the partial order
    *  constraints, returning the partial order constraints that aren't 
    *  satisfied. 
    *  @param bnet graph to test
    *  @param c the constraints that the grapht doesn't satisfy. Must be empty.
    *  @return if the graph satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testPartialOrderConstraints(Graph bnet, LinkList c) {

	int i,j;
	LinkList aux=new LinkList();

	boolean out=testRealPartialOrderConstraints(bnet,aux);

	for (i=0;i<aux.size();i++) {
	    Link l=aux.elementAt(i);
	    //we look if the violated constraints is in order constraints or if it's a transitive constraint
	    if ( this.order.getLinkList().getID(l.getTail().getName(),l.getHead().getName()) == -1) {
		//we search the partial order constraints involved in the transitive constraint
		LinkList ll=new LinkList();
		this.order.setVisitedAll(false);
		//l.getHead().setVisited(false);l.getTail().setVisited(false);
		Node head=this.order.getNodeList().getNode(l.getHead().getName());
		Node tail=this.order.getNodeList().getNode(l.getTail().getName());
		
		getPath(head,tail,this.order,ll);
		for (j=0;j<ll.size();j++) {
		    l=ll.elementAt(j);
		    c.insertLink(l);
		}//end for j
	    } else {
		c.insertLink(l);
	    }

	}//end for i

	//return that the bnet verify all the Partial Order constraints or not
	return out;

    }//end testPartialOrderConstraints graph with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy the partial order
    *  constraints, returning the partial order constraints that aren't 
    *  satisfied; constraints returned are computed using the transitive
    *  property.
    *  @param bnet bayesian network to test
    *  @param c the constraints that the bnet doesn't satisfy. Must be empty.
    *  @return if the bayesian net satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testRealPartialOrderConstraints(Bnet bnet, LinkList c) {
	return testRealPartialOrderConstraints((Graph)bnet, c);
    }//end testRealPartialOrderConstraints Bnet with LinkList method    
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a graph satisfy the partial order
    *  constraints, returning the partial order constraints that aren't 
    *  satisfied; constraints returned are computed using the transitive
    *  property.
    *  @param bnet graph to test
    *  @param c the constraints that the graph doesn't satisfy. Must be empty.
    *  @return if the graph satisfy the partial order
    *          constraints (true) or not (false)
    */
    public boolean testRealPartialOrderConstraints(Graph bnet, LinkList c) {

	//store the original partial order constraints
	Graph realorder=getRealPartialOrderConstraints(this.order);
	LinkList orderLinks=realorder.getLinkList();

	//For each constraint x->y, we find a path between y and x (y->...->x) in the bnet
	for (int i=0;i<orderLinks.size();i++) {
	    Link olink=orderLinks.elementAt(i);
	    Node t=bnet.getNodeList().getNode(olink.getTail().getName());
	    Node h=bnet.getNodeList().getNode(olink.getHead().getName());
	    bnet.setVisitedAll(false);
	    if (bnet.isThereDirectedPath(h,t))
		c.insertLink(orderLinks.elementAt(i));
	}
	
	//Look if we have contraints that aren't satisfied
	if (c.size()!=0) return false;

	//return that the bnet verify all the Partial Order constraints
	return true;
    }//end testRealPartialOrderConstraints graph with LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy all the
    *  constraints (existence, absence anda partial order)
    *  @param bnet bayesian network to test
    *  @return if the bayesian net satisfy the constraints (true) or not (false)
    */
    public boolean test(Bnet bnet) {

	NodeList bnl=bnet.getNodeList();
	NodeList enl=this.existence.getNodeList();
	NodeList anl=this.absence.getNodeList();
	NodeList onl=this.order.getNodeList();

	//check if the bnet and the constraints are the same problem
	if (!sameProblem(bnet)) {
	    if (warnings) System.out.println("WARNING:The Bayesian Network is for different problem that constraints");
	    return false;
	}

	//check if the constraints are consistents with the bnet
	if (!testExistenceConstraints(bnet) ) {
	    if (warnings) System.out.println("WARNING:The Bayesian Network doesn't verify the existence constraints");
	    return false;
	}

	if (!testAbsenceConstraints(bnet) ) {
	    if (warnings) System.out.println("WARNING:The Bayesian Network doesn't verify the absence constraints");
	    return false;
	}

	if (!testPartialOrderConstraints(bnet) ) {
	    if (warnings) System.out.println("WARNING:The Bayesian Network doesn't verify the partial order constraints");
	    return false;
	}

	//if verify all the constraints return true
	return true;

    }//end test method
    /*---------------------------------------------------------------*/
    /**
    *  This method test if a Bayesian network satisfy all the
    *  constraints (existence, absence and partial order), and return 
    *  the constraints that aren't satisfied.
    *  @param bnet bayesian network to test
    *  @param e the existence constraints that the bnet doesn't satisfy.
    *  @param a the absence constraints that the bnet doesn't satisfy.
    *  @param o the partial order constraints that the bnet doesn't satisfy.
    *  @return if the bayesian net satisfy the constraints (true) or not (false)
    */
    public boolean test(Bnet bnet, LinkList e, LinkList a, LinkList o ) {

	NodeList bnl=bnet.getNodeList();
	NodeList enl=this.existence.getNodeList();
	NodeList anl=this.absence.getNodeList();
	NodeList onl=this.order.getNodeList();
	boolean out=true;

	//check if the bnet and the constraints are the same problem
	if (!sameProblem(bnet)) {
	    if (warnings) System.out.println("WARNING:The Bayesian Network is for different problem that constraints");
	    return false;
	}

	//check if the constraints are consistents with the bnet
	if (!testExistenceConstraints(bnet,e)) out=false;
	if (!testAbsenceConstraints(bnet,a)) out=false;
	if (!testPartialOrderConstraints(bnet,o)) out=false;

	//return if the constraints are consistent with the bnet
	return out;

    }//end test with LinkLists method
    /*---------------------------------------------------------------*/
    /**
     *  Prints the constraints to the standard output
     */
    public void print(){
	NodeList nl=null;
	LinkList ll=null;
	
	nl=this.getExistenceConstraints().getNodeList();
	ll=this.getExistenceConstraints().getLinkList();
	System.out.println("Nodes:");
	for (int i=0;i< nl.size();i++) System.out.println(nl.elementAt(i).getName());
	
	System.out.println("Existence Constraints:");
	for (int i=0;i< ll.size();i++) System.out.print(ll.elementAt(i));
	
	System.out.println("Absence Constraints:");
	ll=this.getAbsenceConstraints().getLinkList();
	for (int i=0;i< ll.size();i++) System.out.print(ll.elementAt(i));
	
	System.out.println("Partial Order Constraints:");
	ll=this.getPartialOrderConstraints().getLinkList();
	for (int i=0;i< ll.size();i++) System.out.print(ll.elementAt(i));
	System.out.println("");
    }//end print method
    /*---------------------------------------------------------------------------*/
    /**
     *  Prints a LinkList to the standard output
     *  @param l LinkList to show
     */
    static public  void print(LinkList l){
	int i;
	int j;
	for (i=0,j=0;i< l.size();i++) {
	    String l1=l.elementAt(i).toString();
	    String l2=l1.substring(0,l1.length()-1);
	    System.out.print(l2+" ");j++;
	    if (j>10) { j=0; System.out.println("");}
	}
	System.out.println("");
    }//end print LinkList method
    /*---------------------------------------------------------------*/
    /**
    *  Check if a existence constraint is consistent with the other
    *  stored constraints. Cases of existence inconsistence:
    *  <ul>
    *  <li>Consistency Condition:<ul>
    *       <li>The existence (partial directed acyclic graphs) graph is acycled
    *       </ul>
    *  <li>With absence constraints:<ul>
    *       <li>The existence constraint is x->y and exists in absence constraints x->y or x--y
    *       <li>The existence constraint is x--y and exists in absence constraints x--y
    *       </ul>
    *  <li>With partial order constraints:<ul>
    *       <li>Merge Existence and Partial Order constraints in one Graph. Test if the graph have diricted cycles
    *       </ul>
    *  </ul>
    *  @param constraint constraint to checked as consistent
    *  @return if the constraint is consistent
    */
    private boolean existenceConstraintConsistent(Link constraint) throws InvalidEditException {

	int i,j;
	Node head=constraint.getHead();
	Node tail=constraint.getTail();
	boolean directed=constraint.getDirected();

	//links to check
	Link xy_d=new Link(tail,head,true); //x->y
	Link xy=new Link(tail,head,false ); //x--y
	Link yx=new Link(head,tail,false ); //y--x

	//check autoconsistence, the existence graph must not have Mixed cycles
	Graph existencegraph=this.existence.duplicate();
	existencegraph.createLink(existencegraph.getNodeList() .getNode(constraint.getTail().getName()),
                           	  existencegraph.getNodeList().getNode(constraint.getHead().getName()),
 				  constraint.getDirected());

        if (existencegraph.hasDirectedCycles()) {//hasMixedCycles()) {
	    if (warnings) {
		System.out.print("\nWARNING: It can't be added like a existence constraint.");
		System.out.println("It isn't autoconsistent.");
	    }
	    return false;
	}

	//check consistence with absence constraints
	if (directed) {
	    //if the links is x-->y look if exists x->y or x--y or y--x en absence constraints
	    if (  (this.absence.getLinkList()).indexOf(xy_d) != -1  || (this.absence.getLinkList()).indexOf(xy)!=-1 ||
		  (this.absence.getLinkList()).indexOf(yx)!=-1) {
		if (warnings) {
		    System.out.print("\nWARNING: It can't be added like a existence constraint.");
		    System.out.println("It isn't consistent with absence constraints.");
		}
		return false;
	    }
	} else {
	    //if the links is x--y look if exists x--y and y--x en absence constraints
	    if (  ((this.absence.getLinkList()).indexOf(xy) != -1) || ((this.absence.getLinkList()).indexOf(yx) != -1) ){
		if (warnings) {
		    System.out.print("\nWARNING: It can't be added like a existence constraint.");
		    System.out.println("It isn't consistent with absence constraints.");
		}
		return false;
	    }
	} //end if-else directed

	//check consistence with order constraints
	//merge existence constraints with the new link and order constraints
	Graph ordergraph = this.order.duplicate();
	Graph uniongraph = ordergraph.union(getRealExistenceConstraints(existencegraph));
	//We have to comply that (x->y) U (x--y) = (x->y)
	LinkList unionlinks=(uniongraph.getLinkList()).copy();
	LinkList orderlinks=(ordergraph.getLinkList()).copy();
	for (i=0;i<unionlinks.size();i++) {
	    Link nodirected=unionlinks.elementAt(i);//x--y ?
	    //if the link it's x--y, we search x->y or x<-y in 
	    if ( !nodirected.getDirected() ) {
		Link one=new Link(nodirected.getTail(), nodirected.getHead(), true); //x->y
		Link two=new Link(nodirected.getHead(), nodirected.getTail(), true); //x<-y
		for (j=0;j<orderlinks.size();j++)
		    //if exists, x->y (or y->x), we delete x--y and add x->y (or y->x)
		    if ( one.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(uniongraph.getNodeList() .getNode(one.getTail().getName()),
                            	              uniongraph.getNodeList().getNode(one.getHead().getName()),
					      one.getDirected());

			break;
		    } else if (two.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(uniongraph.getNodeList().getNode(two.getTail().getName()),
                            	              uniongraph.getNodeList().getNode(two.getHead().getName()),
					      two.getDirected());

			break;
		    }
	    }
	}//end for i

	if (uniongraph.hasDirectedCycles())   {//hasMixedCycles())   {
	    if (warnings) {
		System.out.print("\nWARNING: It can't be added like a existence constraint.");
		System.out.println("It isn't consistent with order constraints.");
	    }
	    return false;
	}

	//It it's consistent with absence constraints and partial order constraints, we can add it
	return true;
    }//end method existenceConstraintConsistent
    /*---------------------------------------------------------------*/
    /**
    *  Check if a absence constraint is consistent with the other
    *  stored constraints. Cases of absence inconsistence:
    *  <ul>
    *  <li>Consistency Condition:<ul>
    *       <li>No needed
    *       </ul>
    *  <li>With existence constraints:<ul>
    *       <li>The absence constraint is x->y and exists in existence constraints x->y
    *       <li>The absence constraint is x->y and exists in existence constraints x<-y and x--y
    *       <li>The absence constraint is x--y and exists in existence constraints x--y or x->y or x<-y
    *       </ul>
    *  <li>With partial order constraints:<ul>
    *       <li>No needed
    *       </ul>
    *  </ul>
    *  @param constraint constraint to checked as consistent
    *  @return if the constraint is consistent
    */
    private boolean absenceConstraintConsistent(Link constraint) throws InvalidEditException {

	Node head=constraint.getHead();
	Node tail=constraint.getTail();
	boolean directed=constraint.getDirected();
	Graph e=getRealExistenceConstraints(this.existence);

	//links to check
	Link xy_d=new Link(tail,head,true); //x->y
	Link xy=new Link(tail,head,false ); //x--y
	Link yx=new Link(head,tail,false ); //y--x
	Link yx_d=new Link(head,tail,true ); //x<-y

	//check consistence with existence constraints
	if (directed) {
	    //if the links is x-->y look if exists x->y or (x--y and x<-y) en existence constraints
	    if (  (e.getLinkList()).indexOf(xy_d) != -1  ||
		  ( (e.getLinkList()).indexOf(xy)!=-1 && (e.getLinkList()).indexOf(yx_d) != -1) ||
		  ( (e.getLinkList()).indexOf(yx)!=-1 && (e.getLinkList()).indexOf(yx_d) != -1)
	       ) {
		if (warnings) {
		    System.out.print("\nWARNING: It can't be added like a absence constraint.");
		    System.out.println("It isn't consistent with existence constraints.");
		}
		return false;
	    } //if the absence link is x->y and  x--y in Ge ( y->x in Ge') and there is a directed path from x to y, we will force a cycle
	    else if ( (e.getLinkList()).indexOf(xy)!=-1 ){
		e.setVisitedAll(false);
		Node x=e.getNodeList().getNode(tail.getName());
		Node y=e.getNodeList().getNode(head.getName());

		if (e.isThereDirectedPath(x,y) ) return false;

		//we have to check that if the absence links is x->y and y--x in Ge ( y->x in Ge') is y->x is consitent
		//with the partial order constraints
		if (!this.partialOrderConstraintConsistent(new Link(y,x,true))) return false;
		
	    } //if the absence link is x->y and  y--x in Ge ( y->x in Ge') and there is a directed path from x to y, we will force a cycle
	    else if ( (e.getLinkList()).indexOf(yx)!=-1 ){
		e.setVisitedAll(false);
		Node x=e.getNodeList().getNode(tail.getName());
		Node y=e.getNodeList().getNode(head.getName());

		if (e.isThereDirectedPath(x,y) ) return false;

		//we have to check that if the absence links is x->y and x--y in Ge ( y->x in Ge') is y->x is consitent
		//with the partial order constraints
		if (!this.partialOrderConstraintConsistent(new Link(y,x,true))) return false;;
	    }

	} else {
	    //if the links is x--y look if exists in existence constraints x--y or y--x or x->y or x<-y
	    if (  (e.getLinkList()).indexOf(xy_d) !=-1 ||
		  (e.getLinkList()).indexOf(xy)   !=-1 ||
		  (e.getLinkList()).indexOf(yx)   !=-1 ||
		  (e.getLinkList()).indexOf(yx_d) !=-1 ) {
		if (warnings) {
		    System.out.print("\nWARNING: It can't be added like a absence constraint.");
		    System.out.println("It isn't consistent with existence constraints.");
		}
		return false;
	    }
	} //end if-else directed

	//It it's consistent with existence constraints, we can add it
	return true;

    }//end method absenceConstraintConsistent
    /*---------------------------------------------------------------*/
    /**
    *  Check if a partial order constraint is consistent with the other
    *  stored constraints. Cases of partial order inconsistence:
    *  <ul>
    *  <li>Consistency Condition:<ul>
    *       <li>The partial order graph is a DAG (Directed Acyclic Graph)
    *       </ul>
    *  <li>With existence constraints:<ul>
    *       <li>Merge Existence and Partial Order constraints in one Graph. Test if the graph have cycles
    *       </ul>
    *  <li>With absence constraints:<ul>
    *       <li>No needed
    *       </ul>
    *  </ul>
    *  @param constraint constraint to checked as consistent
    *  @return if the constraint is consistent
    */
    private boolean partialOrderConstraintConsistent(Link constraint) throws InvalidEditException {

	int i,j;
	Node head=constraint.getHead();
	Node tail=constraint.getTail();
	boolean directed=constraint.getDirected();

	//x--y isn't a valid order constraint
	if (!directed) return false;

	//The new link with the order constraints must be a DAG
	Graph ordergraph=this.order.duplicate();
	ordergraph.createLink(ordergraph.getNodeList().getNode(constraint.getTail().getName()),
                              ordergraph.getNodeList().getNode(constraint.getHead().getName()),
			      constraint.getDirected());
	LinkList orderlinks=(ordergraph.getLinkList()).copy();



	//check if it's a DAG
	if (!ordergraph.isADag()) {
	    if (warnings) {
		System.out.print("\nWARNING: It can't be added like a order constraint.");
		System.out.println("It isn't autoconsistent.");
	    }
	    return false;
	}

	//The new link with the order constraints merged with existence constraints mustn't have
        //directed cycles

	//merge order constraints with the real existence constraints
	Graph uniongraph = ordergraph.union(getRealExistenceConstraints(this.existence));
	//We have to comply that (x->y) U (x--y) = (x->y)
	LinkList unionlinks=(uniongraph.getLinkList()).copy();
	for (i=0;i<unionlinks.size();i++) {
	    Link nodirected=unionlinks.elementAt(i);//x--y ?
	    //if the link it's x--y, we search x->y or x<-y in 
	    if ( !nodirected.getDirected() ) {
		Link one=new Link(nodirected.getTail(), nodirected.getHead(), true); //x->y
		Link two=new Link(nodirected.getHead(), nodirected.getTail(), true); //x<-y
		for (j=0;j<orderlinks.size();j++)
		    //if exists, x->y (or y->x), we delete x--y and add x->y (or y->x)
		    if ( one.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(uniongraph.getNodeList().getNode(one.getTail().getName()),
                            uniongraph.getNodeList().getNode(one.getHead().getName()),
					                  one.getDirected());

			break;
		    } else if (two.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(uniongraph.getNodeList().getNode(two.getTail().getName()),
                            uniongraph.getNodeList().getNode(two.getHead().getName()),
					                  two.getDirected());

			break;
		    }
	    }
	}//end for i
	
        if (uniongraph.hasDirectedCycles()) {//hasMixedCycles()) {
	    if (warnings) {
		System.out.print("\nWARNING: It can't be added like a partial order constraint.");
		System.out.println("It isn't consistent with existence constraints.");
	    }
	    return false;
	}

	//otherwise it can be added
	return true;
    }//end method partialOrderConstraintConsistent

    /*---------------------------------------------------------------*/
    /**
    *  Check if a link can exists in the problem or, that's the same, the
    *  nodes of a link exists in the problem.
    *  @param link link with the two nodes
    *  @return true if the link is possible, false it's not.
    */
    private boolean linkCanExists(Link link) {
	int i;
	NodeList nodelist=this.existence.getNodeList();
	Node head=link.getHead();
	Node tail=link.getTail();
	boolean headexists=false, tailexists=false;

	//check if the link nodes exists
	for (i=0; (i < nodelist.size()) && (!headexists || !tailexists); i++)
	    if ( ((Node)nodelist.elementAt(i)).equals(head) )
		headexists=true;
	    else if ( ((Node)nodelist.elementAt(i)).equals(tail) )
		tailexists=true;
	if ( (!headexists) || (!tailexists)) {
	    if (warnings) System.out.println("\nWARNING: Unknown nodes in constraint. It can't be added.");
	    return false;
	}

	return true;
    }//end method linkCanExists

    /*---------------------------------------------------------------*/
    /**
    *  Add a new constraint; the constraint is represented using a link (it
    *  can be directed or not); if it's a existence constraint, the link
    *  represents a arc/edge that exists in the Bnet; if it's a absence
    *  constraint, the link represents a arc/edge that doesn't exists in
    *  the Bnet; if it's a partial order constraint, the link (must be
    *  directed) represents a order, so the tail/origin < head/destiny, it's assumed
    *  that this constraint is transitive.
    *  @param constcraint type of constraint (EXISTENCE, ABSENCE or PARTIALORDER)
    *  @param link constraint as a graph link (arc/edge)
    *  @return if the constraint is added
    */
    public boolean addConstraint(int constraint, Link link) throws InvalidEditException {

	this.realexistence=null;
	this.realpartialorder=null;
	//check if the link is possible
	if ( !linkCanExists(link)) 
	    return false;

	//add the constraint
	switch (constraint) {
	case EXISTENCE:
	    //check if the link exists
	    if ( (this.existence.getLink(link.getTail(), link.getHead()) != null) ||
           (this.existence.getLink(link.getHead(), link.getTail()) != null) ){
		if (warnings) System.out.println("\nWARNING: There is already a link equals to this one. This Existence constraint can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }

	    //check if the constraint is consistent
	    if (existenceConstraintConsistent(link)) {
    		//add the constraint
				Node tail=this.existence.getNodeList().getNode(link.getTail().getName());
				Node head=this.existence.getNodeList().getNode(link.getHead().getName());
		this.existence.createLink(tail, head,link.getDirected());
	    } else {
		if (warnings) System.out.println("\nWARNING: This Existence constraint isn't consistent, it can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }
	    break;

	case ABSENCE:
	    //check if the link exists
	    if ( (this.absence.getLink(link.getTail(), link.getHead()) != null) || 
		 (this.absence.getLink(link.getHead(), link.getTail()) != null) ){
		if (warnings) System.out.println("\nWARNING: There is already a link equals to this one. This Absence constraint can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }

	    //check if the constraint is consistent
	    if (absenceConstraintConsistent(link)) {
		//add the constraint
		Node tail=this.absence.getNodeList().getNode(link.getTail().getName());
		Node head=this.absence.getNodeList().getNode(link.getHead().getName());

		this.absence.createLink(tail, head,link.getDirected());
	    } else {
		if (warnings) System.out.println("\nWARNING: This Absence constraint isn't consistent, it can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }
	    break;

	case PARTIALORDER:
	    //check if the link exists
	    if ( this.order.getLink(link.getTail(), link.getHead()) != null ) {
		if (warnings) System.out.println("\nWARNING: There is already a link equals to this one. This Partial Order constraint can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }

	    //check if the constraint is consistent
	    if (partialOrderConstraintConsistent(link)) {
		//add the constraint
		Node tail=this.order.getNodeList().getNode(link.getTail().getName());
		Node head=this.order.getNodeList().getNode(link.getHead().getName());
		
		this.order.createLink(tail, head,true);
		
	    } else {
		if (warnings) System.out.println("\nWARNING: This Partial Order constraint isn't consistent, it can't be added.");
		this.realexistence=null;
		this.realpartialorder=null;
		return false;
	    }
	    break;

	default:
	    //unknow constraint type
	    if (warnings) System.out.println("\nWARNING: Unknown constraint type. It isn't added.");
			this.realexistence=null;
			this.realpartialorder=null;
	    return false;
	}//end switch

	//the constraint it's added
	this.realexistence=null;
	this.realpartialorder=null;
	return true;

    }//end method addConstraint
    /*---------------------------------------------------------------*/
    /**
    * Delete a existing constraint; the constraint is represented using a link (it
    * can be directed or not).
    * @param constraint type of constraint (EXISTENCE, ABSENCE or PARTIALORDER)
    * @param link constraint as a graph link (arc/edge)
    * @return true if the constraint is deleted, flase otherwise
    */
    public boolean removeConstraint(int constraint, Link link) throws InvalidEditException {
        int before,after;

        //check if the link is possible
        if ( !linkCanExists(link)) {
            return false;
        }

        //del the constraint
        switch (constraint) {
        case EXISTENCE:
          before=this.existence.getLinkList().size();
          this.existence.getLinkList().removeLink(link);
          after=this.existence.getLinkList().size();
          break;

        case ABSENCE:
          before=this.absence.getLinkList().size();
          this.absence.getLinkList().removeLink(link);
          after=this.absence.getLinkList().size();
          break;

        case PARTIALORDER:
          before=this.order.getLinkList().size();
          this.order.getLinkList().removeLink(link);
          after=this.order.getLinkList().size();
          break;

        default:
            //unknown constraint type
            if (warnings) System.out.println("\nWARNING: Unknown constraint type. It isn't removed.");
            return false;
        }//end switch

        //Look if the constraint it's removed
        if (before==after)
	    return false;
        else {
	    this.realexistence=null;
	    this.realpartialorder=null;
	    return true;
	}

    }//end method removeConstraint
    /*---------------------------------------------------------------*/
    /**
     * This method remove a link(directed or not directed) from a graph,
     * verifying the stored existence constraints. Possibles cases:
     * <ul>
     *   <li>The Link to remove is x->y
     *         <ul>
     *                <li>x->y not in Ge && x--y not in Ge ==>remove x->y</li>
     *                <li>x->y in Ge ==> don't remove x->y</li>
     *                <li>x<-y in Ge ==> remove x->y</li>
     *                <li>x--y in Ge
     *                    <ul>
     *                        <li>x->y in Ga ==> remove x->y, add x<-y (if not exists)</li>
     *                        <li>x<-y in Ga ==> don't remove x->y</li>
     *                        <li>x->y in Go ==> don't remove x->y</li>
     *                        <li>x<-y in Go ==> remove x->y, add x<-y (if not exists)</li>
     *                        <li>x->y not in Ga && x<-y not in Ga ==> remove x->y, add x<-y (if not exists)</li>    
     *                    </ul>
     *                </li>
     *         </ul>
     *   </li>
     *
     *   <li>The Link to remove is x--y
     *         <ul>
     *                <li>x--y in Ge
     *                    <ul>
     *                        <li>x->y in Ga ==> remove x--y, add x<-y</li>
     *                        <li>x<-y in Ga ==> remove x--y, add x->y</li>
     *                        <li>x->y in Go ==> remove x--y, add x->y</li>
     *                        <li>x<-y in Go ==> remove x--y, add x<-y</li>
     *                        <li>x->y not in Ga && x<-y not in Ga ==> don't remove x--y</li>
     *                    </ul>
     *                </li>
     *                <li>x->y in Ge ==> remove x--y, add x->y </li>
     *                <li>x<-y in Ge ==> remove x--y, add x<-y </li>
     *                <li>x--y not in Ge && x->y not in Ge && x<-y not in Ge ==> Remove x--y</li>
     *         </ul>
     *   </li>
     * </ul>
     * @param g graph where we have to remove the link
     * @param l link to remove
     * @return true if the link is removed, false in other case
     */
    public boolean removeLink(Graph g, Link l) throws InvalidEditException {
	Node x=l.getTail();
	Node y=l.getHead();

	Graph reale=getRealExistenceConstraints(this.existence);
	Link elink=reale.getLink(x,y);
	if (elink==null) elink=this.existence.getLink(y,x);
	Link alink=this.absence.getLink(x,y);
	if (alink==null) alink=this.absence.getLink(y,x);
	Link olink=this.order.getLink(x,y);
	if (olink==null) olink=this.order.getLink(y,x);

	//look it the link is directed or not 
	if (l.getDirected()) {
	    //case x->y
	    if (elink==null) {
		//case x->y not in Ge && x--y not in Ge
		g.removeLink(l);
		return true;
	    } else if (elink.getDirected()) {
		//case x->y in Ge || x<-y in Ge
		if ( elink.getTail().equals(x))
		    //case x->y in Ge
		    return false;
		else {
		    //case x<-y in Ge               
		    g.removeLink(l);
		    return true;
		}
	    } else {
		return false;
	    }

	} else {
	    //case the link to remove is not direceted x--y
	    if (elink==null) {
		//x--y not in Ge && x->y not in Ge && x<-y not in Ge ==> Remove x--y
		g.removeLink(l);
		return true;
	    } else if (elink.getDirected()) {
		if (elink.getTail().equals(x)) {
		    //case x->y in Ge ==> remove x--y, add x->y 
		    g.removeLink(l);
		    g.createLink(x,y,true);
		    return true;
		} else {
		    //case x<-y in Ge ==> remove x--y, add x<-y 
		    g.removeLink(l);
		    g.createLink(y,x,true);
		    return true;
		}
	    } else {
		//case x--y in Ge
		if (alink == null) {
		    //case x--y in Ge && x->y not in Ga && x<-y not in Ga
		    if (olink==null) {
			//case x--y in Ge && x->y not in Ga && x<-y not in Ga && x->y not in Go && x<-y not in Go
			return false;
		    } else if (olink.getTail().equals(x)) {
			//x->y in Go ==> remove x--y, add x->y
			g.removeLink(l);
			g.createLink(x,y,true);
			return true;
		    } else {
			//x<-y in Go ==> remove x--y, add x<-y
			g.removeLink(l);
			g.createLink(y,x,true);
			return true;
		    }
		} else if (alink.getTail().equals(x)) {
		    //case x->y in Ga ==> remove x--y, add x<-y
		    g.removeLink(l);
		    g.createLink(y,x,true);
		    return true;
		} else {
		    //case x<-y in Ga ==> remove x--y, add x->y
		    g.removeLink(l);
		    g.createLink(x,y,true);
		    return true;
		}
	    }
	}//end if_else l.getDirected
    }//end method removeLink

    /*---------------------------------------------------------------*/
    /**
     * This method remove a directed link from a graph (that ,
     * verifying the stored existence constraints. Possibles cases:
     * <ul>
     *   <li>The Link to remove is x->y
     *         <ul>
     *                <li>x->y not in Ge && x--y not in Ge ==>remove x->y</li>
     *                <li>x->y in Ge ==> don't remove x->y</li>
     *                <li>x<-y in Ge ==> remove x->y</li>
     *                <li>x--y in Ge
     *                    <ul>
     *                        <li>x->y in Ga ==> remove x->y, add x<-y (if not exists)</li>
     *                        <li>x<-y in Ga ==> don't remove x->y</li>
     *                        <li>x->y in Go ==> don't remove x->y</li>
     *                        <li>x<-y in Go ==> remove x->y, add x<-y (if not exists)</li>
     *                        <li>x->y not in Ga && x<-y not in Ga ==> remove x->y, add x<-y (if not exists)</li>    
     *                    </ul>
     *                </li>
     *         </ul>
     *   </li>
     *
     * </ul>
     * @param g graph where we have to remove the link
     * @param l link to remove
     * @return true if the link can be removed, false in other case
     */
    public boolean locallyRemoveLink(Graph g, Link l) throws InvalidEditException {
	Node x=l.getTail();
	Node y=l.getHead();

	//Graph reale=getRealExistenceConstraints(this.existence);
	Link elink=this.existence.getLink(x,y);
	if (elink==null) elink=this.existence.getLink(y,x);

	//look it the link is directed or not 
	if (l.getDirected()) {
	    //case x->y
	    if (elink==null) {
		//case x->y not in Ge && x--y not in Ge
		return true;
	    } else if (elink.getDirected()) {
		//case x->y in Ge || x<-y in Ge
		if ( elink.getTail().equals(x))
		    //case x->y in Ge
		    return false;
		else {
		    //case x<-y in Ge               
		    return true;
		}
	    } else {
		return false;
	    }
	} else 
	    return false;


    }//end method locallyRemoveLink

    /*---------------------------------------------------------------*/
    /**
     * This method create a directed link in a graph,
     * verifying the stored existence constraints. The link can't 
     * create a directed cycle.
     * We have to verify:
     * <ul>
     *                <li>x->y not in Ga && x--y not in Ga && there is no directed path from y to x in G U Go
     *                </li>
     * </ul>
     * @param g graph where we have to create the directed link
     * @param l link to create
     * @return true if the link is created, false in other case
     */
    public boolean createDirectedLink(Graph g, Link l) throws InvalidEditException {

	//check if the link to create is undirected
	if (!l.getDirected()) return false;

	//check if adding the link will result a directed cycle
	g.setVisitedAll(false);
	Vector path=new Vector();
	path.add(l.getTail());
	if (g.hasDirectedCycle(l.getTail(),l.getHead(),path) ) 
	    return false;

	//Check if adding the link (that isn't a existence constraint) will result a directed cycle
	Graph reale=getRealExistenceConstraints(this.existence);
	Link elink=reale.getLink(l.getTail(),l.getHead());
	if (elink==null) 
	    elink=reale.getLink(l.getHead(),l.getTail());
	if (elink==null){
	    Vector v=new Vector();
	    Node tail=reale.getNodeList().getNode(l.getTail().getName());
	    Node head=reale.getNodeList().getNode(l.getHead().getName());
	    v.add(tail);
	    if (reale.hasDirectedCycle(tail,head,v) ) //hasMixedCycle(tail,head,v) ) 
		return false;
	}

	Node x=l.getTail();
	Node y=l.getHead();
	Link xy_d=new Link(x,y,true); //x->y
	Link xy=new Link(x,y,false ); //x--y
	Link yx=new Link(y,x,false ); //y--x
	
	//first look that x->y not in Ga && x--y not in Ga 
	if ( this.absence.getLink(x,y)  != null) 
	    return false;
	
	if ( ( this.absence.getLinkList().indexOf(xy_d)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(xy)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(yx)  != -1 ) )
	    return false;

	//look if there is a directed path from y to x in G U Go
 	Graph ordergraph=new Graph(order); //Go
	Graph uniongraph = ordergraph.union( new Graph(g.getNodeList().duplicate(),
						       g.getLinkList().duplicate(),Graph.MIXED)); // G U Go

	//We have to comply that (x->y) U (x--y) = (x->y)
	LinkList unionlinks=(uniongraph.getLinkList()).copy();
	LinkList orderlinks=(ordergraph.getLinkList()).copy();
	for (int i=0;i<unionlinks.size();i++) {
	    Link nodirected=unionlinks.elementAt(i);//x--y ?
	    //if the link it's x--y, we search x->y or x<-y in 
	    if ( !nodirected.getDirected() ) {
		Link one=new Link(nodirected.getTail(), nodirected.getHead(), true); //x->y
		Link two=new Link(nodirected.getHead(), nodirected.getTail(), true); //x<-y
		for (int j=0;j<orderlinks.size();j++)
		    //if exists, x->y (or y->x), we delete x--y and add x->y (or y->x)
		    if ( one.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getTail(), nodirected.getHead(), true);			
			//LinkList ulinks=uniongraph.getLinkList().copy();
			//ulinks.insertLink(one);
			//uniongraph.setLinkList(ulinks.copy());
			break;
		    } else if (two.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getHead(), nodirected.getTail(), true);
			//LinkList ulinks=uniongraph.getLinkList().copy();
			//ulinks.insertLink(two);
			//uniongraph.setLinkList(ulinks);
			break;
		    }
	    }
	}//end for i


	uniongraph.setVisitedAll(false);
	if ( uniongraph.isThereDirectedPath(uniongraph.getNodeList().getNode(y.getName()),uniongraph.getNodeList().getNode(x.getName())) )
	    return false;
	
	//Create the directed link
	g.createLink(x,y,true);

	//the link has been created
	return true;

    }//end method createDirectedLink

    /*---------------------------------------------------------------*/
    /**
     * This method create a directed link in a graph,
     * verifying the stored existence constraints. The link can't 
     * create a directed cycle.
     * We have to verify:
     * <ul>
     *                <li>x->y not in Ga && x--y not in Ga && there is no directed path from y to x in G U Go
     *                </li>
     * </ul>
     * In this method we obtain G U Go from the params
     * @param g graph where we have to create the directed link
     * @param l link to create
     * @param g graph with the union of G And Go
     * @return true if the link is created, false in other case
     */
    public boolean createDirectedLink(Graph g, Link l, Graph GUGo) throws InvalidEditException {

	//check if the link to create is undirected
	if (!l.getDirected()) return false;

	//check if adding the link will result a directed cycle
	g.setVisitedAll(false);
	Vector path=new Vector();
	path.add(l.getTail());
	if (g.hasDirectedCycle(l.getTail(),l.getHead(),path) ) 
	    return false;

	//Check if adding the link (that isn't a existence constraint) will result a directed cycle
	Graph reale=getRealExistenceConstraints(this.existence);
	Link elink=reale.getLink(l.getTail(),l.getHead());
	if (elink==null) 
	    elink=reale.getLink(l.getHead(),l.getTail());
	if (elink==null){
	    Vector v=new Vector();
	    Node tail=reale.getNodeList().getNode(l.getTail().getName());
	    Node head=reale.getNodeList().getNode(l.getHead().getName());
	    v.add(tail);
	    if (reale.hasDirectedCycle(tail,head,v) ) //hasMixedCycle(tail,head,v) ) 
		return false;
	}

	Node x=l.getTail();
	Node y=l.getHead();
	Link xy_d=new Link(x,y,true); //x->y
	Link xy=new Link(x,y,false ); //x--y
	Link yx=new Link(y,x,false ); //y--x
	
	//first look that x->y not in Ga && x--y not in Ga 
	if ( this.absence.getLink(x,y)  != null) 
	    return false;
	
	if ( ( this.absence.getLinkList().indexOf(xy_d)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(xy)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(yx)  != -1 ) )
	    return false;

	//look if there is a directed path from y to x in G U Go
	GUGo.setVisitedAll(false);
	if ( GUGo.isThereDirectedPath(GUGo.getNodeList().getNode(y.getName()),GUGo.getNodeList().getNode(x.getName())) )
	    return false;
	
	//Create the directed link
	g.createLink(x,y,true);

	//the link has been created
	return true;

    }//end method createDirectedLink with GUGo
    /*---------------------------------------------------------------*/
    /**
     * This method create a directed link in a graph (that previously 
     * satifies the constraints), verifying the stored existence 
     * constraints. The link can't create a directed cycle.
     * We have to verify:
     * <ul>
     *                <li>x->y not in Ga && x--y not in Ga && there is no directed path from y to x in G U Go
     *                </li>
     * </ul>
     * In this method we obtain G U Go from the params
     * @param g graph where we have to create the directed link
     * @param l link to create
     * @param GUGo graph with the union of G And Go
     * @return true if the link can be created, false in other case
     */
    public boolean locallyCreateDirectedLink(Graph g, Link l, Graph GUGo) throws InvalidEditException {

	//check if the link to create is undirected
	if (!l.getDirected()) return false;

	Node x=l.getTail();
	Node y=l.getHead();
	Link xy_d=new Link(x,y,true); //x->y
	Link xy=new Link(x,y,false ); //x--y
	Link yx=new Link(y,x,false ); //y--x
	
	//first look that x->y not in Ga && x--y not in Ga 
	if ( ( this.absence.getLinkList().indexOf(xy_d)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(xy)  != -1 ) ||
	     ( this.absence.getLinkList().indexOf(yx)  != -1 ) )
	    return false;

	//look if there is a directed path from y to x in G U Go
	GUGo.setVisitedAll(false);
	if ( GUGo.isThereDirectedPath(GUGo.getNodeList().getNode(y.getName()),GUGo.getNodeList().getNode(x.getName())) )
	    return false;
	
	//the link can be created
	return true;

    }//end method locallyCreateDirectedLink
    /*---------------------------------------------------------------*/
    /**
     * This method inverts a directed link in a graph (x->y to y->x),
     * verifying the stored existence constraints. We have to verify:
     * <ul>
     *                <li>x->y not in Ge && x<-y not in Ga && there is no directed path from x to y in Go U (G\{x->y})
     *                </li>
     * </ul>
     * @param g graph where we have to invert the directed link
     * @param l link to invert (the old link x->y, not the new link y->x)
     * @param GUGo graph with the union of G And Go
     * @return true if the link can be inverted, false in other case
     */
    public boolean locallyInvertLink(Graph g, Link l, Graph GUGo) throws InvalidEditException {
	//check if the link to create is undirected
	if (!l.getDirected()) return false;

	Node x=l.getTail();
	Node y=l.getHead();
	Link xy_d=new Link(x,y,true); //x->y
	Link yx_d=new Link(y,x,true );//y->x

	Graph realexistence=getRealExistenceConstraints(this.existence);
	Graph realorder=getRealPartialOrderConstraints(this.order);

	//first look that x->y not in Ge 
	if ( realexistence.getLinkList().indexOf(xy_d)  != -1 ) 
	    return false;

	//second look that y->x not in Ga 
	if ( this.absence.getLinkList().indexOf(yx_d)  != -1 ) 
	    return false;

	//third look that x->y not in Go
	if ( realorder.getLinkList().indexOf(xy_d)  != -1 ) 
	    return false;
 
	//remove x->y from GUGo
	Node tail=GUGo.getNodeList().getNode(x.getName());
	Node head=GUGo.getNodeList().getNode(y.getName());
	GUGo.removeLink(tail,head);

	//look if there is a directed path form x to y in Go U (G/{x->y})
	GUGo.setVisitedAll(false);
	if ( GUGo.isThereDirectedPath(GUGo.getNodeList().getNode(x.getName()),GUGo.getNodeList().getNode(y.getName())) ){
	    //restore x->y in GUGo
	    GUGo.createLink(tail,head,true);
	    return false;
	}

	//restore x->y in GUGo
	GUGo.createLink(tail,head,true);
 
	//the link can be inverted
	return true;
    }//end method locallyInvertLink

    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
     * This method inverts a directed link in a graph (x->y to y->x),
     * verifying the stored existence constraints. We have to verify:
     * <ul>
     *                <li>x->y not in Ge && x<-y not in Ga && there is no directed path from x to y in Go U (G\{x->y})
     *                </li>
     * </ul>
     * @param g graph where we have to invert the directed link
     * @param l link to invert (the old link x->y, not the new link y->x)
     * @return true if the link is inverted, false in other case
     */
    public boolean invertLink(Graph g, Link l) throws InvalidEditException {
	Node x=l.getTail();
	Node y=l.getHead();
	Graph realexistence=getRealExistenceConstraints(this.existence);
	Link elink=realexistence.getLink(x,y);
	if (elink==null) elink=realexistence.getLink(y,x);
	Link alink=this.absence.getLink(x,y);
	if (alink==null) alink=this.absence.getLink(y,x);

	//first look that x->y not in Ge 
	if ( (elink  != null) && (elink.getDirected()) && (elink.getTail().equals(x)) )
	    return false;

	//second look that y->x not in Ga 
	if ( (alink  != null) && (alink.getDirected()) && (elink.getTail().equals(y)) )
	    return false;

	//look that y--x not in Ga 
	if ( (alink  != null) && (!alink.getDirected()) ) {
	    if (warnings) System.out.println("WARNING: Theres is the link x->y and in absence contraints there is x--y");
	    return false;
	}

	//look if there is a directed path form x to y in Go U (G/{x->y})
	Graph ordergraph=this.order.duplicate(); //Go
	Graph ggraph=g.duplicate(); //G
	ggraph.removeLink(new Link(x,y,true));

	Graph uniongraph = ordergraph.union( new Graph(ggraph.getNodeList().duplicate(),ggraph.getLinkList().duplicate(),Graph.MIXED)); // Go U (G/{x-y})

	//The union of Go U (G/{x-y}) must not have a directed path from x to y
	if ( uniongraph.isThereDirectedPath(x,y) )
	    return false;

	//Remove the old link
	g.removeLink(new Link(x,y,true));

	//Create the new inverted link
	g.createLink(y,x,true);
 
	//the link has been inverted
	return true;
    }//end method invertLink

    /*---------------------------------------------------------------*/
    /**
     * This method look for a partial order, in Partial order constraints,
     * between two nodes. 
     * @param x first node 
     * @param y second node
     * @return -1 if x<y a in Partial Order constraints,  1 if x>y a in Partil Order constraints, 0 in other case.
     */
    public int partialOrder(Node x, Node y) {
	//if there is a directed path from "x" to "y" (x->y) in Partial Order Constraints then x<y
	if (this.order.isThereDirectedPath(x,y) ) return -1;

	//if there is a directed path from "y" to "x" (y->x) in Partial Order Constraints then x>y
	if (this.order.isThereDirectedPath(y,x) ) return +1;

	//return 0 in other case
	return 0;
    } //end partialOrder method
    /*---------------------------------------------------------------*/
    /**
     * This recursive method set ramdomly the direction for a undirected 
     * Links List, the returned LinkList must be a DAG
     * @param dlink directed links list. Must be called with the directed links.
     * @param ulink undirected links list. Must have at least one element.
     * @return true if it's found a directed link list, false in other case
     */
    public boolean  setDirections(LinkList dlinks, LinkList ulinks) {
	Graph graph;
	
	//Get the first undirected link to orient it
	Link l=ulinks.elementAt(0);
	ulinks.removeLink(0);
	Node t=l.getTail();
	Node h=l.getHead();
	
	//Test t->h
	dlinks.insertLink(new Link(t,h,true));
	graph = new Graph (this.existence.getNodeList().duplicate(),dlinks,Graph.MIXED);//DIRECTED);

	//check if the graph with the directed links it's a DAG
	if (graph.isADag() && this.testPartialOrderConstraints(graph) ) {
	    if (ulinks.size()==0) return true; //Stop case
	    else if (setDirections(dlinks,ulinks)) return true; //recursive case
	}

	//Test h->t
	dlinks.removeLink(new Link(t,h,true));
	dlinks.insertLink(new Link(h,t,true));
	graph = new Graph (this.existence.getNodeList(),dlinks,Graph.MIXED);//DIRECTED);

	//check if the graph with the directed links it's a DAG
	if (graph.isADag() && this.testPartialOrderConstraints(graph) ) {
	    if (ulinks.size()==0) return true; //Stop case
	    else if (setDirections(dlinks,ulinks)) return true; //recursive case
	} 

	//if the graph isn't a DAG or there is cycles in a deeper level
	dlinks.removeLink(new Link(h,t,true));
	ulinks.insertLink(l);
	return false;

    }//end setDirections method

    /*---------------------------------------------------------------*/
    /**
     * This method create a Bnet object that satisfy the stored constraints. 
     * This Bnet is based in existence constraints, and the undirected links are 
     * oriented randomly but satisfying partial order constraints.
     * @return a basic bnet tha satisfy the contraints
     */
    public Bnet initialBnet() throws InvalidEditException {
	//Get the existence constraints satisfying absence constraints
	Graph e=this.getRealExistenceConstraints(this.existence);
	LinkList links=e.getLinkList();
	LinkList ulinks=new LinkList();

	//Starts the new Bnet
	Bnet initial=new Bnet (e.getNodeList().duplicate());

	//Add the directed links and some undirected links
	for (int i=0; i < links.size(); i++) {
	    Link l=links.elementAt(i);
	    Node tail=initial.getNodeList().getNode(l.getTail().getName());
	    Node head=initial.getNodeList().getNode(l.getHead().getName());

	    //if the link is directed we add it to the initial bnet
	    if (l.getDirected()) 
		initial.createLink(tail,head,true);
	    else {
		int aux=this.partialOrder(tail,head);
		//If tail<head and tail>head store the undirected link
		if (aux==0) ulinks.insertLink(l);
		//If tail<head store tail->head
		else if (aux<0) initial.createLink(tail,head);
		//If head<tail store head->tail
		else initial.createLink(head,tail);
	    }
	}//end for


	//we set a direction to the undirected links and add to the bnet
	if (ulinks.size()>0) {
	    LinkList dlinks=initial.getLinkList().duplicate();//new LinkList();
	    //set directions to the links without cycles
	    if (!this.setDirections(dlinks,ulinks)) {
		if (warnings) System.out.println("ERROR:Can't set directions to undireced links without cycles.");
		throw new SecurityException("ERROR:Can't set directions to undireced links without cycles.");
	    }
			
	    //add the computed directed links
	    for (int i=0; i < dlinks.size(); i++) {
		Link l=dlinks.elementAt(i);
		Node tail=initial.getNodeList().getNode(l.getTail().getName());
		Node head=initial.getNodeList().getNode(l.getHead().getName());

		//if the link is directed we add it to the initial bnet
		if (initial.getLinkList().getID(tail.getName(),head.getName()) == -1)
			initial.createLink(tail,head);//,true);
	    }
	}

	//This lines are only for debug puposes
	if (!this.test(initial)) {
	    if (warnings) System.out.println("ERROR:The initial doesn't satisfy the stored constraints.");
	    try {
		FileWriter fb = new FileWriter("InitialBnetForLearning.elv");
		initial.saveBnet(fb);fb.close();
	    } catch(java.io.IOException ex) {
		System.out.println("Error saving constraints");
	    }
	    throw new SecurityException("ERROR:The initial doesn't satisfy the stored constraints.");
	}
	
	//return the generated initial bnet
	return initial;

    }//end method initialBnet
    /*---------------------------------------------------------------*/
    /**
     * This method test if a Bnet verify the constraints  when remove/invert/add a link.
     * @param bnet bnet to test
     * @param link link to remove/add/invert
     * @param op Operation to be done: 0-remove, 1-invert, 2-add.
     * @return true if the operation can be done satisfying the stored contraints, false in otre case
     */
    public boolean verifyConstraints(Bnet bnet, Link link, int op) {

	boolean sout;
	//copy the original bnet
	Bnet aux= null;
	Graph auxGraph = null;
	
	auxGraph = bnet.duplicate();
	aux = new Bnet();
	aux.setNodeList(auxGraph.getNodeList().duplicate());
	aux.setLinkList(auxGraph.getLinkList().duplicate());

	//apply the operation
	switch (op) {
	    //Operation: remove link
	    case 0: try {
			  aux.removeLink(link);
	            } catch (InvalidEditException e) {return false;};
		    sout=this.test(aux);
	            return sout;

	    //Operation: invert link
	    case 1: try {
	                 aux.removeLink(link);
	                 aux.createLink(link.getHead(),link.getTail(),true);
	            } catch (InvalidEditException e) {return false;};
		    sout=this.test(aux);
	            return sout;

	    //Operation: add link
	    case 2: try {
	                 aux.createLink(link.getTail(),link.getHead(),true);
	            } catch (InvalidEditException e) {return false;};
		    sout=this.test(aux);
	            return sout;

	    //Other (unknown) operation, return false
  	    default:if (warnings) System.out.println("WARNING: Uknown operation in verifyconstraints(bnt,lin,op) method");
	            return false;
	} //end switch
    }//end verifyConstraints method
    /*---------------------------------------------------------------*/
    /**
     * This method test if a Bnet (that verify the constraints) verify the constraints 
     * when remove/invert/add a link. 
     * @param bnet bnet to test
     * @param link link to remove/add/invert
     * @param op Operation to be done: 0-remove, 1-invert, 2-add.
     * @param g graph with the union of G And Go
     * @return true if the operation can be done satisfying the stored contraints, false in otre case
     */
    public boolean locallyVerifyConstraints(Bnet bnet, Link link, int op,  Graph GUGo) {

	//copy the original bnet and the original link
	Graph auxgraph = (Graph)bnet;
	Node tail=auxgraph.getNodeList().getNode(link.getTail().getName());
	Node head=auxgraph.getNodeList().getNode(link.getHead().getName());
	Link auxlink = new Link(tail,head,link.getDirected());

	//apply the operation
	switch (op) {
	    //Operation: remove link
 	    case 0: try {
		return (this.locallyRemoveLink(auxgraph,auxlink));
	    }catch (InvalidEditException e) {return false;}

	    //Operation: invert link
 	    case 1: try {
		return (this.locallyInvertLink(auxgraph,auxlink,GUGo));
	    }catch (InvalidEditException e) {return false;}
		
	    //Operation: add link
	    case 2: try {
		return (this.locallyCreateDirectedLink(auxgraph,auxlink,GUGo));
	    }catch (InvalidEditException e) {return false;}

	    //Other (unknown) operation, return false
	default:if (warnings) System.out.println("Uknown operation in locallyVerifyConstraints(bnt,lin,op,GUGo) method");
		return false;
	} //end switch
    }//end locallyVerifyConstraints method

    /*---------------------------------------------------------------*/
    /**
     * This method is used to know if there a path from h->t 
     * for the nodes t->h, of the same link. The nodes will must set to not visited or they
     * will not be studied. It stores the path from h to t if it exists.
     * @param t a tail of the link
     * @param h a head of the link
     * @param ll a LinkList with the path from h to t.
     * @return true if there is a path from h to t, false in other case
     */
    protected boolean getPath(Node t, Node h, Graph g, LinkList ll) {
        if (t.equals(h)) {
            return true;
	} else {
	    //get the childs of h
	    Enumeration c=g.children(h).elements();
	    Node child;
	    boolean found=false;

	    //We study where the childrens of h ends
	    while ((!found) && (c.hasMoreElements())){
		child = (Node) c.nextElement();
		//only check childs that hasn't visited
		if (!child.getVisited()) { 
		    child.setVisited(true);
		    ll.insertLink(new Link(h,child,true));
		    found=getPath(t,child,g,ll);
		    if (!found) ll.removeLink(new Link(h,child,true));
		}
	    }//end while childs

	    return found;
	}//end else

    }//end getPath method
    /*---------------------------------------------------------------*/
    /**
     * This method removes the links which  its direction can't be inverted
     * @param ll a LinkList with the links
     * @return the ll LinkList with the links that we can invert
     */
    private LinkList removeNonInvertibleLinks(LinkList ll) {
	LinkList result=ll.copy();
	LinkList ell=getRealExistenceConstraints(this.existence).getLinkList();
	LinkList all=this.absence.getLinkList();
	//	LinkList intersection=ll.intersection(ell);
	
	//we remove from the output LinkList the links x->y that are in Ge
	for (int i=0;i<ell.size();i++) {
	    Link l=ell.elementAt(i);
	    int j=ll.indexOf(l);
	    if (j!=-1)
		result.removeLink(l);
	}

	ell=this.existence.getLinkList();
	//we remove from the output LinkList the links x->y that y->x are in Ga and x--y are not in Ge
	for (int i=0;i<all.size();i++) {
	    Link l=all.elementAt(i);
	    if (l.getDirected()) {
		Link li=new Link(l.getHead(),l.getTail(),true);
		int j=ll.indexOf(li);
		if (j!=-1) {
		    Link lu1=new Link(l.getHead(),l.getTail(),false);
		    Link lu2=new Link(l.getHead(),l.getTail(),false);
		    j=ell.indexOf(lu1);
		    if (j==-1) j=ell.indexOf(lu2);
		    if (j==-1)
			result.removeLink(li);
		}
	    }
	}//end for
	
	return result;
    }//end removeNonInvertibleLinks method
    /*---------------------------------------------------------------*/
    /**
     * This method removes the links which can't be removed. It this links
     * are removed, the existence constraints will be not satisfied.
     * @param ll a LinkList with the links
     * @return the ll LinkList with the links that we can remove
     */
    private LinkList removeNonRemovableLinks(LinkList ll) {
	LinkList result=ll.copy();
	LinkList ell=	getRealExistenceConstraints(this.existence).getLinkList();//this.existence.getLinkList();

	//we remove from the output LinkList the links x->y, x--y  that are in Ge
	for (int i=0;i<ell.size();i++) {
	    Link l=ell.elementAt(i);
	    if (l.getDirected()) {
		int j=ll.indexOf(l);
		if (j!=-1)
		    result.removeLink(l);
	    } else {
		Link lu1=new Link(l.getTail(),l.getHead(),true);
		Link lu2=new Link(l.getHead(),l.getTail(),true);
		int j=ll.indexOf(lu1);
		if (j==-1) {
		    j=ll.indexOf(lu2);
		    if (j!=-1)
			result.removeLink(lu2);
		} else {
		    result.removeLink(lu1);
		}
	    }
	}//end for
	
	return result;
    }//end removeNonRemovableLinks method
    /*---------------------------------------------------------------*/
    /**
     * This method repair a graph if this graph doesn't verify the 
     * existence constraints
     * @param graph graph to repair
     */
    public void repairExistenceConstraints(Graph graph) {
	int i;
	LinkList ell=new LinkList();
	LinkList oll=new LinkList();
	LinkList aux_oll=new LinkList();

	//add arcs in existence constraints
	Bnet aux=new Bnet(graph.getNodeList());
	if (!testRealExistenceConstraints(aux, ell)){
	    for (i=0; i < ell.size(); i++) {
		Link laux=ell.elementAt(i);
		Link l=new Link(graph.getNodeList().getNode(laux.getTail().getName()),
				graph.getNodeList().getNode(laux.getHead().getName()),laux.getDirected());

		//If the existence constraint is directed, make a directed link
		if (l.getDirected()) {
		    //if tail<-Head exists, we remove it before.
		    int pos = graph.getLinkList().getID(l.getHead().getName(), l.getTail().getName());
		    if (pos != -1)				
			try {
			    graph.removeLink(l.getHead(),l.getTail());
			} catch (InvalidEditException iee){};
		    
		    try {
			graph.createLink(l.getTail(),l.getHead(),true);
		    } catch (InvalidEditException iee){};
		} else {
		    //If the existence constraint is undirected, make a directed link,
		    //is the number of violated partial order constraints get bigger, we invert
		    //the link
		    aux=new Bnet(graph.getNodeList());
		    testPartialOrderConstraints(aux, oll);
		    
		    //Create the link in one direction
		    try {
			graph.createLink(l.getTail(),l.getHead(),true);
		    } catch (InvalidEditException iee){};

		    //i order constraints get bigger, invert the link
		    aux_oll=new LinkList();
		    testPartialOrderConstraints(aux, aux_oll);
		    if (oll.size() > aux_oll.size()) {
			try {
			    graph.removeLink(l.getTail(),l.getHead());
			    graph.createLink(l.getHead(),l.getTail(),true);
			} catch (InvalidEditException iee){};
		    }
		}//end else
	    }//end for
	}
    }//end repairExistenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     * This method repair a graph if this graph doesn't verify the 
     * existence constraints using a metric
     * @param graph graph to repair
     * @param metric metric used to score the best direction for undirected links
     */
    public void repairExistenceConstraints(Graph graph, Metrics metric) {
	int i;
	LinkList ell=new LinkList();
	LinkList oll=new LinkList();
	LinkList aux_oll=new LinkList();

	//add arcs in existence constraints
	Bnet aux=new Bnet(graph.getNodeList());
	if (!testRealExistenceConstraints(aux, ell)){
	    for (i=0; i < ell.size(); i++) {
		Link laux=ell.elementAt(i);
		Link l=new Link(graph.getNodeList().getNode(laux.getTail().getName()),
				graph.getNodeList().getNode(laux.getHead().getName()),laux.getDirected());

		//If the existence constraint is directed, make a directed link
		if (l.getDirected()) {
		    //if tail<-Head exists, we remove it before.
		    int pos = graph.getLinkList().getID(l.getHead().getName(), l.getTail().getName());
		    if (pos != -1)				
			try {
			    graph.removeLink(l.getHead(),l.getTail());
			} catch (InvalidEditException iee){};
		    
		    try {
			graph.createLink(l.getTail(),l.getHead(),true);
		    } catch (InvalidEditException iee){};
		} else {

		    //Find the direcction that maximices the score
		    double score_r=0;
		    double score_l=0;

		    //score right direction
		    try{
			graph.createLink(l.getTail(),l.getHead(),true);
			score_r=metric.score(new Bnet (graph.getNodeList()) );
			graph.removeLink(l.getTail(),l.getHead());
			
			//score left direction
			graph.createLink(l.getHead(),l.getTail(),true);
			score_l=metric.score(new Bnet (graph.getNodeList()) );
			graph.removeLink(l.getHead(),l.getTail());		    
		    } catch (InvalidEditException iee){ };
		    
		    //look if it's better the right score
		    if (score_r>score_l)  {
			//If the existence constraint is undirected, make a directed link,
			//is the number of violated partial order constraints get bigger, we invert
			//the link
			aux=new Bnet(graph.getNodeList());
			testPartialOrderConstraints(aux, oll);
		    
			//Create the link in one direction
			try {
			    graph.createLink(l.getTail(),l.getHead(),true);
			} catch (InvalidEditException iee){};

			//if order constraints get bigger, invert the link
			aux_oll=new LinkList();
			testPartialOrderConstraints(aux, aux_oll);
			if (oll.size() > aux_oll.size()) {
			    try {
				graph.removeLink(l.getTail(),l.getHead());
				graph.createLink(l.getHead(),l.getTail(),true);
			    } catch (InvalidEditException iee){};
			}

			//if order still constraints get bigger, left the first try
			aux_oll=new LinkList();
			testPartialOrderConstraints(aux, aux_oll);
			if (oll.size() > aux_oll.size()) {
			    try {
				graph.removeLink(l.getHead(),l.getTail());
				graph.createLink(l.getTail(),l.getHead(),true);
			    } catch (InvalidEditException iee){};
			}

		    } else {//better left

			//If the existence constraint is undirected, make a directed link,
			//is the number of violated partial order constraints get bigger, we invert
			//the link
			aux=new Bnet(graph.getNodeList());
			testPartialOrderConstraints(aux, oll);
		    
			//Create the link in one direction
			try {
			    graph.createLink(l.getHead(),l.getTail(),true);
			} catch (InvalidEditException iee){};

			//if order constraints get bigger, invert the link
			aux_oll=new LinkList();
			testPartialOrderConstraints(aux, aux_oll);
			if (oll.size() > aux_oll.size()) {
			    try {
				graph.removeLink(l.getHead(),l.getTail());
				graph.createLink(l.getTail(),l.getHead(),true);
			    } catch (InvalidEditException iee){};
			}

			//if order still constraints get bigger, left the first try
			aux_oll=new LinkList();
			testPartialOrderConstraints(aux, aux_oll);
			if (oll.size() > aux_oll.size()) {
			    try {
				graph.removeLink(l.getTail(),l.getHead());
				graph.createLink(l.getHead(),l.getTail(),true);
			    } catch (InvalidEditException iee){};
			}


		    }
		}//end else
	    }//end for
	}
    }//end repairExistenceConstraints method with Metric
    /*---------------------------------------------------------------*/
    /**
     * This method repair a graph if this graph doesn't verify the 
     * absence constraints
     * @param graph graph to repair
     */
    public void repairAbsenceConstraints(Graph graph) {
	int i;
	LinkList all=new LinkList();
	Bnet bnet=new Bnet(graph.getNodeList());
	
	//get the absence constraints that isn't verified
	testAbsenceConstraints(bnet,all);
	
	//remove arcs in absence constraints
	for (i=0; i < all.size(); i++) {
	    Link laux=all.elementAt(i);
	    Node t=graph.getNodeList().getNode(laux.getTail().getName());
	    Node h=graph.getNodeList().getNode(laux.getHead().getName());
	    Link l=new Link(t,h,laux.getDirected());
	    
	    if (l.getDirected()) {
		//remove directed links
		try {
		    graph.removeLink(t,h);
		} catch (InvalidEditException iee){};
	    } else {
		//remove undirected links
		try {
		    int pos = graph.getLinkList().getID(t.getName(), h.getName());
		    if (pos != -1)				
			graph.removeLink(t,h);
		    else	
			graph.removeLink(h,t);
		} catch (InvalidEditException iee){};
	    }
	}//end for
    }//end repairAbsenceConstraints method
    /*---------------------------------------------------------------*/
    /**
     * This method repair a graph if this graph doesn't verify the 
     * partial order constraints
     * @param graph graph to repair
     */
    public void repairPartialOrderConstraints(Graph graph) {
	int i;
	Random r=new Random();//2345);
	LinkList oll=new LinkList();
	LinkList aux_oll=new LinkList();
	boolean nobackmode=false;

	//invert or remove arcs to verify partial order constraints
	//Bnet aux=new Bnet(graph.getNodeList());
	oll=new LinkList();
	if (!testRealPartialOrderConstraints(graph, oll))
            //we repair every violated partial order constraints
	    while (oll.size() > 0) {
		Link laux=oll.elementAt(0);
		Node t=graph.getNodeList().getNode(laux.getTail().getName());
		Node h=graph.getNodeList().getNode(laux.getHead().getName());
		Link l=new Link(t,h,laux.getDirected());

		//we search the candidate links from h to t (h-> ... ->t) that must not be in existence constraints
		graph.setVisitedAll(false);
		LinkList candidates=new LinkList();
		if (!getPath(t,h,graph,candidates)) {
		    System.out.println("ERROR: The partial order constraint can't be founded");
		    aux_oll=new LinkList();
		    testRealPartialOrderConstraints(graph, aux_oll);
		    oll=aux_oll;
		    laux=oll.elementAt(0);
		    t=graph.getNodeList().getNode(laux.getTail().getName());
		    h=graph.getNodeList().getNode(laux.getHead().getName());
		    l=new Link(t,h,laux.getDirected());
		} 

		LinkList candidates2=candidates.copy();
		LinkList candidates3=candidates.copy();
		candidates=removeNonInvertibleLinks(candidates);
		
		//we try to invert(first) or remove(second) the candidates links to verify the partial
		//order constraints
		int before=oll.size();
		boolean cont=true;
		while  ((candidates.size()>0) && cont) {
		    //we get a candidate link randomly
		    Link link=candidates.elementAt(r.nextInt(candidates.size()));
		    candidates.removeLink(link);
		    Node t2=graph.getNodeList().getNode(link.getTail().getName());
		    Node h2=graph.getNodeList().getNode(link.getHead().getName());

		    //we try to invert it
		    try{
			graph.removeLink(t2,h2);
			graph.createLink(h2,t2,true);
		    } catch (InvalidEditException iee){ };
		    
		    
		    //Look if the constraints number are less than before
		    //aux=new Bnet(graph.getNodeList());
		    aux_oll=new LinkList();
		    testRealPartialOrderConstraints(graph, aux_oll);
		    
		    //if the constraints number are smaller, we go to the next constraints, else 
		    //we undo the invert operation
		    if (oll.size() > aux_oll.size()) {
			cont=false;
		    } else if (!nobackmode) {
			//undo the invert 
			try{
			    graph.removeLink(h2,t2);
			    graph.createLink(t2,h2,true);
			} catch (InvalidEditException iee){ };
		    }
		}//end while invert links

		//If we satisfy the constraint, inverting a candidate link, we update the violated constraints
		if (!cont) {
		    oll=aux_oll;
		} 
		else {
		    //If the constraint isn't satisfied, we try to remove candidates links
		    if (nobackmode) {
			if (!getPath(t,h,graph,candidates)) {
			    System.out.println("WARNING: I cant remove the inverted links");
			    oll=new LinkList();
			    testRealPartialOrderConstraints(graph, aux_oll);
			    oll=aux_oll;
			    laux=oll.elementAt(0);
			    t=graph.getNodeList().getNode(laux.getTail().getName());
			    h=graph.getNodeList().getNode(laux.getHead().getName());
			    l=new Link(t,h,laux.getDirected());
        		} 
			
			
		    } else {
			candidates=candidates2.copy();
		    }
		    candidates=removeNonRemovableLinks(candidates);

		    
		    while  ( (candidates.size()>0) && cont ) {
			//we get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());

			//we try to remove it
			candidates.removeLink(link);
			try{			
			    graph.removeLink(t2,h2);
			} catch (InvalidEditException iee){};
			
			//Look if the constraints number are less than before
			//aux=new Bnet(graph.getNodeList());
			aux_oll=new LinkList();
			testRealPartialOrderConstraints(graph , aux_oll);
		
			//If the constraints number get smaller, go to the next else we undo the remove operation
			if (oll.size() > aux_oll.size()) {
			    cont=false;
			} else if (oll.size() == aux_oll.size()) {
			    //we look if we need remove more candidates to verify the constraint
			    graph.setVisitedAll(false);
			    candidates2=new LinkList();
			    if (!getPath(t,h,graph,candidates2)) {
				try{
				    graph.createLink(t2,h2,true);
				} catch (InvalidEditException iee){ };
			    }
			    candidates2=removeNonRemovableLinks(candidates2);
			    candidates.join(candidates2);
			} else {
			    //we undo the remove operation
			    try{
				graph.createLink(t2,h2,true);
			    } catch (InvalidEditException iee){};
			}
		    }//end while remove links
		}//end else


		//If the constraints number get smaller, we update the list of constraints
		if (!cont) {
		    if (aux_oll.size()>0) 
			cont=true;
		    oll=aux_oll.copy();
		} else {
		    System.out.println("WARNING: The partial order constraints doesn't get smaller");
		    if (nobackmode) nobackmode=false;
		    else nobackmode=true;
		    //System.exit(1);
		}
	    }

    }//end repairPartialOrderConstraints method
    /*---------------------------------------------------------------*/
    /**
     * This method repair a graph if this graph doesn't verify the 
     * partial order constraints
     * @param graph graph to repair
     * @param metric metric used to score the best direction for undirected links
     */
    public void repairPartialOrderConstraints(Graph graph, Metrics metric) {
	int i;
	Random r=new Random();//2345);
	LinkList oll=new LinkList();
	LinkList aux_oll=new LinkList();

	//invert or remove arcs to verify partial order constraints
	Bnet aux=new Bnet(graph.getNodeList());
	oll=new LinkList();
	if (!testRealPartialOrderConstraints(aux, oll))
	
            //we repair every violated partial order constraints
	    while (oll.size() > 0) {

		Link laux=oll.elementAt(0);
		Node t=graph.getNodeList().getNode(laux.getTail().getName());
		Node h=graph.getNodeList().getNode(laux.getHead().getName());
		Link l=new Link(t,h,laux.getDirected());

		//we search the candidate links from h to t (h-> ... ->t) that must not be in existence constraints
		graph.setVisitedAll(false);
		LinkList candidates=new LinkList();
		if (!getPath(t,h,graph,candidates)) {
		    System.out.println("ERROR: The partial order constraint can't be founded");
		    //remove cycles
		    removeCycles(graph);
		    
		   // System.exit(1);
		} 
		LinkList candidates2=candidates.copy();
		LinkList candidates3=candidates.copy();
		candidates=removeNonInvertibleLinks(candidates);

		//we try to invert(first) or remove(second) the candidates links to verify the partial
		//order constraints
		int before=oll.size();
		boolean cont=true;
		while  ((candidates.size()>0) && cont) {
		    //we get a candidate link randomly
		    Link link=candidates.elementAt(r.nextInt(candidates.size()));
		    candidates.removeLink(link);
		    Node t2=graph.getNodeList().getNode(link.getTail().getName());
		    Node h2=graph.getNodeList().getNode(link.getHead().getName());

		    //we try to invert it and check if it's better invert the link than remove it
		    double score_remove=0;
		    double score_invert=0;
		    try{
			graph.removeLink(t2,h2);
			score_remove=metric.score(new Bnet (graph.getNodeList()) );
			graph.createLink(h2,t2,true);
			score_invert=metric.score(new Bnet (graph.getNodeList()) );
		    } catch (InvalidEditException iee){ };

		    //if it's better remove, undo the invert and go next candidate link
		    if (score_remove>score_invert) {
			try{
			    graph.removeLink(h2,t2);
			    graph.createLink(t2,h2,true);
			} catch (InvalidEditException iee){ };
			continue;
		    }

		    //Look if the constraints number are less than before
		    aux=new Bnet(graph.getNodeList());
		    aux_oll=new LinkList();
		    testRealPartialOrderConstraints(aux, aux_oll);
		    
		    //if the constraints number are smaller, we go to the next constraints, else 
		    //we undo the invert operation
		    if (oll.size() > aux_oll.size()) {
			cont=false;
		    } else {
			//undo the invert 
			try{
			    graph.removeLink(h2,t2);
			    graph.createLink(t2,h2,true);
			} catch (InvalidEditException iee){ };
		    }
		}//end while invert links

		//If we satisfy the constraint, inverting a candidate link, we update the violated constraints
		if (!cont) {
		    oll=aux_oll;
		} 
		else {
		    //If the constraint isn't satisfied, we try to remove candidates links
		    candidates=candidates2.copy();
		    candidates=removeNonRemovableLinks(candidates);

		    while  ( (candidates.size()>0) && cont ) {
			//we get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());

			//we try to remove it
			candidates.removeLink(link);
			try{			
			    graph.removeLink(t2,h2);
			} catch (InvalidEditException iee){};
			
			//Look if the constraints number are less than before
			aux=new Bnet(graph.getNodeList());
			aux_oll=new LinkList();
			testRealPartialOrderConstraints(aux, aux_oll);
		
			//If the constraints number get smaller, go to the next else we undo the remove operation
			if (oll.size() > aux_oll.size()) {
			    cont=false;
			} else if (oll.size() == aux_oll.size()) {
			    //we look if we need remove more candidates to verify the constraint
			    graph.setVisitedAll(false);
			    candidates2=new LinkList();
			    if (!getPath(t,h,graph,candidates2)) {
				try{
				    graph.createLink(t2,h2,true);
				} catch (InvalidEditException iee){ };
			    }
			    candidates2=removeNonRemovableLinks(candidates2);
			    candidates.join(candidates2);
			} else {
			    //we undo the remove operation
			    try{
				graph.createLink(t2,h2,true);
			    } catch (InvalidEditException iee){};
			}
		    }//end while remove links
		}//end else
		
		//If the constraints number get smaller, we update the list of constraints
		if (!cont) {
		    if (aux_oll.size()>0) 
			cont=true;
		    oll=aux_oll.copy();
		} else {
		    System.out.println("ERROR: The partial order constraints doesn't get smaller");
		    System.exit(1);
		}
	    }

    }//end repairPartialOrderConstraints with metrics method
    /*---------------------------------------------------------------*/
    /**
     * This method remove graph cycles verifying constraints
     * @param graph graph to repair
     * @param metric metric used to score the best link to invert/remove
     */
    public void removeCycles(Graph graph) {

	Random r=new Random();//12345);
	int i=0;
	LinkList oll=new LinkList();
	Bnet aux=new Bnet(graph.getNodeList());

	//remove cycles
	if (!graph.isADag()) {
	    LinkList ll=graph.getLinkList();
	    int n=0;
	    //we search each cycle
	    while (n<ll.size()) {
		Link l=ll.elementAt(n);
		
		//we look if the link l is in a cycle
		LinkList candidates=new LinkList();
		
		//If there is a cycle, we break it, inverting and/or removing links
		if (getPath(l.getTail(),l.getHead(),graph,candidates)) {
		    if (candidates.getID(l.getTail().getName(),l.getHead().getName())==-1)
			candidates.insertLink(l);
		    n=0;
		    LinkList candidates2=candidates.copy();
		    
		    //we get the candidate links that we can invert
		    candidates=removeNonInvertibleLinks(candidates);
		    
		    //we try to invert links, the partial order constraints number can't get bigger
		    boolean cont=true;
		    while  ((candidates.size()>0) && cont) {
			//we get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			candidates.removeLink(link);
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());

			//we try to invert
			try{			
			    graph.removeLink(t2,h2);
					graph.createLink(h2,t2,true);
			} catch (InvalidEditException iee){};

			
			//Look is the order constrantis get bigget and if the cycle is broken
			aux=new Bnet(graph.getNodeList());
			oll=new LinkList();
			testRealPartialOrderConstraints(aux,oll);
			
			//If the order constranints are 0, and the cycle is removed, we go to the next cyle, else we 
			// undo the invert operation
			if ( (oll.size() ==0) && (!getPath(h2,t2,graph,oll))) {
			    cont=false;
			} else {
			    //undo the invert
			    try{
				graph.removeLink(h2,t2);
				graph.createLink(t2,h2,true);
			    } catch (InvalidEditException iee){};
			}
		    }//end while
		    
		    //If the cycle wasn't remover wit invert operations, we try to remove links
		    candidates=candidates2.copy();
		    candidates=removeNonRemovableLinks(candidates);
		    while  ( (candidates.size()>0) && cont ) {
			//get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());
			
			candidates.removeLink(link);
			
			//we try to remove it
			try{			
			    graph.removeLink(t2,h2);
			} catch (InvalidEditException iee){};
			
			//Look if the cycle is broken
			aux=new Bnet(graph.getNodeList());
			oll=new LinkList();
			if (!getPath(h2,t2,graph,oll)) {
			    //the cycle is removed, go to the next cyle
			    cont=false;
			} 
		    }//end while
		    
		}//end else
		else
		    n++;
		graph.setVisitedAll(false);

	    }//end for i
	}//end if isADag 
    }//end removeCycles method
    /*---------------------------------------------------------------*/
    /**
     * This method remove graph cycles verifying constraints
     * @param graph graph to repair
     * @param metric metric used to score the best link to invert/remove 
     */
    public void removeCycles(Graph graph, Metrics metric) {

	Random r=new Random();//12345);
	int i=0;
	LinkList oll=new LinkList();
	Bnet aux=new Bnet(graph.getNodeList());

	//remove cycles
	if (!graph.isADag()) {
	    LinkList ll=graph.getLinkList();
	    int n=0;
	    //we search each cycle
	    while (n<ll.size()) {
		Link l=ll.elementAt(n);
		
		//we look if the link l is in a cycle
		LinkList candidates=new LinkList();
		
		//If there is a cycle, we break it, inverting and/or removing links
		if (getPath(l.getTail(),l.getHead(),graph,candidates)) {
		    if (candidates.getID(l.getTail().getName(),l.getHead().getName())==-1)
			candidates.insertLink(l);
		    n=0;
		    LinkList candidates2=candidates.copy();
		    
		    //we get the candidate links that we can invert
		    candidates=removeNonInvertibleLinks(candidates);
		    
		    //we try to invert links, the partial order constraints number can't get bigger
		    boolean cont=true;
		    while  ((candidates.size()>0) && cont) {
			//we get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			candidates.removeLink(link);
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());
			
			//we try to invert it
			try{
			    graph.removeLink(t2,h2);
			    graph.createLink(h2,t2,true);
			} catch (InvalidEditException iee){};
			
			//Look is the order constrantis get bigget and if the cycle is broken
			aux=new Bnet(graph.getNodeList());
			oll=new LinkList();
			testRealPartialOrderConstraints(aux,oll);
			
			//If the order constranints are 0, and the cycle is removed, we go to the next cyle, else we 
			// undo the invert operation
			if ( (oll.size() ==0) && (!getPath(h2,t2,graph,oll))) {
			    cont=false;
			} else {
			    //undo the invert
			    try{
				graph.removeLink(h2,t2);
				graph.createLink(t2,h2,true);
			    } catch (InvalidEditException iee){};
			}
		    }//end while
		    
		    //If the cycle wasn't remover wit invert operations, we try to remove links
		    candidates=candidates2.copy();
		    candidates=removeNonRemovableLinks(candidates);
		    while  ( (candidates.size()>0) && cont ) {
			//get a candidate link randomly
			Link link=candidates.elementAt(r.nextInt(candidates.size()));
			Node t2=graph.getNodeList().getNode(link.getTail().getName());
			Node h2=graph.getNodeList().getNode(link.getHead().getName());
			
			candidates.removeLink(link);
			

			//we try to invert it and check if it's better invert the link than remove it
			double score_remove=0;
			double score_invert=0;
			try{
			    graph.removeLink(t2,h2);
			    score_remove=metric.score(new Bnet (graph.getNodeList()) );
			    graph.createLink(h2,t2,true);
			    score_invert=metric.score(new Bnet (graph.getNodeList()) );
			} catch (InvalidEditException iee){ };

			//if it's better remove, undo the invert and go next candidate link
			if (score_remove>score_invert) {
			    try{
				graph.removeLink(h2,t2);
				graph.createLink(t2,h2,true);
			    } catch (InvalidEditException iee){ };
			    continue;
			}
			
			//Look if the cycle is broken
			aux=new Bnet(graph.getNodeList());
			oll=new LinkList();
			if (!getPath(h2,t2,graph,oll)) {
			    //the cycle is removed, go to the next cyle
			    cont=false;
			} 
		    }//end while
		    
		}//end else
		else
		    n++;
		graph.setVisitedAll(false);

	    }//end for i
	}//end if isADag 
    }//end removeCycles with metric method
    /*---------------------------------------------------------------*/
    /**
     * This method repair a Bnet if this Bnet doesn't verify the constraints
     * @param bnet bnet to repair
     * @return repaired bnet.
     */
    public Bnet repair(Bnet bnet) {
	Graph graph=bnet.duplicate();
	graph.setKindOfGraph(Graph.MIXED);//Para que no falle con los ciclos
	

	//First, test the bnet 	
	if ( test(bnet) )
	    return new Bnet(graph.getNodeList());

	//repair the absence constraints that aren't verified
	repairAbsenceConstraints(graph);


	//repair the existence constraints that aren't verified	
	repairExistenceConstraints(graph);

	//repair the partial order constraints that aren't verified	
	repairPartialOrderConstraints(graph);

	//remove cycles
	removeCycles(graph);

	//Build a new Bnet with the graph that verify the constraints
	return new Bnet(graph.getNodeList());
	    
    }//end repair method

    /*---------------------------------------------------------------*/
    /**
     * This method repair a Bnet if this Bnet doesn't verify the constraints
     * @param bnet bnet to repair
     * @param metric metric used to score the best repaired bnet
     * @return repaired bnet.
     */
    public Bnet repair(Bnet bnet, Metrics metric) {
	Graph graph=bnet.duplicate();
	graph.setKindOfGraph(Graph.MIXED);//Para que no falle con los ciclos

	//First, test the bnet 	
	if ( test(bnet) )
	    return new Bnet(graph.getNodeList());

	//repair the absence constraints that aren't verified
	repairAbsenceConstraints(graph);

	//repair the existence constraints that aren't verified	
	repairExistenceConstraints(graph,metric);

	//repair the partial order constraints that aren't verified	
	repairPartialOrderConstraints(graph,metric);

	//remove cycles
	removeCycles(graph,metric);
	    
	//Build a new Bnet with the graph that verify the constraints
	return new Bnet(graph.getNodeList());
	    
    }//end repair method with metrics
    /*---------------------------------------------------------------*/
    /**
     * This method test the integrity of a Grapf, that is, that the 
     * siblings/children/parents list of each node is ok.
     * @param graph graph to test
     * @return true if the graph it's consistent with the list stored in the nodes, false in otre case
     */
    public boolean testIntegrity(Graph graph) {
	boolean out=true;
	LinkList ll=graph.getLinkList();
	NodeList nl=graph.getNodeList();

	for (int i=0; i < ll.size(); i++) {
	    Link l=ll.elementAt(i);
	    Node t=l.getTail();
	    Node h=l.getHead();

	    //First, look the tail node
	    LinkList p=t.getParents();
	    LinkList c=t.getChildren();
	    LinkList s=t.getSiblings();

	    //Look the nodelist links
	    LinkList pnode=nl.elementAt( (nl.getId(t.getName())) ).getParents();
	    LinkList cnode=nl.elementAt( (nl.getId(t.getName())) ).getChildren();
	    LinkList snode=nl.elementAt( (nl.getId(t.getName())) ).getSiblings();

	    if (!pnode.equals(p)) {
		System.out.println("Inconsistency in "+t.getName()+" : Parents in NodeList and LinkList are different");
		out=false;
	    }
	    if (!cnode.equals(c)) {
		System.out.println("Inconsistency in "+t.getName()+" : Children in NodeList and LinkList are different");
		out=false;
	    }
	    if (!snode.equals(s)) {
		System.out.println("Inconsistency in "+t.getName()+" : Siblings in NodeList and LinkList are different");
		out=false;
	    }

	    
	    for (int j=0;j<p.size();j++) {
		Link l2=p.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistency in "+t.getName()+" : A no existing parent="+l2);
		}
	    }//end for j of p
	    for (int j=0;j<c.size();j++) {
		Link l2=c.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistency in "+t.getName()+" : A no existing children="+l2);
		}
	    }//end for j of c
	    for (int j=0;j<s.size();j++) {
		Link l2=s.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistencyin "+t.getName()+" : A no existing sibling="+l2);
		}
	    }//end for j of s
		

	    //Second, look the head node
	    p=h.getParents();
	    c=h.getChildren();
	    s=h.getSiblings();

	    //Look the nodelist links
	    pnode=nl.elementAt( nl.getId(h.getName()) ).getParents();
	    cnode=nl.elementAt( nl.getId(h.getName()) ).getChildren();
	    snode=nl.elementAt( nl.getId(h.getName()) ).getSiblings();

	    if (!pnode.equals(p)) {
		System.out.println("Inconsistency in "+h.getName()+" : Parents in NodeList and LinkList are different");
		out=false;
	    }
	    if (!cnode.equals(c)) {
		System.out.println("Inconsistency in "+h.getName()+" : Children in NodeList and LinkList are different");
		out=false;
	    }
	    if (!snode.equals(s)) {
		System.out.println("Inconsistency in "+h.getName()+" : Siblings in NodeList and LinkList are different");
		out=false;
	    }


	    for (int j=0;j<p.size();j++) {
		Link l2=p.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistency in "+h.getName()+" : A no existing parent="+l2);
		}
	    }//end for j of p
	    for (int j=0;j<c.size();j++) {
		Link l2=c.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistency in "+h.getName()+": A no existing children="+l2);
		}
	    }//end for j of c
	    for (int j=0;j<s.size();j++) {
		Link l2=s.elementAt(j);
		if (ll.indexOf(l2) == -1) {
		    out=false;
		    System.out.println("Inconsistency in "+h.getName()+": A no existing sibling="+l2);
		}
	    }//end for j of s

	}//end for i


	return out;

    
    }//end testIntegrity method
    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
    * For performing tests
    */
    public static void main(String args[]) throws ParseException, IOException , InvalidEditException
    {
	//modo sin parámetro
	if (args.length < 1) {
	    
	    //Open the asia bnet
	    FileInputStream f = new FileInputStream("asia.elv");
	    Bnet bnet = new Bnet(f);
	    f.close();
	    
	    //Build the constraints
	    ConstraintKnowledge ck= new ConstraintKnowledge(bnet);
	    
	    //add constraints
	    Node A=(bnet.getNodeList()).getNode("A");//Tuberculosis
	    Node B=(bnet.getNodeList()).getNode("B");//Lung Cancer
	    Node C=(bnet.getNodeList()).getNode("C");//Tuberculosis or Cancer
	    Node D=(bnet.getNodeList()).getNode("D");//Positive X-ray
	    Node E=(bnet.getNodeList()).getNode("E");//Dyspnea
	    Node F=(bnet.getNodeList()).getNode("F");//Bronchitis
	    Node G=(bnet.getNodeList()).getNode("G");//Visit to Asia
	    Node H=(bnet.getNodeList()).getNode("H");//Smoker
	    
	    //Asian Bnet it's A->C, B->C, C->D, C->E, F->E, H->B, H->F, G->A
	    //Add existence constraints
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("\nAdding existence constraints ....");
	    if (   ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(A,C,true)) //A->C
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(B,C,true)) //B->C
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(C,D,true)) //C->D
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(C,E,true)) //C->E
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(F,E,true)) //F->E
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(H,B,true)) //H->B
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(H,F,false)) //H--F
		   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(G,A,true)) //G->A
	   //	   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(D,A,true)) //D->A //¡¡Directed Cycle!!
	   //	   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(D,A,false)) //D--A //¡¡Mixed Cycle!!
	   //	   && ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(E,H,false)) //E--H //¡¡Undirected Cycle!!
		   )
		System.out.println("Existence constraints added with success");
	    else
		System.out.println("Existence constraints added with fails");

	    //add absence constraints
	    System.out.println("\nAdding absence constraints ....");
	    if (   ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,B,false)) //A--B
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,D,false)) //A--D
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,E,false)) //A--E
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,F,false)) //A--F
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,G,true )) //A->G
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,H,false)) //A--H
		   
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(B,D,false)) //B--D
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(B,E,false)) //B--E
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(B,F,false)) //B--F
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(B,H,true )) //B->H
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(B,G,false)) //B--G
		   
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(C,G,false)) //C--D
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(C,F,false)) //C--F
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(C,H,false)) //C--H
		   
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(D,E,false)) //D--E
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(D,F,false)) //D--F
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(D,G,false)) //D--G
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(D,H,false)) //D--H
		   
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(D,C,true )) //D->C
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(E,C,true )) //E->C
		   && ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(H,G,false )) //H->G
		   )
		System.out.println("Absence constraints added with success");
	    else
		System.out.println("Absence constraints added with fails");
	    

	    //Add partial order constraints
	    System.out.println("\nAdding partial order constraints ....");
	    if (    ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(H,C,true )) //H<C
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(H,E,true )) //H<E
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(G,C,true )) //G<C
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(G,D,true )) //G<D
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(G,E,true )) //G<E
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(A,D,true )) //A<D
		    && ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(A,E,true )) //A<E
		    )
		System.out.println("Partial Order constraints added with success");
	    else
		System.out.println("Partial Order constraints added with fails");


	    //add erronous constraints
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("\nTesting consitencies in constraints. We try to add 7 constrainsts that arent't consistent");
	    
	    //Autoconsistency
	    //-with this existence contraint there is a cycle
	    if ( !ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(C,A,true)) )
		System.out.println("ERROR: This constraint 1 couldn't be added.");
	    
	    //Consistencies between existence and absence
	    //-there is a previuos absence contraint
	    if ( !ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(D,C,true)) )
		System.out.println("ERROR: This constraint 2 couldn't be added.");
	    if ( !ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(C,F,true)) )
		System.out.println("ERROR: This constraint 3 couldn't be added.");
	    if ( !ck.addConstraint(ConstraintKnowledge.EXISTENCE, new Link(C,F,false)) )
		System.out.println("ERROR: This constraint 4 couldn't be added.");
	    
	    //-there is a previuos existence contraint
	    if ( !ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,C,true)) )
		System.out.println("ERROR: This constraint 5 couldn't be added.");
	    if ( !ck.addConstraint(ConstraintKnowledge.ABSENCE, new Link(A,C,false)) )
		System.out.println("ERROR: This constraint 6 couldn't be added.");
	    
	    //Consistencies between existence and partial order
	    //-with this partial order contraint there is a cycle
	    if ( !ck.addConstraint(ConstraintKnowledge.PARTIALORDER, new Link(C,A,true)) )
		System.out.println("ERROR: This constraint 7 couldn't be added.");
	    
	    //test if a asia bnet verify the constraints
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("\nTesting asian Bnet.");
	    
	    if ( ck.test(bnet) )
		System.out.println("The original asian Bayesian Net verify the constraints.");
	    else
		System.out.println("ERROR: The original asian Bayesian Net doesn't verify the constraints.");
	    
	    //test if a modified asia bnet verify the constraints
	    System.out.println("\nTesting first modified asian Bnet.");
	    bnet.createLink(H, G, true); //it's a absence constraint
	    if ( ck.test(bnet) )
		System.out.println("ERROR: First modified asian Bayesian Net verify the constraints.");
	    else
		System.out.println("First modified asian Bayesian Net doesn't verify the constraints.");
	    
	    System.out.println("\nTesting second modified asian Bnet.");
	    bnet.removeLink(H, G); //Remove the absence constraint
	    bnet.removeLink(A, C); //Remove a existence constraint
	    if ( ck.test(bnet) )
		System.out.println("ERROR: Second modified asian Bayesian Net verify the constraints.");
	    else
		System.out.println("Second modified asian Bayesian Net doesn't verify the constraints.");
	    
	    //Save constraints
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("\nSaving asian Bnet constraints in: \n\tAsiaExistenceConstraints.elv, \n\tAsiaAbsenceConstraints.elv and \n\tAsiaPartialOrderConstraints.elv");
	    try {
		FileWriter fe = new FileWriter("AsiaExistenceConstraints.elv");
		FileWriter fa = new FileWriter("AsiaAbsenceConstraints.elv");
		FileWriter fo = new FileWriter("AsiaPartialOrderConstraints.elv");
		ck.save(fe,fa,fo);
		fe.close();
		fa.close();
		fo.close();
	    } catch(java.io.IOException ex) {
		System.out.println("Error saving constraints");
	    }
	    
	}//end with arguments test
	else  if(args.length != 4){
	    System.out.print("ERROR:Bad number of arguments. ");
	} else {
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("Reading Bnet"+args[0]);
	    //read the bnet
	    FileInputStream f = new FileInputStream(args[0]);
	    Bnet bnet = new Bnet(f);
	    f.close();

	    System.out.println("Reading Constraints "+args[1]+", "+args[2]+" and "+args[3]);
	    //Read the constraints
	    ConstraintKnowledge ck= new ConstraintKnowledge(args[1],args[2],args[3]);

	    //test the bnet
	    if ( ck.test(bnet) )
		System.out.println("The "+args[0]+" Bnet verify the constraints.");
	    else
		System.out.println("The "+args[0]+" Bnet DOESN'T verify the constraints.");

	    Bnet ibnet=ck.initialBnet();

	    //Save used constraints
	    System.out.println("\n------------------------------------------------------------------------");
	    System.out.println("\nSaving used asian Bnet constraints in: \n\tUsedExistenceConstraints.elv, \n\tUsedAbsenceConstraints.elv and \n\tUsedPartialOrderConstraints.elv");
	    System.out.println("\nSaving initial Bnet for learning");

	    try {
		FileWriter fe = new FileWriter("UsedExistenceConstraints.elv");
		FileWriter fa = new FileWriter("UsedAbsenceConstraints.elv");
		FileWriter fo = new FileWriter("UsedPartialOrderConstraints.elv");
		FileWriter fb = new FileWriter("InitialBnetForLearning.elv");
		ck.save(fe,fa,fo);
		ibnet.saveBnet(fb);fb.close();
		fe.close();
		fa.close();
		fo.close();
	    } catch(java.io.IOException ex) {
		System.out.println("Error saving constraints");
	    }

	}//end files mode test

	System.out.println("\n------------------------------------------------------------------------");
	System.out.println("This main method of the class can be used like follows:"+
			   "\t\n\tUsage: CosntraintKnowledge bnet.elv existenceConstraints.elv "+
			   "absenceConstraints.elv partialOrdeConstrainsts.elv"+
			   "\n\t(To test a bnet with constraints files)"+
			   "\n\n\tUsage: CosntraintKnowledge (without params)"+
			   "\n\t(To test with the asia bnet)");
	System.out.println("\n------------------------------------------------------------------------");
    }//End main
}//End ConstraintKnowledge class
