package elvira.inference.clustering.IncrementalCompilation;
import elvira.*;
import elvira.inference.clustering.*;
import java.util.ArrayList;

/**
 * ICModificationRemoveLink.java
 * This class implements the single modification of removing a link for Incremental Compilation process.
 *
 * Created:  23/12/2003
 *
 * @author  Julia
 * @version 1.0
 */
public class ICModificationRemoveLink extends ICModification{
    
    private Link myLink;
    static int kind = 0;
   
    //a variable that identifies M_Y
    NodeJoinTree My;
    
    /** This field will represent the relevant links for the modification 
     (L in the paper algorithms)*/
    public LinkList ll;
    
    /** It will be necessary to know the IncrementalCompilation we are using this modification for.
     *  With this information we can access the actual network, moral graph and both junction
     *  and MPS trees.
     */
     IncrementalCompilation myIC;

    
    /** Creates a new instance of ICModificationRemoveLink */
    public ICModificationRemoveLink() {

    }
    
   /** The constructor. It initializes the values for start the 
    *  modification actions. 
    * @param myL will indicate the <code>Link</code> we want to delete
    * @param myIC is the corresponding <code>IncrementalCompilation</code>
    */
    public ICModificationRemoveLink(Link myL,IncrementalCompilation ic) {
        
        if (debug)
        {
           System.out.println("My kind of class " + kind);
        }
        myLink = myL;
        myIC = ic;        
        
    }
    
    /** Method that updates the graph deleting this Link and the moral links it could have implied
    *  and which are no more necessary.
    *  @param g is the <code>Graph</code> we are going to modify
    *  @param GIC will be true just for particular generated experiments done by IC designers.
    *  @return the list of links that are related to this modification
    */
    public LinkList ModifyMoralGraph(Graph g,boolean GIC){
        if (GIC)
        {
         int pos,posT,posH;
         Link linkGC = myLink;
         //Get the exactly position of this link in the net
         pos = myIC.getBNET().getLinkList().getID(linkGC.getTail().getName(),linkGC.getHead().getName());
         if (pos!=-1)
         { myLink = myIC.getBNET().getLinkList().elementAt(pos);
         }
         else System.out.println("The link can't be remove because it doesn't belog to the graph");
        }
        return this.ModifyMoralGraph(g);
      }
    
    
    
    
    /** Method that updates the graph deleting this Link and the moral links it could have implied
    *  and which are no more necessary.
    *  @param g is the <code>Graph</code> we are going to modify
    *  @return the list of links that are related to this modification
    */
    public LinkList ModifyMoralGraph(Graph g){
        ll = new LinkList();
        //We need to know the directed links (taken from the network)
        LinkList dirLinks = myIC.getBNET().getLinkList();
        if (debug)
        { 
            System.out.println("We have entered ModifyMoralGraph in class ICModificationREMOVE_LINK");
        }
        Node nH,nT;
        int i;
        
        //nT is the tail node, X if we suppose we are deleting X --> Y link
        nT = myLink.getTail(); //X
        nH = myLink.getHead(); //Y
        
        //part 2.4.(a) in the paper
        //If both X and Y have no children in common
        if (nH.getChildrenNodes().intersectionNames(nT.getChildrenNodes()).size()==0)
        {
            //We can delete it directly
            int pos = g.getLinkList().getID(myLink.getHead().getName(),myLink.getTail().getName());
            try{
            //g.removeLink(myLink); This didn't work 
            //probably because g is undirected and the link is directed
            g.removeLink(g.getLinkList().elementAt(pos)); 
            myIC.getBNET().removeLink(myLink);
            }
            catch (InvalidEditException iee)
            {
                System.out.println("Problems when removing link " + iee);
            }
            //And add this link to the set of relevant ones
            ll.insertLink(myLink);
        }
        else //We have to remove it from the net and change it to moral in the graph
       {     //When we enter this else part is because the two nodes whose joining
             //link is going to be deleted have a child in common: then they should
             //be joined now by a moral link.
           
            int pos = g.getLinkList().getID(myLink.getHead().getName(),myLink.getTail().getName());
            try{
            //g.removeLink(myLink); //This didn't work, so we use position. It didn't work 
            //probably because g is undirected and the link is directed
            g.removeLink(g.getLinkList().elementAt(pos)); 
            //We create "manually" the corresponding moral link (undirected)
            g.createLink(g.getNodeList().getNode(myLink.getTail().getName()),g.getNodeList().getNode(myLink.getHead().getName()),false);
            //It is necessary to modify (delete the link) both the graph and the net.
            myIC.getBNET().removeLink(myLink);
            }
            catch (InvalidEditException iee)
            {
                System.out.println("Problems when removing link " + iee);
            }
        
        
        }
        //If they have common children, even if we delete the link in the directed grpah
        //It will appear as a moral link.
        
        //part 2.4.(b) in the paper
        NodeList parents = nH.getParentNodes();
        Node pNode;
        NodeList inters;
        Link l;
        for(i=0;i<parents.size();i++)
        {
            pNode = parents.elementAt(i);
            inters = nT.getChildrenNodes().intersectionNames(pNode.getChildrenNodes());
            //We have already deleted X--Y in the moral graph, so although the alg says intersection = {Y}
            //in our case we want an empty intersection, because the link has already been deleted
            if ((inters.size()==0) //if there is no more common children
            && (dirLinks.getID(pNode.getName(),nT.getName())==-1) //and they are
            && (dirLinks.getID(nT.getName(),pNode.getName())==-1) //not connected in the DAG
            )
            {
             try{ 
                 g.removeLink(g.getLinkList().elementAt(g.getLinkList().getID(nT.getName(),pNode.getName())));                  
                 //We don't delete it formn the network because moral links are not reflected
                 //there
                }
                catch (InvalidEditException iee)
                {
                 System.out.println("Problems when removing a moral link derived from link removal" + iee);
                }
                //This link has to be added to the set of relevan links
                ll.insertLink(new Link(nT,pNode,false));
            }
        }
        return ll;
    }
    
    
   /** Method that markes the affected MPS by this edge addition
    * following the algorithm explained in the paper for deletion of a link.
    */
    public void MarkAffectedMPSs(JoinTree JT, JoinTree MPST, ArrayList MM){
        //in the paper we did MarkAffectedMPSsByRemoveLink(My,nil,L) and remember this is recursive
        //Here L is a field of the class (ll) and we need to control both JT and MPST
         My = myIC.getCliqueWithFamily(myLink.getHead()).getCorrespondingMPS();
         MarkAffectedMPSsByRemoveLink(My,null,MM);
        
    }
   
    /** Recursive method first called from the previous one:
     *  @param My is the <code>NodeJoinTree</code> that represents the MPS from which 
     *  we are start looking for the connection
     *  @param Mz is the <code>NodeJoinTree</code> that previously did this recursive call
     *  @param markedM is the list of MPS we are marking through this process
     */
      public void MarkAffectedMPSsByRemoveLink(NodeJoinTree My,NodeJoinTree Mz,ArrayList markedM){
        int i;
        
        //1st step.- Mark My, if it isn't yet.
        if (!My.getIsMarked())
        { My.setIsMarked(true);
          markedM.add(My);
        }
        
        //2nd step
        //For all neighbours Mk <> Mz of My do
        NeighbourTreeList neighList = My.getNeighbourList();
        NodeList separatorYK;
        NodeJoinTree Mk;
        
        for(i=0;i<neighList.size();i++)
        {
            Mk = neighList.elementAt(i).getNeighbour();
            if (Mz==null || Mk.getLabel()!=Mz.getLabel())
            {
                //(a) SyK <-- separator between My and Mk
               separatorYK = My.getVariables().intersection(Mk.getVariables()); 
               
               //(b) if L \cap links(Syk) <> empty then
               if (commonPart(separatorYK,ll))
               {
                if (!Mk.getIsMarked()) //for avoiding cycles
                   MarkAffectedMPSsByRemoveLink(Mk,My,markedM);
                } 
            }
        }
    }

    /** From a separator (NodeList) and a set of moral links (LinkList)
     * we take the common part, that is, we are interested if two nodes
     * contained in the separator are those involved in any of the links 
     * inside the links set. (auxiliar function)
     */
    private boolean commonPart(NodeList sep, LinkList moralS)
    {
        int i;
        Link currentLink;
        boolean common = false;
        for(i=0;i<moralS.size();i++)
        {
            currentLink = moralS.elementAt(i);
            /* If the separator contains the head and the tail
             * of one link, there is a common part
             */
            if ((sep.getId(currentLink.getTail().getName())!=-1)&&
               (sep.getId(currentLink.getHead().getName())!=-1))
            {  common=true;
               break;
            }
        }
        return common;
    }
    
   
    
}
    

