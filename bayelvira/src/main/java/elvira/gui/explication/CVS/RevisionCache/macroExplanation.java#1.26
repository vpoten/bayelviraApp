package elvira.gui.explication;

import elvira.*;
import elvira.potential.*;
import java.util.*;

public class macroExplanation{
    
    static Bnet bnet;
    
    static CasesList clist;
    
    public static int LESSDIST=0;
    
    public static int GREATERDIST=1;
    
    public static int EQUALDIST=2;
   
    public static int NOTCOMPARABLEDIST=3;
    
    /** Given a node n and an evidence case c in wich n has a probability P for each state 
      * P(X=xi), calculates its accumulated distribution, i.e, P(X>=xi) for that case. 
      * At the moment, it is calulated only for random variables.
    */
    public static double[] greaterdist(Case c, Node n){
        FiniteStates fs=(FiniteStates) n;
 
        double[] probp=c.getProbOfNode(fs);
        double[] accdis=new double[fs.getNumStates()];
        
        double acc=0;
        for (int st=0; st<fs.getNumStates(); st++){
            accdis[st]=acc+probp[st];
            acc=acc+probp[st];
        }
        accdis[fs.getNumStates()-1]=1;
        return accdis;
    }

    /** Given a node n, one of its parents m and its probability table, calculates P(n>=ni|m). 
      * At the moment, it is calulated only for random variables.
      * It is used to draw the arcs in edition and inference mode.
    */
    public static double[][][] greaterdist(Bnet bnet, Node n, Node m){
        FiniteStates fsn;
        FiniteStates fsm;
 
        //Measure the distance over the cpn if we have an ID
        if ((bnet.getClass()==IDiagram.class)||(bnet.getClass()==IDWithSVNodes.class)){
        	bnet = ((IDiagram)bnet).getCpn();
        	n = bnet.getNode(n.getName());
        	m = bnet.getNode(m.getName());
        }
        
        fsn=(FiniteStates) n;
    	fsm=(FiniteStates) m;
        
        Relation relation=bnet.getRelation(n);
        //una distribución tendrá tantos valores como estados tenga el nodo

        Vector nodos=new Vector();
        //nodos.addElement(n);
        int posm=-1;
        for (int no=0; no<fsn.getParentNodes().size(); no++){
            if (fsn.getParentNodes().elementAt(no).equals(fsm))
                posm=no;
                else nodos.addElement(fsn.getParentNodes().elementAt(no));
        }
        //PotentialTable pot=(PotentialTable)relation.getValues();
        Potential pot=relation.getValues();
        
        //int tdis=(pot.getSize()/fsn.getNumStates())/fsm.getNumStates();
        int tdis=((int)FiniteStates.getSize(pot.getVariables())/fsn.getNumStates())/
                                                       fsm.getNumStates();

        //guardaremos en una matriz cada una de las distribuciones calculadas para cada estado de m
        //habrá tantas como el producto del nº de estados de cada padre del nodo n.
        double[][][] totaldis=new double[fsm.getNumStates()][tdis][fsn.getNumStates()];
        
  
        for (int t=0; t<totaldis.length; t++){
        //rellenamos la colección de distribuciones para el estado t del nodo m. Dicho estado es
        //el "mayor" suponiendo que sea un nodo ordinal. (p.ej, severo)
            Configuration config=new Configuration(nodos);
            for (int d=0; d<tdis; d++){
             //con d recorremos las posibles configuraciones de valores de los padres de n menos m
                 config.putValueAt(fsm, t, posm);
                 double acc=0;
                 int pos=0;
                 for (int a=0; a<=fsn.getNumStates()-1; a++){
                      config.putValueAt(fsn, a, 0);
                      double value=pot.getValue(config);
                      totaldis[t][d][a]=acc+value;
                      acc=acc+value; 
                      pos++;
                 }
                 totaldis[t][d][fsn.getNumStates()-1]=1;
                 //pongo esta sentencia porque a veces java no trabaja bien los doubles y la suma no da 1, sino 0,99999999999999999
                 config.remove(0);
                 config.remove(posm);
                 config.nextConfiguration();
                 
            }//end for d
        }//end for t

        return totaldis;
    }
    
    public static void print(double[][][] totaldis){
    
        System.out.println("Número de filas "+totaldis.length);    
        for (int t=0; t<totaldis.length; t++){
            System.out.print("[ ");
            for (int d=0; d<totaldis[t].length; d++){
                System.out.print("(");
                for (int a=0; a<totaldis[t][d].length; a++){
                    System.out.print(totaldis[t][d][a]+",");
                }
                System.out.print("), ");
            }
            System.out.println(" ],");
        }
    }


    
    //se supone que en dist tendremos D(D|x1,...,N,...,xm). La primera dimensión la define el
    //número de estados del nodo N sobre el que vamos a comparar.La segunda dimensión 
    //la define el producto de los estados de los nodos del conjunto {x1...Xm}\N
    //La tercera dimensión es para el número de estados de D.
    //Hay que ver si las filas de dist están "ordenadas".
    public static int compare(double[][][] dist){
		if (dist.length!=1){
                boolean orden=true;
                int compant=compare(dist[dist.length-1][0], dist[dist.length-2][0]);
                boolean iguales=(compant==2);
                int compact=compant; 
                if (iguales)
                	compant=-1;
 
                for (int f=dist.length-1; (f>0 && orden); f--){
                    for (int d=1; (d<dist[f].length && orden); d++){
                        compact=compare(dist[f][d], dist[f-1][d]);
    
                        iguales=iguales && (compact==2);
                        
                        if (!iguales && compact!=2) {
                        	if (compant==-1)
                        		compant=compact;
                            orden=(compact==compant);
                        }
                    //el orden que se define permite < ó <= (lo mismo para >).Al menos debe haber 
                    //un menor o mayor estricto.
  //                      System.out.println("Orden: "+orden);
                    } 
                }
                if (orden && !iguales) 
                    return compant;
                    else if (iguales)
                            return EQUALDIST;
                          else return NOTCOMPARABLEDIST;
            
            }//end if dist.length!=1
            else return EQUALDIST;
    }
    
    public static double influences(double[] d, double[] t){
        //calcula la mayor diferencia entre los valores de d y t
        double mayor=0;
        for (int v=0; v<d.length-1; v++){
        	double mayoract=Math.abs(d[v]-t[v]);
        	if (mayoract>mayor)
        		mayor=mayoract;
        	}
        return mayor;
    }
        
    public static boolean influencesTheta(double d, double theta){
    	//decide si la diferencia de todos los valores de d y t en valor absoluto es mayor que theta
        return d>theta;
    }
    
    public static int compare(double[] d, double[] t){
        //decide si la primera distribución es menor, mayor o igual que la segunda. Lo será si todos sus valores
        //lo son. Al menos un menor/mayor debe ser estricto.
//       for (int i=0; i<d.length; i++)
//            System.out.print("d["+i+"]:"+d[i]+";t["+i+"]:"+t[i]);
//        System.out.println();
        boolean orden=true;
        boolean estricto=true;
        int signo=EQUALDIST;
        if (d[0]>t[0])
            signo=GREATERDIST;
        else if (d[0]<t[0])
                signo=LESSDIST;
             else estricto=false;
        int st=1;
        while (st<d.length-1 && (orden)) {
              if (d[st]>t[st]){
              	 if (signo==EQUALDIST)
              	 	signo=GREATERDIST;
                 else if (estricto && signo!=GREATERDIST)
                 	orden=false;
                 
                 }
              else if (d[st]<t[st]){
              		  if (signo==EQUALDIST)
              		  	  signo=LESSDIST;
                      else if (estricto && signo!=LESSDIST)
                      	  orden=false;
                      }
              st++;                          
        }
        if (!orden)
           return NOTCOMPARABLEDIST;
        else return signo;
    }

    /* Returns the maximum of the differences between the values of the distribution dist, i.e.
    * It is used to get the influence from A to B.
    */
    private static double maxcompare(double[][][] dist){
 		double max=0;
 		if (dist.length!=1){
                for (int f=dist.length-1; f>0; f--)
                    for (int d=0; d<dist[f].length; d++)
                    	for (int s=0; s<dist[f][d].length; s++){
				        	double maxact=Math.abs(dist[f][d][s]-dist[f-1][d][s]);
        					if (maxact>max)
	                        	max=maxact;
                        }
		}
		return max;
    }

    /* Returns maxk(maxi(P(B>=bk|ai)-P(B>=bk|ao)))*/
        public static double influence(double[][][] dist){
           return maxcompare(dist);
    }


	private static void getRelatedNeighbours(Bnet bnet, Node n, Vector members, Vector rels){
		members.removeElement(n);
		NodeList neigh=bnet.neighbours(n);
		for (int i=0; i<neigh.size(); i++){
			Node nb=(Node) neigh.elementAt(i);
			if (members.indexOf(nb)!=-1){
				rels.addElement(nb);
				getRelatedNeighbours(bnet,nb, members, rels);
			}
		}
		
	}
  
    public static NodeList pathExplanation(CasesList c, Bnet b, Evidence e, Node h){
    	//Suermondt's algorithm
        clist=c;
        bnet=b;
        Vector evi=clist.getCurrentCase().getEvidence().getVariables();
        evi.addElement(h);
        //calculates all the predecessors of e and h because unobserved nodes under e and h don't influence them
        Vector members=new Vector(evi);
        for (int i=0; i<evi.size(); i++){
        	Node n = (Node) evi.elementAt(i);
        	Vector ascn=bnet.ascendants(n);
        	for (int a=0; a<ascn.size(); a++){
        		Node ascdn=(Node)ascn.elementAt(a);
        		if (members.indexOf(ascdn)==-1)
       				members.addElement(ascdn);
       		}
        }
        //gets the related nodes
        Vector related=new Vector();
        related.addElement(h);
        getRelatedNeighbours(bnet,h,members,related);
        evi.removeElement(h);
        //removes the nodes n which are d-separated from h by e\n
        NodeList separators=new NodeList(evi);
        for (int i=0; i<related.size(); i++){
	       	Node n = (Node) related.elementAt(i);
	       	boolean del=false;
        	if (!n.equals(h) && separators.getId(n)!=-1){
        		separators.removeNode(n);
        		del=true;
        	}
        	if (!n.equals(h) && bnet.independents(n,h,separators))
         		related.removeElement(n);
        	if (del)
        		separators.insertNode(n);
        	}
        //marks the nodes included in all paths from e to h which contains only related nodes and which are not d-separated
        NodeList marked=markNodes(related,e,h);
   		return marked;
    }
    
    static NodeList markNodes(Vector rel, Evidence e, Node h){
    //to do it, we mark as visited all nodes which are not related to e and h. They are in vector "rel"
      NodeList visit=new NodeList();
      for (int i=0 ; i<bnet.getNodeList().size() ; i++) {
           Node n = (Node) bnet.getNodeList().elementAt(i);
           n.setMarked(false);
           if (rel.indexOf(n)==-1)
           	visit.insertNode(n);
      }
	  NodeList allmarked=new NodeList();
	  //we look for all paths from each finding nm to target variable h. All nodes of those paths are saved in allmarked
	  for (int i=0; i<e.size() ; i++) {
           Node nm = (Node) e.getVariable(i); 	
      	   NodeList marks=new NodeList();
      	   marks.insertNode(nm);
      	   //in marks we get the nodes in all the paths from nm to h
      	   markAllPaths(nm,h,rel,visit,marks);
           for (int n=0; n<marks.size(); n++){
           		Node mn=(Node) marks.elementAt(n);
               	if (allmarked.getId(mn)==-1)
               		allmarked.insertNode(mn);
           }
      }
      return allmarked;
    }


	private static NodeList relatedAdyacents(Bnet bnet, Vector rel, Node node){
		NodeList nbg=bnet.parents(node);
		for (int c=0; c<bnet.children(node).size(); c++)
			nbg.insertNode(bnet.children(node).elementAt(c));
		NodeList relnbg=new NodeList();
		for (int n=0; n<nbg.size(); n++)
			if (rel.indexOf(nbg.elementAt(n))!=-1)
				relnbg.insertNode(nbg.elementAt(n));
		return relnbg;
	}
			
    public static boolean markAllPaths(Node a, Node b, Vector related, NodeList visited, NodeList marked) {
      //typical deep-first algorithm to find all paths from a to b.
      if (visited.getId(a)!=-1)
      	return false;
      	else{
      		 visited.insertNode(a);
        	 boolean found;
        	 if (a.equals(b)){
           		 a.setMarked(true);
           		 if (marked.getId(a)==-1)
        	   		marked.insertNode(a);
           	 	 visited.removeNode(a);
           	 	 return true;
             }
        	 else {
      		NodeList rn=relatedAdyacents(bnet, related, a);
            Enumeration e = rn.elements();
            Node ady;
            found = false;
            boolean marcar=false;
            while (e.hasMoreElements()) {
                ady = (Node) e.nextElement();
                if (visited.getId(ady)==-1){
                	if (markAllPaths(ady,b,related,visited,marked)){
                		found=true;
                    	a.setMarked(true);
                    	if (marked.getId(a)==-1)
                    		marked.insertNode(a);
                    }
                }
            }//end while
            visited.removeNode(a);
            return found;   
      }
    }
} //end markAllPaths

}//end class macroExplanation
