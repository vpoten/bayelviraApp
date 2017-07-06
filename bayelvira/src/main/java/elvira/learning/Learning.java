/* Learning.java */

package elvira.learning;

import elvira.Bnet;
import elvira.Graph;
import elvira.Node;
import elvira.Link;
import elvira.NodeList;
import elvira.LinkList;
import elvira.InvalidEditException;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * Learning.java
 * This class implements the Learning Algorithms.
 *
 * Created: Tue May 11 12:09:55 1999
 *
 * @author P. Elvira
 * @version 1.0
 */


public abstract class Learning  {
    
    private Bnet output; // The output of a Learning Algorithm.

    public static void main(String args[]) 
        throws ParseException, IOException{ 
      if(args.length < 1){
        System.out.println("Too few arguments. Usage: file.dbc");
        System.exit(0);
      }
      else{
        FileInputStream f = new FileInputStream(args[0]);
        DataBaseCases cases = new DataBaseCases(f);
        System.out.println(cases.getNodeList().toString2());      
      }
    }
    
    /**
     * This method carries out the learning proccess.
     */

    abstract public void learning();

    /** Access methods ***/
    
    public Bnet getOutput(){
	return output;
    }

    public void setOutput(Bnet b){
	output = b;
    }
	

	
    /**
     * This method compare the output of the learning algorithm 
     * with the true bayes net used as input in the 
     * learning process.
     * @param Bnet. the true bayes net to be compared.
     * @return LinkList[3]. [0]= the links added in the learning process.
     *                      [1]= the links deleted in the learning process.
     *                      [2]= the links wrong oriented in the output net.
     */

   public LinkList[] compareOutput(Bnet b){

	LinkList addel[] = new LinkList[3];
	int i,pos1,pos2;
	NodeList orderednodesb,orderednodesbap;
	LinkList reversiblesb,reversiblesbap,linksbcopy,linksbapcopy,linksadd,linksdel,linkschange;
	Link link,linkbap,link2,linkbap2;
	Node nodeT,nodeH;
	Graph bcopy = (Graph) b.duplicate();
	bcopy.setKindOfGraph(2);
        Graph bapcopy = (Graph) getOutput().duplicate();
	bapcopy.setKindOfGraph(2);

	orderednodesb = bcopy.topologicalOrder();
	reversiblesb = bcopy.reversibleLinks(orderednodesb);
	orderednodesbap = bapcopy.topologicalOrder();
	reversiblesbap = bapcopy.reversibleLinks(orderednodesbap);
	for(i=0 ; i< reversiblesb.size(); i++)
	 {
	    link = (Link) reversiblesb.elementAt(i);
	    nodeT =(Node)link.getTail();
	    nodeH =(Node)link.getHead();
	    try{
                bcopy.removeLink(link);
                bcopy.createLink(nodeT,nodeH,false);
               }catch(InvalidEditException iee){};
	  }
	for(i=0 ; i< reversiblesbap.size(); i++)
	 {
	    link = (Link) reversiblesbap.elementAt(i);
	    nodeT =(Node)link.getTail();
	    nodeH =(Node)link.getHead();
	    try{
                bapcopy.removeLink(link);
                bapcopy.createLink(nodeT,nodeH,false);
               }catch(InvalidEditException iee){};
	  }

	linksbcopy = bcopy.getLinkList();
        linksbapcopy = bapcopy.getLinkList();

	linksadd = new LinkList();
	linksdel = new LinkList();
	linkschange = new LinkList();

	for(i=0 ; i< linksbcopy.size(); i++)
           {
	    link = (Link) linksbcopy.elementAt(i);
	    nodeT =(Node)link.getTail();
	    nodeH =(Node)link.getHead();
	    linkbap = bapcopy.getLink(nodeT,nodeH);
	    linkbap2 = bapcopy.getLink(nodeH,nodeT);
	    if ((linkbap != null) || (linkbap2 != null))
	       if (linkbap != null)
	          if (link.getDirected())
		     if (!linkbap.getDirected()) linkschange.insertLink(link);
	             else
		     {if (nodeH.equals(linkbap.getTail()) && nodeT.equals(linkbap.getHead()))
		          linkschange.insertLink(link);
		     }
	          else
		  {if (linkbap.getDirected()) linkschange.insertLink(link);
		  }
	       else //linkbap2 is not null
	          if (link.getDirected())
		     if (!linkbap2.getDirected()) linkschange.insertLink(link);
	             else
		     {if (nodeH.equals(linkbap2.getTail()) && nodeT.equals(linkbap2.getHead()))
		          linkschange.insertLink(link);
		     } 
	          else
		  {if (linkbap2.getDirected()) linkschange.insertLink(link);
		  }
	    else linksdel.insertLink(link);
	   }

	for(i=0 ; i< linksbapcopy.size();i++)
	   {
	    linkbap = (Link) linksbapcopy.elementAt(i);
	    nodeT =(Node)linkbap.getTail();
	    nodeH =(Node)linkbap.getHead();
	    link = bcopy.getLink(nodeT,nodeH);
	    link2 = bcopy.getLink(nodeH,nodeT);
	    if ((link == null) && (link2 == null)) linksadd.insertLink(linkbap);
	   }

	addel[0]=linksadd;
	addel[1]=linksdel;
	addel[2]=linkschange;
	return addel;

   }//end compareOutput method


} // Learning
