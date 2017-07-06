/* ContinuousProbabilityTree.java */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.Math;
import elvira.*;
import elvira.inference.abduction.Explanation;
import elvira.potential.MixtExpDensity;
import java.io.*;
import elvira.learning.*;
import elvira.tools.ContinuousFunction;
import elvira.tools.LinearFunction;
import elvira.tools.QuadraticFunction;
import java.io.BufferedReader;
import elvira.tools.SampleGenerator;

/**
 * Implementation of class <code>ContinuousProbabilityTree</code>.
 * A continuous probability tree is a compact representation of a
 * probability function which depends on continuous and discrete variables.
 * Each internal node represents a variable (continuos or discrete) and each
 * leaf node represents a <code>MixtExpDensity</code>. Each discrete variable node
 * has as many children as possible values it has and each continuous
 * variable has an associated vector of cut points and as many children
 * as the size of this vector minus 1. Each child <code>i</code> corresponds
 * to the interval limited by the cut point <code>i</code> and cut
 * point <code>i+1</code>. The density stored in a leaf corresponds to
 * the probability of the configuration that leads from the root
 * node to that leaf.
 *
 * @since 19/7/2011
 * @version 1.59
 */

public class ContinuousProbabilityTree implements Serializable{
    
    static final long serialVersionUID = 7102459497928466836L;
    /**
     * Variable in this node of the tree.
     */
    Node var;
    
    /**
     * Value of the node (in case of being a leaf).
     */
    MixtExpDensity value;
    
    /**
     * Label of the node. Possible labels are dfined below as constants.
     */
    int label;
    
    /**
     * Vector of children of this node.
     */
    Vector child;
    
    /**
     * Vector of cut-points. They define the intervals in which the domain
     * of the variable is partitioned. There will be one sub-interval for each
     * child of this node.
     */
    Vector cutPoints;
    
    /**
     * Number of leaves below this node.
     */
    long leaves;
    
    
    /**
     * Number of exponential terms in which the continuous variables of the tree are splitted
     */
    
    int numSplits;
    
    /**
     * Number of exponential terms allowed as maximum (including a constant)
     */
    
    int numTerms;
    
    /**
     * Possible labels of a node.
     */
    static final int EMPTY_NODE=0;
    static final int DISCRETE_NODE=1;
    static final int PROBAB_NODE=2;
    static final int CONTINUOUS_NODE=3;
    
    
    /**
     * Constructor. Creates an empty tree node.
     */
    
    public ContinuousProbabilityTree() {
        
        label = EMPTY_NODE;
        leaves = 0;
        child=new Vector();
    }
    
    
    /**
     * Creates a tree with the argument (a discrete variable) as root node.
     * @param variable a <code>FiniteStates</code> variable.
     */
    
    public ContinuousProbabilityTree(FiniteStates variable) {
        
        int i, j;
        ContinuousProbabilityTree tree;
        
        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector();
        
        j = variable.getNumStates();
        for (i=0 ; i<j ; i++) {
            tree = new ContinuousProbabilityTree();
            tree.setVar((Node)variable);
            child.addElement(tree);
        }
    }
    
    /**
     * Creates a tree with the argument (a discrete variable) as root node
     * and assigning the values given as parameter
     * @param variable a <code>FiniteStates</code> variable.
     * @param values the values to store in the leaves
     */
    
    public ContinuousProbabilityTree(FiniteStates variable, double[] values) {
        
        int i, j;
        ContinuousProbabilityTree tree;
        
        
        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector();
        
        j = variable.getNumStates();
        for (i=0 ; i<j ; i++) {
            tree = new ContinuousProbabilityTree(values[i]);
            tree.setVar((Node)variable);
            child.addElement(tree);
        }
    }
    
    
    
    
    /**
     * Creates a tree with the argument (a continuous variable) as root node.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of the
     * continuous variable.
     */
    
    public ContinuousProbabilityTree(Continuous variable, Vector cp) {
        
        int i, j;
        ContinuousProbabilityTree tree;
        
        label = CONTINUOUS_NODE;
        leaves = 0;
        var = variable;
        child = new Vector();
        cutPoints = (Vector)  cp.clone();
        
        j = cp.size()-1;
        for (i=0 ; i<j ; i++) {
            tree = new ContinuousProbabilityTree();
            tree.setVar((Node)variable);
            child.addElement(tree);
        }
    }
    
    
    /**
     * Creates a tree with the argument (a continuous variable) as root node
     * and all its children being equal to a constant value given
     * as argument.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     * @param x the double value to store at the leaves.
     */
    
    public ContinuousProbabilityTree(Continuous variable, Vector cp, double x) {
        
        int i;
        ContinuousProbabilityTree tree;
        
        label = CONTINUOUS_NODE;
        leaves = cp.size() - 1;
        var = variable;
        child = new Vector();
        cutPoints = (Vector)  cp.clone();
        
        for (i=0 ; i<leaves ; i++) {
            tree = new ContinuousProbabilityTree(x);
            tree.setVar((Node)variable);
            child.addElement(tree);
        }
    }
    
    
    /**
     * Creates a probability node with a density that is constant and equal to
     * a given value.
     * @param p a double value.
     */
    
    public ContinuousProbabilityTree(double p) {
        
        label = PROBAB_NODE;
        value = new MixtExpDensity(p);
        leaves = 1;
    }
    
    
    /**
     * Creates a probability node with a given density.
     * @param f a density function.
     */
    
    public ContinuousProbabilityTree(MixtExpDensity f) {
        
        label = PROBAB_NODE;
        value = f.duplicate();
        leaves = 1;
    }
    
    
    /**
     * Creates a Continuous Probability Tree from
     * a discrete Probability Tree.
     * @param pt a <code>ProbabilityTree</code>
     */
    
    public ContinuousProbabilityTree(ProbabilityTree pt) {
        
        ContinuousProbabilityTree tree;
        int i, nv;
        
        child = new Vector();
        var = pt.getVar();
        label = DISCRETE_NODE;
        leaves = leaves;
        
        if (!pt.isProbab()) { // If it is not a probability,
            
            nv = pt.getChild().size();
            
            for (i=0 ; i<nv ; i++) {
                tree = new ContinuousProbabilityTree(pt.getChild(i));
                //tree.setVar((Node)var);
                child.addElement(tree);
            }
        } else
            assignProb(pt.getProb());
    }
    
    
    
    /*********************************** Aqui va el MAIN**************************************/
    //Main para podar un cpt
    //Primero hago un cpt con dos nodos, una variable X continua y un hijo suyo que es el
    // mte correspondiente.
    
    public static void main(String args[]){
        
        int i;
        ContinuousProbabilityTree cpt, hijo, hijo1, hijo2, hijo3;
        
        Continuous Y = new Continuous();
        Y.setMin(0);
        Y.setMax(2);
        Y.setName("Y");
        
        Vector cp = new Vector();
        cp.addElement(new Double(0));
        cp.addElement(new Double(1));
        cp.addElement(new Double(2));
        
        cpt = new ContinuousProbabilityTree(Y,cp);
        MixtExpDensity mte = new MixtExpDensity();
        
        // Hijo de Y
        Continuous X = new Continuous();
        X.setMin(0);
        X.setMax(2);
        X.setName("X");
        
        //hijo1 de X
        
        
        cp = new Vector();
        cp.addElement(new Double(0));
        cp.addElement(new Double(1));
        cp.addElement(new Double(2));
        
        mte = new MixtExpDensity(1,1,0.001,0.2,3,X);
        hijo = new ContinuousProbabilityTree(X,cp);
        hijo.label = CONTINUOUS_NODE;
        hijo1 = new ContinuousProbabilityTree(mte);
        hijo1.label = PROBAB_NODE;
        hijo.child.setElementAt(hijo1,0);
        
        //Ahora el hijo2 de X
        mte = new MixtExpDensity(3,1,-0.01,0.2,4,X);
        hijo2 = new ContinuousProbabilityTree(mte);
        hijo2.label = PROBAB_NODE;
        hijo.child.setElementAt(hijo2,1);
        
        cpt.child.setElementAt(hijo,0);
        
        // Segundo hijo de Y
        mte = new MixtExpDensity(3);
        hijo3 = new ContinuousProbabilityTree(mte);
        hijo3.label = PROBAB_NODE;
        cpt.child.setElementAt(hijo3,1);
        cpt.label = CONTINUOUS_NODE;
        
        System.out.println("Antes de hacer el prune");
        cpt.print();
        cpt.prune(cpt,0.1,0.1,0.1,0.1);
        System.out.println("Despues de hacer el prune");
        cpt.print();
        
        
    }
    
    
    
    /******************************* Acaba el main******************************************/
    
    
    
    
    /**
     * Learns a tree from a database. This method only learns univariate
     * densities. It uses the method estimate6 from MixtExpDensity
     * @param values The values of the variable to estimate.
     * @param X The continuous variable we will learn
     * @param intervals Number of intervals as maximum when splitting the domain.
     * @param numpoints Number of points used for each line in the derivative method
     *
     *
     * @return A tree representing the density learnt from that database
     */
    
    public static ContinuousProbabilityTree learnUnivariate(Continuous X, Vector values, int intervals, int numpoints){
        
        Vector x,y,result,cp,xInterval,yInterval;
        
        MixtExpDensity mixture = new MixtExpDensity();
        MixtExpDensity mixture2 = new MixtExpDensity();
        
        ContinuousProbabilityTree myTree = new ContinuousProbabilityTree();
        
        int i,j,pos;
        //number of equidistant point we generate the empirical density with
        int sampleSize = 1000;
        
        double xValue, yValue,max,min, first, last, firstNeg = 0, lastNeg = 1;
        boolean below; //A boolean showing rtue if the point is below the max
	//System.out.println("Variable que aprendo");
        //X.print();
      //  System.out.println("Voy a ejecutar el learnUnivariate sobre y que tiene max: "+X.getMax()+" y min: "+X.getMin());
        
        //System.out.println("Resultado de aplicar el learnUnivariate con los siguientes parametros:");
        //System.out.println("");
        //System.out.println("Numero de intervalos en los que partir el dominio: "+intervals);
        //System.out.println("Longitud con la que hemos creado el empiricDensity: "+length);
        //System.out.println("Numero de puntos con los que hace el derivative "+numpoints);
        //System.out.println("");
        //System.out.println("--------------------------");
        result = new Vector();
        
        x = new Vector();
        y = new Vector();
        
        values.add(new Double(X.getMax()));
        values.add(new Double(X.getMin()));
        
        
        //System.out.println("***********ORIGINAL VALUES***********************");
        for (i=0 ; i<values.size(); i++){
            xValue = ((Double)values.elementAt(i)).doubleValue();
            //System.out.println(xValue);
            
        }
        //System.out.println("***********ORIGINAL VALUES***********************");
        //System.out.println("Valores del empiric Density:");
        
        myTree.sort(values);
        //we use kernel densities to calculate the empirical density
        KernelDensity density = new KernelDensity();
        if(((Double)values.elementAt(0)).doubleValue() == ((Double)values.elementAt(values.size()-1)).doubleValue()){
            //System.out.println("All the values are the same. Return one value with one as density");
            x.addElement(values.elementAt(0));
            y.addElement(new Double(1));
        }else{
            //instead of considering the sample to estimate the kernel density
            //I'm going to not consider the first either the last, to avoid problems with
            //boundary bias
            //I consider an auxiliary vector, in which I store all the values except first and last
            //for (i=0 ; i<values.size() ; i++)
            //    System.out.println("Values "+((Double)values.elementAt(i)).doubleValue());
            Vector auxValues = new Vector();
            //copy values, sort it and remove min and max
            auxValues = (Vector) values.clone();
            myTree.sort(auxValues);
            double minimum = ((Double)auxValues.elementAt(0)).doubleValue();
            double maximum = ((Double)auxValues.elementAt(auxValues.size()-1)).doubleValue();
            auxValues.removeElementAt(values.size()-1);
            auxValues.removeElementAt(0);
            
            //if there are only two values, "for" will not be used
            x = new Vector();
            //I'm goint to consider sampleSize equidistant points to calculate the empirical density
            for (int m = 0 ; m <= sampleSize ; m++ ){
                double increment = (maximum - minimum)/sampleSize;
                x.insertElementAt(new Double(minimum + m*increment), m);
            }
            
            //we use the sample to calculate the kernel density
            density = new KernelDensity(auxValues, 0);
            //result = density.getValues(x);
            result = density.getValues(auxValues);
            x = (Vector)result.elementAt(0);
            y = (Vector)result.elementAt(1);
            
        }//end else
        
        //System.out.println("Estos son los valores con los que llamo a domainSplitting");
        int negatives = 0;
        for (i=0 ; i < x.size() ; i++){
           // System.out.println(i+"\t"+((Double)x.elementAt(i)).doubleValue()+"\t"+(((Double)y.elementAt(i)).doubleValue()));
            if (((Double)y.elementAt(i)).doubleValue() <= 0){
            negatives++;
           // System.out.println("Negative value: "+ ((Double)x.elementAt(i)).doubleValue()+"\t"+(((Double)y.elementAt(i)).doubleValue()));
            //values with density equal to 0 has to be removed
            if (((Double)y.elementAt(i)).doubleValue() == 0){
                x.removeElementAt(i);
                y.removeElementAt(i);
            }
            }
        }
       // System.out.println("Number of negative values : "+negatives);
        
        // Now I have the x and y vectors to apply the algorithm estimate, but first I need to split the domain
        
        cp = new Vector();//This vector will contain the cut points of the variable
        
        myTree.sort(values);
        myTree.sort(x);
        if((x.size() == 1) | (((Double)x.elementAt(0)).doubleValue() == ((Double)x.elementAt(x.size()-1)).doubleValue())){// Since the size is one, f(x) = yValue (constant)
          //  System.out.println("Solo un elemento en la muestra (o repetido)");
            cp.addElement(new Double(X.getMin()));
            cp.addElement(new Double(X.getMax()));
            myTree = new ContinuousProbabilityTree(X,cp);
            myTree.leaves = 1;
            mixture2 = new MixtExpDensity(((Double)y.elementAt(0)).doubleValue());
            ContinuousProbabilityTree tree = new ContinuousProbabilityTree(mixture2);
            myTree.child.setElementAt(tree,0);
            
        }else{
            
            cp = myTree.domainSplitting(x,y,intervals);
            //System.out.println("Primer valor, y por tanto primer del cut "+((Double)values.firstElement()).doubleValue());
            //System.out.println("ultimo valor, y por tanto ultimo del cut "+((Double)values.lastElement()).doubleValue());
            cp.setElementAt(values.firstElement(),0);
            first = ((Double)values.firstElement()).doubleValue();
            last = ((Double)values.lastElement()).doubleValue();
            cp.setElementAt(values.lastElement(),cp.size()-1);
            
            //X.setMax(((Double)cp.firstElement()).doubleValue());
            //X.setMin(((Double)cp.lastElement()).doubleValue());
            
            //NOW I BUILD THE TREE
            //System.out.println("En el CPT en el learnUnivariate la vble y tiene max: "+X.getMax()+" y min: "+X.getMin());
            myTree = new ContinuousProbabilityTree(X,cp);
            myTree.leaves = cp.size()-1;
            
            below = true;
            pos = -1; //A variable showing the last index of x belonging to the former interval
            i = 0;
            //System.out.println("X's size "+x.size());
            while(i < myTree.leaves){
                
                xInterval = new Vector();
                yInterval = new Vector();
                min = ((Double)cp.elementAt(i)).doubleValue();
                max = ((Double)cp.elementAt(i+1)).doubleValue();
                
                //System.out.println("Ha de estar entre "+min+" y "+max);
                while(below & ((pos+1) < x.size())){
                    //System.out.println("Comprobamos "+((Double)x.elementAt(pos+1)).doubleValue());
                    if ((pos+2) == x.size()) {//We check if it is the last one
                        //System.out.print("Es el ultimo");
                        below = false;
                        if((((Double)x.elementAt(pos+1)).doubleValue() <= max)){
                            //System.out.print(" y pertenece al intervalo");
                            xInterval.addElement(x.elementAt(pos+1));
                            yInterval.addElement(y.elementAt(pos+1));
                            pos++;
                           // System.out.println("Para el hijo "+i+" meto el punto "+xInterval);
                        } else{//System.out.print(" pero no es del intervalo");
                            pos = pos-1;}}
                    
                    else {
                        
                        if((((Double)x.elementAt(pos+1)).doubleValue() > max)){
                            below = false;
                            pos = pos-1;
                            
                        }//End of if
                        
                        else{
                            
                            xInterval.addElement(x.elementAt(pos+1));
                            yInterval.addElement(y.elementAt(pos+1));
                            pos++;
                            //System.out.println("Para el hijo "+i+" meto el punto "+xInterval);
                        }//End of else
                        
                    } // End of else
                    
                }//End of while
                //System.out.println("pos vale: "+pos);
                
                below = true;
                //Now I have the vectors xInterval and yInterval containing the
                //values of the database in the corresponding interval
                //System.out.println("Valores con los que hace la regresion exponencial");
                //Now I 8 an MixtExpDensity on each of the intervals
                for(int h=0 ; h<xInterval.size() ; h++){
                    //    System.out.println("x:"+((Double)xInterval.elementAt(h)).doubleValue());
                    //   System.out.println("y:"+((Double)yInterval.elementAt(h)).doubleValue());
                    
                }
                //      System.out.println("The number of points in the interval is "+xInterval.size());
                //first and last are different for each intervals, so we need to recalculated it
                //they are equal to the next and the last cutpoints
                if(i == 0 ){
                    firstNeg = first;
                    lastNeg = myTree.getCutPoint(1);
                }
                if ( i == (myTree.leaves - 1)){
                    lastNeg = last;
                    int numLeaves = (new Double(myTree.leaves)).intValue();
                    firstNeg = myTree.getCutPoint(numLeaves - 1);
                }
                if((i > 0 ) && (i < (myTree.leaves - 1)) ){
                    firstNeg = myTree.getCutPoint(i);
                    lastNeg = myTree.getCutPoint(i + 1);
                }
                //System.out.println("Hay "+myTree.getCutPoints().size()+" cutpoints, q son: ");
                //for (int i2 = 0; i2 < myTree.getCutPoints().size(); i2++){
                //    System.out.print(myTree.getCutPoint(i2)+"   ");
               // }
                // System.out.println("Estimamos la MTE "+i);
                 //System.out.println("VAR "+X.getName());
                mixture2 = mixture.estimate6(X,xInterval,yInterval,numpoints,firstNeg,lastNeg);
                for (int i1= 0 ; i1 < yInterval.size(); i1 ++ ){
                    //System.out.println("Imprimo un valor si es negativo:");
                    //if (((Double)(yInterval.elementAt(i1))).doubleValue() < 0) {
                    //System.out.println("y : "+((Double)yInterval.elementAt(i1)).doubleValue());
                    //System.out.print("x:"+((Double)xInterval.elementAt(i1)).doubleValue()+"    ");
                    //System.out.println("y:"+((Double)yInterval.elementAt(i1)).doubleValue());
                    //}
                }
                //System.out.println("Resultados de la estimacion:");
                //System.out.println("primero value "+mixture2.getValue(X,firstNeg));
                //System.out.println("ultimo value "+mixture2.getValue(X,lastNeg));
                ContinuousProbabilityTree tree = new ContinuousProbabilityTree(mixture2);
                myTree.child.setElementAt(tree,i);
                mixture2 = new MixtExpDensity();
                //System.out.println("Ya hemos estimado la MTE "+i);
                i++;
                
            }//End of while
        }
        // Now we normalize the MTE
       // System.out.println("Antes de normalizar, el arbol es:");
      //  myTree.print();
        myTree.normalizeLeaves(myTree.getVar(),1);
      //  System.out.println("Tras  normalizar, el arbol es:");
       // myTree.print();
        
        return myTree;
        
    }//End of method
    
    
    /**
     * This method splits the domain of the continuous variable in
     * several intervals satisfying this two conditions: No changes
     * in concavity and no changes in increas/decrease.
     * We give a parameter indicating the maximun number of intervals
     * so that we join those neighbour intervals with a minimun
     * distance in terms of increase/decrease and concavity.
     *
     * This method will have three differents parts: First we will
     * split the domain in terms of increase/decrease. Later we will
     * split each of the intervals obtained before in terms of changes
     * in concavity/convexity, and at last we will join the intervals
     * if necessary so that there are only <code>intervals</code> splitting
     * the domain.
     *
     * @param x A vector with the values
     * @param y A vector with the value of the density in x
     * @param intervals number maximun of intervals
     *
     * @return A vector with the limits of the intervals
     */
    
    
    public static Vector domainSplitting(Vector x, Vector y, int intervals){
        
        Vector cpx, cpy, xInterval, yInterval,newCpx, newCpy;
        int i,j,pos,join;
        int increase,increaseNow; //Possible values : 1 if it increases , 0 if it decreases
        int convexity,convexityNow; //Possible values : 1 if it is convexe , 0 if it is concave
        double a,b,y0,y1,y2,x0,x1,x2,max,min,dist;
        boolean below = true;
        cpx = new Vector();
        cpy = new Vector(); //This vector will be required to build the lines between the points
        
        //First we'll split the domain interms of increase/decrease
        cpx.addElement(x.elementAt(0));
        cpy.addElement(y.elementAt(0));
        
        increase = 0;
        if (((Double)y.elementAt(0)).doubleValue() < ((Double)y.elementAt(1)).doubleValue()) increase = 1;
        
        for (i=1 ; i<(x.size()-1) ; i++){
           // System.out.println("i: "+((Double)y.elementAt(i)).doubleValue() +" i+1:"+((Double)y.elementAt(i+1)).doubleValue());
            increaseNow = 0;
            if (((Double)y.elementAt(i)).doubleValue() < ((Double)y.elementAt(i+1)).doubleValue()) increaseNow = 1;
            
            if (((Double)y.elementAt(i)).doubleValue() == ((Double)y.elementAt(i+1)).doubleValue()) increaseNow = increase;
            
            if(increase != increaseNow){
                //System.out.println("Añado el valor de x:"+ ((Double)x.elementAt(i)).doubleValue());
                cpx.addElement(x.elementAt(i));
                cpy.addElement(y.elementAt(i));
                increase = increaseNow;
                
            }//End of if
            
        }//End of for
        
        cpx.addElement(x.lastElement());
        cpy.addElement(y.lastElement());
        //System.out.println("Despues de partir por creci/decreci hay "+cpx.size()+" intervalos distintos");
       // System.out.println(cpx);
        
        //With this we have the domain splitted in terms of increase/decrease
        
        //Now we split each of the intervals obtained before in terms of
        //changes in concavity/convexity
        
        //These vectors will contain the cutpoints after this second stage
        newCpx = new Vector();
        newCpy = new Vector();
        
        newCpx.addElement(cpx.elementAt(0));
        newCpy.addElement(cpy.elementAt(0));
        
        i=0;
        pos = -1;
        
        while(i < (cpx.size()-1)){
            
            xInterval = new Vector();
            yInterval = new Vector();
            min = ((Double)cpx.elementAt(i)).doubleValue();
            max = ((Double)cpx.elementAt(i+1)).doubleValue();
            
            while(below & ((pos+1) < x.size())){
                
                if ((pos+2) == x.size()) {
                    
                    below = false;
                    xInterval.addElement(x.elementAt(pos+1));
                    yInterval.addElement(y.elementAt(pos+1));
                    pos++;
                    
                }//End of if ((pos+2) == x.size())
                
                else {
                    
                    if((((Double)x.elementAt(pos+1)).doubleValue() > max)){
                        
                        below = false;
                        pos = pos-1;
                        
                    }//End of if((((Double)x.elementAt(pos+1)).doubleValue() > max))
                    
                    else{
                        
                        xInterval.addElement(x.elementAt(pos+1));
                        yInterval.addElement(y.elementAt(pos+1));
                        pos++;
                        
                    }//End of else
                    
                } // End of else
                
            }//End of while
            below = true;
            
            //Now I have in the vectors xInterval and yInterval the values between
            //min and max.
            
            //if there are less than four values I do not split the interval
            
            if (xInterval.size() > 3){
                
                convexity = 0;
                //The line will that goes through (x0,y0 ) and (x2,y2) y = a*x+b
                //where a=(y0-y2)/(x0-x2) and b = y0-a*x0
                
                //Therefore, the line that goes through the values 1 and 3 will have this
                // coefficients
                
                y0 = ((Double)yInterval.elementAt(0)).doubleValue();
                y1 = ((Double)yInterval.elementAt(1)).doubleValue();
                y2 = ((Double)yInterval.elementAt(2)).doubleValue();
                x0 = ((Double)xInterval.elementAt(0)).doubleValue();
                x1 = ((Double)xInterval.elementAt(1)).doubleValue();
                x2 = ((Double)xInterval.elementAt(2)).doubleValue();
                
                a = (y0-y2)/(x0-x2);
                b = y0-a*x0;
                
                //Now I have to check whether it gos up or down the intermediate value
                
                if (y1 > a*x1+b) convexity = 1;
                
                for (j=1 ; j<xInterval.size()-2 ; j++){
                    
                    convexityNow = 0;//This variable shows the convexity of the point x1
                    //and convexity the convexity of the former point
                    
                    y0 = ((Double)yInterval.elementAt(j)).doubleValue();
                    y1 = ((Double)yInterval.elementAt(j+1)).doubleValue();
                    y2 = ((Double)yInterval.elementAt(j+2)).doubleValue();
                    x0 = ((Double)xInterval.elementAt(j)).doubleValue();
                    x1 = ((Double)xInterval.elementAt(j+1)).doubleValue();
                    x2 = ((Double)xInterval.elementAt(j+2)).doubleValue();
                    
                    a = (y0-y2)/(x0-x2);
                    b = y0-a*x0;
                    
                    if (y1 > a*x1+b) convexityNow = 1;
                    
                    if(convexity != convexityNow){//If they are different there is a change in concavity/convexity
                        //so we insert a point there (point x1)
                        
                        newCpx.addElement(xInterval.elementAt(j+1));
                        newCpy.addElement(yInterval.elementAt(j+1));
                        
                        convexity = convexityNow;
                        
                    }//End of if(convexity != convexityNow)
                    
                }//End of for(j=1 ; j<xInterval.size()-3 ; j++)
                
            }//End of if (xInterval.size() > 3)
            
            newCpx.addElement(cpx.elementAt(i+1));
            newCpy.addElement(cpy.elementAt(i+1));
            
            i++;
        }//End of while
        
        //Now I have to join the intervals in order to leave just the
        // exact number "intervals".
        //System.out.println("Despues de partir por todo hay "+newCpx.size()+" intervalos, y quiero "+intervals);
        while (newCpx.size() > intervals+1){
            
            y0 = ((Double)newCpy.elementAt(0)).doubleValue();
            y1 = ((Double)newCpy.elementAt(1)).doubleValue();
            y2 = ((Double)newCpy.elementAt(2)).doubleValue();
            x0 = ((Double)newCpx.elementAt(0)).doubleValue();
            x1 = ((Double)newCpx.elementAt(1)).doubleValue();
            x2 = ((Double)newCpx.elementAt(2)).doubleValue();
            
            
            a = (y0-y2)/(x0-x2);
            b = y0-a*x0;
            
            dist = Math.abs(y1-(a*x1+b));
            min = dist;
            join = 1;//Shows the point in the cp vector to remove, so that we join the two intervals
            // this point was separating
            
            for (i=1 ; i<newCpx.size()-2 ; i++){
                
                //Now I calculate the line between points x0 and x2
                
                y0 = ((Double)newCpy.elementAt(i)).doubleValue();
                y1 = ((Double)newCpy.elementAt(i+1)).doubleValue();
                y2 = ((Double)newCpy.elementAt(i+2)).doubleValue();
                x0 = ((Double)newCpx.elementAt(i)).doubleValue();
                x1 = ((Double)newCpx.elementAt(i+1)).doubleValue();
                x2 = ((Double)newCpx.elementAt(i+2)).doubleValue();
                
                a = (y0-y2)/(x0-x2);
                b = y0-a*x0;
                
                dist = Math.abs(y1-(a*x1+b));
                
                if (dist < min ){
                    
                    min = dist;
                    join = i+1;
                    
                }//End of if (dist < min )
                
            }//End of for (i=0 ; i<newCpx.size()-2 ; i++)
            
            newCpx.removeElementAt(join);
            newCpy.removeElementAt(join);
            
            
            
            
        }//End of while (cp.size() > intervals)
        
        //System.out.println("Voy a mostrar la particion del dominio de Y que hago:");
        //for(i=0 ; i<newCpx.size() ; i++)
        //System.out.println(((Double)newCpx.elementAt(i)).doubleValue());
        
        
        //Doing this there may be some repeated values, now I will try to remove these values
        
        Vector cpClone;
        
        for(i=0 ; i<newCpx.size() ; i++){
            
            cpClone = new Vector();
            cpClone = (Vector)newCpx.clone();
            
            cpClone.remove(i);
            
            if(cpClone.contains(newCpx.elementAt(i))){
                //System.out.println("El "+((Double)newCpy.elementAt(i)).doubleValue()+" estaba repetido");
                newCpx.remove(i);
            }
            
        }
        return newCpx;
        
    }//End of method
    
    
    /**
     * Creates a tree which is constant and equal to 1.
     * @return a unit <code>ContinuousProbabilityTree</code>.
     */
    
    public static ContinuousProbabilityTree unitTree() {
        
        ContinuousProbabilityTree t;
        
        t = new ContinuousProbabilityTree(1.0);
        
        return t;
    }
    
    
    /**
     * Creates a copy of the ContinuousProbabilityTree
     *
     *
     *
     */
    
    public ContinuousProbabilityTree copy(){
        
        ContinuousProbabilityTree res = new ContinuousProbabilityTree();
        int i;
        Vector cp = new Vector();
        
        res.label = getLabel();
        
        if (isContinuous()){//Si es continua tiene cutPoints
            for (i=0 ; i<(getNumberOfChildren()+1) ; i++)
                cp.addElement(new Double(getCutPoint(i)));
            
            res.assignVar((Continuous)getVar(),cp);
        } else //Es discreta o prob
            if (isDiscrete())//Es discreta
                res.assignVar((FiniteStates)getVar());
        
        
        if (!isProbab())//Si no es probabilidad es pq tiene hijos
            for (i=0 ; i<getNumberOfChildren() ; i++)
                res.child.setElementAt(getChild(i),i);
        else // Es una probabilidad, luego tenemos que copiar su MTE
            res.value = getProb();
        
        return res;
        
    }//End of mehtod
    
    
    /**
     * Assigns a discrete variable to a tree. It creates
     * a vector of children of size equal to the number
     * of cases of the discrete variable.
     * @param variable the discrete variable.
     */
    
    public void assignVar(FiniteStates variable) {
        
        int i, j;
        ContinuousProbabilityTree tree;
        
        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector();
        cutPoints = null;
        j = variable.getNumStates();
        for (i=0 ; i<j ; i++) {
            tree = new ContinuousProbabilityTree();
            child.addElement(tree);
        }
    }
    
    
    /**
     * Assigns a continuous variable with given cutpoints to a continuous tree.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     */
    
    public void assignVar(Continuous variable, Vector cp) {
        
        int i, j;
        ContinuousProbabilityTree tree;
        
        label = CONTINUOUS_NODE;
        leaves = 0;
        var = variable;
        child = new Vector();
        cutPoints = (Vector)  cp.clone();
        
        j = cp.size()-1;
        for (i=0 ; i<j ; i++) {
            tree = new ContinuousProbabilityTree();
            child.addElement(tree);
        }
    }
    
    /**
     * Assigns a continuous variable with given cutpoints to a continuous tree.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     */
    
    public void setVar(Node variable) {
        
        var = variable;
        
    }
    
    
    /**
     * Determines whether a node is a density or not.
     * @return <code>true</code> if the node is a density and
     * <code>false</code> otherwise.
     */
    
    public boolean isProbab() {
        
        if (label == PROBAB_NODE)
            return true;
        
        return false;
    }
    
    /**
     * Determines whether a node is a discrete variable.
     * @return <code>true</code> if the node is a discrete variable and
     * <code>false</code> otherwise.
     */
    
    public boolean isDiscrete() {
        
        if (label == DISCRETE_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * Determines whether a node is a continuous variable.
     * @return <code>true</code> if the node is a continuous variable and
     * <code>false</code> otherwise.
     */
    
    public boolean isContinuous() {
        
        if (label == CONTINUOUS_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * Determines whether a node is a variable or not.
     * @return <code>true</code> if the node is a variable (discrete or continuous) and
     * <code>false</code> otherwise.
     */
    
    public boolean isVariable() {
        
        if ((label == DISCRETE_NODE) || (label == CONTINUOUS_NODE))
            return true;
        
        return false;
    }
    
    
    /**
     * Determines whether a node is empty or not.
     * @return <code>true</code> if the node is empty and
     * <code>false</code> otherwise.
     */
    
    public boolean isEmpty() {
        
        if (label == EMPTY_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * Gets the label of the node.
     * @return the label of the node.
     */
    
    public int getLabel() {
        
        return label;
    }
    
    /**
     * Sets the number of splits in which the domain of the continuous
     * variable are splitted.
     */
    
    public void setNumSplits(int n ){
        
        numSplits = n;
    }
    
    
    /**
     * Sets the number of exponential terms allowed as maximum
     *
     */
    
    public void setNumTerms(int n){
        
        numTerms = n;
    }
    
    
    /**
     * Gets the number of children of this node.
     * @return the number of children of this node.
     */
    
    public int getNumberOfChildren() {
        
        return child.size();
    }
    
    
    /**
     * Gets the density function associated with this node.
     * @return the density attached to the node.
     */
    
    public MixtExpDensity getProb() {
        
        return value;
    }
    
    
    /**
     * Gets the variable stored in this node.
     * @return the variable attached to a node.
     */
    
    public Node getVar() {
        
        return var;
    }
    
    
    /**
     * Gets a child of this node.
     * @param i an int value. Number of child to be returned.
     * (first value is <code>i=0</code>).
     * @return the <code>i</code>-th child of this node.
     */
    
    public ContinuousProbabilityTree getChild(int i) {
        
        return ((ContinuousProbabilityTree)(child.elementAt(i)));
    }
    
    /**
     * Gets the childs of this node.
     */
    
    public Vector getChilds() {
        
        return child;
    }
    
    
    /**
     * Replaces a <code>ProbabilityTree</code> as child of this node.
     * It is inserted at the specified position in the vector of
     * children. The previous child at that position is discarded.
     * <b>The object is modified</b>.
     * @param tree <code>ProbabilityTree</code> to be inserted as child.
     * @param pos an <code>int</code> with the position of the new child.
     */
    
    public void setNewChild(ContinuousProbabilityTree tree, int pos) {
        
        child.setElementAt(tree,pos);
    }
    
    
    /**
     * Inserts a new child to the node
     * @param newChild The new child to introduce
     *
     */
    
    public void insertChild(ContinuousProbabilityTree newChild){
        
        child.addElement(newChild);
        
    }
    
    
    /**
     * Gets the cutpoint at the given position.
     * @param pos the position in the list of cutpoints.
     * @return the value of the cutpoint.
     */
    
    public double getCutPoint(int pos) {
        
        if ((pos < 0) || (pos >= cutPoints.size())) {
            //System.out.println("pos: "+pos+" cutpoints size "+cutPoints.size());
            System.out.println("ERROR: Not so many cutpoints!");
            System.exit(1);
        }
        
        return ((Double)cutPoints.elementAt(pos)).doubleValue();
    }
    
    
    /**
     * Gets the position of the cutpoint corresponding to
     * a given value.
     * @param x the value to locate.
     * @return the position of the cutpoint containing <code>x</code>, and -1
     * if the value is not found.
     */
    
    public int getCutPoint(double x) {
        
        int i;
        
        for (i=0 ; i< (cutPoints.size()-1) ; i++) {
            if ((getCutPoint(i)<=x) && (getCutPoint(i+1)>=x))
                return i;
        }
        
        return -1;
    }
    
    
    /**
     * Gets the number of leaves below this node.
     * @return the size of the tree (i.e. the value of <code>leaves</code>).
     */
    
    public long getSize() {
        
        return leaves;
    }
    
    
    /**
     * Retrieves the density stored in a leaf.
     * @param conf a <code>ContinuousConfiguration</code>.
     * @return the density of the tree following the path indicated by
     * configuration <code>conf</code> and restricted to the values of
     * that configuration.
     */
    
    public MixtExpDensity getProb(ContinuousConfiguration conf) {
        
        int index, i, s, val;
        ContinuousProbabilityTree tree;
        double xval;
        double inf, sup;
        
        if (isDiscrete()) { // If the node is a discrete variable
            
            val = conf.getValue((FiniteStates) var);
            return(((ContinuousProbabilityTree)child.elementAt(val)).getProb(conf));
        } else {
            if (isContinuous()) {
                xval = conf.getValue((Continuous) var);
                index = -1;
                s = cutPoints.size();
                inf = ((Double) cutPoints.elementAt(0)).doubleValue();
                if (xval >= inf) {
                    for (i=1 ; i<s ; i++) {
                        sup = ((Double) cutPoints.elementAt(i)).doubleValue();
                        if (xval <= sup) {
                            index = i-1;
                            break;
                        }
                    }
                } else {
                    return (new MixtExpDensity(0.0));
                }
                if (index > -1) {
                    return (((ContinuousProbabilityTree)child.elementAt(index)).getProb(conf));
                } else {
                    return (new MixtExpDensity(0.0));
                }
            } else {
                if (isProbab()) // If the node is a prob.
                    return value;
                else
                    return (new MixtExpDensity(-1.0));
            }
        }
    }
    
    
    /**
     * Assigns a density to a node.
     * The density is not duplicated.
     * Also, sets the label to PROBAB_NODE.
     * @param f a density to be assigned.
     */
    
    public void assignProb(MixtExpDensity f) {
        
        label = PROBAB_NODE;
        value = f;
        leaves = 1;
    }
    
    
    /**
     * Assigns a density to a node.
     * The density is not duplicated.
     * Also, sets the label to PROBAB_NODE.
     * @param f a density to be assigned.
     */
    
    public void assignProb(double x) {
        
        label = PROBAB_NODE;
        value = new MixtExpDensity(x);
        leaves = 1;
    }
    
    
    /**
     * Combines a density with a Continuous tree.
     * It multiplies the densities.
     * To be used as a static function.
     * @param f a <code>MixtExpDensity</code>.
     * @param tree a <code>ContinuousProbabilityTree</code> to be multiplied
     * with <code>f</code>.
     * @param flag an integer indicating if the MixtExpDensities must be simplified
     * @return a new <code>ContinuousProbabilityTree</code> resulting of
     * combining <code>f</code> and <code>tree</code>.
     */
    
    public static ContinuousProbabilityTree combine(MixtExpDensity f, ContinuousProbabilityTree tree, int flag) {
        
        ContinuousProbabilityTree treeResult;
        int i, s;
        
        treeResult = new ContinuousProbabilityTree();
        treeResult.label = tree.label;
        treeResult.var = tree.var;
        treeResult.leaves = tree.leaves;
        if (tree.isContinuous()) {
            treeResult.cutPoints = (Vector) tree.cutPoints.clone();
        }
        if (tree.isProbab()) {
            treeResult.value = f.multiplyDensities(tree.value,flag);
        } else {
            s = tree.child.size();
            for (i=0 ; i<s ; i++) {
                treeResult.child.addElement( combine(f, (ContinuousProbabilityTree) tree.child.elementAt(i),flag));
            }
        }
        
        return treeResult;
    }
    
    
    /**
     * Combines two Continuous trees.
     * It multiplies the associated densities.
     * To be used as a static function.
     * @param tree1 a <code>ContinuousProbabilityTree</code>.
     * @param tree2 a <code>ContinuousProbabilityTree</code> to be multiplied
     * with <code>tree1</code>.
     * @param flag an integer indicating if the MixtExpDensities must be simplified
     * @return a new <code>ContinuousProbabilityTree</code> resulting of
     * combining <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ContinuousProbabilityTree combine(ContinuousProbabilityTree tree1,
            ContinuousProbabilityTree tree2, int flag) {
        
        ContinuousProbabilityTree tree;
        Configuration c1;
        ContinuousIntervalConfiguration c2;
        int i, nv;
        
        if (tree1.isProbab()) { // Probability node.
            tree = combine(tree1.value,tree2,flag);
        } else {
            tree = new ContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            tree.child = new Vector();
            
            nv = tree1.child.size();
            
            if (tree1.isContinuous())
                tree.cutPoints = (Vector)  tree1.cutPoints.clone();
            
            for (i=0 ; i<nv ; i++) {
                c1 = new Configuration();
                c2 = new ContinuousIntervalConfiguration();
                if (tree1.isContinuous())
                    c2.putValue((Continuous)tree.var,tree.getCutPoint(i),tree.getCutPoint(i+1));
                else {
                    if (tree1.isDiscrete())
                        c1.putValue((FiniteStates) tree.var,i);
                }
                
                tree.child.addElement(combine(c1,c2,tree1.getChild(i),tree2,flag));
            }
        }
        
        return tree;
    }
    
    
    /**
     * Combines two Continuous trees.
     * The second tree is to be restricted to the configuration <code>c1</code>
     * and to the configuration of intervals <code>c2</code>.
     * It is an auxiliar function to
     * <code>combine(ContinuousProbabilityTree tree1,ContinuousProbabilityTree tree2)</code>.
     * It multiplies the associated densities.
     * To be used as a static function.
     * @param c1 a <code> Configuration </code> containing the path followed
     * in <code>tree1</code> for discrete variables and to be used as
     * restriction for <code>tree2</code>.
     * @param c2 a <code>ContinuousIntervalConfiguration</code> containing the
     * path followed in <code>tree1</code> for continuous variables and to be
     * used as restriction for <code>tree2</code>.
     * @param tree1 a <code>ContinuousProbabilityTree</code>.
     * @param tree2 a <code>ContinuousProbabilityTree</code> to be multiplied
     * with <code>tree1</code>.
     * @param flag an integer indicating if the MixtExpDensities must be simplified
     * @return a new <code>ContinuousProbabilityTree</code> resulting of
     * combining <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ContinuousProbabilityTree combine(Configuration c1,
            ContinuousIntervalConfiguration c2,
            ContinuousProbabilityTree tree1,
            ContinuousProbabilityTree tree2, int flag) {
        
        ContinuousProbabilityTree tree;
        ContinuousIntervalConfiguration cx2;
        int i, nv;
        
        
        if (tree1.isProbab()) // Probability node.
            tree = combine(tree1.value,tree2.restrict(c1,c2),flag);
        else {
            tree = new ContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            nv = tree1.child.size();
            
            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector)  tree1.cutPoints.clone();
                for (i=0 ; i<nv ; i++) {
                    cx2 = c2.duplicate();
                    cx2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i+1)).doubleValue());
                    if(tree.child==null){
                     //   System.out.println("Tree.child=null");
                      //  System.out.println("TREE1");
                     //   tree1.print();
                     //   System.out.println("TREE");
                    //    tree.print();
                    }
                    tree.child.addElement(combine(c1,cx2,(ContinuousProbabilityTree) tree1.child.elementAt(i),tree2,flag));
                }
            }
            if (tree1.isDiscrete()) {
                for (i=0 ; i<nv ; i++) {
                    c1.putValue((FiniteStates) tree.var,i);
                    tree.child.addElement(combine(c1,c2,(ContinuousProbabilityTree) tree1.child.elementAt(i),tree2,flag));
                }
                c1.remove(c1.size()-1);
            }
        }
        
        return tree;
    }
    
    
    /**
     * Sums a density to a continuous tree.
     *
     * To be used as a static function.
     * @param f a <code>MixtExpDensity</code>.
     * @param tree a <code>ContinuousProbabilityTree</code> to be added with
     * <code>f</code>.
     * @return a new <code>ContinuousProbabilityTree</code> resulting from adding
     * <code>f</code> and <code>tree</code>.
     */
    
    public static ContinuousProbabilityTree add(MixtExpDensity f,
            ContinuousProbabilityTree tree) {
        
        ContinuousProbabilityTree treeResult;
        int i, s;
        
        treeResult = new ContinuousProbabilityTree();
        treeResult.label = tree.label;
        treeResult.var = tree.var;
        treeResult.leaves = tree.leaves;
        
        if (tree.isContinuous())
            treeResult.cutPoints = (Vector) tree.cutPoints.clone();
        if (tree.isProbab())
            treeResult.value = f.sumDensities(tree.value);
        else {
            s = tree.child.size();
            for (i=0 ; i<s ; i++) {
                treeResult.child.addElement( add(f, (ContinuousProbabilityTree) tree.child.elementAt(i)));
            }
        }
        
        return treeResult;
    }
    
    
    /**
     * Sums two continuous trees.
     * It sums the associated densities.
     * To be used as a static function.
     * @param tree1 a <code>ContinuousProbabilityTree</code>.
     * @param tree2 a <code>ContinuousProbabilityTree</code> to be added with
     * <code>tree1</code>.
     * @return a new <code>ContinuousProbabilityTree</code> resulting from adding
     * <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ContinuousProbabilityTree add(ContinuousProbabilityTree tree1,
            ContinuousProbabilityTree tree2) {
        
        ContinuousProbabilityTree tree;
        Configuration c1;
        ContinuousIntervalConfiguration c2;
        int i, nv;
        if (tree1.isProbab()) // Probability node.
            tree = add(tree1.value,tree2);
        else {
            tree = new ContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            
            
            if (tree1.isContinuous()){
                tree.cutPoints = (Vector)  tree1.cutPoints.clone();
            }
            //tree.cutPoints = new Vector();
            nv = tree1.child.size();
            for (i=0 ; i<nv ; i++) {
                c1 = new Configuration();
                c2 = new ContinuousIntervalConfiguration();
                if (tree1.isContinuous()) {
                    c2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i+1)).doubleValue());
                }
                if (tree1.isDiscrete())
                    c1.putValue((FiniteStates) tree.var,i);
                
                tree.child.addElement(add(c1,c2,(ContinuousProbabilityTree) tree1.child.elementAt(i),tree2));
            }
        }
        
        return tree;
    }
    
    
    /**
     * Sums two continuous trees.
     * The second tree is to be restricted to the configuration <code>c1</code>
     * and to the configuration of intervals <code>c2</code>.
     * It is an auxiliar function to <code> combine(ContinuousProbabilityTree tree1,
     *				      ContinuousProbabilityTree tree2) </code>
     * It sums the associated densities.
     * To be used as a static function.
     * @param c1 a <code> Configuration </code> containing the path followed in
     * <code>tree1</code> for discrete variables and to be used as restriction
     * for <code>tree2</code>.
     * @param c2 a <code> ContinuousIntervalConfiguration </code> containing
     * the path followed in <code>tree1</code> for continuous variables and
     * to be used as restriction for <code>tree2</code>.
     * @param tree1 a <code>ContinuousProbabilityTree</code>.
     * @param tree2 a <code>ContinuousProbabilityTree</code> to be added with
     * <code>tree1</code>.
     * @return a new <code>ContinuousProbabilityTree</code> resulting from adding
     * <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ContinuousProbabilityTree add(Configuration c1,
            ContinuousIntervalConfiguration c2,
            ContinuousProbabilityTree tree1,
            ContinuousProbabilityTree tree2) {
        
        ContinuousProbabilityTree tree;
        ContinuousIntervalConfiguration cx2;
        int i, nv;
        
        if (tree1.isProbab()) // Probability node.
            tree = add(tree1.value,tree2.restrict(c1,c2));
        else {
            tree = new ContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            nv = tree1.child.size();
            
            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector)  tree1.cutPoints.clone();
                for (i=0 ; i<nv ; i++) {
                    cx2 = c2.duplicate();
                    cx2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i+1)).doubleValue());
                    tree.child.addElement(add(c1,cx2,(ContinuousProbabilityTree) tree1.child.elementAt(i),tree2));
                }
            }
            if (tree1.isDiscrete()) {
                for (i=0 ; i<nv ; i++) {
                    c1.putValue((FiniteStates) tree.var,i);
                    tree.child.addElement(add(c1,c2,(ContinuousProbabilityTree) tree1.child.elementAt(i),tree2));
                }
                c1.remove(c1.size()-1);
            }
        }
        
        return tree;
    }
    
    
    /**
     * Adds the children of this node.
     * @return a new continuous tree equal to the addition of all the children
     *         of the current tree.
     */
    
    public ContinuousProbabilityTree addChildren() {
        
        ContinuousProbabilityTree tree;
        int i, nv;
        
        tree = getChild(0);
        nv = child.size();
        for (i=1 ; i<nv ; i++)
            tree = add(getChild(i),tree);
        
        return tree;
    }
    
    
    /**
     * It removes a discrete variable from a continuous probability tree
     * by summing over it.
     *
     * @param variable the variable to be removed.
     * @return the continuous probability tree result of the deletion.
     */
    
    public ContinuousProbabilityTree addVariable(FiniteStates variable) {
        
        ContinuousProbabilityTree aux;
        int i, s;
        
        aux = new ContinuousProbabilityTree();
        
        if (isProbab()) {
            aux.value =  value.multiplyDensities(variable.getNumStates());
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        } else {
            if (isDiscrete()) {
                if (var == variable) {
                    aux = addChildren();
                } else {
                    aux.label = DISCRETE_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.child = new Vector();
                    s = child.size();
                    for (i=0 ; i<s ; i++) {
                        aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).addVariable(variable));
                    }
                }
            } else {
                if (isContinuous()) {
                    aux.label = CONTINUOUS_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.cutPoints = (Vector) cutPoints.clone();
                    aux.child = new Vector();
                    s = child.size();
                    for (i=0 ; i<s ; i++) {
                        aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).addVariable(variable));
                    }
                }
            }
        }
        return aux;
    }
    
    
    /**
     * It removes a continuous variable from a continuous probability tree
     * by integrating with respect to it.
     *
     * @param variable the variable to be removed.
     * @return the continuous probability tree result of the deletion.
     */
    
    public ContinuousProbabilityTree addVariable(Continuous variable) {
        
        return integral(variable,variable.getMin(),variable.getMax());
    }
    
    
    /**
     * It makes the integral of a continuous tree with respect to a continuous
     * variable between two values. It is necessary that lower <= upper.
     *
     * @param variable the continuous variable with respect to which the integral
     * is carried out.
     * @param lower a double: the low limit of the integral.
     * @param upper a double: the up limit of the integral.
     * @param flag An integer indicating if the MixtExpDensities must be simplified.
     * @return a continuous tree with the result of the integral.
     *
     */
    
    public  ContinuousProbabilityTree integral(Continuous variable, double lower,
            double upper, int flag) {
        
        ContinuousProbabilityTree aux;
        int i, s;
        double x, y;
        
        aux = new ContinuousProbabilityTree(0.0);
        
        if (lower == upper) {
            return aux;
        }
        
        if (isProbab()) {
            aux.value =  value.integral(variable,lower,upper,flag);
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        } else {
            if (isDiscrete()) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).integral(variable,lower,upper,flag));
                }
            } else {
                if (isContinuous()) {
                    if (variable.equals(var)){
                        s = child.size();
                        x = ((Double) cutPoints.elementAt(0)).doubleValue();
                        for (i=0 ; i<s ; i++) {
                            y = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                            if (lower > x) {
                                x = lower;
                            }
                            if (y > upper) {
                                y = upper;
                            }
                            if (y > x) {
                                aux = add(aux,getChild(i).integral(variable,x,y,flag));
                            }
                            x = y;
                        }
                    } else {
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints=(Vector) cutPoints.clone();
                        aux.child = new Vector();
                        s = child.size();
                        for (i=0 ; i<s ; i++) {
                            aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).integral(variable,lower,upper,flag));
                        }
                    }
                }
            }
        }
        return aux;
    }
    
    
    /**
     * It makes the integral of a continuous tree with respect to a continuous
     * variable between two values. It is necessary that lower <= upper.
     *
     * @param variable the continuous variable with respect to which the integral
     * is carried out.
     * @param lower a double: the low limit of the integral.
     * @param upper a double: the up limit of the integral.
     * @return a continuous tree with the result of the integral.
     *
     */
    
    public  ContinuousProbabilityTree integral(Continuous variable, double lower,
            double upper) {
        
        ContinuousProbabilityTree aux;
        int i, s;
        double x, y;
        
        aux = new ContinuousProbabilityTree(0.0);
        
        if (lower == upper) {
            return aux;
        }
        
        if (isProbab()) {
            aux.value =  value.integral(variable,lower,upper,1);
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        } else {
            if (isDiscrete()) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).integral(variable,lower,upper,1));
                }
            } else {
                if (isContinuous()) {
                    if (variable.equals(var)){
                        s = child.size();
                        x = ((Double) cutPoints.elementAt(0)).doubleValue();
                        for (i=0 ; i<s ; i++) {
                            y = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                            if (lower > x) {
                                x = lower;
                            }
                            if (y > upper) {
                                y = upper;
                            }
                            if (y > x) {
                                aux = add(aux,getChild(i).integral(variable,x,y,1));
                            }
                            x = y;
                        }
                    } else {
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints=(Vector) cutPoints.clone();
                        aux.child = new Vector();
                        s = child.size();
                        for (i=0 ; i<s ; i++) {
                            aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).integral(variable,lower,upper,1));
                        }
                    }
                }
            }
        }
        return aux;
    }
    
    /**
     * It restricts a tree to a discrete configuration and to a configuration
     * of intervals for continuous variables.
     *
     * @param c1 the discrete configuration.
     * @param c2 the configuration of continuous intervals.
     * @return the restricted tree.
     */
    
    public ContinuousProbabilityTree restrict(Configuration c1,
            ContinuousIntervalConfiguration c2) {
        
        ContinuousProbabilityTree aux;
        int pos, i, s, element, initial, fin;
        double x, y, x1, y1;
        
        aux = new ContinuousProbabilityTree();
        if (isProbab()) {
            aux.value = value.duplicate();
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        }
        if (isDiscrete()) {
            pos = c1.indexOf(var);
            if (pos == -1) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c1,c2));
                }
            } else {
                element = c1.getValue(pos);
                aux =  ((ContinuousProbabilityTree) child.elementAt(element)).restrict(c1,c2);
            }
        }
        if (isContinuous()) {
            pos = c2.indexOf(var);
            if (pos == -1) {
                aux.label = CONTINUOUS_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.cutPoints = (Vector) cutPoints.clone();
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c1,c2));
                }
            } else {
                x = c2.getLowerValue(pos);
                y = c2.getUpperValue(pos);
                s = child.size();
                initial = -1;
                fin = -1;
                for (i=0 ; i<s ; i++) {
                    x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                    
                    if ( (y1> x) && (initial==-1)  ) { /*andrew*/
                        initial = i;
                    }
                    if  (y <= y1) {
                        if (x1 == y) {
                            fin = i-1;
                        } else {
                            fin = i;
                        }
                        break;
                    }
                }
                
                x1 = ((Double) cutPoints.elementAt(0)).doubleValue();
                y1 = ((Double) cutPoints.elementAt(s)).doubleValue();
                
                if (fin==-1 && initial!=-1 && y>y1)
                    fin=s-1;
                
                if (initial==0 && fin==0 && y<=x1){
                    initial=-1;
                    fin=-1;
                }
                
                
                
                if (fin==-1 && initial==0){
                    //In this case, the restriction process return a empty tree.
                    aux.value = new MixtExpDensity(0.0);
                    aux.label = PROBAB_NODE;
                    aux.leaves = 1;
                    
                }else if (initial==-1 && fin==-1){
                    //In this case, the restriction process return a empty tree.
                    aux.value = new MixtExpDensity(0.0);
                    aux.label = PROBAB_NODE;
                    aux.leaves = 1;
                    
                }else if (fin == initial) {
                    x1 = ((Double) cutPoints.elementAt(initial)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(cutPoints.size()-1)).doubleValue();
                    if (x<x1 && initial==0){
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints = new Vector();
                        aux.child = new Vector();
                        aux.cutPoints.addElement( new Double(x));
                        aux.child.addElement(new ContinuousProbabilityTree(0.0));
                        
                        
                        aux.cutPoints.addElement( new Double(x1));
                        aux.child.addElement(((ContinuousProbabilityTree) child.elementAt(initial)).restrict(c1,c2));
                        
                        y1 = ((Double) cutPoints.elementAt(initial+1)).doubleValue();
                        if (y < y1) {
                            y1 = y;
                        }
                        aux.cutPoints.addElement( new Double(y1));
                        
                        
                        
                        if (y>y1){
                            aux.cutPoints.addElement( new Double(y));
                            aux.child.addElement(new ContinuousProbabilityTree(0.0));
                        }
                    }else if (y>y1 && fin==s-1){
                        
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints = new Vector();
                        aux.child = new Vector();
                        
                        x1 = ((Double) cutPoints.elementAt(initial)).doubleValue();
                        if (x1 < x) {
                            x1 = x;
                        }
                        
                        aux.cutPoints.addElement( new Double(x1));
                        aux.child.addElement(((ContinuousProbabilityTree) child.elementAt(initial)).restrict(c1,c2));
                        y1 = ((Double) cutPoints.elementAt(cutPoints.size()-1)).doubleValue();
                        aux.cutPoints.addElement( new Double(y1));
                        
                        aux.child.addElement(new ContinuousProbabilityTree(0.0));
                        aux.cutPoints.addElement( new Double(y));
                    }else
                        aux =  ((ContinuousProbabilityTree) child.elementAt(initial)).restrict(c1,c2);
                    
                }else {
                    aux.label = CONTINUOUS_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.cutPoints = new Vector();
                    aux.child = new Vector();
                    
                    x1 = ((Double) cutPoints.elementAt(0)).doubleValue();
                    if (x<x1 && initial==0){
                        aux.cutPoints.addElement( new Double(x));
                        aux.child.addElement(new ContinuousProbabilityTree(0.0));
                    }
                    
                    for (i=initial ; i<=fin ; i++) {
                        //System.out.println("i="+i);
                        //print();
                        //System.out.println("x="+x+" y="+y+" s="+s);
                        x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                        if (x1 < x) {
                            x1 = x;
                        }
                        y1 = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                        if (y < y1) {
                            y1 = y;
                        }
                        
                        //ContinuousIntervalConfiguration newInterval=new ContinuousIntervalConfiguration();
                        ContinuousIntervalConfiguration newInterval=c2.duplicate();//new ContinuousIntervalConfiguration();
                        newInterval.putValue((Continuous)var,x1,y1);
                        aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c1,newInterval));
                        aux.cutPoints.addElement( new Double(x1));
                    }
                    y1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    if (y < y1) {
                        y1 = y;
                    }
                    aux.cutPoints.addElement( new Double(y1));
                    
                    y1 = ((Double) cutPoints.elementAt(s)).doubleValue();
                    if (y>y1 && fin==s-1){
                        aux.cutPoints.addElement( new Double(y));
                        aux.child.addElement(new ContinuousProbabilityTree(0.0));
                    }
                    
                }
            }
        }
        
        return aux;
    }
    
    
    /**
     * It restricts a tree to a continuous configuration containing values
     * for discrete and continuous variables.
     *
     * @param c the continuous configuration.
     * @return the restricted tree.
     */
    
    public ContinuousProbabilityTree restrict(ContinuousConfiguration c) {
        
        ContinuousProbabilityTree aux;
        double x, x1, y1;
        int i, s, pos, element;
        
        aux = new ContinuousProbabilityTree();
        
        if (isProbab()) {
            //aux.value = new MixtExpDensity(value.getValue(c));
            aux.value = value.restrict(c);
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        }
        if (isDiscrete()) {
            pos = c.indexOf(var);
            if (pos == -1) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c));
                }
            } else {
                element = c.getValue(pos);
                if (element==-1)
                    System.out.println();
                aux =  ((ContinuousProbabilityTree) child.elementAt(element)).restrict(c);
            }
        }
        
        if (isContinuous()) {
            pos = c.getIndex( (Continuous) var);
            if (pos == -1) {
                aux.label = CONTINUOUS_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.cutPoints=(Vector) cutPoints.clone();
                aux.child = new Vector();
                s = child.size();
                for (i=0 ; i<s ; i++) {
                    aux.child.addElement( ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c));
                }
            } else {
                x = c.getContinuousValue(pos);
                s = child.size();
                
                for (i=0 ; i<s ; i++) {
                    x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                    if ( (x>=x1) && (x <=y1) ) {
                        aux = ((ContinuousProbabilityTree) child.elementAt(i)).restrict(c);
                        return aux;
                    }
                }
                return new ContinuousProbabilityTree(0.0);
            }
        }
        
        return aux;
    }
    
    
    
    /**
     * Simulates a vector of values of this variable (continuous or discrete)
     * using the method <code>simulateValue</code>.
     * @param sampleSize The number of values to simulate
     *
     * @return a vector with the simulated values
     *
     */
    
    public Vector simulateVector(int sampleSize){
        
        int i;
        Vector result = new Vector();
        
        for(i=0 ; i<sampleSize ; i++){
            
            result.addElement(new Double(simulateValue()));
            
        }//End of for
        
        return result;
        
    }//End of method
    
    
    /**
     * This method operates over a continuous (or a discrete variable) whose
     * children are probability nodes (<code>MixtExpDensity</code>).
     * The result is that the leaves are normalized to get
     * that the integral (sum) over the whole domain of the
     * continuous variable is equal to 1.
     * The object is modified.
     * @param flag An integer indicating if the MixtExpDensities in the leaves must be simplified
     */
    
    public void normalizeLeaves(Node variable,int flag) {
        
        double acum = 0.0;
        int i;
        MixtExpDensity density, resDensity;
        ContinuousProbabilityTree auxTree;
        
        
        if (isDiscrete()) {
            
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = getChild(i);
                if (!auxTree.isProbab()) {
                    System.out.println("Error in normalizeLeaves: no leaf to normalize.");
                    System.exit(1);
                }
                density = auxTree.getProb();
                
                if (density.getNumberOfExp()==0){
                    acum += density.getIndependent();
                }else{
                    acum += density.integral((Continuous)variable,((Continuous)variable).getMin(),((Continuous)variable).getMax(),flag).getIndependent();
                }
            }
            
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = getChild(i);
                density = auxTree.getProb();
                if (acum!=0.0)
                    density = density.multiplyDensities(1/ acum);
                else{
                    //System.out.println("Problema.......................");
                    density=new MixtExpDensity(1/(double)getNumberOfChildren());
                }
                getChild(i).assignProb(density);
            }
            
        }else if (isContinuous()){
            auxTree=integral((Continuous)getVar(),getCutPoint(0),getCutPoint(getCutPoints().size()-1),flag);
            acum=auxTree.getProb().getIndependent();
            // Here, acum is the normalization factor.
            // Now we divide each leaf by its integral
            MixtExpDensity exp=new MixtExpDensity(1/acum);
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = combine(exp,getChild(i),0);
                setChild(auxTree,i);
            }
            
            
        }else{
            density = getProb();
            
            if (variable.getClass()==Continuous.class && variable.getTypeOfVariable()==Node.CHANCE){
                density = density.integral(((Continuous)variable),((Continuous)variable).getMin(),((Continuous)variable).getMax(),flag);
                acum = density.getIndependent();
            }else if (variable.getClass()==FiniteStates.class){
                acum = density.getIndependent()*((FiniteStates)variable).getNumStates();
            }
            
            
            density = getProb();
            density = density.multiplyDensities(1/ acum);
            assignProb(density);
            
        }
    }
    
    /**
     * This method operates over a continuous (or a discrete variable) whose
     * children are probability nodes (<code>MixtExpDensity</code>).
     * The result is that the leaves are normalized to get
     * that the integral (sum) over the whole domain of the
     * continuous variable is equal to 1.
     * The object is modified.
     */
    
    public void normalizeLeaves(Node variable) {
        
        double acum = 0.0;
        int i;
        MixtExpDensity density, resDensity;
        ContinuousProbabilityTree auxTree;
        
        
        if (isDiscrete()) {
            
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = getChild(i);
                if (!auxTree.isProbab()) {
                    System.out.println("Error in normalizeLeaves: no leaf to normalize.");
                    System.exit(1);
                }
                density = auxTree.getProb();
                
                if (density.getNumberOfExp()==0){
                    acum += density.getIndependent();
                }else{
                    acum += density.integral((Continuous)variable,((Continuous)variable).getMin(),((Continuous)variable).getMax()).getIndependent();
                }
            }
            
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = getChild(i);
                density = auxTree.getProb();
                
                if (acum!=0.0)
                    density = density.multiplyDensities(1/ acum);
                else{
                    //System.out.println("Problema.......................");
                    density=new MixtExpDensity(1/(double)getNumberOfChildren());
                }
                
                getChild(i).assignProb(density);
            }
            
        }else if (isContinuous()){
            auxTree=integral((Continuous)getVar(),getCutPoint(0),getCutPoint(getCutPoints().size()-1),1);
            acum=auxTree.getProb().getIndependent();
            // Here, acum is the normalization factor.
            // Now we divide each leaf by its integral
            MixtExpDensity exp=new MixtExpDensity(1/acum);
            for (i=0 ; i<getNumberOfChildren() ; i++) {
                auxTree = combine(exp,getChild(i),0);
                setChild(auxTree,i);
            }
            
            
        }else{
            density = getProb();
            
            if (variable.getClass()==Continuous.class && variable.getTypeOfVariable()==Node.CHANCE){
                density = density.integral(((Continuous)variable),((Continuous)variable).getMin(),((Continuous)variable).getMax());
                acum = density.getIndependent();
            }else if (variable.getClass()==FiniteStates.class){
                acum = density.getIndependent()*((FiniteStates)variable).getNumStates();
            }
            
            
            density = getProb();
            if (acum!=0.0)
                density = density.multiplyDensities(1/ acum);
            else{
                //System.out.println("Problema.......................");
                density=new MixtExpDensity(1/(double)getNumberOfChildren());
            }
            
            //density = density.multiplyDensities(1/ acum);
            assignProb(density);
            
        }
    }
    
    /**
     * Puts <code>copt</code> as a child of the ContinuousProbabilityTree
     * in the position <code>i</code> of the vector child.
     *
     * @param cpt The child to include
     * @param i the position to put it
     */
    
    public void setChild(ContinuousProbabilityTree cpt, int i){
        
        //System.out.println("Hay "+getNumberOfChildren()+" hijos");
        if(getNumberOfChildren() >= i)
            child.setElementAt(cpt,i);
        else
            System.out.println("Error: The position "+i+" does not exist in the vector of children");
        
    }
    
    /**
     * It assumes that the node is continuous and sets all the
     * children to value 0.0.
     * The object is modified.
     */
    
    public void setToZero() {
        
        int i, s;
        ContinuousProbabilityTree tree;
        
        if (!isContinuous()) {
            System.out.println("Error in setToZero: no continuous node.");
            System.exit(1);
        }
        
        s = getNumberOfChildren();
        child = new Vector();
        
        for (i=0 ; i<s ; i++) {
            tree = new ContinuousProbabilityTree(0.0);
            child.addElement(tree);
        }
    }
    
    
    /**
     * This method operates over a continuous variable whose
     * children are probability nodes (<code>MixtExpDensity</code>).
     * The result is that the independent term in the selected leaf is
     * incremented in a given value.
     * The object is modified.
     *
     * @param l the selected leaf.
     * @param x the value to increment.
     */
    
    public void incValue(int l, double x) {
        
        ContinuousProbabilityTree auxTree;
        MixtExpDensity density;
        
        auxTree = getChild(l);
        density = auxTree.getProb();
        density.setIndependent(density.getIndependent() + x);
    }
    
    
    /**
     * Saves the tree to a file.
     *
     * @param p the <code>PrintWriter</code> where the tree will be written.
     */
    
    public void save(PrintWriter p) {
        
        int i;
        
        if (isProbab()) {
            value.save(p);
            p.println();
        } else {
            if (var != null)
                var.save(p);
            
            for (i=0 ; i<child.size() ; i++) {
                if (cutPoints != null) {
                    if (cutPoints.size() > 0)
                        p.print("Interval : ("+getCutPoint(i)+","+getCutPoint(i+1)+")\n");
                }
                getChild(i).save(p);
            }
        }
    }


    /**
     * Saves the tree to a file in R format.
     *
     * @param p the <code>PrintWriter</code> where the tree will be written.
     */

    public void saveR(PrintWriter p, String cond) {

        int i;

        if (isProbab()) {
            value.saveR(p,cond);
            p.println();
        } else {

            for (i=0 ; i<child.size() ; i++) {
                String condition = "";
                if (cutPoints != null) {
                    if (cutPoints.size() > 0) {
                        condition = "[("+var.getName() + ">=" + getCutPoint(i) + ")&(" + var.getName() + "<=" + getCutPoint(i + 1) + ")]";
                        p.print("result"+condition+" = ");
                    }
                }
                getChild(i).saveR(p,condition);
            }
        }
    }
    
    
    /**
     * Saves the tree starting in this node to a file.
     * @param p the <code>PrintWriter</code> where the tree will be written.
     * @param j a tab factor (number of blank spaces before a child
     * is written).
     */
    
    public void save(PrintWriter p, int j) {
        
        int i, l, k;
        
        if (isProbab())
            p.print(value.ToString()+";\n");
        else if (isDiscrete()){
            p.print("case "+var.getName()+" {\n");
            
            for (i=0 ; i< child.size() ; i++) {
                for (l=1 ; l<=j ; l++)
                    p.print(" ");
                
                p.print(((FiniteStates)var).getState(i) + " = ");
                getChild(i).save(p,j+10);
            }
            
            for (i=1 ; i<=j ; i++)
                p.print(" ");
            
            p.print("          } \n");
        }else if (isContinuous()){
            
            p.print("case "+var.getName()+CutPointsToString()+" {\n");
            
            for (i=0 ; i< child.size() ; i++) {
                for (l=1 ; l<=j ; l++)
                    p.print(" ");
                
                p.print(i + " = ");
                getChild(i).save(p,j+10);
            }
            
            for (i=1 ; i<=j ; i++)
                p.print(" ");
            
            p.print("          } \n");
            
            
        }
    }
    
    /**
     * Prints the tree to the standard output.
     */
    
    public void print() {
        print(0);
    }
    
    /**
     * Prints the tree to the standard output with n tabs.
     */
    
    public void print(int n) {
        
        int i;
        
        if (isProbab()) {
            value.print(n);
            System.out.println();
        } else {
            //      System.out.println("la variable: "+var.getName());
            var.print(n);
            //System.out.println("Number of childs: "+child.size());
            
            for (i=0 ; i<child.size() ; i++) {
                if (cutPoints != null) {
                    if (cutPoints.size() > 0) {
                        for (int z=0; z<n; z++)
                            System.out.print("\t");
                        System.out.print("Interval : ("+getCutPoint(i)+","+getCutPoint(i+1)+")\n");
                    }
                }
                getChild(i).print(n+1);
            }
        }
    }
    
    /**
     * Reads data from a file and puts it into a vector.
     * It is necessary just for experiments from the command line
     *
     * @param name The name of the file.
     *
     * @return the vector
     */
    
    public Vector readFile(String name){
        
        Vector number= new Vector();
        
        try{
            
            int c,i;
            
            String read;
            
            FileInputStream fil1;
            
            BufferedReader str;
            
            File fil2= new File(name);
            
            Double valor1,valor2;
            fil1 = new FileInputStream(fil2);
            str = new BufferedReader(new InputStreamReader(fil1));
            read = str.readLine();
            i = 0;
            
            try{
                
                while(read!=null) {
                    
                    number.addElement(new Double(read));
                    read = new String();
                    read = str.readLine();
                    i+=1;
                    
                }//End of while(read!=null)
                
            } catch (EOFException e){System.out.println(e);}
            
        }catch (IOException e ){System.out.println(e);}
        
        return number;
        
    }//End of method
    
    
    /**
     * This method transforms a vector of reals in two vectors,
     * one with the values of the variable, and another one
     * with the frecuencies.
     *
     *
     * @param data Vector containing the data.
     * @param longitud Length of the intervals used to get the empirical density.
     *
     * @return Two vectors with the values.
     */
    
    public Vector empiricDensity(Vector data, double longitud ){
        
        int n,i;
        
        double limitInf,frec=0,value,limitSup=0,xValue,min,sum,max,intLength,yValue;
        
        Vector x , y, result;
        
        boolean found=false,first = true;
        
        x = new Vector();
        y = new Vector();
        result = new Vector();
        
        i=0;
        n = data.size();
        
        sort(data);
        
        max = ((Double)data.elementAt(n-1)).doubleValue();
        min = ((Double)data.elementAt(0)).doubleValue();
        
        // This lower limit is like that in order to make the first value as the center of the first interval
        // so that the first one of the values is the first point returned
        limitInf = min-(longitud/2);
        limitSup = limitInf;
        sum = 0;
        
        while (i<n){
            
            limitSup = limitSup+longitud;
            found = false;//This shows if we have found a value above the limitSup
            frec=0;
            
            while ((!found)&(i<n)){
                
                xValue = ((Double)data.elementAt(i)).doubleValue();
                
                if (xValue < limitSup){
                    
                    frec++;
                    i++;
                    
                    if (i==n){//This is the last one of the values
                        
                        if (frec>0){//Although it is not above the limitSup, as it is the last value we have to
                            // 'close' the interval
                            
                            value = xValue;
                            frec = (double)(frec/(double)n);
                            frec = frec/(max-limitInf);
                            
                            x.addElement(new Double(value));
                            y.addElement(new Double(frec));
                            
                            result.addElement(x);
                            result.addElement(y);
                            
                            sum +=frec;
                            limitInf = limitSup;
                            
                        }//End of if (frec>0)
                        
                    }//End of if(i==n)
                    
                }//End of if (xValue < limitSup)
                
                else{//This point is above the limitSup
                    
                    if (frec > 0){//We 'close' the interval
                        
                        frec = (double)(frec/(double)n);
                        intLength = limitSup-limitInf;
                        
                        if (first){//It is the first one of the intervals
                            
                            frec = 2*frec/(intLength);
                            first = false;
                            
                            
                        }//End of if (first)
                        else {
                            
                            frec = frec/intLength;
                            
                        }//End of else
                        
                        value = (limitSup+limitInf)/2;//The midpoint of the interval
                        
                        x.addElement(new Double(value));
                        y.addElement(new Double(frec));
                        sum += frec;
                        limitInf = limitSup;
                        
                    }//End of if (frec > 0)
                    
                    found = true;
                    
                }//End of else
                
            }//End of while de found y i<n
            
        }//End of while (i<=n)
        
        return result;
        
    }//End of method
    
    
    /**
     * This method transforms a vector of reals in two vectors,
     * one with the values of the variable, and another one
     * with the frecuencies. It makes the same as <code>empiricDensity</code>
     *
     *
     * @param values Vector containing the data.
     * @param length Length of the intervals used to get the empirical density.
     *
     * @return Two vectors with the values.
     */
    
    public Vector empiric2(Vector values, double length){
        
        Vector aux,x,y,result;
        int i,j,nIntervals;
        double xValue, yValue,inf,sup,totalLength,max,min,newInf,frec;
        
        sort(values);
        
        aux = new Vector();
        x = new Vector();
        y = new Vector();
        result = new Vector();
        
        j = 0;
        
        max = ((Double)values.lastElement()).doubleValue();
        min = ((Double)values.firstElement()).doubleValue();
        
        inf = min -(length/2);
        totalLength = max -inf;
        nIntervals = (int)(totalLength/length)+1;//number of interval that will be when building the empitic density
        j = 1;
        xValue = ((Double)values.elementAt(0)).doubleValue();
        for (i=0 ; i<nIntervals ; i++){
            
            Vector triple = new Vector();
            triple.addElement( new Double(inf));
            triple.addElement(new Double(inf+length));
            
            frec = 0;
            
            while ((xValue < ((Double)triple.elementAt(1)).doubleValue()) & (j<=values.size())){//Now I count the frecuency
                
                
                if(j<values.size()){
                    
                    xValue = ((Double)values.elementAt(j)).doubleValue();
                    
                }//end of if(j<values.size())
                
                j++;
                frec = frec+1;
            }//End of while
            
            
            triple.addElement(new Double(frec));
            aux.addElement(triple);
            inf = inf+length;
            
        }//End of for
        
        //Now I have the values in the vector aux, so I have to 'remove' the ones with frecuency 0
        
        //With this for I join the interval with frecuency 0 with the one just by his right. What I do is to put
        //as the lower limit of the one by the right the lower limit of the one with frecuency 0
        for (i=0 ; i<aux.size()-1 ; i++){
            
            Vector triple = new Vector();
            triple = (Vector)aux.elementAt(i);
            Vector tripleSig = new Vector();
            tripleSig = (Vector)aux.elementAt(i+1);
            
            if (((Double)triple.elementAt(2)).doubleValue() == 0){
                
                newInf = ((Double)triple.elementAt(0)).doubleValue();
                tripleSig.setElementAt(new Double(newInf),0);
                
            }//End of if(aux.elementAt(i).triple[2] == 0)
            
        }//End of for (i=0 ; i<aux.size() ; i++)
        
        for (i=0 ; i<aux.size() ; i++){
            
            Vector triple = new Vector();
            triple = (Vector)aux.elementAt(i);
            
            //I only add a new point to the result only if the frecuency is not 0.
            if (((Double)triple.elementAt(2)).doubleValue() != 0){
                
                if (i==0){//It is the first interval, so I modify its length since I know that below its midpoint there is no value
                    
                    x.addElement(new Double((((Double)triple.elementAt(0)).doubleValue()+((Double)triple.elementAt(1)).doubleValue())/2));
                    y.addElement(new Double(( ((Double)triple.elementAt(2)).doubleValue()/values.size() )*2/length));
                    
                }
                
                else{//It is not the first interval
                    
                    if (i == nIntervals-1) {//It is the las interval interval
                        x.addElement(new Double(((Double)values.lastElement()).doubleValue()));
                        y.addElement(new Double(( ((Double)triple.elementAt(2)).doubleValue()/values.size() )/(((Double)values.lastElement()).doubleValue()-((Double)triple.elementAt(0)).doubleValue())));
                    }//End of if (i == nIntervals-1)
                    
                    else {
                        
                        x.addElement(new Double((((Double)triple.elementAt(0)).doubleValue()+((Double)triple.elementAt(1)).doubleValue())/2));
                        y.addElement(new Double(( ((Double)triple.elementAt(2)).doubleValue()/values.size() )/(((Double)triple.elementAt(1)).doubleValue()-((Double)triple.elementAt(0)).doubleValue())));
                    }//End of else
                    
                }//End of else
                
            }//End of if (((Double)triple.elementAt(2)).doubleValue() != 0)
            
        }//End of for (i=0 ; i<aux.size() ; i++)
        
        result.addElement(x);
        result.addElement(y);
        
        return result;
        
    }//End of method
    
    /**
     * This method transforms a vector of reals in two vectors,
     * one with the values of the variable, and another one
     * with the frecuencies. It divides the interval in
     * <code>intervals</code> each of them with equal number
     * of points on them.
     *
     * @param values Vector containing the data.
     * @param intervals Number of intervals in which the range will be divided
     *
     * @return Two vectors with the values.
     */
    
    public Vector empiricEqualNumberOfPoints(Vector values, int intervals){
        
        Vector aux,x,y,result;
        int i,j,n,nPoints,nMore,indexMore,Points,h,indexInf,indexSup,indexInfFormer,frecPoints,frecPointsFormer;
        double xValue, yValue,inf,sup,totalLength,max,min,newInf,frec,medinf,medsup;
        
        x = new Vector();
        y = new Vector();
        result = new Vector();
        
        sort(values);
        n = values.size();
        
        
        // System.out.println("n� values "+n);
        //for(i=0 ; i<values.size() ; i++)
        //System.out.println(((Double)values.elementAt(i)).doubleValue());
        
        
        if(((Double)values.elementAt(0)).doubleValue() == ((Double)values.elementAt(n-1)).doubleValue()){
            //System.out.println("Son todos iguales, ha de devolver s�lo un valor con 1 en el y");
            x.addElement(values.elementAt(0));
            y.addElement(new Double(1));
            
        }else{
            
            //  for(i=0 ; i<values.size() ; i++)
            // 	System.out.println(((Double)values.elementAt(i)).doubleValue());
            
            //System.out.println("Hay "+n+" valores, y el n�mero de intervalos es: "+intervals);
            if (n < intervals){
                intervals = n;
                //System.out.println("Ahora hay "+n+" valores, y el n�mero de intervalos es: "+intervals);
            }
            nPoints = (int)(n/intervals);
            //  System.out.println("Hay que poner "+nPoints+" en cada intervalo");
            nMore = n%intervals;
            //  System.out.println("Hay "+nMore+" intervalos con "+(nPoints+1)+" puntos");
            indexMore = intervals-nMore;
            indexInf = 0;
            indexInfFormer = 0;
            frecPointsFormer = 0;
            for(i=0 ; i<intervals ; i++){
                
                if(i<indexMore){//Estamos en un intervalo que tendra exactamente nPoints
                    Points = nPoints;
                }else{//Estamos en un intervalo que tendra exactamente nPoints+1
                    Points = nPoints+1;
                }
                indexSup = indexInf+Points-1;
                //System.out.println("indexInf: "+indexInf);
                //System.out.println("indexSup: "+indexSup);
                inf = ((Double)values.elementAt(indexInf)).doubleValue();
                //System.out.println("el inf es: "+inf);
                sup = ((Double)values.elementAt(indexSup)).doubleValue();
                //System.out.println("el sup es: "+sup);
                frecPoints = Points;
                while(inf == sup ){// Both values are the same, so this interval
                    // will have length zero, and that cannot happen
                    //   System.out.println("Fallo, el max y el min son el mismo, paso de este intervalo");
                    //   System.out.println("sup vale: "+sup);
                    ////   System.out.println("inf vale: "+inf);
                    // System.out.println("i valia: "+i);
                    i++;
                    if(i == intervals){// There are no more points and still maybe
                        // sup is equal to inf, it must be checked
                        //System.out.println("This must be solved.");
                        //System.out.println("No hay mas a la derecha, el sup ha de ser el m�ximo, y el indexSup tb");
                        indexInf = indexInfFormer;
                        indexSup = values.size()-1;
                        sup = ((Double)values.elementAt(indexSup)).doubleValue();
                        inf = ((Double)values.elementAt(indexInf)).doubleValue();
                        frecPoints = frecPointsFormer+frecPoints;
                        x.removeElementAt(x.size()-1);
                        y.removeElementAt(y.size()-1);
                    }else{
                        //System.out.println("i vale ahora: "+i);
                        if(i>=indexMore)
                            Points = nPoints+1;
                        indexSup = indexSup+Points;
                        //System.out.println("indexSup ahora: "+indexSup);
                        frecPoints = frecPoints + Points;
                        //System.out.println("frecPoints es ahora: "+frecPoints);
                        sup = ((Double)values.elementAt(indexSup)).doubleValue();
                        //	System.out.println("Ahora sup vale: "+sup);
                    }// End of else
                    
                }// End of while
                //Este es el punto medio del inferior
                if ((i==0)|(indexInf==0)){
                    //medsup = (sup+((Double)values.elementAt(indexSup+1)).doubleValue())/2;
                    //medinf=inf-(medsup-sup);}
                    // System.out.println("Estamos en el primero, luego el punto medio inferior es el extremo inicial");
                    medinf = inf;} else{
                    //System.out.println("El punto medio inferior es el pto medio de "+inf+" y de "+((Double)values.elementAt(indexInf-1)));
                    medinf = (inf+((Double)values.elementAt(indexInf-1)).doubleValue())/2;}
                
                if((i==(intervals-1))|(indexSup==(values.size()-1))){ //medsup = sup+ (inf-medinf);
                    //      System.out.println("Estamos en el ultimo, luego el punto medio superior es el extremo final");
                    medsup = sup;
                } else{
                    //    System.out.println("El punto medio superior es el pto medio de "+sup+" y de "+((Double)values.elementAt(indexSup+1)));
                    medsup = (sup+((Double)values.elementAt(indexSup+1)).doubleValue())/2;
                }
                
                x.addElement(new Double((medsup+medinf)/2));
                //System.out.println("el medinf es: "+medinf);
                // System.out.println("el medsup es: "+medsup);
                //System.out.println("el frecPoints es: "+frecPoints);
                indexInfFormer = indexInf;
                frecPointsFormer = frecPoints;
                yValue = frecPoints/(n*(medsup-medinf));
                y.addElement(new Double(yValue));
                indexInf = indexSup+1;
                
            }//End of for
        }//End of else
        result.addElement(x);
        result.addElement(y);
        
        return result;
        
    }//End of method
    
    
    /**
     * This method sorts a vector using the method bubble.
     * It does not return anything, it changes the elements positions
     * of the vector.
     *
     * @param X The vector to sort.
     */
    
    public void sort(Vector X){
        
        boolean lChanged;
        int nIndex,i;
        double intermediate;
        
        // We will use the method burbuja mejorado
        
        lChanged = true;
        nIndex = 1;
        
        while (lChanged ){
            
            lChanged = false;
            
            for (i=0 ; i<(X.size()-nIndex) ; i++){
                
                if (((Double)X.elementAt(i+1)).doubleValue() < ((Double)X.elementAt(i)).doubleValue()){
                    
                    // We exchange this elements positions
                    intermediate = ((Double)X.elementAt(i)).doubleValue();
                    X.setElementAt(X.elementAt(i+1),i);
                    X.setElementAt(new Double(intermediate),i+1);
                    
                    lChanged = true;
                    
                }// End of if
                
            }// End of for
            
            nIndex += 1;
            
        }//End of while
        
    }//End of method
    
    
/*
 * Return a String who contain the cutpoints as characters: "[0,1.2,3.4)"
 */
    
    private String CutPointsToString(){
        
        int i;
        String t=new String("");
        String s=new String("(");;
        
        for (i=0; i<cutPoints.size()-1;i++)
            s=s+t.valueOf(getCutPoint(i))+", ";
        s=s+t.valueOf(getCutPoint(i))+")";
        return s;
    }
    
    /**
     *  Return CutPoints of tree.
     */
    
    public Vector getCutPoints(){
        
        return cutPoints;
    }
    
    
    /**
     * This method will prune the tree according to some parameters. We will prune just leaves of
     * the tree, that is, the probability values. The prune will be done in terms of deleting
     * some exponential term that countls less than a delta% of the total mass of the function.
     * Also we will prune by joining two branches of a continuous node, or deleting a whole discrete
     * variable by its mean.
     *
     * @param t The tree to prune (It will be modified)
     * @param delta The percentage that must achive an exponential term in order not to be deleted.
     * @param epsilon The error limit to decide if we prune or not.
     * @param conf The configuration that gets to this leave
     * @param tIntegral The total value of the potential. It is used for computing the error.
     *
     */
    
    public void pruneTerms(ContinuousProbabilityTree t, double delta, double epsilon,ContinuousIntervalConfiguration conf, double tIntegral){
        
        int i,j,nMTE,d;
        double total,max,min,coef,k,tam,z,error;
        LinearFunction exponent;
        ContinuousProbabilityTree child,copyCPT;
        MixtExpDensity mte, mteNew,copyMTE;
        Continuous var = new Continuous();
        Node X;
        ContinuousIntervalConfiguration confNew;
        //System.out.println("I am in pruneterms, and this is the tree I try to prune: ");
        //t.print();
        
        X =t.getVar();
        
        nMTE = t.numberOfMTEChildren();
        
        if(nMTE > 0){//There is at least 1 MTE, so I go on.
            
            if (t.numberOfMTEChildrenNoConstant() > 0){
                //System.out.println("There is at least 1 exponential term");
                for(j=0 ; j<t.getNumberOfChildren() ; j++){
                    
                    //System.out.println("Child  "+j);
                    
                    if (X.getTypeOfVariable() == 0){// The variable is continuous
                        
                        child = t.getChild(j);
                        min = t.getCutPoint(j);
                        max = t.getCutPoint(j+1);
                        
                        
                        if (child.getLabel() == 2){// It is a probability, so we try to delete some term
                            //System.out.println("This child is a probability");
                            mte = new MixtExpDensity();
                            mte = child.getProb();
                            copyCPT = new ContinuousProbabilityTree();
                            copyCPT = child.copy();
                            if((mte.factors).size() > 0){// It has at least on exponential term
                                
                                // This interval is included in the configuration.
                                confNew = new ContinuousIntervalConfiguration();
                                confNew = conf.duplicate();
                                //System.out.println("This is confNew before adding this new configuration");
                                //confNew.print();
                                confNew.putValue((Continuous)X,min,max);
                                //System.out.println("This is confNew after adding this new configuration");
                                //confNew.print();
                                //X.print();
                                // The integral is calculated, to see how much is the total
                                for (i=0 ; i<confNew.size() ; i++){
                                    copyCPT = copyCPT.integral((Continuous)confNew.getVariable(i),confNew.getLowerValue(i),confNew.getUpperValue(i));
                                }
                                // This is the value of the MTE on this interval
                                total = (copyCPT.getProb()).getIndependent();
                                tam = (mte.factors).size();
                                i = 0;
                                // Now we try to find a term to be deleted
                                while( i < tam){
                                    //tam = (mte.factors).size();//The size
                                    child = new ContinuousProbabilityTree();
                                    child = t.getChild(j);
                                    mte = new MixtExpDensity();
                                    mte = child.getProb();
                                    copyCPT = new ContinuousProbabilityTree();
                                    copyCPT = child.copy();
                                    //System.out.println("This is the CPT we will try to delete some terms from");
                                    //child.print();
                                    //System.out.println("There are "+tam+" Exponential terms. This is the "+i);
                                    exponent = (LinearFunction)(mte.getExponent(i));
                                    coef = mte.getFactor(i);
                                    
                                    // This MixtExpDensity has just the term we want to delete
                                    mteNew = new MixtExpDensity(0.0);
                                    ((Vector)(mteNew.terms)).addElement(exponent);
                                    ((Vector)(mteNew.factors)).addElement(new Double(coef));
                                    copyMTE = new MixtExpDensity();
                                    copyMTE = mteNew.duplicate();
                                    //System.out.println("This should be zero: "+copyMTE.getIndependent());
                                    // Now the weight of the term is calculated
                                    for (d=0 ; d<confNew.size() ; d++){
                                        copyMTE = copyMTE.integral((Continuous)confNew.getVariable(d),confNew.getLowerValue(d),confNew.getUpperValue(d));
                                    }
                                    k = copyMTE.getIndependent();
                                    //System.out.println("This is the weight of the term: "+Math.abs(k));
                                    //System.out.println("The total weight is: "+total);
                                    if ((Math.abs(k)/total)<delta){//We delete, if the error decreases.
                                        
                                        // This MixtExpDensity is like the child, but without what we have to remove
                                        copyMTE = new MixtExpDensity(0.0);
                                        copyMTE = mte.duplicate();
                                        (copyMTE.factors).removeElementAt(i);
                                        (copyMTE.terms).removeElementAt(i);
                                        
                                        
                                        //Instead of adding the weight of the term to the independent term, we will
                                        // divide it up into all the terms (including independent term)
                                        
                                        // z is the volume of the intervals of the continuous variables
                                        /**
                                         *z = 1;
                                         *
                                         *for (d=0 ; d<confNew.size() ; d++){
                                         *
                                         *  z = z*(confNew.getUpperValue(d)-confNew.getLowerValue(d));
                                         *}
                                         *
                                         *copyMTE.addIndependent(k/z);
                                         */
                                        
                                        copyMTE = copyMTE.multiplyDensities(total/(total-k));
                                        
                                        // This copy1 is the CPT that contains to copy MTE
                                        ContinuousProbabilityTree copy1 = new ContinuousProbabilityTree(copyMTE);
                                        // Vamos a probar que pode si el peso es menor que delta, pasando del epsilon
                                        //error = ErrorPruning(copy1,child,confNew,tIntegral);
                                        //if (error < epsilon){// We have to remove thid term
                                        
                                        t.setChild(copy1,j);
                                        tam--;
                                        //copyCPT.print();
                                        //t.print();
                                        //System.out.println("This term is deleted, since the error,  "+error+" is lower than epsilon");
                                        //}
                                        //else{
                                        //i++;
                                        //System.out.println("Since the pruning error is not lower than epsilon, it is not pruned.");
                                        //}
                                    }// End of if (Math.abs(k)/total)<delta)
                                    
                                    else{//We do not prune this term
                                        i++;
                                        //System.out.println("This term is not pruned becouse its weight is over delta");
                                    }
                                    
                                    
                                }//End of while(i < tam)(for each exponential)
                                
                            }// End of if has at least one exponential term
                            
                        }// End of if
                        
                    }// End of if (it is continuous)
                    
                }// End of for (each child)
                
            }//End of if
            
        }//End of if
        
        //System.out.println("I exit pruneterms");
    }//End of pruneTerms
    
    /**
     * This method prunes t joining two adjacent children by the average of them
     *
     *
     * @param t The tree to prune (It will be modified)
     * @param conf The configuration that gets to this leave
     * @param epsilon The error limit to decide if we prune or not.
     * @param tIntegral The total value of the potential. It is used for computing the error.
     *
     *
     */
    
    public void pruneJoin(ContinuousProbabilityTree t, ContinuousIntervalConfiguration conf, double epsilon, double tIntegral ){
        
        int i,j,nChildren;
        double p1,p2,min1,min2,max1,max2,K;
        MixtExpDensity mte1,mte2,mteJoin;
        ContinuousProbabilityTree child1,child2,integralCPT,CPTPruned,CPTPrunedChild,cptJoin;
        ContinuousIntervalConfiguration conf1,conf2,confNew;
        Node X;
        boolean poda;
        //System.out.println("**I'm in JOIN***");
        if(t.getLabel() != 2){// It is not a probability
            
            X = t.getVar();
            if (X.getTypeOfVariable() == 0){// The variable is continuous
                //System.out.println("The variable is continuous");
                if (t.numberOfMTEChildrenNoConstant() > 1){// It has at least 2 MTE children that colud be joint.
                    i = 0;
                    nChildren = t.getNumberOfChildren();
                    while ((i+1) < nChildren){// We try to join each couple of children
                        System.out.println("We try to join children "+i+" and "+(i+1));
                        poda = false;
                        child1 = new ContinuousProbabilityTree();
                        child2 = new ContinuousProbabilityTree();
                        
                        child1 = t.getChild(i);
                        child2 = t.getChild(i+1);
                        
                        if((child1.getLabel() == 2)&(child2.getLabel() == 2)){//Both are probability
                            //System.out.println("Both children are probability");
                            // These are the values of the intervals for each child
                            min1 = t.getCutPoint(i);
                            max1 = t.getCutPoint(i+1);
                            max2 = t.getCutPoint(i+2);
                            min2 = max1;
                            //System.out.println("The three values of the cutpoint are: "+min1+" , "+max1+" , "+max2);
                            //Now I need two configurations to add these values
                            //System.out.println("This is conf: (I am in pruneJoin): ");
                            //conf.print();
                            conf1 = conf.duplicate();
                            conf2 = conf.duplicate();
                            //System.out.println("This is the size of the current configuration:"+conf.size());
                            
                            conf1.putValue((Continuous)X,min1,max1);
                            conf2.putValue((Continuous)X,min2,max2);
                            
                            //System.out.println("This is conf1: ");
                            //conf1.print();
                            
                            //System.out.println("Now I have created the configurations");
                            
                            //Now I obtain the weight for each child
                            //First child
                            integralCPT = new ContinuousProbabilityTree();
                            integralCPT = child1.copy();
                            //System.out.println("This should be the child "+i);
                            //integralCPT.print();
                            //System.out.println("This is the size of conf1: "+conf1.size());
                            
                            for (j=0 ; j<conf1.size() ; j++){
                                integralCPT = integralCPT.integral(conf1.getVariable(j),conf1.getLowerValue(j),conf1.getUpperValue(j));
                                //System.out.println("This is the tree after integrating over variable");
                                //(conf1.getVariable(j)).print();
                                //System.out.println(" over values "+conf1.getLowerValue(j)+" and "+conf1.getUpperValue(j));
                            }
                            
                            p1 = (integralCPT.getProb()).getIndependent();
                            System.out.println("p1 = "+p1);
                            //For child2
                            integralCPT = new ContinuousProbabilityTree();
                            integralCPT = child2.copy();
                            //System.out.println("This should be the child "+(i+1));
                            //integralCPT.print();
                            for (j=0 ; j<conf2.size() ; j++){
                                integralCPT = integralCPT.integral(conf2.getVariable(j),conf2.getLowerValue(j),conf2.getUpperValue(j));
                            }
                            
                            p2 = (integralCPT.getProb()).getIndependent();
                            System.out.println("p2 = "+p2);
                            //Now I join them
                            mte1 = new MixtExpDensity();
                            mte1 = child1.getProb();
                            mte2 = new MixtExpDensity();
                            mte2 = child2.getProb();
                            
                            mteJoin = new MixtExpDensity();
                            
                            mte1 = mte1.multiplyDensities(p1);
                            mte2 = mte2.multiplyDensities(p2);
                            
                            mteJoin = mte1.sumDensities(mte2);
                            
                            mteJoin = mteJoin.multiplyDensities(1/(p1+p2));
                            
                            //System.out.println("I have already created the mteJoin, it is: ");
                            //mteJoin.print();
                            // Now I need a value K, so that both threes (the original and the pruned one) integrate the
                            // same over the two domains joint, that is int(K*mteJoin)=p1+p2, so K = (p1+p2)/int(mteJoin)
                            // R. Rumi thesis, page 168.
                            
                            confNew = conf.duplicate();
                            confNew.putValue((Continuous)X,min1,max2);
                            
                            cptJoin = new ContinuousProbabilityTree(mteJoin);
                            integralCPT = new ContinuousProbabilityTree();
                            integralCPT = cptJoin.copy();
                            
                            for (j=0 ; j<confNew.size() ; j++){
                                integralCPT = integralCPT.integral(confNew.getVariable(j),confNew.getLowerValue(j),confNew.getUpperValue(j));
                            }
                            K = (integralCPT.getProb()).getIndependent();
                            K = (p1+p2)/K;
                            //System.out.println("The K making both weights equal is: "+K);
                            // Now the real mteJoin is mteJoin*k
                            
                            mteJoin = mteJoin.multiplyDensities(K);
                            
                            // I need this to obtain the error
                            confNew = conf.duplicate();
                            confNew.putValue((Continuous)X,min1,max2);
                            
                            // This will contain the tree resulting from pruning
                            CPTPruned = new ContinuousProbabilityTree();
                            CPTPruned = t.copy();
                            
                            CPTPrunedChild = new ContinuousProbabilityTree(mteJoin);
                            
                            // Now I remove the two joint branches, and replace them for the joint one
                            
                            CPTPruned.setChild(CPTPrunedChild,i);
                            //System.out.println("t after placing the pruned in position "+i);
                            //t.print();
                            
                            //Now I should remove child i+1;
                            (CPTPruned.child).removeElementAt(i+1);
                            //System.out.println("t after removing child "+(i+1));
                            //t.print();
                            
                            // and remove from the cutPoints i+1;
                            (CPTPruned.cutPoints).removeElementAt(i+1);
                            
                            // Now that I have already created this tree, I compare its error
                            
                            System.out.println("Now I see the error");
                            System.out.println("The error is: "+ErrorPruning(t,CPTPruned,confNew,tIntegral));
                            if (ErrorPruning(t,CPTPruned,confNew,tIntegral) < epsilon){//The error is lower, so we prune
                                System.out.println("We prune");
                                poda = true;
                                // Now I must remove the two branches that have just been joitn, and replace them by the joint
                                t.setChild(CPTPrunedChild,i);
                                //System.out.println("t after placing the pruned in position "+i);
                                //t.print();
                                
                                //Now I should remove child i+1;
                                (t.child).removeElementAt(i+1);
                                //System.out.println("t after removing child "+(i+1));
                                //t.print();
                                
                                
                                // And from cutpoints i+1;
                                (t.cutPoints).removeElementAt(i+1);
                                
                                //System.out.println("After pruning, t is:");
                                //t.print();
                                
                                i = 0; // So that it starts from the begining.
                                nChildren = t.getNumberOfChildren();
                            } else{ System.out.println("We do not prune");}
                            
                        }//End of if((child1.getLabel() == 2)&(child2.getLabel() == 2))
                        
                        if(!poda)//If we have not pruned, we increasi i, if we have pruned it will be zero, and we do not increase it.
                            i++;
                        
                    }//End of while ((i+1)<t.getNumberOfChildren())
                    
                    
                }// End of if (t.numberOfMTEChildreNoConstant() > 1)
                
            }// End of (X.getTypeOfVariable == 0){
        }// End of if (t.getLabel() != 2)
        //System.out.println("**I EXIT JOIN***");
    }//End of method pruneJoin
    
    
    /**
     * This method prunes deleting the tree and replacing it by the average of the values
     * All the values must be constant in the children.
     *
     * @param t The tree to prune (It will be modified)
     * @param epsilonDisc The error limit to decide if we prune or not.
     *
     *
     */
    
    public void pruneDiscrete(ContinuousProbabilityTree t, double epsilonDisc){
        
        int i;
        double average = 0;
        double chi,tot = 0;
        ContinuousProbabilityTree child,cpt;
        if (t.numberOfMTEChildren() > 0)
            if ((t.numberOfMTEChildren() == (t.getNumberOfChildren()-t.numberOfMTEChildrenNoConstant()))){
            // Every children are constant MTE, so I can try this kind of pruning
            //Let's get the average.
            for (i=0 ; i<t.getNumberOfChildren() ; i++){
                
                child = new ContinuousProbabilityTree();
                child = t.getChild(i);
                average = average + (child.getProb()).getIndependent();
                
            }//End of for
            
            average = average / t.getNumberOfChildren();
            
            chi = -((0.5-epsilonDisc)*Math.log(0.5-epsilonDisc)+(0.5+epsilonDisc)*Math.log(0.5+epsilonDisc));
            for (i=0 ; i<t.getNumberOfChildren() ; i++){
                
                child = new ContinuousProbabilityTree();
                child = t.getChild(i);
                tot = tot+((child.getProb()).getIndependent()*Math.log((child.getProb()).getIndependent()/average));
                
            }//End of for
            if (tot < chi){//Replace
                t.assignProb(average);
                System.out.println("We have prunedDiscrete");
            }
            
            }//End of if
        
    }//End of method
    
    
    /**
     * This method prunes a ContinuousProbabilityTree
     * It tries to apply the three pruning methods.
     *
     * @param t The tree to prune
     * @param delta The threshold
     * @param epsilon the threshold
     * @param epsilonJoin the threshold
     * @param epsilonDisc the threshold for discrete pruning
     * @param conf the configuration of the continuous variables leading to this node
     * @param tIntegral The total mass of the cpt
     */
    
    public void pruneIterative(ContinuousProbabilityTree t, double delta, double epsilon, double epsilonJoin, double epsilonDisc, ContinuousIntervalConfiguration conf, double tIntegral){
        
        int i,nChildren;
        double min,max;
        Node X;
        ContinuousIntervalConfiguration confNew;
        ContinuousProbabilityTree child,child1;
        MixtExpDensity mte1;
        //System.out.println("I'm on the iterative method");
        X = t.getVar();
        if (X.getTypeOfVariable() == 0){// The variable is continuous
            //System.out.println("The node variable is continuous");
            if(t.numberOfMTEChildren() == 0){// Its children are not probability, I do not prune, I call iteratively to this
                // method for each child
                
                for(i = 0 ; i<t.getNumberOfChildren() ; i++){
                    
                    confNew = new ContinuousIntervalConfiguration();
                    confNew = conf.duplicate();
                    
                    min = t.getCutPoint(i);
                    max = t.getCutPoint(i+1);
                    
                    confNew.putValue((Continuous)X,min,max);
                    
                    child = new ContinuousProbabilityTree();
                    child = t.getChild(i);
                    
                    pruneIterative(child,delta,epsilon,epsilonJoin,epsilonDisc,confNew,tIntegral);
                    
                }// End of for
                
                
            }//End of if(t.numberOfMTEChildren() == 0
            
            else{// We can prune, at least any of them
                //System.out.println("Any chid is probability, so I prune:");
                //System.out.println("-----First the join-----");
                pruneJoin(t,conf,epsilonJoin,tIntegral);
                if((delta > 0) && (epsilon > 0)){
                    //System.out.println("------Now the terms------");
                    pruneTerms(t,delta,epsilon,conf,tIntegral);
                }
                // Maybe we have joint all the children, so there is no more division in the range,
                // so the variable should not appear in the tree.
                
                nChildren = t.getNumberOfChildren();
                
                /** Lo quito pq no siempre ha de hacer esto, s�lo cuando no es de la forma X-->f, que es donde falla
                 *
                 *   if(nChildren == 1){// There is only one child,
                 *
                 *     System.out.println("Estamos en la situacion de mte_pos. Quitamos la variables y ponemos solo el potencial");
                 *     child1 = t.getChild(0);
                 *     mte1 = child1.getProb();
                 *     t.var = null;
                 *     t.assignProb(mte1);
                 *     t.label = 2;
                 *     t.cutPoints = null;
                 *     t.leaves = 0;
                 *   }
                 */
                
                // Not every child must be a MixtExpDensity
                // If any child is not an MTE function, I have to prune it too
                if(t.numberOfMTEChildren() != t.getNumberOfChildren())
                    
                    for(i = 0 ; i<t.getNumberOfChildren() ; i++){
                    child = new ContinuousProbabilityTree();
                    child = t.getChild(i);
                    
                    if (child.getLabel() != 2){//This child is not Mixt
                        
                        confNew = new ContinuousIntervalConfiguration();
                        confNew = conf.duplicate();
                        
                        min = t.getCutPoint(i);
                        max = t.getCutPoint(i+1);
                        
                        confNew.putValue((Continuous)X,min,max);
                        
                        child = new ContinuousProbabilityTree();
                        child = t.getChild(i);
                        
                        pruneIterative(child,delta,epsilon,epsilonJoin,epsilonDisc,confNew,tIntegral);
                        
                    }//End of if
                    
                    }//End of for
                
                
            }//End of else{//We can prune, at least any of them
            
        }//End of if(X.getTypeOfVariable == 0){//The variable is continuous
        
        else{// The variable is discrete
            
            if(t.numberOfMTEChildren() == 0){// No child is probability, I do not prune, I call iteratively to this
                // method for each child
                for(i = 0 ; i<t.getNumberOfChildren() ; i++){
                    
                    child = new ContinuousProbabilityTree();
                    child = t.getChild(i);
                    
                    pruneIterative(child,delta,epsilon,epsilonJoin,epsilonDisc,conf,tIntegral);
                    
                }// End of for
                
            }//End of if
            
            else{// Some children are probability, I prune
                if((delta >0) && (epsilon>0)){
                    pruneTerms(t,delta,epsilon,conf,tIntegral);
                }
                if(epsilonDisc > 0)
                    pruneDiscrete(t,epsilonDisc);
                
                //Not every child must be Mixt
                //If any child is not an MTE function, we prune it too
                if(t.numberOfMTEChildren() != t.getNumberOfChildren())
                    
                    for(i = 0 ; i<t.getNumberOfChildren() ; i++){
                    child = new ContinuousProbabilityTree();
                    child = t.getChild(i);
                    
                    if (child.getLabel() != 2){//This child is not Mixt
                        
                        child = new ContinuousProbabilityTree();
                        child = t.getChild(i);
                        
                        pruneIterative(child,delta,epsilon,epsilonJoin,epsilonDisc,conf,tIntegral);
                        
                    }//End of if
                    
                    }//End of for
                
            }// End of else {//Some children are probability, I prune
            
        }//End of else
        
        
    }//end of method PruneIterative
    
    
    /**
     * Method for pruning a ContinuousProbabilityTree. This method will call the iterative method.
     *
     * @param t The tree to prune
     * @param delta The threshold
     * @param epsilon the threshold
     * @param epsilonDisc the threshold for discrete pruning
     *
     */
    
    public void prune(ContinuousProbabilityTree t, double delta, double epsilon, double epsilonJoin, double epsilonDisc){
        
        
        int i;
        ContinuousIntervalConfiguration conf = new ContinuousIntervalConfiguration();
        double tIntegral;
        NodeList nl;
        nl = new NodeList();
        ContinuousProbabilityTree cpt;
        Node X;
        // The first thing we do is to calculate the integral, becouse we need it to obtain the errors
        // To do it we'll keep in nl every variable we run into in the CPT.
        // We will use an auxiliar function called getVariables
        
        //System.out.println("We begin with the pruning");
        //System.out.println("We will calculate the total weight");
        nl = getVariables(t);
        cpt = t.copy();
        //System.out.println("This is the tree we are pruning (at the beginning)");
        //t.print();
        // For each of these variables, we remove it from the CPT
        for(i = 0 ; i< nl.size() ; i++){
            
            X = nl.elementAt(i);
            
            if(X.getTypeOfVariable() == 0)//It is continuous
                cpt = cpt.addVariable((Continuous)X);
            
            else
                cpt = cpt.addVariable((FiniteStates)X);
            
        }//End of for
        
        // Now in the CPT there is only one value
        
        tIntegral = (cpt.getProb()).getIndependent();
        
        //System.out.println("The total weight is: "+tIntegral);
        //Now we call the iterative
        //System.out.println("Now we call the iterative");
        if(!t.isProbab())
            pruneIterative(t,delta,epsilon,epsilonJoin,epsilonDisc,conf,tIntegral);
        
    }
    
    /**
     * Computes the error while pruning of two ContinuousProbabilityTree
     * error = int(f(f-g)^2)/(total^3)
     * @param t1 the first tree
     * @param t2 the second tree
     * @param conf The configuration for the integral
     * @param A The total mass of the original tree.
     */
    
    public double ErrorPruning(ContinuousProbabilityTree t1, ContinuousProbabilityTree t2, ContinuousIntervalConfiguration conf, double A){
        
        int i;
        double res;
        MixtExpDensity f;
        
        ContinuousProbabilityTree cpt;
        //System.out.println("I am in error pruning");
        // quiero t1(t1-t2)(t1-t2)
        f = new MixtExpDensity(-1);
        //System.out.println("I will turn t2 to negative");
        
        //System.out.println("t2 before:");
        //System.out.println();
        //t2.print();
        
        cpt = t1.combine(f,t2,1);
        
        //System.out.println("t2 afater:");
        //System.out.println();
        //cpt.print();
        //System.out.println();
        
        //System.out.println("Now I add the negative and the positive");
        cpt = t1.add(cpt,t1);
        
        //cpt.print();
        
        //System.out.println("Now I do the square of the sustraction");
        cpt = cpt.combine(cpt,cpt,1);
        
        //System.out.println("Now I multiply the square by t1");
        cpt = cpt.combine(t1,cpt,1);
        //System.out.println("Now I get the integral");
        for (i=0 ; i<conf.size() ; i++){
            cpt = cpt.integral(conf.getVariable(i),conf.getLowerValue(i),conf.getUpperValue(i));
        }
        
        res = (cpt.getProb()).getIndependent()/(A*A*A);
        //System.out.println("This is the error: "+res);
        //System.out.println("I exit error pruning");
        return res;
    }
    
    
    /**
     * Computes the error while pruning of two ContinuousProbabilityTree
     * error = int(f(f-g)^2)/(total^3)
     * @param t1 the first tree
     * @param t2 the second tree
     * @param conf The configuration for the integral
     * @param A The total mass of the original tree.
     * @param flag an integer indicating if the MixtExpDensities must be simplified
     */
    
    public double ErrorPruning(ContinuousProbabilityTree t1, ContinuousProbabilityTree t2, ContinuousIntervalConfiguration conf, double A, int flag){
        
        int i;
        double res;
        MixtExpDensity f;
        
        ContinuousProbabilityTree cpt;
        //System.out.println("I am in error pruning (flag), and the flag is: "+flag);
        // quiero t1(t1-t2)(t1-t2)
        f = new MixtExpDensity(-1);
        //System.out.println("I will turn t2 to negative");
        
        //System.out.println("t2 before:");
        //System.out.println();
        //t2.print();
        
        //System.out.println("Combining the two potentials");
        cpt = t1.combine(f,t2,flag);
        
        //System.out.println("t2 afater:");
        //System.out.println();
        //cpt.print();
        //System.out.println();
        
        //System.out.println("Now I add the negative and the positive");
        cpt = t1.add(cpt,t1);
        
        //cpt.print();
        
        //System.out.println("Now I do the square of the sustraction");
        cpt = cpt.combine(cpt,cpt,flag);
        
        //System.out.println("Now I multiply the square by t1");
        cpt = cpt.combine(t1,cpt,flag);
        
        //System.out.println("Now I get the integral");
        for (i=0 ; i<conf.size() ; i++){
            cpt = cpt.integral(conf.getVariable(i),conf.getLowerValue(i),conf.getUpperValue(i),flag);
        }
        
        res = (cpt.getProb()).getIndependent()/(A*A*A);
        //System.out.println("This is the error: "+res);
        //System.out.println("I exit error pruning (flag)");
        return res;
    }
    
    /**
     * Obtains a ContinuousProbabilityTree representing the marginal density
     * for the variable X.
     *
     * @param X the variable
     * @param sample A ContinuousCaseListMem with the database
     *
     * @return A ContinuousProbabilityTree
     */
    
    public ContinuousProbabilityTree learnUnivariate(FiniteStates X, ContinuousCaseListMem sample){
        
        int i,n,j,npaxi,nxi;
        double p,num,den;
        ContinuousProbabilityTree cpt = new ContinuousProbabilityTree(X);
        ContinuousProbabilityTree childi;
        Configuration conf;
        MixtExpDensity mte;
        
        n = X.getNumStates();
        // The total number of the database cases
        npaxi = sample.getNumberOfCases();
        for(i = 0; i<n ; i++){
            conf = new Configuration();
            conf.putValue(X,i);
            // The absolute counts of the configuration i of X
            nxi = getAbsCounts(sample,X,i);
            
            //This will be the probability value for X = i using Laplace's correction
            num = (double)((double)(nxi)+1);
            den = (double)((double)(npaxi)+(double)n);
            //System.out.println("The numerator is "+num+" and the denominator "+den);
            p = num/den;
            //System.out.println("This is the p we obtain: "+p);
            childi = new ContinuousProbabilityTree(p);
            cpt.setChild(childi,i);
            
        }//End of for
        
        return cpt;
        
    }
    
    
    /**
     * Returns the absolute counts of X = i in a ContinuousCaseListMem
     *
     * @param X the variable
     * @param sample A ContinuousCaseListMem with the database
     * @param i The value of the variable
     *
     * @return The counts of X = i
     */
    
    public int getAbsCounts(ContinuousCaseListMem sample, FiniteStates X, int i){
        
        int j,cont,xValue,indexX;
        NodeList vars2;
        Vector vars = new Vector();
        
        cont = 0;
        
        //First of all I need to know the index of the variable
        vars = sample.getVariables();
        
        vars2 = new NodeList();
        // I jump from the nodes vector to the nodelist vars2
        for(j=0 ; j<vars.size() ; j++){
            vars2.insertNode((Node)vars.elementAt(j));
        }
        
        indexX = vars2.getId(X);
        
        // Now I check if the value of X in each case is i
        for(j = 0 ; j<sample.getNumberOfCases() ; j++){
            xValue = (int)sample.getValue(j,indexX);
            if (xValue == i) cont++;
        }
        
        //System.out.println("There are "+cont+" values with X = "+i+" in the sample");
        return cont;
        
    }
    
    /**
     * This method operates over a ContinuousProbabilityTree whose children are probabilities
     * It returns the value of a X=x, when the MixtExpDensities are defined just for this variable X
     * and the ContinuousProbabilityTree's variable is X
     *
     * @param X the variable
     * @param x A ContinuousCaseListMem with the database
     *
     * @return A double f(x)
     */
    public double getValue(Continuous X, double x){
        
        int i;
        double res,min,max;
        MixtExpDensity f;
        res = 0;
        for (i = 0 ; i<getNumberOfChildren() ; i++){
            
            System.out.println("el number of children: "+getNumberOfChildren());
            
            min = getCutPoint(i);
            max = getCutPoint(i+1);
            if ((min <= x) & (x <= max) ){ //This is the child  we must use
                f = getChild(i).getProb();
                System.out.println("This is the f we must use:");
                f.print();
                res = f.getValue(X,x);
            }
            
        }// End of for
        return res;
    }// ENd of method
    
    
    /**
     * This method tells you how many of the children are MTE functions (probability)
     *
     * @return An integer
     */
    
    public int numberOfMTEChildren(){
        
        int i,res;
        ContinuousProbabilityTree child;
        
        res = 0;
        for(i=0 ; i<getNumberOfChildren() ; i++){
            child = getChild(i);
            if (child.getLabel() == 2)
                res++;
        }
        return res;
    }//End of method
    
    
    
    /**
     * This method tells you how many of the children are MTE functions
     * not constant.
     *
     * @return An integer
     */
    
    public int numberOfMTEChildrenNoConstant(){
        
        int i,j,res;
        ContinuousProbabilityTree child;
        MixtExpDensity f;
        boolean one;//Tells if there is one exponential terms
        res = 0;
        for(i=0 ; i<getNumberOfChildren() ; i++){
            child = getChild(i);
            
            if (child.getLabel() == 2){
                f = new MixtExpDensity();
                f = child.getProb();
                one = false;
                for (j=0 ; j<f.getNumberOfExp() ; j++){
                    if ((f.getFactor(j) != 0) & (!one)){//Then the factor is not zero, so there is at least one exponential term
                        res++;
                        one = true;
                        j = f.getNumberOfExp();
                    }
                }
            }
        }
        return res;
    }//End of method
    
    
    /**
     *
     * Returns a nodelist containing all the variables involved in the CPT. It uses getVarIterative
     *
     * @param t A ContinuousProbabilityTree
     *
     * @return The variables for which the CPT are defined for
     */
    
    public NodeList getVariables(ContinuousProbabilityTree t){
        
        NodeList nl = new NodeList();
        
        getVarIterative(t,nl);
        
        return nl;
        
    }
    
    
    /**
     *
     * Obtains all the variables involved in the CPT and below (Iterative).
     *
     * @param t The ContinuousProbabilityTree
     * @param nl The nodelist where the variables are stored
     */
    
    public void getVarIterative(ContinuousProbabilityTree t, NodeList nl){
        
        int i;
        ContinuousProbabilityTree child;
        Node X;
        
        if (t.getLabel() != 2){//This node is a variable
            
            X = t.getVar();
            if(nl.getId(X) == -1){//It is not in nl
                
                nl.insertNode(X);
                
            }//End of if
            
            //Now I do the same with the children.
            
            for(i = 0 ; i< t.getNumberOfChildren() ; i++){
                
                child = new ContinuousProbabilityTree();
                child = t.getChild(i);
                getVarIterative(child,nl);
                
            }
            
        }//End of if
        
    }//End of method
    
    /**
     * Return the minimun cut point value for all expanded continuous variables in the tree.
     */
    public double minCutPoint(){
        
        int nv;
        double min;
        
        if (isProbab()){
            min=Double.MAX_VALUE;
        }else if(isDiscrete()){
            nv=getNumberOfChildren();
            min=getChild(0).minCutPoint();
            for (int i=1; i<nv; i++){
                double tmp=getChild(i).minCutPoint();
                if (tmp<min)
                    min=tmp;
            }
        }else if (isContinuous()){
            min=getCutPoint(0);
        }else
            min=Double.MAX_VALUE;
        
        return min;
    }
    
    /**
     * Return the maximum cut point value for all expanded continuous variables in the tree.
     */
    public double maxCutPoint(){
        int nv;
        double max;
        
        if (isProbab()){
            max=Double.MIN_VALUE;
        }else if(isProbab()){
            nv=getNumberOfChildren();
            max=getChild(0).maxCutPoint();
            for (int i=1; i<nv; i++){
                double tmp=getChild(i).maxCutPoint();
                if (tmp>max)
                    max=tmp;
            }
        }else if (isContinuous()){
            max=getCutPoint(getCutPoints().size()-1);
        }else
            max=Double.MIN_VALUE;
        
        return max;
    }
    
/*
 * Return a vector with all tree's sorted cut points.
 */
    
    public Vector extractAllCutPoints(){
        
        int i;
        Vector v=new Vector();
        if (isProbab()){
            return v;
        }else{
            for (i=0; i<getNumberOfChildren(); i++){
                if (getChild(i).isProbab()){
                    v.addElement(new Double(getCutPoint(i)));
                }else{
                    Vector p=(Vector)getChild(i).extractAllCutPoints().clone();
                    p.removeElementAt(p.size()-1);
                    addVector(v,p);
                }
            }
            v.addElement(new Double(getCutPoint(i)));
        }
        return v;
    }
    
    
/*
 * Return a vector with all MixtExpDensity sorted by its cut points.
 */
    
    public Vector extractAllProbabilities(){
        
        Vector v=new Vector();
        if (isProbab()){
            v.addElement(getProb());
        }else{
            for (int i=0; i<getNumberOfChildren(); i++)
                addVector(v,getChild(i).extractAllProbabilities());
        }
        return v;
        
        
    }
/*
 * Add the v2 elements in vector v1.
 */
    private void addVector(Vector v1, Vector v2){
        for (int i=0; i<v2.size(); i++)
            v1.addElement(v2.elementAt(i));
    }
    
    /**
     *  This method completes the tree with new branchs. So, the range
     *  of all continuous variables of the tree is included in this tree.
     *  If a subinterval is not defined then it's added a new branch with null probability.
     *  Example:
     *  X1
     *      - (1,2) --> 0.3
     *      - (2,3) --> 0.7
     *  if range(X1) = (0,4). Output:
     *  X1
     *      - (0,1) --> 0.0
     *      - (1,2) --> 0.3
     *      - (2,3) --> 0.7
     *      - (3,4) --> 0.0
     *
     */
    public void expandZeros(){
        expandZerosP(new NodeList());
    }
    /**
     *  This method completes the tree with new branches. So, the range
     *  of all continuous variables of the tree is included in this tree.
     *  If a subinterval is not defined then it's added a new branch with null probability.
     *
     *  @param visitedContinuous, is a Nodelist with the continuous nodes that are zeros expanded in
     *                            the tree already. At the beginning, this nodelist is empty.
     */
    private void expandZerosP(NodeList visitedContinuous){
        
        NodeList newnl=null;
        if (isProbab())
            return;
        else if (isContinuous()){
            
            double min=((Continuous)var).getMin();
            double max=((Continuous)var).getMax();
            
            double min1=getCutPoint(0);
            double max1=getCutPoint(getCutPoints().size()-1);
            if (visitedContinuous.getId(var)==-1){
                Vector v=visitedContinuous.getNodes();
                v.addElement(var);
                newnl=new NodeList((Vector)v.clone());
                if (min<min1){
                    getCutPoints().insertElementAt(new Double(min),0);
                    getChilds().insertElementAt(new ContinuousProbabilityTree(0.0),0);
                }
                if (max>max1){
                    getCutPoints().addElement(new Double(max));
                    getChilds().addElement(new ContinuousProbabilityTree(0.0));
                }
            }
        }
        if (newnl!=null)
            visitedContinuous=newnl;
        for (int i=0; i<getNumberOfChildren();i++)
            getChild(i).expandZerosP(visitedContinuous.copy());
        
    }
    
    
    
    /**
     * Creates randomly a fully expanded ContinuousProbabilityTree. The probability values for the discrete variables
     * are simulated by an Exponential distribution, the value of the MTE is simulated as follows:
     * The independent term by an Exponential distribution, the coefficient of the exponential by
     * an Exponential distribution, and the exponent by a Normal distribution. Afterwards the MTE is
     * normalised. Those nodes not being a leaf are splitted according to the parameters.
     *
     * @param parents A vector containing the set of parents.
     * @param var The variable (not conditioning).
     * @param nSplits The number of splits in the range of the continuous variables (1 2, 3, or 4).
     * @param nTerms The number of exponential terms in a leaf (0, 1 or 2).
     * @param meanExpDiscrete The mean of the Exponential distribution the probability of the discrete variables are simulated with.
     * @param meanExpIndep The mean of the Exponential distribution the independent terms are simulated with.
     * @param meanExpcoef The mean of the Exponential distribution the coefficients of the expoenntial terms are simulated with.
     * @param meanNormExponent The mean of the Normal distribution the exponents are simulated with.
     * @param sdevNormExponent The standar deviation of the Normal distribution the exponents are simulated with.
     *
     */
    
    public ContinuousProbabilityTree createFullyExpandedCPTIterative(Vector parents, Node var, int nSplits, int nTerms, double meanExpDiscrete, double meanExpIndep, double meanExpCoef, double meanNormalExponent, double sdevNormalExponent){
        
        int i, nStates;
        ContinuousProbabilityTree resCPT,childCPT;
        MixtExpDensity mte;
        double value,cutValue, indep, a, b, c, d;
        Vector cpoint, parentsCopy;
        Node parentVar;
        SampleGenerator samGen = new SampleGenerator();
        
        System.out.println("We are in createFullyExpandedCPTIterative in CPT class");
        
        resCPT = new ContinuousProbabilityTree();
        System.out.println("Variable: ");
        var.print();
        if(parents.size() == 0){// No more parents variable to split with, so I must create the leaf
            System.out.println("No more splitting, now I get the MTE");
            // First I must check if it is continuous or discrete
            if(var.getTypeOfVariable() == 1){//It is discrete
                System.out.println("The variable is discrete");
                nStates = ((FiniteStates)var).getNumStates();
                resCPT = new ContinuousProbabilityTree((FiniteStates)var);
                for(i=0 ; i <nStates ; i++){
                    
                    //Now I simulate a value for each children
                    
                    value = samGen.randomExponential(meanExpDiscrete);
                    mte = new MixtExpDensity(value);
                    childCPT = new ContinuousProbabilityTree(mte);
                    resCPT.setChild(childCPT,i);
                    
                }
                
                resCPT.normalizeLeaves(var);
                
            }else{// It is continuous
                System.out.println("The variable is continuous");
                // This will be the cutPoints
                cpoint = new Vector();
                for(i=0 ; i<=nSplits ; i++){
                    cutValue = ((Continuous)var).getMin()+((double)i)/nSplits*(((Continuous)var).getMax()-((Continuous)var).getMin());
                    cpoint.addElement(new Double(cutValue));
                }
                
                resCPT = new ContinuousProbabilityTree((Continuous)var,cpoint);
                
                // Now I create the children and insert them in the cpt
                for(i=0 ; i < nSplits ; i++){
                    indep = samGen.randomExponential(meanExpIndep);
                    if(nTerms > 0 ){
                        a = samGen.randomExponential(meanExpCoef);
                        b = samGen.randomNormal(meanNormalExponent,sdevNormalExponent);
                        if (nTerms > 1 ){
                            c = samGen.randomExponential(meanExpCoef);
                            d = samGen.randomNormal(meanNormalExponent,sdevNormalExponent);
                        }else{
                            c = 0;
                            d= 0;
                        }// End of else
                        
                    }else{
                        a = 0;
                        b = 0;
                        c = 0;
                        d = 0;
                    }// End of else
                    
                    mte = new MixtExpDensity(a,b,c,d,indep,(Continuous)var);
                    childCPT = new ContinuousProbabilityTree(mte);
                    resCPT.setChild(childCPT,i);
                    
                }// End of for
                
                resCPT.normalizeLeaves(var);
                
            }// End of else
            
        }// End of if (parents == 0)
        else{// Now we must continue splitting the tree
            
            parentVar = (Node)parents.firstElement();
            System.out.println("This is the variable we must go on splitting:");
            parentVar.print();
            if(parentVar.getTypeOfVariable() == 1){// The variable is discrete.
                // I must create as many children as states of the variable
                System.out.println("The parent variable is discrete:");
                nStates = ((FiniteStates)parentVar).getNumStates();
                resCPT = new ContinuousProbabilityTree((FiniteStates)parentVar);
                
                // Now I call this method iteratively for each child
                for(i=0 ; i <nStates ; i++){
                    
                    //Now I create each child
                    parentsCopy = (Vector)parents.clone();
                    parentsCopy.removeElementAt(0);
                    System.out.println("This is the size of parents "+parents.size()+" and this is the size of parentsCopy"+parentsCopy.size());
                    System.out.println("Before calling creteFullyExpandedCPTIterative for each child");
                    resCPT.setChild(createFullyExpandedCPTIterative(parentsCopy,var,nSplits,nTerms,meanExpDiscrete,meanExpIndep,meanExpCoef,meanNormalExponent,sdevNormalExponent),i);
                }
            }else{// The variable is continuous
                System.out.println("The parent variable is continuous:");
                //parentVar.print();
                cpoint = new Vector();
                for(i=0 ; i<=nSplits ; i++){
                    cutValue = ((Continuous)parentVar).getMin()+((double)i)/nSplits*(((Continuous)parentVar).getMax()-((Continuous)parentVar).getMin());
                    cpoint.addElement(new Double(cutValue));
                }
                resCPT = new ContinuousProbabilityTree((Continuous)parentVar,cpoint);
                
                // Now I call this method iteratively for each child
                for(i=0 ; i < nSplits ; i++){
                    parentsCopy = (Vector)parents.clone();
                    parentsCopy.removeElementAt(0);
                    System.out.println("This is the size of parents "+parents.size()+" and this is the size of parentsCopy"+parentsCopy.size());
                    System.out.println("Before calling creteFullyExpandedCPTIterative for each child");
                    resCPT.setChild(createFullyExpandedCPTIterative(parentsCopy,var,nSplits,nTerms,meanExpDiscrete,meanExpIndep,meanExpCoef,meanNormalExponent,sdevNormalExponent),i);
                }
                
            }// End of else
            
        }// End of else
        
        System.out.println("We exit createFullyExpandedCPTIterative in CPT class");
        
        return resCPT;
        
    }// End of method
    
    
/*
 * This method discretises a ContinuousProbabilityTree, it removes the exponential terms, and adds
 * the weight of the term to the independent term. The object is modified.
 */
    
    public void getDiscretePotential(ContinuousProbabilityTree cpt,Continuous var){
        
        int i,j;
        ContinuousProbabilityTree discreteTree, child;
        MixtExpDensity mteCont;
        double min, max, acum;
        Continuous contVar;
        
        // If the tree children are MixtExpDensities, the exponential terms are removed, if not
        // we apply this method to the children
        
        for(i=0 ; i < cpt.getNumberOfChildren() ; i++){
            
            child = cpt.getChild(i);
            
            
            if(child.isProbab()){//This children is an MTE, so we remove its exponential terms
                
                mteCont = child.getProb();
                
                contVar = new Continuous();
                contVar = (Continuous)cpt.getVar();
                min = cpt.getCutPoint(i);
                max = cpt.getCutPoint(i+1);
                if(contVar.equals(var)){
                    mteCont = mteCont.integral(contVar,min,max);
                    mteCont = mteCont.multiplyDensities(1/(max-min));
                }else{
                    System.out.println("ERROR");
                }// end of else
                
                cpt.setNewChild(new ContinuousProbabilityTree(mteCont),i);
                
            }else{// This child is not an MTE, so apply this method to its children
                
                getDiscretePotential(child,var);
                
            }// End of else
            
        }// End of for
        
        
    }// End of method
    
    /**
     * Computes the mean square error of two Discrete ContinuousProbabilityTree (normalised)
     * error = sum(pi(pi-aproxpi)^2)
     * @param t1 the first tree
     * @param t2 the second tree
     *
     */
    
    public double ErrorDiscrete(ContinuousProbabilityTree t1, ContinuousProbabilityTree t2 ){
        
        int i;
        double res = 0.0;
        double p1,p2;
        //System.out.println("First Tree: ");
        //t1.print();
        //System.out.println("Second Tree: ");
        //t2.print();
        //System.out.println(t1.getVar().getTypeOfVariable());
        if(((t1.getVar().getTypeOfVariable())!= 1) || ((t2.getVar().getTypeOfVariable())!= 1)){
            System.out.println("Error: The variables are not discrete");
            System.exit(0);
        }
        if(!t1.getVar().equals(t2.getVar())){
            System.out.println("Error: The variables are not the same");
            System.exit(0);
        }
        
        for(i=0 ; i<t1.getNumberOfChildren() ; i++){
            
            p1 = t1.getChild(i).getProb().getIndependent();
            p2 = t2.getChild(i).getProb().getIndependent();
            res = res + p1*(p1-p2)*(p1-p2);
            
        }
        
        return res;
    }
    
    
    /**
     * Obtains the number of terms on each leaf, assuming it is constant in every leaf
     *
     * @return (int) The number of terms (including independent term)
     */
    
    public int obtainNumTerms(){
        
        
        int i;
        MixtExpDensity mte;
        //System.out.println("Estoy en obtainNumterms");
        if(isProbab()){//It is a Probability
            //System.out.println("Es un MTE, este:");
            
            mte = getProb();
            //mte.print();
            //System.out.println("Su n�mero de t�rminos exponenciales es: "+mte.getNumberOfExp());
            return (mte.getNumberOfExp() + 1);
            
        }else{//It is not a probability, I must go deeper in the tree
            
            return getChild(0).obtainNumTerms();
            
        }
        
    }//End of method
    
    
    /**
     * Obtains the number of splits on each range of continuous variable, assuming it is constant in every variable
     *
     * @return (int) The number of splits
     */
    
    public int obtainNumSplits(){
        
        
        int i;
        //System.out.println("Estoy en obtainNumterms");
        if(isContinuous()){//It is a continuous variable
            //System.out.println("Es una var continua, este es un n�mero de hijos: "+getNumberOfChildren());
            return getNumberOfChildren();
        } else{//It is not continuous, I must go deeper in the tree
            //System.out.println("No es una var continua");
            if(isProbab()){// It is a MTE, so there is no cont variable
                //System.out.println("Es un MTE, he llegado al final y no he encontrado una var continua");
                return 0;
            } else{
                //System.out.println("Busco en su hijo");
                return getChild(0).obtainNumSplits();
            }
        }
        
    }//End of method
    
    /**
     * Obtains the actual size of the potential, that is, the number of exponential terms (including contants)
     * this ContinuousProbabilityTree has.
     *
     * @return (int) The actual size.
     */
    
    public int actualSize(){
        
        int i, s;
        
        if(isProbab()){
            
            s = (getProb().getNumberOfExp()+1);
            
        }else{
            
            s = 0;
            for(i=0 ; i<getNumberOfChildren() ; i++)
                s = s + getChild(i).actualSize();
            
        }
        
        return s;
        
    }
    
    public boolean isUnity(){
        
        MixtExpDensity f;
        boolean res = false;
        if(isProbab()){
            f = getProb();
            if(f.getNumberOfExp() == 0)
                res = true;
        }
        
        return res;
    }
    
    
    /**
     * This method prunes a tree just eliminating exponentials terms
     * so that remains only two exponential terms.
     * This method is an interative method that tries to localize where the leaves are
     *
     * @return (int) The number of splits
     */
    public void prune2(ContinuousIntervalConfiguration conf){
        
        int i;
        MixtExpDensity mte = new MixtExpDensity();
        MixtExpDensity mte2 = new MixtExpDensity();
        ContinuousProbabilityTree child = new ContinuousProbabilityTree();
        Node X;
        double min,max;
        
        if(isProbab()){ // Es una hoja, asi que hay que le pasamos el metodo de MTE
            
            mte = getProb();
            mte2 = mte.prune2Leaf(conf);
            assignProb(mte2);
            
        }else{// No es una hoja, asi que seguimos buscando, en concreto buscamos
            // en todos sus hijos.
            X = getVar();
            for(i = 0 ; i < getNumberOfChildren() ; i ++){
                
                if (X.getTypeOfVariable() == 0){// The variable is continuous
                    min = getCutPoint(i);
                    max = getCutPoint(i+1);
                    conf.putValue((Continuous)X,min,max);
                    
                }
                
                child = getChild(i);
                child.prune2(conf);
                
            }
            
        }
        
        
    }
    
    
    /**
     * Computes an approximation of the Kullback-Leibler divergence from this mixed tree to
     * a given one. The integral is approximated by a sum of as many rectangles as given.
     * Both trees are supposed to be defined over the same variable, and only one.
     *
     * @param t the <code>ContinuousProbabilityTree</code> with respect to which the KL
     * divergence will be computed.
     * @param r the number of rectangles.
     * @return the estimated KL divergence.
     */
    
    public double kullbackLeiblerDivergence(ContinuousProbabilityTree t, int r) {
        
        double kl = 0.0, interval, increment, int1, int2, point;
        int i, j;
        ContinuousProbabilityTree f1, f2;
        
        interval = this.minCutPoint();
        increment = (double)(this.maxCutPoint()-this.minCutPoint()) / (double)r;
        
        for (i=1 ; i<(r-1) ; i++) {
            f1 = this.integral((Continuous)this.getVar(),interval,interval+increment);
            f2 = t.integral((Continuous)this.getVar(),interval,interval+increment);
            
            int1 = 0.0;
            int2 = 0.0;
            
            for (j=0 ; j<f1.getNumberOfChildren() ; j++) {
                int1 += f1.getChild(j).getProb().getIndependent();
            }
            
            for (j=0 ; j<f2.getNumberOfChildren() ; j++) {
                int2 += f2.getChild(j).getProb().getIndependent();
            }
            
            kl += int1 * Math.log(int1/int2);
        }
        
        return kl;
    }
    
    
    public double firstOrderMoment(){
        
        double res = 0.0;
        MixtExpDensity mte;
        Vector general;
        Vector vecMin;
        Vector vecMax;
        Vector vecMTEs;
        int i,j;
        double min, max, a, b;
        
        general = getMaxMinMTEsVector();
        vecMin = (Vector)general.elementAt(0);
        vecMax = (Vector)general.elementAt(1);
        vecMTEs = (Vector)general.elementAt(2);
        
        for(i=0 ; i<vecMin.size(); i++){
            
            mte = (MixtExpDensity)vecMTEs.elementAt(i);
            min = ((Double)vecMin.elementAt(i)).doubleValue();
            max = ((Double)vecMax.elementAt(i)).doubleValue();
            
            res = res + (mte.getIndependent()*(((max*max)-(min*min))/2));
            for(j=0 ; j<mte.getNumberOfExp() ; j++){
                
                //LinearFunction l= ((LinearFunction)mte.getExponent(i));
                LinearFunction l= ((LinearFunction)mte.getExponent(j));
                b = l.getCoefficient(0);
                a = mte.getFactor(j);
                
                res = res + (a/(b*b))*(Math.exp(b*max)*(b*max-1)-Math.exp(b*min)*(b*min-1));
            }
        }
        
        return res;
        
    }
    
    
    /**
     * Computes the quantiles of the distribution. It assumes that the
     * tree is a density, i.e., integrates up to one. Actually,
     * the method returns an approximation with error lower than 0.01
     *
     * @param the probability value for which the quantile will be computed.
     * @return the corresponding quantile.
     */
    
    public double quantile(double prob) {
        
        double res = 0.0;
        MixtExpDensity mte;
        Vector general;
        Vector vecMin;
        Vector vecMax;
        Vector vecMTEs;
        int i,j;
        double min, max, a, b, acum, intervalMass, midPoint = 0.0, auxProb, auxMax, auxMin;
        boolean intervalFound, found;
        Continuous v = (Continuous)this.getVar();
        
        // First of all, we get the mte functions in the leaves.
        general = getMaxMinMTEsVector();
        vecMin = (Vector)general.elementAt(0);
        vecMax = (Vector)general.elementAt(1);
        vecMTEs = (Vector)general.elementAt(2);
        
        intervalFound = false;
        acum = 0.0;
        i = 0;
        
        while ((!intervalFound) && (i < vecMin.size())) {
            mte = (MixtExpDensity)vecMTEs.elementAt(i);
            min = ((Double)vecMin.elementAt(i)).doubleValue();
            max = ((Double)vecMax.elementAt(i)).doubleValue();
            intervalMass = mte.integral(v,min,max).getIndependent();
            
            if ((acum+intervalMass >= prob)) {
                intervalFound = true;
            }
            else {
                i++;
                acum += intervalMass;
            }
        }
        
        // At this point, acum must be the integral of the subintervals
        // below the quantile, and i is the interval where the quantile is.
        
        mte = (MixtExpDensity)vecMTEs.elementAt(i);
        min = ((Double)vecMin.elementAt(i)).doubleValue();
        max = ((Double)vecMax.elementAt(i)).doubleValue();
        
        auxMax = max;
        auxMin = min;
        found = false;
        
        while (!found) {
            midPoint = (auxMax + auxMin) / 2.0;
            auxProb = acum + mte.integral(v,min,midPoint).getIndependent();
            if (Math.round(prob*100) == Math.round(auxProb*100)) {
                found = true;
            }
            else {
                if (auxProb > prob) {
                    auxMax = midPoint;
                }     
                else {
                    auxMin = midPoint;
                }
            }
        }
        
        return (midPoint);
    }
    
    
    /**
     * Computes the median of the distribution. It assumes that the
     * tree is a density, i.e., integrates up to one. Actually,
     * the method returns an approximation with error lower than 0.01
     *
     * @return the estimated median.
     */
    
    public double median() {
        
        return (quantile(0.5));
    }
    
    
    public double secondOrderMoment(){
        
        double res = 0.0;
        MixtExpDensity mte;
        Vector general;
        Vector vecMin;
        Vector vecMax;
        Vector vecMTEs;
        int i,j;
        double min, max,a , b;
        
        general = getMaxMinMTEsVector();
        vecMin = (Vector)general.elementAt(0);
        vecMax = (Vector)general.elementAt(1);
        vecMTEs = (Vector)general.elementAt(2);
        
        for(i=0 ; i<vecMin.size(); i++){
            
            mte = (MixtExpDensity)vecMTEs.elementAt(i);
            min = ((Double)vecMin.elementAt(i)).doubleValue();
            max = ((Double)vecMax.elementAt(i)).doubleValue();
            
            res = res + (mte.getIndependent()*(((max*max*max)-(min*min*min))/3));
            for(j=0 ; j<mte.getNumberOfExp() ; j++){
                
                //LinearFunction l= ((LinearFunction)mte.getExponent(i));
                LinearFunction l= ((LinearFunction)mte.getExponent(j));
                b = l.getCoefficient(0);
                a = mte.getFactor(j);
                
                res = res + (a/(b*b*b))*(Math.exp(b*max)*(2-2*b*max+b*b*max*max)-Math.exp(b*min)*(2-2*b*min+b*b*min*min));
            }
        }
        
        return res;
        
    }
    
    
    public double Variance(){
        
        double res;
        
        res = secondOrderMoment() - (Math.pow(firstOrderMoment(),2));
        
        return res;
        
        
    }
    
    
    public double firstOrderMomentDiscrete(){
        
        int i;
        double res = 0.0;
        
        //Doy por hecho que los estados son 0,1, 2...
        
        for(i=0 ; i < getNumberOfChildren(); i++){
            
            
            res = res + i*(getChild(i).getProb().getIndependent());
            
        }
        
        return res;
    }
    
    public double secondOrderMomentDiscrete(){
        
        int i;
        double res = 0.0;
        
        //Doy por hecho que los estados son 0,1, 2...
        
        for(i=0 ; i < getNumberOfChildren(); i++){
            
            
            res = res + i*i*(getChild(i).getProb().getIndependent());
            
        }
        return res;
    }
    
    public double VarianceDiscrete(){
        
        double res;
        
        res = secondOrderMomentDiscrete() - (Math.pow(firstOrderMomentDiscrete(),2));
        
        return res;
        
        
    }
    
    
    public Vector getMeans(Vector relVec){
        
        int i;
        double media;
        Vector medias = new Vector();
        
        for(i=0 ; i<relVec.size() ; i++){
            PotentialContinuousPT exactPot = (PotentialContinuousPT)(((Relation)relVec.elementAt(i)).getValues());
            //exactRel = (Relation)((propagation2.results).elementAt(i));
            //exactPot = (PotentialContinuousPT)exactRel.getValues();
            ContinuousProbabilityTree exactCPT = exactPot.getTree();
            Node exactVar = exactCPT.getVar();
            
            if(exactVar.getTypeOfVariable() == 0){// The variable is continuous
                media = exactCPT.firstOrderMoment();
                medias.addElement(new Double(media));
                
            }else{//La variable es discreta
                
                media = exactCPT.firstOrderMomentDiscrete();
                medias.addElement(new Double(media));
            }
        }
        return medias;
    }
    
    public Vector getVariances(Vector relVec){
        int i;
        double var;
        Vector vars = new Vector();
        
        for(i=0 ; i<relVec.size() ; i++){
            PotentialContinuousPT exactPot = (PotentialContinuousPT)(((Relation)relVec.elementAt(i)).getValues());
            //exactRel = (Relation)((propagation2.results).elementAt(i));
            //exactPot = (PotentialContinuousPT)exactRel.getValues();
            ContinuousProbabilityTree exactCPT = exactPot.getTree();
            Node exactVar = exactCPT.getVar();
            
            if(exactVar.getTypeOfVariable() == 0){// The variable is continuous
                var = exactCPT.Variance();
                vars.addElement(new Double(var));
                
            }else{//La variable es discreta
                
                var = exactCPT.VarianceDiscrete();
                vars.addElement(new Double(var));
            }
        }
        return vars;
        
        
    }
    
    /**
     * Esta funcion se usa en simulateValue
     *
     *
     */
    
    public Vector getMaxMinMTEsVector(){
        
        //La funcion no puede ser probabilidad, asi que
        //vemos como son los hijos
        Vector general = new Vector();
        Vector vecMin = new Vector();
        Vector vecMax = new Vector();
        Vector vecMTEs = new Vector();
        int i,j,n;
        ContinuousProbabilityTree child;
        n = getNumberOfChildren();
        
        for(i=0 ; i<n ; i++){
            child = getChild(i);
            if (child.isProbab()){
                //Tengo que a�adir a los vectores vecMax y vecMin los valores
                //System.out.println("El �rbol tiene "+cutPoints.size()+" cutpoints");
                //System.out.println("Quiero los cutPoints "+i+" y "+(i+1));
                //System.out.println(" ya que el hijo es probabilidad:");
                //child.print();
                vecMin.addElement(getCutPoint(i));
                vecMax.addElement(getCutPoint(i+1));
                vecMTEs.addElement(child.getProb());
                //System.out.println("Ya tengo los cutpoints");
                
            }else{
                Vector aux = new Vector();
                Vector vecMinAux = new Vector();
                Vector vecMaxAux = new Vector();
                Vector vecMTEsAux = new Vector();
                aux = child.getMaxMinMTEsVector();
                //Ahora tengo que a�adir a vecMin,vecMax y vecMTEs lo que
                // tiene aux
                vecMinAux = (Vector)aux.elementAt(0);
                vecMaxAux = (Vector)aux.elementAt(1);
                vecMTEsAux = (Vector)aux.elementAt(2);
                for(j=0 ; j<vecMinAux.size() ; j++){
                    vecMin.addElement(vecMinAux.elementAt(j));
                    vecMax.addElement(vecMaxAux.elementAt(j));
                    vecMTEs.addElement(vecMTEsAux.elementAt(j));
                }
                
            }
            
        }
        
        general.addElement(vecMin);
        general.addElement(vecMax);
        general.addElement(vecMTEs);
        return general;
    }
    
    
    /**
     * Simulates a value for the variable in this node.
     * The node must be a variable (discrete or continuous)
     * and all of its children must be probability nodes.
     * The MTE must be normalized
     *
     * @return the simulated value (a <code>double</code>).
     */
    
    public double simulateValue() {
        
        //System.out.println("Este es el arbol que simulamos: ");
        //print();
        
        ContinuousProbabilityTree tree;
        MixtExpDensity density;
        int i, numberOfChildren;
        double[] table;
        double acum = 0.0, min, max, simulatedValue = -1E20, u;
        boolean done = false;
        
        
        if ((isEmpty()) || (isProbab())) {
            System.out.println("simulateValue: The variable cannot be simulated. Es vacio o probabilidad");
            print();
            System.exit(1);
        }
        
        // numberOfChildren = child.size();
//         table = new double[numberOfChildren];
        
//         // First, a function (branch) is selected with probability
//         // proportional to the integral of the function in the interval
//         // where it is defined. In this loop, the probabiity of
//         // each branch is calculated.
        
//         for (i=0 ; i<numberOfChildren ; i++) {
//             tree = getChild(i);
        
//             //System.out.println("tree is prob: "+tree.isProbab());
//             if (!tree.isProbab()) {
//                 System.out.println("ERROR: The variable cannot be simulated. EL arbol no es probabilidad");
//                 System.exit(1);
//             }
        
//             density = tree.getProb();
        
//             if (isContinuous()) {
//                 min = getCutPoint(i);
//                 max = getCutPoint(i+1);
//                 density = density.integral((Continuous)getVar(),min,max,1);
//             }
        
//             table[i] = density.getIndependent();
//         }
        
        
        
//Lo de antes que aparece comentado ya no lo vamos a utilizar.
// Vamos a tener tres vectores, uno llamado vecMin, otro llamado vecMax,
// y otro llamado vecMTEs
// dada una MTE de vecMTEs, vamos a tener en los otros vectores su rango.
// Una vez que tenemos eso nos construiremos el table, con los valroes de probabilidad.
// Para ello utilizamos esta funci�n, que devuelve un vector con tres posiciones, en la
// primera el vecMin, en la segunda el vecMax, y en la tercera el vecMTEs.
        Vector vecMTEs = new Vector();
        Vector vecMin = new Vector();
        Vector vecMax = new Vector();
        if(isContinuous()){
            //System.out.println("La variable es continua");
            Vector general = new Vector();
            //System.out.println("Finalizado el getMaxMin");
            general = getMaxMinMTEsVector();
            //System.out.println("Finalizado el getMaxMin");
            vecMin = (Vector)general.elementAt(0);
            vecMax = (Vector)general.elementAt(1);
            vecMTEs = (Vector)general.elementAt(2);
            //System.out.println("Estos son los vectores de Max, min y mte:");
            // for(int h=0 ; h<vecMin.size(); h++){
// 	    double mmin = ((Double)vecMin.elementAt(h)).doubleValue();
// 	    double mmax = ((Double)vecMax.elementAt(h)).doubleValue();
// 	    MixtExpDensity mmte = (MixtExpDensity)vecMTEs.elementAt(h);
// 	    System.out.println("Intervalo ["+mmin+" , "+mmax+"]");
// 	    System.out.println("MTE: ");
// 	    mmte.print();
// 	}
//	System.out.println("****");
            // AHORA CON ESTOS VECTORES CONSTRUYO LA TABLA DE PROBABILIDADES
            
            table = new double[vecMTEs.size()];
            for (i=0 ; i<vecMTEs.size() ; i++) {
                density = (MixtExpDensity)vecMTEs.elementAt(i);
                min = ((Double)vecMin.elementAt(i)).doubleValue();
                max = ((Double)vecMax.elementAt(i)).doubleValue();
                if (isContinuous()) {
                    
                    density = density.integral((Continuous)getVar(),min,max,1);
                }
                
                table[i] = density.getIndependent();
            }}else{//No es continuo
            numberOfChildren = child.size();
            table = new double[numberOfChildren];
            for (i=0 ; i<numberOfChildren ; i++) {
                tree = getChild(i);
                if (!tree.isProbab()) {
                    System.out.println("ERROR: The variable cannot be simulated.");
                    System.exit(1);
                }
                
                density = tree.getProb();
                table[i] = density.getIndependent();
            }
            }
        
        u = Math.random();
        i = 0;
        
        while ((!done) && (i<table.length)) {
            if (i == (table.length-1)) // Due to precision errors
                acum = 1.0;
            else
                acum += table[i];
            
            if (u <= acum) {
                if (isContinuous()) {
                    // Obtain a density proportional to the function stored in
                    // this branch
                    
                    density = ((MixtExpDensity)vecMTEs.elementAt(i)).multiplyDensities(1/table[i]);
                    
                    Continuous v = (Continuous)getVar();
                    //min = getCutPoint(i);
                    //max = getCutPoint(i+1);
                    min = ((Double)vecMin.elementAt(i)).doubleValue();
                    max = ((Double)vecMax.elementAt(i)).doubleValue();
                    simulatedValue = density.simulateGen(v,min,max);
                } else
                    simulatedValue = i;
                done = true;
            } else
                i++;
        }
        
        return simulatedValue;
    }
    
    
    /**
     * Computes the mode by Monte Carlo.
     *
     * @param iterations the number of points to generate.
     * @return the maximum found.
     */
    
    public double plainMonteCarloMode(int iterations) {
        
        int i;
        double max, min,  point, pointY, mode, modeY;
        MixtExpDensity density;
        Continuous v;
        ContinuousConfiguration conf;
        Vector a, b;
        
        v = (Continuous)var;
        
        max = v.getMax();
        min = v.getMin();
        
        mode = min;
        
        a = new Vector();
        b = new Vector();
        a.addElement(v);
        b.addElement(new Double(mode));
        conf = new ContinuousConfiguration(a,b);
        density = this.getProb(conf);
        modeY = density.getValue(v, mode);
        
        for (i=0 ; i<iterations ; i++) {
            point = min + (max-min)*Math.random();
            
            b = new Vector();
            b.addElement(new Double(point));
            conf = new ContinuousConfiguration(a,b);
            density = this.getProb(conf);
            pointY = density.getValue(v, point);
            if (pointY > modeY) {
                modeY = pointY;
                mode = point;
            }
        }
        
        
        return (mode);
    }
    
    
    
    /**
     * Computes the mode by Monte Carlo, but following the gradient direction.
     *
     * @param iterations the number of initial points.
     * @return the maximum found.
     */
    
    public double gradientMonteCarloMode(int iterations) {
        
        int i;
        double max, min,  point, pointY, mode, modeY, inc, direction;
        MixtExpDensity density;
        Continuous v;
        ContinuousConfiguration conf;
        Vector a, b;
        
        v = (Continuous)var;
        
        max = v.getMax();
        min = v.getMin();
        
        inc = (max-min)/1000;
        
        mode = min;
        modeY = 0.0;
        
        a = new Vector();
        a.addElement(v);
        
        for (i=0 ; i<iterations ; i++) {
            point = min + (max-min)*Math.random();
            
            direction = 1.0;
            
            b = new Vector();
            b.addElement(new Double(point));
            conf = new ContinuousConfiguration(a,b);
            density = this.getProb(conf);
            pointY = density.getValue(v, point);
            if (pointY > modeY) {
                modeY = pointY;
                mode = point;
            }
            
            // Now choose the gradient ascending direction
            
            point = Math.max(point,point+inc);
            b = new Vector();
            b.addElement(new Double(point));
            conf = new ContinuousConfiguration(a,b);
            density = this.getProb(conf);
            pointY = density.getValue(v, point);
            if (pointY > modeY) {
                modeY = pointY;
                mode = point;
                direction = 1.0;
            }
            
            point = Math.min(point,point - 2*inc);
            
            b = new Vector();
            b.addElement(new Double(point));
            conf = new ContinuousConfiguration(a,b);
            density = this.getProb(conf);
            pointY = density.getValue(v, point);
            if (pointY >= modeY) {
                modeY = pointY;
                mode = point;
                direction = -1.0;
            }
            
            boolean found = false;
            
            while (!found) {
                point = point + direction * inc;
                if ((point > max) || (point < min)) {
                    found = true;
                } else {
                    b = new Vector();
                    b.addElement(new Double(point));
                    conf = new ContinuousConfiguration(a,b);
                    density = this.getProb(conf);
                    pointY = density.getValue(v, point);
                    if (pointY > modeY) {
                        modeY = pointY;
                        mode = point;
                    } else {
                        found = true;
                    }
                }
            }
        }
        
        
        return (mode);
    }
    
    
      /**
     * Gives an unbiased estimation of the mutual information between two 
     * ContinuousProbabilityTres
     *
     * @param tree_i a ContinuousProbabilityTree representing the prior 
     * distribution of the variable i.
     * @param tree_j a ContinuousProbabilityTree representing the conditional 
     * distribution of variable j given variable i
     * @param iterations number of iterations of the Monte Carlo simulation.
     * @return an estimate of the mutual information between both variables.
     */    
    public static double estimateMutualInformation(ContinuousProbabilityTree tree_i, ContinuousProbabilityTree tree_j, int iterations) {
        
        Node var_i, var_j;
        double Vi, Vj, estimate, px, pf;
        int i;
        ContinuousConfiguration conf, confAux;
        ContinuousProbabilityTree aux, marginalTree;
        MixtExpDensity density;
        
        var_i = tree_i.getVar();
        //featureVar = featureTree.getVar();
         
        // We compute the marginal for the feature variable.
        
        marginalTree = ContinuousProbabilityTree.combine(tree_i,tree_j,0);
        
        if (var_i instanceof Continuous)
            marginalTree = marginalTree.addVariable((Continuous)var_i);
        else
            marginalTree = marginalTree.addVariable((FiniteStates)var_i);
        
        estimate = 0.0;
               
        for (i=0 ; i<iterations ; i++) {
            // simulate a value for the class variable.
            Vi = tree_i.simulateValue();
            
            conf = new ContinuousConfiguration();
            
            if (var_i instanceof Continuous)
                conf.insert((Continuous)var_i,Vi);
            else
                conf.insert((FiniteStates)var_i,(int)Vi);
            
            // Restrict the conditional tree to the simulated value
            
            aux = tree_j.restrict(conf);
            var_j = aux.getVar();
            
            // Simulate for the feature variable.
            Vj = aux.simulateValue();
            //System.out.println("Valor simulado para la otra "+f);
            
            // Now evaluate the value of the joint distribution for both variables and values.
            confAux = new ContinuousConfiguration();
            
            if (var_j instanceof Continuous)
                confAux.insert((Continuous)var_j,Vj);
            else
                confAux.insert((FiniteStates)var_j,(int)Vj);
                
            density = marginalTree.getProb(confAux);
            
            double vx =density.getValue(confAux);
            
            //double vx = density.getValue((Continuous)featureVar,f);
            
            if (vx > 0) {
                px = Math.log(vx);
            }
            else {
                px = 0.0;
            } 
            //conf.insert((Continuous)featureVar,f);
            conf = new ContinuousConfiguration();
            if (var_j instanceof Continuous)
                conf.insert((Continuous)var_j,Vj);
            else
                conf.insert((FiniteStates)var_j,(int)Vj);
         
            //aux.print();
            
            //density = featureTree.getProb(conf);
            density = aux.getProb(conf);
            
            //System.out.println("Densidad ");
            //density.print();
            
            
            double vf=density.getValue(conf);
            //double vf = density.getValue((Continuous)featureVar,f);
            if (vf > 0) {
                pf = Math.log(vf);
            }
            else {
                pf = 0.0;
            }
            
            estimate += (pf - px);
        }
        
        estimate /= (double)iterations;
        
        return (estimate);
    } 
    
    
   /**
    * Estimates the conditional mutual information between two variables given
    * the class variable
    * 
    * @param p1 = P(C)
    * @param p2 = P(Xj|C)
    * @param p3 = P(Xi|Xj,C)
    * @param p4 = P(Xi|C)
    *
    * @return the conditional mutual information between both variables �(Xi,Xj|C)
    *
    */     
    public static double estimateConditionalMutualInformation(
                                        ContinuousProbabilityTree p1, 
                                        ContinuousProbabilityTree p2,
                                        ContinuousProbabilityTree p3,
                                        ContinuousProbabilityTree p4,
                                        int iterations)
    {    
        Node classVar;
        double C, Xj, Xi, p4value, estimate, px, pf,v3,v4;
        ContinuousConfiguration conf1, conf2, conf3, conf4;
        ContinuousProbabilityTree aux2, aux3,aux4;
        MixtExpDensity d3,d4;
        
        classVar = p1.getVar();      
        estimate = 0.0;
        
        for (int i=0 ; i<iterations ; i++) {
            
            //Simulation of P(C)
            //-------------------------------------------------------------
            C = p1.simulateValue();
                
            //Simulation of P(Xj|C)
            //-------------------------------------------------------------
            conf1 = new ContinuousConfiguration(); 
            
            if (classVar instanceof Continuous)
                  conf1.insert((Continuous)classVar,C);           
            else
                conf1.insert((FiniteStates)classVar,(int)C);           
            
            aux2=p2.restrict(conf1);
            Xj=aux2.simulateValue();
                      
            //Simulation of P(Xi|Xj,c)
            //-------------------------------------------------------------
            conf2=new ContinuousConfiguration();
                   
            if (aux2.getVar() instanceof Continuous)
                conf2.insert((Continuous)aux2.getVar(),Xj);
            else
                conf2.insert((FiniteStates)aux2.getVar(),(int)Xj);
            
            
            if (classVar instanceof Continuous)
                conf2.insert((Continuous)classVar,C);
            else
                conf2.insert((FiniteStates)classVar,(int)C);
           
            aux3=p3.restrict(conf2);
           
            Xi=aux3.simulateValue();  
           
            //Nueva configuracion con 3 valores
            conf3=new ContinuousConfiguration();
            
            if (classVar instanceof Continuous)
                conf3.insert((Continuous)classVar,C);
            else
                conf3.insert((FiniteStates)classVar,(int)C);
            
            if (aux2.getVar() instanceof Continuous)
                conf3.insert((Continuous)aux2.getVar(),Xj);
            else
                conf3.insert((FiniteStates)aux2.getVar(),(int)Xj);
            
            if (aux3.getVar() instanceof Continuous)
                conf3.insert((Continuous)aux3.getVar(),Xi);
            else
                conf3.insert((FiniteStates)aux3.getVar(),(int)Xi);
           
            d3=p3.getProb(conf3);
            v3=d3.getValue(conf3);            
            
            //Nueva configuracion con 3 valores
            conf4=new ContinuousConfiguration();
            if (classVar instanceof Continuous)
                conf4.insert((Continuous)classVar, C);
            else
                conf4.insert((FiniteStates)classVar, (int)C);
            
            if (aux3.getVar() instanceof Continuous)
                conf4.insert((Continuous)aux3.getVar(),Xi);
            else
                conf4.insert((FiniteStates)aux3.getVar(),(int)Xi);
                       
            d4=p4.getProb(conf4);
            v4=d4.getValue(conf4);
            

            if (v3 > 0) {//log could be 0.0
                pf = Math.log(v3);
            }
            else {
                pf = 0.0;
            }           
            //System.out.println("P3(Xi|Xj,C)="+v3+ "  ;  log(P3(Xi|Xj,C))="+pf);

            if (v4 > 0) {
                px = Math.log(v4);            
            }
            else {
                px = 0.0;
            }
            if (pf==0.0)
                px=0.0;
            
            //System.out.println("P4(Xi|C)="+v4+ " ; log(P4(Xi|C))="+pf);
            //------------------------------------------------------------
            double aux=pf-px;
            //System.out.println("log P3(Xi|Xj,C) - log P4(Xi|C) = "+ aux+"\n\n");
            
            estimate =estimate + (pf - px);
        }        
        estimate /= (double)iterations;
        return (estimate);
    }     
    
    
    /**
     * Replaces a variable by a linear function.
     * It requires that the exponents are linear functions.
     * The object is modified.
     * 
     * @param v the variable (Continuous) to replace.
     * @param lf the LinearFunction that will replace variable <code>v</code>.
     * @return a new density, where <code>v</code> is replaced by <code>lf</code>.
     */
    
    public void replaceVariableByLF(Continuous v, LinearFunction lf) {
        
        ContinuousProbabilityTree temp;
        
        if (this.isProbab()) {
            this.value = this.value.replaceVariableByLF(v,lf);
        }
        else {
            for (int i=0 ; i<this.child.size() ; i++) {
                this.getChild(i).replaceVariableByLF(v,lf);
            }
        }
    }
    
    
    /**
     * Multiplies all the leaves by a given constant.
     * The object is modified.
     * @param c the constant (a double).
     */
    
    public void multiplyByConstant(double c) {
        
        ContinuousProbabilityTree temp;
        
        if (this.isProbab()) {
            this.value = this.value.multiplyDensities(c);
        } else {
            for (int i=0 ; i<this.child.size() ; i++) {
                this.getChild(i).multiplyByConstant(c);
            }
        }
        
    }
    
    
    /**
     * Inserts a new variable that is a deterministic function of the others.
     * The object is modified.
     *
     * @param variable the continuous variable to insert as a leaf.
     * @param f the function.
     * @param conf the interval configuration that leads to the current node.
     * @return a continuous tree with the result of the insertion.
     */
    
    public  void insertDeterministicVariable(Continuous variable, LinearFunction f,
            ContinuousIntervalConfiguration conf) {
        
        ContinuousProbabilityTree aux;
        int i, s;
        double x, y;
        
        aux = new ContinuousProbabilityTree(0.0);
        
        
        if (isProbab()) {
            // We assume that the linear function is increasing.
            // otherwise, the interval should be upside down.
            ContinuousConfiguration c1 = new ContinuousConfiguration(), c2 = new ContinuousConfiguration();
            double v1=0.0, v2=0.0;
            for (i=0 ; i<conf.size() ; i++) {
                c1.insert((Continuous)conf.getVariable(i),conf.getLowerValue(i));
                c2.insert((Continuous)conf.getVariable(i),conf.getUpperValue(i));
            }
            v1 = f.getValue(c1);
            v2 = f.getValue(c2);
            
            this.cutPoints = new Vector();
            this.cutPoints.addElement(new Double(v1));
            this.cutPoints.addElement(new Double(v2));
            
            this.child = new Vector();
            this.var = variable;
            this.label = CONTINUOUS_NODE;
            
            child.addElement(new ContinuousProbabilityTree(this.value));
        } else {
            // We assume that all the variables in the tree are continuous
            s = child.size();
            x = ((Double) cutPoints.elementAt(0)).doubleValue();
            for (i=0 ; i<s ; i++) {
                y = ((Double) cutPoints.elementAt(i+1)).doubleValue();
                ContinuousIntervalConfiguration confAux = conf.duplicate();
                confAux.putValue((Continuous)this.getVar(),x,y);
                x = y;
                this.getChild(i).insertDeterministicVariable(variable,f,confAux);
            }
            
        }
           
    }
    
    

} // End of class
