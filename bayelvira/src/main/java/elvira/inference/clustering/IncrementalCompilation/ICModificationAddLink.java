package elvira.inference.clustering.IncrementalCompilation;

import elvira.inference.clustering.*;
import elvira.*;
import java.util.ArrayList;
import java.util.Vector;
import elvira.potential.*;

/**
 * ICModificationAddLink.java
 * This class implements the single modification of adding a link for Incremental Compilation process. 
 *
 * Created:  23/12/2003
 *
 * @author julia
 * @version 1.0
 */


public class ICModificationAddLink extends ICModification{

   /** The link we are going to introduce in the network
    */
    private Link myLink;
    
    /* This kind of modification is identified by kind 1*/
    static int kind = 1;

    /** This field will represent the relevant links for the modification 
     (L in the paper algorithms)*/
    public LinkList ll;
    
    /** It will be necessary to know the IncrementalCompilation we are using this modification for.
     *  With this information we can access the actual network, moral graph and both junction
     *  and MPS trees.
     */
     IncrementalCompilation myIC;
   
    /** Creates a new  empty instance of ICModificationAddLink 
     */
    public ICModificationAddLink() {
        
    }
    
   /** The constructor. It initializes the values for start the 
    *  modification actions. 
    * @param myL will indicate the <code>Link</code> we want to add
    * @param myIC is the corresponding <code>IncrementalCompilation</code>
    */
   public ICModificationAddLink(Link myL,IncrementalCompilation ic) {
       
       //debugging
       if (debug)
       { 
           System.out.println("My kind of class " + kind);
       }
       //Just initialisation of the main two fields: IC and the added link
        myLink = myL;
        myIC = ic;
    }
    
    
    
   /** By this method we make the corresponding modifications to the moral
    *  graph.
    * @param g will indicate the <code>Graph</code> we are working with
    * @param GIC is only used by the programmer for experiments' work by means
    * of automatic generation of incremental cases. Default users should set GIC 
    * to false or just call the default method with only one parameter.
    */
  
    public LinkList ModifyMoralGraph(Graph g,boolean GIC){
     if (GIC)
     {
        int posT,posH;
        Node nodeT= new FiniteStates(),nodeH= new FiniteStates();
        posT = myIC.getBNET().getNodeList().getId(myLink.getTail().getName());
        if (posT!=-1)
            nodeT = myIC.getBNET().getNodeList().elementAt(posT);
        else System.out.println("Error. We can't add a link whose nodes don't exist yet");
        
        posH = myIC.getBNET().getNodeList().getId(myLink.getHead().getName());
        if (posH!=-1)
            nodeH = myIC.getBNET().getNodeList().elementAt(posH);
        myLink = new Link (nodeT,nodeH,true);
     }
     return this.ModifyMoralGraph(g);
    }
          
    
    
   /** Method that updates the graph adding this new Link and the moral links it could imply.
    *  @param g is the <code>Graph</code> we are going to modify
    *  @return the list of links that are related to this modification
    */
    public LinkList ModifyMoralGraph(Graph g){
    
        int j,k;
        //Creates an empty list of relevant links we are going to search next.
        ll = new LinkList();
        //debugging  
        if (debug)
        { System.out.println("We have entered ModifyMoralGraph in class ICModificationADD_LINK");
        }
        
        //We have to modify Gm including X-->Y and all the moral links implied by this
        try
        {
          //adds the link in the network (directed graph)
           myIC.getBNET().createLink(myLink.getTail(),myLink.getHead(),true);
          //------------------------------------------------------------------
          //Be careful with this "dirty" solution. The problem was that
          //adding the link in the dirG adds also the corresponding parents
          //and childrens, BUT not the link in the linkList
          //On the other hand, we cannot add a directed link in an undirected graph
          //at least with the function createLink
          //------------------------------------------------------------------
          //and adds the link in the moral graph (undirected graph)
           int posUndT, posUndH;
           Link newL;
           posUndT = g.getNodeList().getId(myLink.getTail().getName());
           posUndH = g.getNodeList().getId(myLink.getHead().getName());
        
           //If there was a moral link remove it and change it for directed
           newL = g.getLink(g.getNodeList().elementAt(posUndT),g.getNodeList().elementAt(posUndH));
           if (newL!=null) g.removeLink(newL);
           newL = new Link(g.getNodeList().elementAt(posUndT),g.getNodeList().elementAt(posUndH),false);
           g.getLinkList().insertLink(newL);
           
           g.getNodeList().elementAt(posUndT).getChildren().insertLink(newL);
           g.getNodeList().elementAt(posUndH).getParents().insertLink(newL);
           
        } catch (InvalidEditException iee)
        {
            System.out.println("An error ocurred when adding a link in the graph " + iee);
        }
        
        //This link has to be included in the relevant links set
        ll.insertLink(new Link(myLink.getTail(),myLink.getHead(),true));
        
        //Now let's introduce the moralisations this new directed link can produce
        Node directedHead = myIC.getBNET().getNodeList().getNode(myLink.getHead().getName());
        
        NodeList nodeParents = directedHead.getParentNodes();
        
        //We assume that moral glinks produce siblings, never parents-
        //Otherwise this wouldn´t work. Imagine A-->B C-->B  then A--C
        //But if now we make X-->A, there is no need of joining C and X
        Node n1,n2;
        for (j=0 ; j<nodeParents.size()-1 ; j++) {
	  n1 = (Node) nodeParents.elementAt(j);
          for (k=j+1 ; k<nodeParents.size() ; k++) {
	    n2 = (Node) nodeParents.elementAt(k);
	    if ((g.getLink(n1,n2)==null) &&
		(g.getLink(n2,n1)==null))
            { 
               try
               {  
                   n1 = g.getNodeList().getNode(n1.getName());
                   n2 = g.getNodeList().getNode(n2.getName());
                   g.createLink(n1,n2,false);
               } catch (InvalidEditException iee)
               {
                 System.out.println("An error ocurred when adding derived moral links (add a link in the graph) " + iee);
               }
              
              ll.insertLink(new Link(n1,n2,false));
              //maybe we should do Link l = new Link(n1,n2,false) and use
              //it in the two previous sentences
             }
           }
        }
        return ll;
    }
    
    
/** Method that marks the affected MPS by this edge addition
 * following the algorithm explained in the paper for addition of a link.
 */
    public void MarkAffectedMPSs(JoinTree JT, JoinTree MPST, ArrayList MM){
        int i,j,digits;
        Integer number;
        Node nX,nY;
        NodeJoinTree Mx,My;
        Link linkXY;
        
                
        //Theoretical algorithm taken from UAI paper 2003
        //For each link X --> Y in L do
        for(i=0;i<1;i++) //ll.elementAt(0) is myLink because of ModifyMoralGraph 
        {
          linkXY=ll.elementAt(i);
          nX = linkXY.getTail();
          nY = linkXY.getHead();
          
          //1.- Let Mx be the nearest neighbor to My containing X.
          //Locate MPS with family of Y
          My = myIC.getCliqueWithFamily(nY).getCorrespondingMPS();
          
          //If we do not enter next if, My has to be marked anyway.
          if (!My.getIsMarked())
          {       My.setIsMarked(true);
                  MM.add(My);
          }
          
         if (My.getVariables().getId(nX)==-1) //It is in the same MPS
         {
          //Now the nearest neighbor to My containing X
          ArrayList path = new ArrayList();
          
          path = locateNNcontaining(My,nX,MPST);
          
          int NN = ((NodeJoinTree)path.get(path.size()-1)).getLabel();
          
          //Debugging
          if (debug)
          {
            System.out.println("The nearest neighbour with X is " + NN);
          }
        
        
          /******** to compute nums and disc(from disconnection) ********************/
            
           int [] nums = new int[path.size()];
           boolean disc = false;
           for(i=0;i<path.size();i++)
           { 
            nums[i]=((NodeJoinTree)path.get(i)).getLabel();
            if (nums[i]==0) //If there is a disconnection, that will be linked to node labelled as 0
            {
               NodeJoinTree root = ((NodeJoinTree)path.get(i));
               NodeJoinTree nex2it;
               int n1;
               
               //check before 0
               if (i>0)
               {
                   nex2it = ((NodeJoinTree)path.get(i-1));
                   n1 = root.getNeighbourList().indexOf(nex2it);
                   if (root.getNeighbourList().elementAt(n1).getMessage().getVariables().size()==0)
                       disc = true;
               }
               //check after 0
               if (!disc&&(i<path.size()-1))
               {
                   nex2it = ((NodeJoinTree)path.get(i+1));
                   n1 = root.getNeighbourList().indexOf(nex2it);
                   if (root.getNeighbourList().elementAt(n1).getMessage().getVariables().size()==0)
                       disc = true;
                   
               }
           }
       }
          /*********************************************************/
       
                 
          //2.- If there is an empty separator S on the path between Mx and My then
          //(a) Disconnect Tmpd and delete S
          //(b) Connect Mx to My by an (artificial) separator containing X
          //==> we do that by means of the boolean value of dis returned by the previous call
        
          //3.- Mark Mx, My and all Mz on the path between them
          //debugging
          if (debug)
          {
            System.out.println("path is ");
            for(i=0;i<nums.length;i++)
                System.out.print(nums[i]);
            System.out.println("disc is " + disc);
          }
          int count,before=-1,after=-1;
        
          for(count=0;count<nums.length;count++)
           {
            if (disc && nums[count]==0 && count>0)
                before = nums[count-1];
            if (disc && count>1 && nums[count-1]==0)
                after = nums[count];
           }
          
          
          //debugging
          if (debug)
          {
           System.out.println("before value is " + before);
           System.out.println("after value is " + after);
          }
          
          
         /*----------------------- 
          EXCEPTIONAL CASE: THE NODE IS INVOLVED IN THE OPERATION 
         -------------*/
           if ((disc)&&(nums.length==2)&&(after==-1)&&(before==-1)&&(My.getLabel()==0||NN==0))
           {
               if (debug)
               { System.out.println("Disconnexion with the root (involved in adding this link)");
               }
               //Now, they are already connected by an empty separator, but we have to connect
               //them with the appropiate separator.
               
               //If size is 2
               if (My.getLabel()==0)
               { 
               if (nums.length==2)
               {
                   //1.Change the separador from empty to X 
                   int n1 = My.getNeighbourList().indexOf(MPST.elementAt(nums[1]));
                   NodeJoinTree Mx1 = My.getNeighbourList().elementAt(n1).getNeighbour();
                   
                   Vector v1 = new Vector();
                   v1.addElement(nX);
                   
                   My.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                   n1 = Mx1.getNeighbourList().indexOf(My);
                   Mx1.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                   
                   //2.Connect the JT cliques to the same node
                   
                   //Cx1 is a clique that will contain nX
                   NodeJoinTree Cx1 = new NodeJoinTree();
                   int j1;
                   for(j1=0;j1<Mx1.getCliques().size();j1++)
                   {
                    Cx1 = (NodeJoinTree) Mx1.getCliques().get(j1);
                    if (Cx1.getVariables().getId(nX)!=-1)
                      break;
                   }
                   
                   //Cy1 is a clique that will contain nY
                   NodeJoinTree Cy1 = new NodeJoinTree();
                   for(j1=0;j1<My.getCliques().size();j1++)
                   {
                    Cy1 = (NodeJoinTree) My.getCliques().get(j1);
                    if (Cy1.getNodeRelation().getVariables().getId(nY)!=-1)
                     break;
                   }
                   
                   //If Cy1 is the root, as happened before.
                   if (Cy1.getLabel()==0)
                   {
                       n1 = Cy1.getNeighbourList().indexOf(Cx1);
                       Cy1.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                       n1 = Cx1.getNeighbourList().indexOf(Cy1);
                       Cx1.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                   }
                   else
                   {
                       n1 = Cx1.getNeighbourList().indexOf(JT.elementAt(0));
                       if (n1!=-1)
                       {
                           Cx1.removeNeighbour(JT.elementAt(0));
                           JT.elementAt(0).removeNeighbour(Cx1);
                       }
                       
                       //The same, but previously disconnect from root, if necessary
                       n1 = Cy1.getNeighbourList().indexOf(Cx1);
                       Cy1.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                       n1 = Cx1.getNeighbourList().indexOf(Cy1);
                       Cx1.getNeighbourList().elementAt(n1).setMessage(new Relation(v1));
                   }
                
                     if (!My.getIsMarked())
                     {      
                         My.setIsMarked(true);
                         MM.add(My);
                     }
                     if (!Mx1.getIsMarked())
                     {
                       Mx1.setIsMarked(true);
                       MM.add(Mx1);
                     }
                   
                   
                   
                }
               }
           }
           else if (disc)
           {
               int n;
               
              //We have before and after
              //If before disconnected, then remove empty separator with root
              NodeJoinTree root = MPST.elementAt(0);
              NodeJoinTree rootJT = JT.elementAt(0);
                        
              
              //The change of order after and before made on purpose
              //to avoid some possible problems.
              if (after!=-1)
              {
              //If after disconnected, then remove empty separator with root
              n = root.getNeighbourList().indexOf(MPST.elementAt(after));
              if (n!=-1)
              {
                  if (root.getNeighbourList().elementAt(n).getMessage().getVariables().size()==0)
                  {
                      if (root.getCliques().indexOf(rootJT)==-1)
                          System.out.println("Error: bad disconnection");
                      else 
                      {
                          NodeJoinTree nJT,nMT;
                          int n2;
                          nMT = MPST.elementAt(after);
                          for(j=0;j<nMT.getCliques().size();j++)
                          { nJT = (NodeJoinTree) nMT.getCliques().get(j);
                            n2 = rootJT.getNeighbourList().indexOf(nJT);
                            if ((n2!=-1)&&(rootJT.getNeighbourList().elementAt(n2).getMessage().getVariables().size()==0))
                            {  rootJT.getNeighbourList().removeElementAt(n2);
                               nJT.removeNeighbour(rootJT);
                               break;
                            }
                          }
                          
                      }
                      
                      root.getNeighbourList().removeElementAt(n);
                      MPST.elementAt(after).removeNeighbour(root);
                      
                  }
                  else after = -1;
              }
              }
              
              
              
              
              if (before!=-1)
              { 
              //Attention!!! ==> if a node is removed it can happen 
              //that the MPST with label K is not anymore at position K
              n = root.getNeighbourList().indexOf(MPST.elementAt(before));
              
              if ((after==-1)&&(n!=-1))
              {
                  if (root.getNeighbourList().elementAt(n).getMessage().getVariables().size()==0)
                  {
                      if (root.getCliques().indexOf(rootJT)==-1)
                          System.out.println("Error: bad disconnection");
                      else 
                      {
                          NodeJoinTree nJT,nMT;
                          int n2;
                          nMT = MPST.elementAt(before);
                          for(j=0;j<nMT.getCliques().size();j++)
                          { nJT = (NodeJoinTree) nMT.getCliques().get(j);
                            n2 = rootJT.getNeighbourList().indexOf(nJT);
                            if ((n2!=-1)&&(rootJT.getNeighbourList().elementAt(n2).getMessage().getVariables().size()==0))
                            {  rootJT.getNeighbourList().removeElementAt(n2);
                               nJT.removeNeighbour(rootJT);
                               break;
                            }
                          }
                          
                      }
                      root.getNeighbourList().removeElementAt(n);
                      MPST.elementAt(before).removeNeighbour(root);
                      
                      
                  }
              }
              }
              
              //Connect My and Mx, and also Cy and Cx
              Mx=MPST.elementAt(NN);
              NodeJoinTree Cx = new NodeJoinTree();
              
              if (!isThereAPathIterative(My,Mx,MPST.size()))
              {
                if (debug)
                { System.out.println("We connect My and Mx because they were disconnected");
                }
                for(j=0;j<Mx.getCliques().size();j++)
                {
                  Cx = (NodeJoinTree) Mx.getCliques().get(j);
                  if (Cx.getVariables().getId(nX)!=-1)
                      break;
                }
                if (j>Mx.getCliques().size())
                { System.out.println("Problems searching Cx");}
                NodeJoinTree Cy = new NodeJoinTree();
                for(j=0;j<My.getCliques().size();j++)
                {
                  Cy = (NodeJoinTree) My.getCliques().get(j);
                  if (Cy.getNodeRelation().getVariables().getId(nY)!=-1)
                  {break;}
                 }
                  
                 if (j>My.getCliques().size())
                 { System.out.println("Problems searching Cy");}
                 My.insertNeighbour(Mx);
                 Mx.insertNeighbour(My);
              
                 Cy.insertNeighbour(Cx);
                 Cx.insertNeighbour(Cy);
              
              
                 //artificial separator
              
                 Vector v = new Vector();
                 v.addElement(nX);
                 My.getNeighbourList().elementAt(My.getNeighbourList().size()-1).setMessage(new Relation(v));
                 Mx.getNeighbourList().elementAt(Mx.getNeighbourList().size()-1).setMessage(new Relation(v));
             
                 //Messages are set also in the JT
                 Cy.getNeighbourList().elementAt(Cy.getNeighbourList().size()-1).setMessage(new Relation(v));
                 Cx.getNeighbourList().elementAt(Cx.getNeighbourList().size()-1).setMessage(new Relation(v));
                 
                 if (!My.getIsMarked())
                {      
                  My.setIsMarked(true);
                  MM.add(My);
                }
                if (!Mx.getIsMarked())
                {
                 
                  Mx.setIsMarked(true);
                  MM.add(Mx);
                 }
              } //end of if_there_is_no_path
              else 
              { 
                if (debug)
                  System.out.println("There exists already a path, do not provoke cycles.");
                for(j=0;j<nums.length;j++)
                {   
                    if (!MPST.elementAt(nums[j]).getIsMarked())
                    { MPST.elementAt(nums[j]).setIsMarked(true);
                      MM.add(MPST.elementAt(nums[j]));
                   }
                }
              }
                                          
          }
            
          else 
          for(j=0;j<nums.length;j++)
          {   
              if (!MPST.elementAt(nums[j]).getIsMarked())
              { MPST.elementAt(nums[j]).setIsMarked(true);
                MM.add(MPST.elementAt(nums[j]));
              }
          }
          
        }
       }
       
 }


 /* Method that finds the nearest MPS containing node X. 
  *  @param My is the <code>NodeJoinTreeh</code> is the initial subgraph from which we start the search
  *  @param X is the <code>Nodeh</code> we are trying to find
  *  @param MT is the MPSTree structure where this path is need to be located 
  *  @return the obtained path as an ArrayList of Subgraphs
  */
 private ArrayList locateNNcontaining(NodeJoinTree My,Node X,JoinTree MT)
 {
     NodeJoinTree Mx = myIC.getCliqueWithFamily(X).getCorrespondingMPS();
     int treeS = MT.size();
     ArrayList path = new ArrayList();
     int counter = 0;
     int top = 0;
     int i,pointer;
     NodeJoinTree[] queue= new NodeJoinTree[treeS];
     int[] previous = new int[treeS];
     boolean found = false;
     NodeJoinTree current;
     NeighbourTreeList ntl;
     NodeJoinTree neigh;
     
     //intiliaze the queue
     queue[top] = My;
     previous[top]=-1;
     top++;
     
     while (!found) //&&(counter<top)
     {
         current = queue[counter];
         ntl = current.getNeighbourList();
         for(i=0;i<ntl.size();i++)
         {
             neigh = ntl.elementAt(i).getNeighbour();
             if ((previous[counter]==-1)||(queue[previous[counter]]!=neigh))
             {
                 if (neigh.getLabel()==Mx.getLabel()) //neigh==Mx
                 {
                     found = true;
                 }
                 else if (neigh.getVariables().getId(X.getName())!=-1)
                      found = true;
                 queue[top] = neigh;
                 previous[top] = counter;
                 top++;
             }
             if (found) break;
         }counter++;
     }
     
     if (found)
     { pointer = top -1;
       path.add(0,queue[pointer]);
       while (previous[pointer]!=-1)
       {
           pointer = previous[pointer];
           path.add(0,queue[pointer]);       
       }
       return path;
       
     }
     else 
     { System.out.println("Error. There is no path from MPS "+ My.getLabel() +" to another one containing " + X.getName());
            return null;
     }
  }


     
    
    /* This is an auxiliary function that verifies if there is a path from a source node to a target one.
     * Here we have the Recursive version.
     */
    public boolean isThereAPathRecursive(NodeJoinTree source,NodeJoinTree target,int previous)
    {
        NeighbourTreeList ntl = source.getNeighbourList();
        int i;
        NodeJoinTree neigh;
        
        if (source.getNeighbourList().indexOf(target)!=-1)
            return true;
        else 
        for(i=0;i<ntl.size();i++)
        {
            neigh = ntl.elementAt(i).getNeighbour();
            if (neigh.getLabel()==target.getLabel())
               return true;
            else if (neigh.getLabel()!=previous)
                if (isThereAPathRecursive(neigh,target,source.getLabel()))
                    return true;
        }
        return false;
    }
     
    /*Iterative version of the previous function*/
    public boolean isThereAPathIterative(NodeJoinTree source,NodeJoinTree target,int tsize)
    {
        //tsize = MPST.size();
        NodeJoinTree[] queue = new NodeJoinTree[tsize];
        int[] previousN = new int[tsize];
        int counter = 0;
        int pointer = 0;
        
        NeighbourTreeList ntl;
        int i;
        NodeJoinTree currentN,neigh;
        boolean found = false;
        boolean brokenPath = false;
        
        queue[counter] = source;
        previousN[counter] = -1;
        counter++;
        while ((pointer<tsize)&&(!found)&&(!brokenPath))
        {
            if (pointer>=counter)
            {
                brokenPath = true;
                break;
            }
            currentN = queue[pointer];
            if (currentN.getLabel()==target.getLabel())
            {
                found = true;
                break;
            }
            ntl = currentN.getNeighbourList();
            for(i=0;i<ntl.size();i++)
            {
                   neigh =  ntl.elementAt(i).getNeighbour();
                   if (neigh.getLabel()!=previousN[pointer])
                   { if (counter>=queue.length)
                         System.out.println("counter vale " + counter + " y la long  es " + tsize);
                     queue[counter] = neigh;
                     previousN[counter] = currentN.getLabel();
                     counter++;
                   }
            }
            pointer++;
         }
        
        if (brokenPath) return false;
        else return found;
        
    }
        
    
    
    
      /**
     * Method that finds the subgraph that contains X nearest to the one containing family of Y 
     * (remember the modification is adding link X --> Y)
     * @param famY is the index (position in joinTreeNodes) of the subgraph that contains Y
     * @param X is the tail node
     * @param MT is the MPS tree
     * @param pathV will contain the different information we need after this call:
     * the complete path (by a String), digits (the length of every word in this String represting
     * a MPS, and if there is or not a disconnection in this path.
     * @return the index (position in joinTreeNodes) of the nearest neighbour containing X
     */
   private int locateNearestMPScontaining(int famY,Node X,JoinTree MT,ArrayList pathV)
   {
       //The idea is to work using levels:
       //0.- first, the same MPS
       //1.- first the proper neighbours of famY
       //2.- the neighbours of the neighbours
       //.....
       //until (one is found) OR (all has been visited==> it is supposed to be impossible)
       int i,j,current,counter=0,max=0,label;
       boolean found = false;
       int treeSize = MT.size();
       //The nodes we have to visit next, those that will be finally returned are accumulated in this array
       int[] queueOfNodes2Visit = new int[treeSize];
       //The nodes that leaded to visit  queueOfNodes2Visit[i]
       int[] previous = new int[treeSize];
       //The String resulting from the path already done until queueOfNodes2Visit[i]
       String[] queueOfNodes2VisitString = new String[treeSize];
       //If this resulting path in queueOfNodes2Visit[i] has a disconnection or not
       boolean[] disc = new boolean[treeSize];
       
       NodeJoinTree MPSNode= new NodeJoinTree(),MPSNeigh;
       NeighbourTreeList nt;
       String path;
       ArrayList visited = new ArrayList();
       int digits = computeDigits(treeSize);
       Relation r;
       boolean d;
               
       //For all elements in the tree
       for(i=0;i<treeSize;i++)
       { //Add an associated String
         queueOfNodes2VisitString[i]=new String("");
         //Make it as not disconnected first
         disc[i] = false;
         //None got to it yet
         previous[i] = -1;
       }
       
       //First node to be studied: the one we start from (container of family of Y)
       queueOfNodes2Visit[max] = famY;
       queueOfNodes2VisitString[max] = new String("");
       max++;
       current = 0;
       
       //debugging
       if (debug)
       {
         System.out.println("treeSize vale " + treeSize);
       }
      //While we do not find the nearest neighbour and there are
      //still new subgraphs to explore
       while ((!found)&&(counter<treeSize))
       {
          //Take current subgraph of the list
          current = queueOfNodes2Visit[counter];
          MPSNode = MT.elementAt(current);
          //If it contains X we have found what we werw looking for
          if (MPSNode.getVariables().getId(X.getName())!=-1)
          {      if (MPSNode.getVariables().intersectionNames(MT.elementAt(previous[counter]).getVariables()).size()==0)
                     disc[counter] = true;   
                 found = true;
          }
          else //otherwise, include its neighbours in the list
          {
              nt = MPSNode.getNeighbourList();
              for(i=0;i<nt.size();i++)
              {
                  MPSNeigh = nt.elementAt(i).getNeighbour();
                  label = MPSNeigh.getLabel();
                  //Not the one that leaded to it
                  if (label!=previous[counter])
                  { 
                    if (visited.indexOf(new Integer(label))==-1)
                    { 
                      //Include this neighbour to the list
                      queueOfNodes2Visit[max] = MPSNeigh.getLabel();
                      //Append this neighbour (its number) in the already existing path until the current node
                      queueOfNodes2VisitString[max] = concatWithFixLength(queueOfNodes2VisitString[counter],current,digits);
                      //If there is a previous node and this one is connected by an empty separator --> disconnexion
                      if (previous[counter]>=0)
                      if (MPSNode.getVariables().intersectionNames(MT.elementAt(previous[counter]).getVariables()).size()==0)
                        disc[counter] = true;
                      previous[max] = MPSNode.getLabel(); //The previos of this neighbour is the current node
                      if (disc[counter]) disc[max]=true; //If the path of the current node was disconnected, this is also so.
                      max++; //Increment the pointer in the list to add new elements
                    }
                   }
                }
               counter++;   //Counter is the pointer to the next element to treat
               visited.add(new Integer(current));
             }
           
       }//end of while
       
       if (!found)
       {
           return -1;
       }
       else
       { path = queueOfNodes2VisitString[counter];
         path = concatWithFixLength(path,current,digits);
         pathV.add(path);
         pathV.add(new Integer(digits));
        
         pathV.add(new Boolean(disc[counter])); 
         //debugging
         if (debug)
         {
            System.out.println("path vale " + (String)pathV.get(0));
            System.out.println("disc vale " + (Boolean)pathV.get(2));
         }
          
         return MPSNode.getLabel();
       }
       
   }
   
   /** This methods concat to beginning the String represented by num, but
    * taking into account that we use totalSize digits to represent each 
    * number. Auxiliary function.
    */
    public String concatWithFixLength(String beginning,int num,int totalSize)
    {
      String cStr= new String();
      int i;
      String numStr = Integer.toString(num);
      String zero= new String("0");
    
      if (totalSize < numStr.length()) 
            System.out.println(" Error: we cannot represent " + num + " with " + totalSize + "digits.\n");
      else
      { for(i=numStr.length();i<totalSize;i++)
        cStr = cStr.concat(zero);
        cStr = cStr.concat(numStr);
      }
    
      return beginning.concat(cStr);
     }
     
    /** This method, given a size
     * tells us the number of digits we need to represent
     * the biggest number associated (beginning with 0 until size-1)
     */
    public int computeDigits(int totalSize)
    {
     int digits;
     //d gives us the log_10(totalSize)
     double d = java.lang.Math.log(totalSize)/java.lang.Math.log(10);
     Double dDouble = new Double(d);
     digits = dDouble.intValue();
     //if digits is an int number is ok
     //otherwise we need to have the bigger nearest integer
     if (!dDouble.equals(new Double(digits)))
        digits++;
     return digits;
     }


    
}
