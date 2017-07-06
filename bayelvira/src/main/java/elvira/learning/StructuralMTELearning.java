/*  StructuralMTELearning.java  */

package elvira.learning;

import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.DataBaseCases;
import elvira.learning.*;
import elvira.potential.*;
import elvira.parser.ParseException;


/**
 *
 * Performs structural learning from dataBases with discrete and continuous
 * variables. Conditional distributions of the result net are MTE. It's necessary
 * to exit any continuous variable.
 *
 * @author  avrofe
 * @author asc
 * @since 20/11/07
 * @version 1.2
 */



public class StructuralMTELearning{
    
    //dataBase that we use to learn
    DataBaseCases cases;
    
    //the learnt net
    Bnet output;
    
    //hash table to store values for families
    Hashtable table;
    
    //number of split
    int numSplit = 3;
    
    //constants to make the tree
    int numPoints = 4 ;
    
    
    private final int ADD = 0;
    private final int REMOVE = 1;
    private final int REVERSE = 2;
    
    
    /**
     * Creates a new instance of StructuralMTELearning
     */
    
    public StructuralMTELearning() { }
    
    
    /**
     * Creates a new instance of StructuralMTELearning.
     * The 'cases' instance variable is initialised to the argument cases
     * and the hash table that will sotore the families during
     * the search is also initialised.
     * @param cases the DataBaseCases from which to learn.
     */
    
    public StructuralMTELearning(DataBaseCases cases) {
        this.cases = cases;
        
        //compute maximum size of the hash table, i.e.
        //maximum number of potentials in the net
        
        int numVars, numCases, tamMax;
        numVars = cases.getVariables().size();
        numCases = cases.getNumberOfCases();
        
        //if n is even
        if ( (numVars % 2) == 0){
            tamMax = numVars;
            for (int j = 2; j < (numVars/2) ; j++){
                tamMax = tamMax + combination(numVars - 1 , j);
            }
            tamMax = tamMax * 2 * numVars;
        } else{//if n is odd
            tamMax = numVars;
            
            for (int j = 2; j < (numVars/2) ; j++){
                tamMax = tamMax + combination(numVars - 1 , j);
            }
            tamMax = tamMax * 2;
            tamMax = tamMax + combination(numVars - 1 , numVars / 2);
            tamMax = tamMax * numVars;
        }
        
        //to decrease the time, I don't consider this maximum size
        tamMax = tamMax/2 + 1;
        if (tamMax > 50000)
            tamMax = tamMax/2 + 1;
        table = new Hashtable(tamMax);
    }
    
    
    /**
     * Return combinations of m element over n
     *
     * @param m int
     * @param n int
     * @return combinations of m element over n
     */
    
    public static int combination( int m , int n){
        int combination = 1;
        int nfactorial = 1;
        for (int i = 0; i < n; i++)
            combination = (m - i)*combination;
        for (int i = 0 ; i < n; i++)
            nfactorial = (n-i)*nfactorial;
        
        // The next line is just for safety.
        if (nfactorial == 0)
            return 0;
        return combination/nfactorial;
    }
    
    
    /**
     * Just for compatibility.
     */
    
    public void learning() { }
    
    
    
    /**
     * Creates an initial nework without links, ans stores it in
     * argument 'net'. Object 'net' is modified.
     *
     * @param net the Bnet that will contain the initial net.
     * @return a Vector with: quality of the network, log-likelihood,
     * penalty factor and maximum probability.
     */
    
    public Vector setInitialBNet(Bnet net){
        
        Vector results = new Vector();
        NodeList vars = cases.getVariables();
        Node var, parent;
        int numVars = cases.getVariables().size();
        int numCases = cases.getNumberOfCases();
        double quality, log, dim;
        double likelihood = 0, penal = 0;
        double[] values = new double[3];
        String key;
        
        
        //begin with a net without links
        
        int numLinks = 0;
        NodeList nodes = new NodeList();
        for(int i=0 ; i< vars.size(); i++){
            var = (Node) vars.elementAt(i);
            var.setParents(new LinkList());
            var.setChildren(new LinkList());
            var.setSiblings(new LinkList());
            nodes.insertNode(var);
        }
        net.setNodeList(nodes);
        
        //set the relations of the net
        
        MTELearning l0 = new MTELearning(cases) ;
        ContinuousProbabilityTree tree0;
        NodeList X0;
        Configuration conf0;
        PotentialContinuousPT p0;
        Vector relations0 = new Vector();
        Node y0;
        
        for (int i = 0; i < numVars; i++){
            y0 = net.getNodeList().elementAt(i);
            X0 =  y0.getParentNodes();
            NodeList variables0 = new NodeList();
            variables0.insertNode(y0);
            for (int j = 0; j < X0.size(); j++){
                variables0.insertNode(X0.elementAt(j));
            }
            tree0 = l0.learnConditional(y0 , X0, cases, numSplit, numPoints);
            p0 = new PotentialContinuousPT(variables0, tree0);
            Relation relationy0 = new Relation(y0);
            relationy0.setValues(p0);
            relations0.insertElementAt(relationy0, i);
        }
        
        net.setRelationList(relations0);
        
        //the first time we have to calculate all terms of quality
        
        //calculamos el maximo de las prob cond en todas las variables y todos los casos
        double max = Double.NEGATIVE_INFINITY;
        double maxi;
        log = 0;
        dim = 0;
        for (int i = 0; i < numVars; i++){
            //suma para todas las variables de la
            //suma en todos los casos del log de la prob condicionada de la
            //var i a sus padres en esta red, osea: semiQuality
            var = net.getNodeList().elementAt(i);
            values = new double[3];
            int ki = 1;
            int type = var.getTypeOfVariable();
            if (type!=Node.CONTINUOUS && type!=Node.MIXED){
                //is finite-states
                ki = ((FiniteStates) var).getNumStates()-1;
            } else
                ki = 14;
            values[1] = ki * dimension(var);
            PotentialContinuousPT p = new PotentialContinuousPT();
            //esta es la suma en todos los casos de la log likelihood
            values[0] = ((double[])semiQuality( var, p))[0];
            //este es el maximo de las probabilidades cond en todos los casos para esta variable
            maxi = ((double[])semiQuality( var, p))[1];
            values[2] = maxi;
            if (maxi > max ) max = maxi;
            
            key = (new Integer(net.getNodeList().getId(var))).toString();
            key = "s"+key+"/";
            
            table.put(key, values);
            
            log = values[0] + log;
            //Calcule the dimension for each family
            dim = values[1] + dim;
        }
        
        
        //tengo que a�adir una penalizacion por los cortes
        /*int numNodes;
        Node node;
        int numContVar = 0;
        double states = 1;
        double penalSplit, penalSplitCont;
        numContVar = 0;
        states = 1;
        penalSplitCont = 0;
        penalSplit = 1;
        numNodes = net.getNodeList().getNodes().size();
        for (int i = 0; i < numNodes; i++ ){
            node = (Node) net.getNodeList().getNodes().elementAt(0);
            if (node.getTypeOfVariable() == Node.CONTINUOUS){
                //calculo el numero de variables continuas
                numContVar++;
            }
            if (node.getTypeOfVariable() == Node.FINITE_STATES){
                //calculo el producto de todos estados de las discretas
                states = states * ((FiniteStates) node).getNumStates();
            }
        }
        for (int i = 0; i < numContVar; i++ ){
            penalSplitCont = penalSplitCont + Math.pow(numSplit, i)*(numSplit-1);
        }
        //por si no hay ninguna variable continua ???????????????????
        if (penalSplitCont == 0) penalSplitCont = 1;
        penalSplit = states*penalSplitCont;
         
        //a la dimension le a�ado lo que necesito para hacer los cortes
        dim = dim + penalSplit;
         */
        
        //our proposed metric takes into account the likelihood but with a penalty
        //quality = log - numVars*numCases*((Math.log(max))) - (0.5)*dim*((Math.log(numCases)));
        //likelihood = log - numVars*numCases*((Math.log(max)));
        //penal = ((0.5)*dim*((Math.log(numCases))));
        
        quality = log - (0.5)*dim*((Math.log(numCases)));
        likelihood = log;
        penal = ((0.5)*dim*((Math.log(numCases))));
        
        //System.out.println("el maximo es : "+max+" y la calidad : "+quality);
        
        results.add(new Double(quality));
        results.add(new Double(likelihood));
        results.add(new Double(penal));
        results.add(new Double(max));
        
        //----------------------------------------------------------------------
        //YA HE INICIALIZADO LA RED INICIAL Y CALCULADO LA Q
        
        return results;
    }
    
    
    //net es modificada
    
    public Vector getBestNet(Bnet net, double oldQuality, double oldLikelihood, double oldPenal, double maxLastIterat) throws elvira.InvalidEditException{
        
        Vector results = new Vector();
        
        NodeList vars = cases.getVariables();
        Node head, tail, var, parent;
        
        int numVars, numCases, numLinks ;
        double old1, oldLog, oldDim, newLog, newDim, newLog1, newDim1, newLog2, newDim2;
        double oldLog1, oldLog2, oldDim1, oldDim2, newMax, newMax1, newMax2;
        double maxQuality, auxQuality, quality , log, dim;
        quality = oldQuality;
        double oldMax, maxMax; //el valor maximo de prob cond para el movimiento elegido en cada iteracion
        double[] values = new double[3];
        boolean firstTime;
        String key, maxMovement;
        double maxCurrentIterat;
        double likelihood = 0, penal = 0;
        double max;
        double maxLikelihood = 0, maxPenal = 0 ;
        
        numVars = cases.getVariables().size();
        numCases = cases.getNumberOfCases();
        
        //links that I can add
        LinkList addLinks = new LinkList();
        //links that I can reverse
        LinkList reverseLinks = new LinkList();
        //Links that I can remove
        LinkList removeLinks = new LinkList();
        Link newLink, auxLink, maxLink, link ;
        
        
        addLinks = new LinkList();
        reverseLinks = new LinkList();
        removeLinks = new LinkList();
        
        //check which arcs can be added
        
        for (int i = 0 ; i < numVars ; i++){
            //test every combination
            for (int j = i+1; j < numVars ; j++){
                
                //with this combination, I have two possible arcs: one and the reverted
                //we have to make sure no one already exits in the net, and that
                //adding it the net has not directed cicle
                if ((net.getLink(vars.elementAt(i), vars.elementAt(j))==null)
                && (net.getLink(vars.elementAt(j), vars.elementAt(i))==null)){
                    //test i -> j
                    Link enlace = new Link(vars.elementAt(i), vars.elementAt(j));
                    auxLink = new Link(vars.elementAt(i), vars.elementAt(j));
                    
                    try{
                        net.createLink(vars.elementAt(i), vars.elementAt(j), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(i), vars.elementAt(j)));
                    } catch (InvalidEditException e3){
                    }
                    
                    //test j -> i
                    auxLink = new Link(vars.elementAt(j), vars.elementAt(i));
                    try{
                        net.createLink(vars.elementAt(j), vars.elementAt(i), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(j), vars.elementAt(i)));
                    } catch (InvalidEditException e2){
                    }
                }//end if
            }//end j
        }//end i
        
        //System.out.println("Puedo a�adir "+addLinks.size()+" arcos");
        
        //check which arcs can be removed
        LinkList links = net.getLinkList();
        numLinks = links.size();
        LinkList linksCopy = links.copy();
        for(int i = 0; i < numLinks; i++ ){
            auxLink = new Link();
            auxLink = (Link) links.getLinks().elementAt(i);
            removeLinks.insertLink(auxLink);
        }
        
        //System.out.println("Puedo borrar "+removeLinks.size()+" arcos");
        
        
        Link auxLink2;
        
        ////check which arcs can be reversed
        for (int i = 0; i< numLinks; i++){
            //we have to make sure that reversing this arc, the net has not directed cicle
            auxLink = new Link();
            auxLink = (Link) linksCopy.getLinks().elementAt(i);
            //that is the reversed
            auxLink2 = new Link(auxLink.getHead(), auxLink.getTail());
            //try to reverse it
            net.removeLink(auxLink);
            try{
                net.createLink(auxLink.getHead(), auxLink.getTail());
                if (net.isADag()){
                    reverseLinks.insertLink(auxLink);
                }
                net.removeLink(auxLink2);
            } catch(InvalidEditException e4){
            }
            net.createLink(auxLink.getTail(), auxLink.getHead());
            
        }
        
        //System.out.println("Puedo invertir "+reverseLinks.size()+" arcos");
        
        
        net.setLinkList(linksCopy);
        
        //System.out.println("number of links now "+net.getLinkList().size()+" and those are \n"+net.getLinkList()+"\n");
        
        //Recalculate terms for each movement, and the quality for each one
        //Store the movement that maximise the quality
        
        maxMax = 0;
        maxQuality = 0;
        maxLink = new Link();
        maxMovement = "";
        link = new Link();
        firstTime = true;
        
        //System.out.println("Busco el mejor movimiento entre los de a�adir");
        
        //movement: add (only change head's values)
        for (int i = 0; i < addLinks.size(); i++){
            link = addLinks.elementAt(i);
            var = link.getHead();
            values = new double[3];
            net.getLinkList().getID(link.getTail().getName(), link.getHead().getName());
            values = new double[3];
            values = getValues(var, net);
            oldLog = values[0];
            oldDim = (int) values[1];
            //try to add
            //System.out.println("Pruebo a a�adir "+link);
            net.createLink(link.getTail(), link.getHead());
            values = new double[3];
            values = getValues(var, net);
            newLog = values[0];
            newDim = (int) values[1];
            //maximo para la variable var con este movimiento
            newMax = values[2];
            //recalcule quality
            //si el maximo para esta variable con este movimiento es mayor q el maximo
            //q teniamos en la etapa anterior , lo intercambiamos (los maximos de las otras variables
            //no cambian respecto a la etapa anterior)
            if (newMax > maxLastIterat) maxCurrentIterat = newMax;
            else maxCurrentIterat = maxLastIterat;
            //auxQuality = quality + newLog - oldLog - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat))) - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            auxQuality = quality + newLog - oldLog - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            likelihood = oldLikelihood + newLog - oldLog;
            penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            
            //System.out.println("Lo borro\n");
            net.removeLink(link.getTail(), link.getHead());
            
            if (Double.isInfinite(auxQuality)){
                auxQuality =  0 - Double.MAX_VALUE;
                //System.out.println("*************  quality es -Infinito ******************");
            }
            
            
            if (firstTime){
                //first time there is not with which compare
                maxQuality = auxQuality;
                maxLink = link;
                maxMovement = "Add";
                //I have to store the value of the max for this move, because if it was chosen
                maxMax = maxCurrentIterat;
                maxLikelihood = likelihood;
                maxPenal = penal;
                firstTime = false;
            } else{
                //compare qualities
                if (auxQuality > maxQuality){
                    //that is the best move, until this moment, with features:
                    maxQuality = auxQuality;
                    maxLink = link;
                    maxMovement = "Add";
                    //guardo las dos componentes de la calidad
                    maxLikelihood = likelihood;
                    maxPenal= penal;
                    //I have to store the value of the max for this move, because if it was chosen
                    maxMax = maxCurrentIterat;
                }
            }
            
        }//end add movement
        
        //System.out.println("Busco el mejor movimiento entre los de borrar");
        
        //movement: remove (only change head's values)
        for (int i = 0; i < removeLinks.size(); i++){
            link = removeLinks.elementAt(i);
            values = new double[3];
            values = getValues(link.getHead(), net);
            oldLog = values[0];
            oldDim = values[1];
            //try to remove
            net.removeLink(link.getTail(), link.getHead());
            values = new double[3];
            values = getValues(link.getHead(), net);
            
            newLog = values[0];
            newDim = (int) values[1];
            newMax = values[2];
            //si el maximo para esta variable con este movimiento es mayor q el maximo
            //q teniamos en la etapa anterior , lo intercambiamos (los maximos de las otras variables
            //no cambian respecto a la etapa anterior)
            if (newMax > maxLastIterat) maxCurrentIterat = newMax;
            else maxCurrentIterat = maxLastIterat;
            auxQuality = quality + newLog - oldLog
                    - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            
            likelihood = oldLikelihood + newLog - oldLog;
            penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            
            if (Double.isInfinite(auxQuality)){
                auxQuality =  0 - Double.MAX_VALUE;
                //System.out.println("*************  quality es -Infinito ******************");
            }
            
            //undo the movement
            net.createLink(link.getTail(), link.getHead());
            
            //compare qualities
            if (auxQuality > maxQuality){
                maxQuality = auxQuality;
                maxLink = link;
                maxMovement = "Remove";
                maxLikelihood = likelihood;
                maxPenal = penal;
                //I have to store the value of the max for this move, because if it was chosen
                maxMax = maxCurrentIterat;
            }
        }//end remove movements
        
        //System.out.println("Busco el mejor movimiento entre los de invertir");
        
        //movement: reverse (change values of both terms)
        for (int i = 0; i < reverseLinks.size(); i++){
            link = new Link();
            link = reverseLinks.elementAt(i);
            
            values = new double[3];
            values = getValues(link.getHead(), net);
            old1 = values[0];
            oldDim1 = values[1];
            
            values = new double[3];
            values = getValues(link.getTail(), net);
            oldLog2 = values[0];
            oldDim2 = values[1];
            oldLog1 = 0;
            oldLog = oldLog1 + oldLog2;
            oldDim = oldDim1 + oldDim2;
            
            net.removeLink(link.getTail(), link.getHead());
            net.createLink(link.getHead(),link.getTail());
            values = new double[3];
            values =  getValues(link.getHead(), net);
            newLog1 = values[0];
            newDim1 = (int) values[1];
            newMax1 = values[2];
            
            values = new double[3];
            values = getValues(link.getTail(), net);
            newLog2 = values[0];
            newDim2 = (int) values[1];
            newMax2 = values[2];
            newLog = newLog1 + newLog2;
            newDim = newDim1 + newDim2;
            //considero el mayor de los dos
            if (newMax1 > newMax2) newMax = newMax1;
            else newMax = newMax2;
            
            //si el maximo para alguna de estas variables con este movimiento es mayor q el maximo
            //q teniamos en la etapa anterior  , lo intercambiamos (los maximos de las otras variables
            //no cambian respecto a la etapa anterior)
            if (newMax > maxLastIterat) maxCurrentIterat = newMax;
            else maxCurrentIterat = maxLastIterat;
            auxQuality = quality + newLog - oldLog
                    - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            
            likelihood = oldLikelihood + newLog - oldLog;
            penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
            
            //undo the movement
            net.removeLink(link.getHead(),link.getTail());
            
            net.createLink(link.getTail(),link.getHead());
            
            //compare qualities
            if (auxQuality > maxQuality){
                maxQuality = auxQuality;
                maxLink = link;//I store the original link, nor the reversed
                maxMovement = "Reverse";
                maxLikelihood = likelihood;
                maxPenal = penal;
                //I have to store the value of the max for this move, because if it was chosen
                maxMax = maxCurrentIterat;
            }
        }//end reverse movement
        
        //System.out.println("Ya he encontrado el mejor movimiento");
        
        results.add(new Double(maxQuality));
        results.add(new Double(maxLikelihood));
        results.add(new Double(maxPenal));
        results.add(new Double(maxMax));
        results.add(maxLink);
        results.add(maxMovement);
        
        return results;
    }
    
    
    
    public void simulatedAnnealing( ) throws InvalidEditException, java.lang.Throwable,  elvira.InvalidEditException{
        
        int numVars, numCases, numLinks ;
        double oldLog, oldDim, newLog, newDim, newLog1, newDim1, newLog2, newDim2;
        double oldLog1, oldLog2, oldDim1, oldDim2, oldMax, newMax, newMax1, newMax2;
        double maxQuality, auxQuality, quality, log, dim;
        double maxMax; //el valor maximo de prob cond para el movimiento elegido en cada iteracion
        double[] values = new double[3];
        boolean firstTime;
        String key, maxMovement;
        double maxLikelihood = 0, maxPenal = 0 ;
        
        double newQuality = 0;
        
        NodeList vars = cases.getVariables();
        Node head, tail, var, parent;
        Link maxLink;
        
        numVars = cases.getVariables().size();
        numCases = cases.getNumberOfCases();
        
        //to print the quality as sum of the likelihood y the penalisation
        double likelihood = 0, penal = 0;
        double max;
        
        System.out.println("Empiezo a inicializar la inicial");
        //INICIALIZO LA RED INICIAL (VACIA)
        Bnet net = new Bnet();
        Vector initialResults = new Vector();
        initialResults = setInitialBNet(net);
        quality = ((Double)initialResults.elementAt(0)).doubleValue();
        likelihood = ((Double)initialResults.elementAt(1)).doubleValue();
        penal = ((Double)initialResults.elementAt(2)).doubleValue();
        max = ((Double)initialResults.elementAt(3)).doubleValue();
        //YA LA HE INICIALIZADO
        System.out.println("Inicializo la red inicial");
        
        //maximum in the last iteration
        double maxLastIterat = max;
        
        if (Double.isInfinite(quality)){
            quality =  0 - Double.MAX_VALUE;
            //System.out.println("*************  quality es -Infinito ******************");
        }
        
        double oldQuality = quality;
        double oldLikelihood = likelihood;
        double oldPenal = penal;
        boolean beginning = true;
        Vector relations = new Vector();
        
        double maxCurrentIterat;
        
        //guarda el movimiento, primero la cadena donde se indica el movimiento y segundo los nodos del link original
        Vector move = new Vector();
        int movement;
        
        Node t, h;
        Link link;
        
        double random;
        
        //la proibabilidad
        double prob = -1;
        
        
        
        //net es la red inicial
        
        //auxNet es una auxiliar q es una copia de la actual
        Bnet oldNet = net.copyBnet();
        Bnet auxNet = oldNet.copyBnet();
        
        //los resultados anteriores al principio son los originales
        Vector oldResults = new Vector();
        oldResults.add(initialResults.elementAt(0));
        oldResults.add(initialResults.elementAt(1));
        oldResults.add(initialResults.elementAt(2));
        oldResults.add(initialResults.elementAt(3));
        
        
        
        Vector auxResults = new Vector();
        
        double temp;
        
        for (int i = 1; i < 5000; i++){
            
            temp = 100/Math.log(i+1);
            
            //System.out.print("\nTemperatura = "+temp+ " Iteracion = "+i);
            
            
            auxNet = new Bnet();
            auxNet = oldNet.copyBnet();
            
            //generamos un movimiento aleatorio
            move = new Vector();
            move = generateMove(auxNet);
            
            movement = ((Integer)move.elementAt(0)).intValue();
            t = (Node)move.elementAt(1);
            h = (Node)move.elementAt(2);
            link = new Link(t, h);
            
            System.out.println("Trying move "+t.getName()+"->"+h.getName());
            
            //llevamos a cabo el movimiento y obtenemos la calidad
            auxResults = performMove(auxNet, movement, link, oldResults);
            auxQuality = ((Double)auxResults.elementAt(0)).doubleValue();
            
            //elegimos un numero aleatorio de 0 a 1
            random = Math.random();
            
            //calculamos la probabilidad
            
            
            //oldQuality = ((Double)oldResults.elementAt(0)).doubleValue();
            
            //OJOOOOOOOOOOOOOOO
            
            oldQuality = ((Double)oldResults.elementAt(0)).doubleValue();
            
            //compruebo cual es mayor si el max del old o el max del aux
            double maxOld = ((Double)oldResults.elementAt(3)).doubleValue();
            double maxAux = ((Double)auxResults.elementAt(3)).doubleValue();
            
            //como hay q normalizar, le resto n*m*log max
            
            double logOld, logAux;
            logOld = oldQuality;
            logAux = auxQuality;
            
            if (maxOld < maxAux ){
                //normalizo con el max del aux
                //logOld = logOld - numVars*numCases*((Math.log(maxAux)));
            } else{
                //normalizo con el max del old
                //logOld = logOld - numVars*numCases*((Math.log(maxOld)));
                //normalizo el de la aux con el max del old
                //logAux = logAux + numVars*numCases*((Math.log(maxAux))) - numVars*numCases*((Math.log(maxOld)));
            }
            
            
            
            auxQuality = logAux;
            oldQuality = logOld;
            
            
            
            // prob = Math.min(Math.exp((auxQuality-oldQuality)/temp), 1);
            //System.out.print("  Probabilidad = "+prob+"  ");
            
            //CAMBIO
            
            if (auxQuality > oldQuality) { // Aceptamos la nueva seguro
                //nos quedamos con ese modelo
                System.out.println("Move accepted");
                oldNet = auxNet.copyBnet();
                oldResults = new Vector();
                oldResults.add(initialResults.elementAt(0));
                oldResults.add(initialResults.elementAt(1));
                oldResults.add(initialResults.elementAt(2));
                oldResults.add(initialResults.elementAt(3));
            } else { // Es decir, auxQuality <= oldQuality
                prob = Math.min(Math.exp((auxQuality-oldQuality)/temp), 1);
                
                if (random < prob) {
                    oldNet = auxNet.copyBnet();
                    oldResults = new Vector();
                    oldResults.add(initialResults.elementAt(0));
                    oldResults.add(initialResults.elementAt(1));
                    oldResults.add(initialResults.elementAt(2));
                    oldResults.add(initialResults.elementAt(3));
                    System.out.println("Move accepted");
                } else{
                    System.out.println("Move rejected");
                    //nos quedamos con el modelo que teniamos (oldNet)
                }
            }
            
            
            
            
            
            //CAMBIO
            
            /**
             * //si es menor o igual, t quedas con esta red, sino no
             * if (random < prob){
             * //System.out.println("Aceptamos. *** ");//Links ahora mismo "+auxNet.getLinkList());
             * //nos quedamos con ese modelo
             * oldNet = auxNet.copyBnet();
             * oldResults = new Vector();
             * oldResults.add(initialResults.elementAt(0));
             * oldResults.add(initialResults.elementAt(1));
             * oldResults.add(initialResults.elementAt(2));
             * oldResults.add(initialResults.elementAt(3));
             *
             * }
             * else{
             * //nos quedamos con el modelo que teniamos (oldNet)
             * //System.out.println(" ");//Links ahora mismo "+auxNet.getLinkList());
             * }
             *
             * //decrementamos la temperatura
             *
             * //temp = temp - decremento;
             */
            
            
            
        }//end for
        
        //System.out.println("Ya tengo el modelo final");
        
        //set the relations
        
        MTELearning l = new MTELearning(cases) ;
        ContinuousProbabilityTree tree;
        NodeList X;
        Configuration conf;
        PotentialContinuousPT p;
        relations = new Vector();
        Node y;
        for (int i = 0; i < numVars; i++){
            y = oldNet.getNodeList().elementAt(i);
            X =  y.getParentNodes();
            NodeList variables = new NodeList();
            variables.insertNode(y);
            for (int j = 0; j < X.size(); j++){
                variables.insertNode(X.elementAt(j));
            }
            tree = l.learnConditional(y , X, cases, numSplit, numPoints);
            p = new PotentialContinuousPT(variables, tree);
            Relation relationy = new Relation(y);
            relationy.setValues(p);
            relations.insertElementAt(relationy, i);
        }
        
        oldNet.setRelationList(relations);
        
        output = oldNet;
        
    }//end simulatedAnnealing
    
    
    
    public Vector generateMove(Bnet net) throws elvira.InvalidEditException{
        
        Vector move = new Vector();
        int movement;
        Node t,h;
        
        //movimientos posibles:
        //- a�adir un enlace de los posibles
        //- borrar un enlace de los actuales
        //- invertir un enlace de los posibles
        
        
        int numVars, numCases, numLinks ;
        double oldLog, oldDim, newLog, newDim, newLog1, newDim1, newLog2, newDim2;
        double oldLog1, oldLog2, oldDim1, oldDim2, oldMax, newMax, newMax1, newMax2;
        double maxQuality, auxQuality, quality, log, dim;
        double maxMax; //el valor maximo de prob cond para el movimiento elegido en cada iteracion
        double[] values = new double[3];
        boolean firstTime;
        String key, maxMovement;
        double maxLikelihood = 0, maxPenal = 0 ;
        
        
        
        NodeList vars = cases.getVariables();
        Node head, tail, var, parent;
        Link maxLink;
        
        numVars = cases.getVariables().size();
        numCases = cases.getNumberOfCases();
        //links that I can add
        LinkList addLinks = new LinkList();
        //links that I can reverse
        LinkList reverseLinks = new LinkList();
        //Links that I can remove
        LinkList removeLinks = new LinkList();
        Link newLink, auxLink, link ;
        
        
        addLinks = new LinkList();
        reverseLinks = new LinkList();
        removeLinks = new LinkList();
        
        //check which arcs can be added
        
        for (int i = 0 ; i < numVars ; i++){
            //test every combination
            for (int j = i+1; j < numVars ; j++){
                
                //with this combination, I have two possible arcs: one and the reverted
                //we have to make sure no one already exits in the net, and that
                //adding it the net has not directed cicle
                if ((net.getLink(vars.elementAt(i), vars.elementAt(j))==null)
                && (net.getLink(vars.elementAt(j), vars.elementAt(i))==null)){
                    //test i -> j
                    Link enlace = new Link(vars.elementAt(i), vars.elementAt(j));
                    auxLink = new Link(vars.elementAt(i), vars.elementAt(j));
                    
                    try{
                        net.createLink(vars.elementAt(i), vars.elementAt(j), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(i), vars.elementAt(j)));
                    } catch (InvalidEditException e3){
                    }
                    
                    //test j -> i
                    auxLink = new Link(vars.elementAt(j), vars.elementAt(i));
                    try{
                        net.createLink(vars.elementAt(j), vars.elementAt(i), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(j), vars.elementAt(i)));
                    } catch (InvalidEditException e2){
                    }
                }//end if
            }//end j
        }//end i
        
        ///System.out.println("Puedo a�adir "+addLinks.size()+" arcos = "+numAddLinks(net));
        
        //check which arcs can be removed
        LinkList links = net.getLinkList();
        numLinks = links.size();
        LinkList linksCopy = links.copy();
        for(int i = 0; i < numLinks; i++ ){
            auxLink = new Link();
            auxLink = (Link) links.getLinks().elementAt(i);
            removeLinks.insertLink(auxLink);
        }
        
        //System.out.println("Puedo borrar "+removeLinks.size()+" arcos");
        
        
        Link auxLink2;
        
        ////check which arcs can be reversed
        for (int i = 0; i< numLinks; i++){
            //we have to make sure that reversing this arc, the net has not directed cicle
            auxLink = new Link();
            auxLink = (Link) linksCopy.getLinks().elementAt(i);
            //that is the reversed
            auxLink2 = new Link(auxLink.getHead(), auxLink.getTail());
            //try to reverse it
            net.removeLink(auxLink);
            try{
                net.createLink(auxLink.getHead(), auxLink.getTail());
                if (net.isADag()){
                    reverseLinks.insertLink(auxLink);
                }
                net.removeLink(auxLink2);
            } catch(InvalidEditException e4){
            }
            net.createLink(auxLink.getTail(), auxLink.getHead());
            
        }
        
        //System.out.println("Puedo invertir "+reverseLinks.size()+" arcos");
        
        
        //num total de movimientos son los de los 3 posibles movim
        
        int nTotal = addLinks.size() + removeLinks.size() + reverseLinks.size();
        
        //elijo un numero de esos:
        
        int nMove = (int)((Math.random())*nTotal);
        //System.out.println("Elijo el movimiento : "+nMove);
        
        
        if (nMove < addLinks.size()){
            //he elegido a�adir
            movement = ADD;
            t = ((Link)addLinks.elementAt(nMove)).getTail();
            h = ((Link)addLinks.elementAt(nMove)).getHead();
            move.add(new Integer(movement));
            move.add(t);
            move.add(h);
            //System.out.println("Elijo a�adir "+t.getName()+"->"+h.getName());
        }
        
        if ((addLinks.size() <= nMove)&&(nMove < addLinks.size()+removeLinks.size())){
            //he elegido a�adir
            movement = REMOVE;
            t = ((Link)removeLinks.elementAt(nMove-addLinks.size())).getTail();
            h = ((Link)removeLinks.elementAt(nMove-addLinks.size())).getHead();
            move.add(new Integer(movement));
            move.add(t);
            move.add(h);
            //System.out.println("Elijo borrar "+t.getName()+"->"+h.getName());
        }
        
        if (nMove >= addLinks.size()+removeLinks.size()){
            //he elegido a�adir
            movement = REVERSE;
            t = ((Link)reverseLinks.elementAt(nMove-addLinks.size()-removeLinks.size())).getTail();
            h = ((Link)reverseLinks.elementAt(nMove-addLinks.size()-removeLinks.size())).getHead();
            move.add(new Integer(movement));
            move.add(t);
            move.add(h);
            // System.out.println("Elijo invertir "+t.getName()+"->"+h.getName());
        }
        
        
        if (move.size()!=3)
            System.out.println("ojo, el movimiento no se ha elegido bien");
        
        
        return move;
        
    }//end generate move
    
    
    
    
    /**
     *
     * Performs structural learning from dataBases with discrete and continuous
     * variables. It's a hill-climbing algorithm based on a metric.
     *
     */
    
    public void structuralLearning() throws InvalidEditException{
        
        int numVars, numCases, numLinks ;
        double oldLog, oldDim, newLog, newDim, newLog1, newDim1, newLog2, newDim2;
        double oldLog1, oldLog2, oldDim1, oldDim2, oldMax, newMax, newMax1, newMax2;
        double maxQuality, auxQuality, quality, log, dim;
        double maxMax; //el valor maximo de prob cond para el movimiento elegido en cada iteracion
        double[] values = new double[3];
        boolean firstTime;
        String key, maxMovement;
        double maxLikelihood = 0, maxPenal = 0 ;
        
        
        
        NodeList vars = cases.getVariables();
        Node head, tail, var, parent;
        Link maxLink;
        
        numVars = cases.getVariables().size();
        numCases = cases.getNumberOfCases();
        
        //to print the quality as sum of the likelihood y the penalisation
        double likelihood = 0, penal = 0;
        double max;
        
        //INICIALIZO LA RED INICIAL (VACIA)
        Bnet net = new Bnet();
        Vector initialResults = new Vector();
        initialResults = setInitialBNet(net);
        quality = ((Double)initialResults.elementAt(0)).doubleValue();
        likelihood = ((Double)initialResults.elementAt(1)).doubleValue();
        penal = ((Double)initialResults.elementAt(2)).doubleValue();
        max = ((Double)initialResults.elementAt(3)).doubleValue();
        //YA LA HE INICIALIZADO
        //System.out.println("Inicializo la red inicial");
        
        //maximum in the last iteration
        double maxLastIterat = max;
        
        if (Double.isInfinite(quality)){
            quality =  0 - Double.MAX_VALUE;
            //System.out.println("*************  quality es -Infinito ******************");
        }
        
        double oldQuality = quality;
        double oldLikelihood = likelihood;
        double oldPenal = penal;
        boolean beginning = true;
        Vector relations = new Vector();
        
        double maxCurrentIterat;
        
        while ( (oldQuality < quality) || beginning ){
            
            if (!beginning)
                System.out.println("\n\n***************  NEXT ITERATION: ********************************************************");
            else
                System.out.println("\nQUALITY: "+quality);
            
            System.out.println("The last quality is "+oldQuality+" and the current one is "+quality);
            
            oldQuality = quality;
            oldLikelihood = likelihood;
            oldPenal = penal;
            
            
            //------------------------------------------------------------------
            //PRUEBO CON TODOS LOS MOVIMIENTOS POSIBLES Y CALCULO SU Q
            Vector maxResults = new Vector();
            maxResults = getBestNet(net, oldQuality, oldLikelihood, oldPenal, maxLastIterat);
            maxQuality = ((Double)maxResults.elementAt(0)).doubleValue();
            maxLikelihood = ((Double)maxResults.elementAt(1)).doubleValue();
            maxPenal = ((Double)maxResults.elementAt(2)).doubleValue();
            maxMax = ((Double)maxResults.elementAt(3)).doubleValue();
            maxLink = (Link) maxResults.elementAt(4);
            maxMovement = (String) maxResults.elementAt(5);
            //------------------------------------------------------------------
            //YA HE TERMINADO DE PROBAR CON TODOS LOS MOVIMIENTOS Y HE ENCONTRADO EL
            //QUE MAXIMIZA LA CALIDAD
            
            
            //YA TENEMOS LA RED QUE MAXIMIZA LA CALIDAD:
            
            quality = maxQuality;
            
            //oldQuality no me vale la anterior, ya que no puedo compararla conla actual dado
            //q usa otra constante como maximo para normalizar, lo q hago es normalizar la anterior
            //con la constante actual del maximo
            //si hiciese ese movimiento el maximo seria maxMax
            //oldQuality = oldQuality - numVars*numCases*((Math.log(maxMax))) +
            //numVars*numCases*((Math.log(maxLastIterat)));
            
            //if the quality increases, make the movement
            if ( quality - oldQuality > 0.00000000001 ) {
                
                //como en este se mejora, para q permita detedctar otro opt local
                //empeoraPoco = false;
                
                //make the movement that maximise the quality
                tail = maxLink.getTail();
                head = maxLink.getHead();
                
                if (maxMovement.compareTo("Add")==0){
                    net.createLink(tail, head);
                }
                if (maxMovement.compareTo("Remove")==0){
                    net.removeLink(tail, head);
                }
                if (maxMovement.compareTo("Reverse")==0){
                    net.removeLink(tail, head);
                    net.createLink(head, tail);
                }
                
                System.out.print("\nquality that I chose: "+maxQuality+ " corresponding to ");
                System.out.println(maxMovement+" "+tail.getName()+" -> "+head.getName());
                System.out.println("likelihood ("+maxLikelihood+") - penal ("+maxPenal+")\n");
                quality = maxQuality;
                likelihood = maxLikelihood;
                penal = maxPenal;
            } else{
                System.out.println("\n\nquality does not increase, I consider the last one");
                System.out.println("The quality is: "+quality);
                
                //set the relations
                
                MTELearning l = new MTELearning(cases) ;
                ContinuousProbabilityTree tree;
                NodeList X;
                Configuration conf;
                PotentialContinuousPT p;
                relations = new Vector();
                Node y;
                for (int i = 0; i < numVars; i++){
                    y = net.getNodeList().elementAt(i);
                    X =  y.getParentNodes();
                    NodeList variables = new NodeList();
                    variables.insertNode(y);
                    for (int j = 0; j < X.size(); j++){
                        variables.insertNode(X.elementAt(j));
                    }
                    tree = l.learnConditional(y , X, cases, numSplit, numPoints);
                    p = new PotentialContinuousPT(variables, tree);
                    Relation relationy = new Relation(y);
                    relationy.setValues(p);
                    relations.insertElementAt(relationy, i);
                }
                
                net.setRelationList(relations);
                
            }//end else
            
            beginning = false;
            
        }//end of while
        
        output = net;
        
    }//end structuralLearning
    
    
    /**
     * Calculates the sum, in all cases, of the logarithm of the conditional
     * probability, for the current variable. Also, returns the maximum of these probabilities.
     *
     * @param y the objetive variable
     * @return results two doubles: the semiquality and the maximum for the variable y
     *
     */
    
    public double[] semiQuality( Node y, PotentialContinuousPT p ){
        
        double[] result = new double[2];
        double maxi = Double.NEGATIVE_INFINITY;
        double semiQuality = 0;
        double value;
        MTELearning l = new MTELearning(cases) ;
        ContinuousProbabilityTree tree;
        NodeList X = y.getParentNodes();
        Configuration conf;
        NodeList variables = new NodeList();
        variables.insertNode(y);
        for (int i = 0; i < X.size(); i++){
            
            variables.insertNode(X.elementAt(i));
            
        }
        
        CaseList caseList = cases.getCases();
        tree = l.learnConditional(y , X, cases, numSplit, numPoints);
        p = new PotentialContinuousPT(variables, tree);
        
        for (int i = 0; i < cases.getNumberOfCases(); i++){
            conf = caseList.get(i);
            value = p.getValue(conf);
            //maxi es el maximo del valor de la dist cond para todos los casos
            if (value > maxi ) maxi = value;
            
            if ( value < 0 ){
                //System.out.println("*************** log de un n. negativo ***************");
            }
            
            if ( value == 0 ){
                //System.out.println("*************** log de 0 ***************");
            }
            
            //if it's 0, I assign the smallest double
            if ( value == 0){
                //System.out.println("sale un valor 0");
                value = 0 - Double.MAX_VALUE;
            }else
                value = (Math.log(value));
            
            //if ((new Double(value)).isNaN()) System.out.println("Un valor tiene log NaN");
            
            semiQuality = semiQuality + value;
        }
        
        if ((new Double(semiQuality)).isInfinite()){
            //System.out.println("*************** sale -infinity ********************");
            semiQuality = 0 - Double.MAX_VALUE;
        }
        
        result[0] = semiQuality;
        result[1] = maxi;
        
        //if (maxi == Double.NEGATIVE_INFINITY) System.out.println("el maximo sale infinito");
        
        return result;
        
    }//end semiQuality
    
    
    /**
     * Calculates the dimension of the var's family, i.e. var and its parents.
     * It's the product of |Xi| for each member, where |Xi| is the number of
     * states, if it is finite-states, or the number of splits, if it is continuous.
     *
     * @ param var the objective variable
     * @ return dimension the dimension
     */
    
    public int dimension( Node var){
        
        int size, type, dimension;
        Vector nodesSet;
        Node currentNode;
        
        //nodesSet is the set of nodes which dimension I'm going to calculate
        nodesSet = new Vector();
        nodesSet = var.getParentNodes().getNodes();
        //I'm not considering var, only its parents
        size = nodesSet.size();
        dimension = 1;
        
        for (int i = 0; i < size; i++){
            currentNode = (Node) nodesSet.elementAt(i);
            type = currentNode.getTypeOfVariable();
            //we consider the number of values, if var is discrete, and the number
            //of splits, if it's continuous
            if (type!=Node.CONTINUOUS && type!=Node.MIXED){
                //is finite-states
                dimension = dimension * ((FiniteStates) currentNode).getNumStates();
            } else{
                dimension = dimension * numSplit;
            }
        }
        //if var has no any parent, return 1
        return dimension;
        
    }//end dimension
    
    
    /**
     * Check if this configuration: the current variable and its parents, has been studied before.
     * If it has, the dimension, the semiQuality and the maximum of the values of the density functions are stored in the hash table.
     * Else, they have to be calculated. Returns these values.
     *
     * @ param var the objective variable
     * @ param net the current Bnet
     * @ retun values three values: the semiQuality (0), the dimension of var (1) and the maximum (2)
     */
    
    public double[] getValues(Node var, Bnet net){
        String key;
        Node parent;
        double[] values = new double[3];
        int i,k,aux;
        int N = var.getParentNodes().size();
        double maxi;
        
        
        //I have to sort parents' ID, to don't distinguish, for example, x1|x2,x3 from x1|x3,x2
        
        PotentialContinuousPT pot = new PotentialContinuousPT();
        NodeList parents = new NodeList();
        parents = var.getParentNodes();
        Vector vectorID = new Vector();
        int[] id  = new int [var.getParentNodes().size()];
        
        //vectorID has parents' Id
        for (int j = 0; j < var.getParentNodes().size(); j++){
            parent = parents.elementAt(j);
            id [j] = net.getNodeList().getId(parent);
        }
        
        //sort the vector, using burble method
        for(i=0;i<N-1;i++)
            for(k=0;k<N-i-1;k++)
                if(id[k+1]<id[k]) {
            aux=id[k+1];
            id[k+1]=id[k];
            id[k]=aux;
                }
        
        key = (new Integer(net.getNodeList().getId(var))).toString()+"/";
        for (int j = 0; j < var.getParentNodes().size(); j++){
            key = key+(id[j])+"_";
        }
        key = "s"+key;
        
        int ki = 1;
        if ( table.get(key)==null){
            //if they are not into the table, I have to calculate them
            
            double[] resultSemiquality = new double[2];
            resultSemiquality = ((double[])semiQuality( var, pot));
            values[0] = resultSemiquality[0];
            maxi = resultSemiquality[1];
            //en values[1] meto tb el ki, q es 14 si la var es cont y numStates-1 si es discreta
            int type = var.getTypeOfVariable();
            if (type!=Node.CONTINUOUS && type!=Node.MIXED){
                //is finite-states
                ki = ((FiniteStates) var).getNumStates()-1;
                
            } else{
                ki = 14;
            }
            
            
            values[1] = ki*dimension(var);
            //el maximo de las prob cond para esa variable
            values[2] = maxi;
            //Save these values with the respective distribution into the hash table
            table.put(key, values);
        }//end if
        else{
            //if they are into the table, recover them
            values = (double[]) table.get(key);
        }
        
        return values;
        
    }//end getValues
    
    
    
    public Bnet getOutput(){
        System.out.println("\nNet has been learnt sucessfuly :");
        System.out.println("Number of Nodes: "+cases.getVariables().size());
        System.out.println("Number of Links: "+output.getLinkList().size());
        return output;
    }
    
    
    /**
     * Computes the log-likelihood of the net over the data; also returns the maximum
     * of the values of the density functions.
     *
     * @param net the Bnet
     * @return results two doubles: the logLikelihood and the maximum for the variable
     */
    
    public double[] logLikelihood(Bnet net){
        
        double[] results = new double[2];
        double logLikelihood = 0;
        double aux;
        int numVars = cases.getVariables().size();
        int numCases = cases.getNumberOfCases();
        double[] values = new double[3];
        Node var;
        //maximo para todas las variables y casos de la prob cond
        double max = Double.NEGATIVE_INFINITY;
        //maximo de dist cond para una variable (para todos los casos)
        double maxi = 0;
        
        for (int i = 0; i < numVars; i++){
            var = cases.getVariables().elementAt(i);
            values = getValues(var, net);
            //esta es la suma en todos los casos del log de la prob condic
            aux = values[0];
            maxi = values[2];
            if(maxi > max) max = maxi;
            logLikelihood = logLikelihood + aux;
        }
        
        //to normalise, remain  n*m*log max
        //logLikelihood = logLikelihood - numVars*numCases*((Math.log(max)));
        
        results[0] = logLikelihood;
        results[1] =  max;
        
        return results;
    }
    
    
    /**
     * Calculates the number of coincident links between two Bnets.
     *
     * @param learnt a Bnet
     * @param original a Bnet
     * @return an integer
     */
    
    public int sameArcs(Bnet learnt, Bnet original){
        
        int i ;
        LinkList learnList, originalList, interList;
        Link l;
        
        originalList = original.getLinkList();
        learnList = learnt.getLinkList();
        
        interList = learnList.intersection(originalList);
        
        
        return interList.size();
        
    }
    
    
    /**
     * Calculates the number of reversed links between two Bnets.
     *
     * @param learnt a Bnet
     * @param original a Bnet
     * @return an integer
     */
    
    public int reversedArcs(Bnet learnt, Bnet original){
        
        int i, rev = 0;
        LinkList learnList, originalList;
        Link l, lReversed;
        Node a , b;
        
        originalList = original.getLinkList();
        learnList = learnt.getLinkList();
        
        for(i=0 ; i < learnList.size() ; i++){
            
            l = learnList.elementAt(i);
            a = l.getHead();
            b = l.getTail();
            
            lReversed = new Link(a,b);
            
            if(originalList.indexOf(lReversed)>= 0)
                rev++;
            
        }
        
        return rev;
    }
    
    
    //lleva a cabo el movimiento indicado y calcula los nuevos valores de quality, likel, penal y max
    //al final pone si es un dag o no
    
    public Vector performMove(Bnet net, int movement, Link link, Vector oldResults) throws elvira.InvalidEditException{
        
        //System.out.println("Entro en el performMove");
        boolean isDag = true;
        
        double oldQuality, oldLikelihood, oldPenal, maxLastIterat;
        oldQuality = ((Double)oldResults.elementAt(0)).doubleValue();
        oldLikelihood = ((Double)oldResults.elementAt(1)).doubleValue();
        oldPenal = ((Double)oldResults.elementAt(2)).doubleValue();
        maxLastIterat = ((Double)oldResults.elementAt(3)).doubleValue();
        
        int numVars = net.getNodeList().size();
        int numCases = cases.getNumberOfCases();
        Vector results = new Vector();
        double[] values = new double[3];
        Node var;
        double oldLog, oldDim, newLog, newDim, newMax, auxQuality;
        double oldLog1, oldDim1, newLog1, newDim1, newMax1;
        double oldLog2, oldDim2, newLog2, newDim2, newMax2;
        //(las tengo q inicializar a algo)
        double quality = 0, likelihood = 0, penal = 0, maxCurrentIterat = 0;
        
        
        
        switch(movement){
            
            case ADD:
                //si elegimos a�adir un enlace, solo hay q modificar lo q respecta a la cabeza
                var = link.getHead();
                values = new double[3];
                //�?esto no puede dar el q no debe, si cambia el linklist????
                //net.getLinkList().getID(link.getTail().getName(), link.getHead().getName());
                values = new double[3];
                values = getValues(var, net);
                oldLog = values[0];
                oldDim = (int) values[1];
                //try to add
                //System.out.println("a�adir "+link);
                
                //System.out.println("Trato de crear el enlace");
                
                //si se puede a�adir, lo a�ado, sino elijo otros nodos
                try{
                    //System.out.println("Numero de links: "+net.getLinkList().size());
                    net.createLink(link.getTail(), link.getHead());
                    isDag = true; //sino se habria salido
                    //net.removeLinkOnly(new Link(net.getNodeList().elementAt(i), net.getNodeList().elementAt(j)));
                } catch (InvalidEditException e3){
                    isDag  = false;
                    //System.out.println("Numero de links: "+net.getLinkList().size());
                    //System.out.println("Al a�adir ya no es un DAG ");
                    //net.removeLinkOnly(new Link(vars.elementAt(i), vars.elementAt(j)));
                }
                
                //System.out.println("Acabo de crear el enlace");
                
                if (isDag){
                    values = new double[3];
                    values = getValues(var, net);
                    newLog = values[0];
                    newDim = (int) values[1];
                    //maximo para la variable var con este movimiento
                    newMax = values[2];
                    
                    
                    //recalcule quality
                    //si el maximo para esta variable con este movimiento es mayor q el maximo
                    //q teniamos en la etapa anterior , lo intercambiamos (los maximos de las otras variables
                    //no cambian respecto a la etapa anterior)
                    if (newMax > maxLastIterat) maxCurrentIterat = newMax;
                    else maxCurrentIterat = maxLastIterat;
                    quality = oldQuality + newLog - oldLog - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)))
                    - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                    
                    likelihood = oldLikelihood + newLog - oldLog - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)));
                    penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                    if (Double.isInfinite(quality)){
                        auxQuality =  0 - Double.MAX_VALUE;
                        //System.out.println("*************  quality es -Infinito ******************");
                    }
                }
                break;
                
            case REMOVE:
                //si elegimos borrar un enlace, solo hay q modificar lo q respecta a la cabeza
                values = new double[3];
                values = getValues(link.getHead(), net);
                oldLog = values[0];
                oldDim = values[1];
                //try to remove
                //System.out.println("borrar "+link);
                net.removeLink(link.getTail(), link.getHead());
                values = new double[3];
                values = getValues(link.getHead(), net);
                
                newLog = values[0];
                newDim = (int) values[1];
                newMax = values[2];
                //si el maximo para esta variable con este movimiento es mayor q el maximo
                //q teniamos en la etapa anterior , lo intercambiamos (los maximos de las otras variables
                //no cambian respecto a la etapa anterior)
                if (newMax > maxLastIterat) maxCurrentIterat = newMax;
                else maxCurrentIterat = maxLastIterat;
                quality = oldQuality + newLog - oldLog
                        - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)))
                        - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                
                likelihood = oldLikelihood + newLog - oldLog
                        - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)));
                penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                
                if (Double.isInfinite(quality)){
                    auxQuality =  0 - Double.MAX_VALUE;
                    //System.out.println("*************  quality es -Infinito ******************");
                }
                break;
            case REVERSE:
                //si elegimos invertir un enlace, hay q modificar la cabeza y la cola
                values = new double[3];
                values = getValues(link.getHead(), net);
                oldLog1 = values[0];
                oldDim1 = values[1];
                
                values = new double[3];
                values = getValues(link.getTail(), net);
                oldLog2 = values[0];
                oldDim2 = values[1];
                oldLog = oldLog1 + oldLog2;
                oldDim = oldDim1 + oldDim2;
                
                net.removeLink(link.getTail(), link.getHead());
                net.createLink(link.getHead(),link.getTail());
                values = new double[3];
                values =  getValues(link.getHead(), net);
                newLog1 = values[0];
                newDim1 = (int) values[1];
                newMax1 = values[2];
                
                values = new double[3];
                values = getValues(link.getTail(), net);
                newLog2 = values[0];
                newDim2 = (int) values[1];
                newMax2 = values[2];
                newLog = newLog1 + newLog2;
                newDim = newDim1 + newDim2;
                //considero el mayor de los dos
                if (newMax1 > newMax2) newMax = newMax1;
                else newMax = newMax2;
                
                //si el maximo para alguna de estas variables con este movimiento es mayor q el maximo
                //q teniamos en la etapa anterior  , lo intercambiamos (los maximos de las otras variables
                //no cambian respecto a la etapa anterior)
                if (newMax > maxLastIterat) maxCurrentIterat = newMax;
                else maxCurrentIterat = maxLastIterat;
                quality = oldQuality + newLog - oldLog
                        - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)))
                        - (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                
                likelihood = oldLikelihood + newLog - oldLog
                        - numVars*numCases*((Math.log(maxCurrentIterat))) + numVars*numCases*((Math.log(maxLastIterat)));
                penal = oldPenal + (0.5)*(newDim - oldDim)*((Math.log(numCases)));
                if (Double.isInfinite(quality)){
                    auxQuality =  0 - Double.MAX_VALUE;
                    //System.out.println("*************  quality es -Infinito ******************");
                }
                break;
                
        }//end switch
        
        if(isDag){
            
            //System.out.println("calidad para este vecino: "+quality);
            results.add(new Double(quality));
            results.add(new Double(likelihood));
            results.add(new Double(penal));
            results.add(new Double(maxCurrentIterat));
            
        }
        
        if (!isDag){
            results.add(new Integer(0));
        }
        
        //System.out.println("Acabo el performMove");
        
        return results;
        
    }
    
    
    
    public int numAddLinks(Bnet net){
        
        LinkList addLinks = new LinkList();
        
        NodeList vars = cases.getVariables();
        Node head, tail, var, parent;
        Link maxLink, auxLink;
        
        int numVars = cases.getVariables().size();
        int numCases = cases.getNumberOfCases();
        
        //check which arcs can be added
        
        for (int i = 0 ; i < numVars ; i++){
            //test every combination
            for (int j = i+1; j < numVars ; j++){
                
                //with this combination, I have two possible arcs: one and the reverted
                //we have to make sure no one already exits in the net, and that
                //adding it the net has not directed cicle
                if ((net.getLink(vars.elementAt(i), vars.elementAt(j))==null)
                && (net.getLink(vars.elementAt(j), vars.elementAt(i))==null)){
                    //test i -> j
                    Link enlace = new Link(vars.elementAt(i), vars.elementAt(j));
                    auxLink = new Link(vars.elementAt(i), vars.elementAt(j));
                    
                    try{
                        net.createLink(vars.elementAt(i), vars.elementAt(j), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(i), vars.elementAt(j)));
                    } catch (InvalidEditException e3){
                    }
                    
                    //test j -> i
                    auxLink = new Link(vars.elementAt(j), vars.elementAt(i));
                    try{
                        net.createLink(vars.elementAt(j), vars.elementAt(i), true);
                        if (net.isADag()){
                            addLinks.insertLink(auxLink);
                        }
                        net.removeLinkOnly(new Link(vars.elementAt(j), vars.elementAt(i)));
                    } catch (InvalidEditException e2){
                    }
                }//end if
            }//end j
        }//end i
        
        return (addLinks.size());
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws IOException, ParseException, elvira.InvalidEditException, java.lang.Throwable {
        
        FileInputStream f, f3;
        FileWriter f2;
        DataBaseCases cases;
        Bnet net, originalNet;
        StructuralMTELearning l;
        
        if(args.length < 3){
            System.out.println("too few arguments: Usage: file.dbc outNet.elv originalNet.elv ");
            System.exit(0);
        }
        
        //Learns a network from a database, with this algorithm
        
        f3 = new FileInputStream(args[2]);
        originalNet = new Bnet(f3);
        f3.close();
        
        f = new FileInputStream(args[0]);
        cases = new DataBaseCases(f);
        f.close();
        
//        int nIter = Integer.valueOf(args[2]).intValue();
//        double temp = Double.valueOf(args[3]).doubleValue();
//        int nRepet = Integer.valueOf(args[4]).intValue();
        
        l = new StructuralMTELearning(cases);
        
        //l.structuralLearning();
        //System.out.println("voy a hacer el hill climbing");
        //l.structuralLearning();
        System.out.println("voy a hacer el simulated annealing");
        l.simulatedAnnealing();
        
        f2 = new FileWriter(args[1]);
        net = l.getOutput();
        net.saveBnet(f2);
        f2.close();
        
        
        
        
        //para comparar:
        
        
        
        //calculo la log verosimilitud de las dos redes con
        //respecto a esa base de datos
        
        
        //para la red resultado muchas ya las habre calculado
        double logOut = l.logLikelihood(net)[0];
        double maxOut =  l.logLikelihood(net)[1];
        System.out.println("\n\n**************************************************");
        System.out.println("log-verosimilitud para la red resultado: "+logOut+" y el max "+maxOut);
        
        //para la red original lo tengo q hacer todo
        /**
            double semiQuality;
            double value;
            Node y;
            Configuration conf;
            PotentialContinuousPT p;
            int numVars = l.cases.getVariables().size();
            //maximo para todas las variables y casos de la prob cond
            double max = Double.NEGATIVE_INFINITY;
            //maximo de dist cond para una variable (para todos los casos)
            double maxi = Double.NEGATIVE_INFINITY;
            int numCases = cases.getNumberOfCases();
            double logOriginal = 0;
 
 
            //sumo en todas las variables
            for (int k = 0; k < numVars; k++){
                semiQuality = 0;
 
                y = cases.getVariables().elementAt(k);
                CaseList caseList = l.cases.getCases();
 
                //ya esta calculado????????
                p = (PotentialContinuousPT) originalNet.getRelation(y).getValues();
 
                //sumo en todos los casos el log ....
                for (int i = 0; i < cases.getNumberOfCases(); i++){
                    conf = caseList.get(i);
                    value = p.getValue(conf);
                    if (value > maxi ) maxi = value;
                    //System.out.println("valor para el caso "+i+" : "+value+" y variable "+k);
                    if ( value < 0 ){
                        //System.out.println("*************** log de un num negativo ***************");
                    }
                    //si es 0 le asigno el menor double
                    if ( value == 0){
                        value = 0 - Double.MAX_VALUE;
                    }else{
                        value = (Math.log(value));
                    }
                    semiQuality = semiQuality + value;
                }
                if ((new Double(semiQuality)).isInfinite()){
                    semiQuality = 0 - Double.MAX_VALUE;
                    //System.out.println("*************** sale -infinity ********************");
                }
 
                //System.out.println("max de la var "+k+" : "+maxi);
                if(maxi > max) max = maxi;
                //sumo en todas las variables el semiquality
                logOriginal = logOriginal + semiQuality;
 
            }//end for k ( en las variables)
 
            //System.out.println("max en todas las variables "+max);
 
            //compruebo cual es mayor si este max o el maxOut
 
            //como hay q normalizar, le resto n*m*log max
 
            if (max < maxOut ){
                //normalizo con el max de out
                //logOriginal = logOriginal - numVars*numCases*((Math.log(maxOut)));
            }
            else{
                //normalizo con el max de la original
                //logOriginal = logOriginal - numVars*numCases*((Math.log(max)));
                //normalizo el de la out con el max de la original
                //logOut = logOut + numVars*numCases*((Math.log(maxOut))) - numVars*numCases*((Math.log(max)));
            }
 
 
 
            System.out.println("log-verosimilitud para la red original: "+logOriginal);
 
        int eq = l.sameArcs(net,originalNet);
            int rev = l.reversedArcs(net,originalNet);
            int total = net.getLinkList().size();
            System.out.println("Numero de arcos coincidentes: "+eq);
            System.out.println("Numero de arcos invertidos: "+rev);
            System.out.println("Numero de arcos erroneos: "+(total -eq - rev));
           // System.out.println("log Original: "+logOriginal+" , log Learnt: "+logOut);
         */
    }//end main
    
    /**
     * @param args the command line arguments
     */
    /**
     *
     * public static void main(String[] args) throws IOException, ParseException, elvira.InvalidEditException, java.lang.Throwable {
     *
     * FileInputStream f,f3;
     * FileWriter f2;
     * DataBaseCases cases;
     * Bnet outNet, netPGM, originalNet;
     * StructuralMTELearning_EnfriamientoPartoPGM l_Enfriamiento;
     *
     * //        if(args.length < 5){
     * //            System.out.println("too few arguments: Usage: file.dbc file.elv nIter Temperature nRepeticiones");
     * //            System.exit(0);
     * //        }
     *
     * //Learns a network from a database, with this algorithm
     *
     * //        f = new FileInputStream(args[0]);
     * //        cases = new DataBaseCases(f);
     * //        f.close();
     * //
     * f = new FileInputStream("segundaVez100_0Enf.dbc");
     * cases = new DataBaseCases(f);
     * f.close();
     *
     * //        int nIter = Integer.valueOf(args[2]).intValue();
     * //        double temp = Double.valueOf(args[3]).doubleValue();
     * //        int nRepet = Integer.valueOf(args[4]).intValue();
     * //
     * //con esta comparo
     * f3 = new FileInputStream("basesRedesSalidas/salida0_100_red15EnfPartoPGM.elv");
     * originalNet = new Bnet(f3);
     * f3.close();
     *
     *
     * l_Enfriamiento = new StructuralMTELearning_EnfriamientoPartoPGM(cases);
     *
     * //uso la del pgm como punto de partida
     * // netPGM = new Bnet(new FileInputStream("basesRedesSalidas/salida0_100_red15PGM.elv"));
     *
     *
     * //uso la red q obtengo como punto de partida para
     * //el enfriamiento simulado
     * //    System.out.println("La red de la que parto tiene "+netPGM.getLinkList().size()+" enlaces");
     *
     * //    System.out.println("\nEMPIEZO EL ENFRIAMIENTO SIMULADO\n");
     * //    l_Enfriamiento.simulatedAnnealing(netPGM);
     * //    System.out.println("\nACABO EL ENFRIAMIENTO SIMULADO\n");
     *
     * l_Enfriamiento.structuralLearning();
     *
     *
     * f2 = new FileWriter("red100_0AprendoSegundaVezComparoconEnf.elv");
     * outNet = l_Enfriamiento.getOutput();
     * outNet.saveBnet(f2);
     * f2.close();
     *
     *
     *
     *
     *
     * //calculo la log verosimilitud de las dos redes con
     * //respecto a esa base de datos
     *
     *
     * //para la red resultado muchas ya las habre calculado
     * double logOut = l_Enfriamiento.logLikelihood(outNet)[0];
     * double maxOut =  l_Enfriamiento.logLikelihood(outNet)[1];
     * System.out.println("\n\n**************************************************");
     * System.out.println("log-verosimilitud para la red resultado: "+logOut+" y el max "+maxOut);
     *
     * //para la red original lo tengo q hacer todo
     *
     * double semiQuality;
     * double value;
     * Node y;
     * Configuration conf;
     * PotentialContinuousPT p;
     * int numVars = l_Enfriamiento.cases.getVariables().size();
     * //maximo para todas las variables y casos de la prob cond
     * double max = Double.NEGATIVE_INFINITY;
     * //maximo de dist cond para una variable (para todos los casos)
     * double maxi = Double.NEGATIVE_INFINITY;
     * int numCases = cases.getNumberOfCases();
     * double logOriginal = 0;
     *
     *
     * //sumo en todas las variables
     * for (int k = 0; k < numVars; k++){
     * semiQuality = 0;
     *
     * y = cases.getVariables().elementAt(k);
     * CaseList caseList = l_Enfriamiento.cases.getCases();
     *
     * //ya esta calculado????????
     * p = (PotentialContinuousPT) originalNet.getRelation(y).getValues();
     *
     * //sumo en todos los casos el log ....
     * for (int i = 0; i < cases.getNumberOfCases(); i++){
     * conf = caseList.get(i);
     * value = p.getValue(conf);
     * if (value > maxi ) maxi = value;
     * //System.out.println("valor para el caso "+i+" : "+value+" y variable "+k);
     * if ( value < 0 ){
     * //System.out.println("*************** log de un num negativo ***************");
     * }
     * //si es 0 le asigno el menor double
     * if ( value == 0){
     * value = 0 - Double.MAX_VALUE;
     * }else{
     * value = (Math.log(value));
     * }
     * semiQuality = semiQuality + value;
     * }
     * if ((new Double(semiQuality)).isInfinite()){
     * semiQuality = 0 - Double.MAX_VALUE;
     * //System.out.println("*************** sale -infinity ********************");
     * }
     *
     * //System.out.println("max de la var "+k+" : "+maxi);
     * if(maxi > max) max = maxi;
     * //sumo en todas las variables el semiquality
     * logOriginal = logOriginal + semiQuality;
     *
     * }//end for k ( en las variables)
     *
     * //System.out.println("max en todas las variables "+max);
     *
     * //compruebo cual es mayor si este max o el maxOut
     *
     * //como hay q normalizar, le resto n*m*log max
     *
     * if (max < maxOut ){
     * //normalizo con el max de out
     * logOriginal = logOriginal - numVars*numCases*((Math.log(maxOut)));
     * }
     * else{
     * //normalizo con el max de la original
     * logOriginal = logOriginal - numVars*numCases*((Math.log(max)));
     * //normalizo el de la out con el max de la original
     * logOut = logOut + numVars*numCases*((Math.log(maxOut))) - numVars*numCases*((Math.log(max)));
     * }
     *
     *
     *
     * System.out.println("log-verosimilitud para la red original: "+logOriginal);
     *
     *
     *
     *
     *
     *
     *
     * int eq = l_Enfriamiento.sameArcs(outNet,originalNet);
     * int rev = l_Enfriamiento.reversedArcs(outNet,originalNet);
     * int total = outNet.getLinkList().size();
     * System.out.println("Numero de arcos coincidentes: "+eq);
     * System.out.println("Numero de arcos invertidos: "+rev);
     * System.out.println("Numero de arcos erroneos: "+(total -eq - rev));
     * // System.out.println("log Original: "+logOriginal+" , log Learnt: "+logOut);
     *
     *
     *
     * }//end main
     *
     */
    //programa de rafa para la revision del PGM 04, variando los parametros con la misma
    //estructura para ver los reslutados del aprendizaje independeientemente de los parametros
    /**
     * public static void main(String[] args) throws IOException, ParseException, elvira.InvalidEditException,java.lang.Throwable {
     *
     * double logOriginal = 0;
     * double logOut;
     * FileInputStream f, f3;
     * FileWriter f2;
     * DataBaseCases cases;
     * Bnet outNet, originalNet;
     * StructuralMTELearning_Enfriamiento l;
     * //System.out.println("Ahora se hacen solo 100 repeticiones");
     * if(args.length < 4){
     * System.out.println("too few arguments: Usage: RedOriginal Tama�oDBC iteracionEmpieza Results.txt");
     * System.exit(0);
     * }
     *
     * FileWriter fW = new FileWriter(args[3]);
     * //FileWriter fW = new FileWriter("resultadosDentro.txt");
     * //BufferedWriter bW = new BufferedWriter(fW);
     * PrintWriter pW = new PrintWriter(fW);
     *
     * int iter = Integer.valueOf(args[2]).intValue();
     *
     * //int nIter = Integer.valueOf(args[4]).intValue();
     * //double temp = Double.valueOf(args[5]).doubleValue();
     * //int nRepet = Integer.valueOf(args[6]).intValue();
     *
     * //for(int d=iter ; d < (iter+100) ; d++){
     * for(int d=iter ; d < (100) ; d++){
     *
     * logOriginal = 0;
     * //archivo desde el q se lee la base de datos
     * //f = new FileInputStream(args[0]);
     * //cases = new DataBaseCases(f);
     * //f.close();
     *
     * //archivo con la red original
     * //f3 = new FileInputStream(args[2]);
     * //originalNet = new Bnet(f3);
     * //f3.close();
     * // ESTO ES JUSTO LO QUE TENGO QUE CAMBIAR EN CADA PASADA
     * f = new FileInputStream("basesRedesSalidas/dbc"+args[1]+"new"+d+args[0].replace('.','_')+".dbc");
     * cases = new DataBaseCases(f);
     * f.close();
     *
     * f3 = new FileInputStream("basesRedesSalidas/new"+d+args[0]);
     * originalNet = new Bnet(f3);
     * f3.close();
     *
     * //aprendo la estructura con esa base de datos
     * l = new StructuralMTELearning_Enfriamiento(cases);
     * //l.structuralLearning();
     * l.simulatedAnnealing();
     *
     * //guardo la red resultado
     * f2 = new FileWriter("basesRedesSalidas/salida"+d+"_"+args[1]+"_red15.elv");
     * outNet = l.getOutput();
     * outNet.saveBnet(f2);
     * f2.close();
     *
     * //calculo la log verosimilitud de las dos redes con
     * //respecto a esa base de datos
     *
     *
     * //para la red resultado muchas ya las habre calculado
     * logOut = l.logLikelihood(outNet)[0];
     * double maxOut =  l.logLikelihood(outNet)[1];
     * System.out.println("\n\n**************************************************");
     * System.out.println("log-verosimilitud para la red resultado: "+logOut+" y el max "+maxOut);
     *
     * //para la red original lo tengo q hacer todo
     *
     * double semiQuality;
     * double value;
     * Node y;
     * Configuration conf;
     * PotentialContinuousPT p;
     * int numVars = l.cases.getVariables().size();
     * //maximo para todas las variables y casos de la prob cond
     * double max = Double.NEGATIVE_INFINITY;
     * //maximo de dist cond para una variable (para todos los casos)
     * double maxi = Double.NEGATIVE_INFINITY;
     * int numCases = cases.getNumberOfCases();
     *
     *
     *
     * //sumo en todas las variables
     * for (int k = 0; k < numVars; k++){
     * semiQuality = 0;
     *
     * y = cases.getVariables().elementAt(k);
     * CaseList caseList = l.cases.getCases();
     *
     * //ya esta calculado????????
     * p = (PotentialContinuousPT) originalNet.getRelation(y).getValues();
     *
     * //sumo en todos los casos el log ....
     * for (int i = 0; i < cases.getNumberOfCases(); i++){
     * conf = caseList.get(i);
     * value = p.getValue(conf);
     * if (value > maxi ) maxi = value;
     * //System.out.println("valor para el caso "+i+" : "+value+" y variable "+k);
     * if ( value < 0 ){
     * //System.out.println("*************** log de un num negativo ***************");
     * }
     * //si es 0 le asigno el menor double
     * if ( value == 0){
     * value = 0 - Double.MAX_VALUE;
     * }else{
     * value = (Math.log(value));
     * }
     * semiQuality = semiQuality + value;
     * }
     * if ((new Double(semiQuality)).isInfinite()){
     * semiQuality = 0 - Double.MAX_VALUE;
     * //System.out.println("*************** sale -infinity ********************");
     * }
     *
     * //System.out.println("max de la var "+k+" : "+maxi);
     * if(maxi > max) max = maxi;
     * //sumo en todas las variables el semiquality
     * logOriginal = logOriginal + semiQuality;
     *
     * }//end for k ( en las variables)
     *
     * //System.out.println("max en todas las variables "+max);
     *
     * //compruebo cual es mayor si este max o el maxOut
     *
     * //como hay q 3izar, le resto n*m*log max
     *
     * if (max < maxOut ){
     * //normalizo con el max de out
     * logOriginal = logOriginal - numVars*numCases*((Math.log(maxOut)));
     * }
     * else{
     * //normalizo con el max de la original
     * logOriginal = logOriginal - numVars*numCases*((Math.log(max)));
     * //normalizo el de la out con el max de la original
     * logOut = logOut + numVars*numCases*((Math.log(maxOut))) - numVars*numCases*((Math.log(max)));
     * }
     *
     *
     *
     * System.out.println("log-verosimilitud para la red original: "+logOriginal);
     *
     *
     *
     * // Ahora voy a ver lo de los arcos coincidentes
     * FileInputStream f4 = new FileInputStream("basesRedesSalidas/salida"+d+"_"+args[1]+"_red15.elv");
     * Bnet learntNet = new Bnet(f4);
     * f4.close();
     *
     * f3 = new FileInputStream("basesRedesSalidas/new"+d+args[0]);
     * originalNet = new Bnet(f3);
     * f3.close();
     *
     * int eq = l.sameArcs(learntNet,originalNet);
     * int rev = l.reversedArcs(learntNet,originalNet);
     * int total = learntNet.getLinkList().size();
     * System.out.println("Numero de arcos coincidentes: "+eq);
     * System.out.println("Numero de arcos invertidos: "+rev);
     * System.out.println("Numero de arcos erroneos: "+(total -eq - rev));
     *
     *
     * //pW.print("\t");
     * pW.print(originalNet.getLinkList().size());
     * pW.print("\t");
     * pW.print(total);
     * pW.print("\t");
     * pW.print(eq);
     * pW.print("\t");
     * pW.print(rev);
     * pW.print("\t");
     * pW.print(total-eq-rev);
     * pW.print("\t");
     * pW.print(logOriginal);
     * pW.print("\t");
     * pW.println(logOut);
     *
     * System.out.println("Termino la iteracion "+d);
     * }
     *
     * //pW.close();
     *
     * fW.close();
     *
     * }//end main
     */
    
    
    
}//end StructuralMTELearning class
