package elvira.learning;

import java.util.*;
import java.io.*;
import elvira.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.MixtExpDensity;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.Potential;


/**
 * MTELearning.java
 *
 *
 * Created: Wed Jan 28  2003
 *
 * @author P. Elvira
 * @since 12/07/07
 * @version 1.6
 */

public class  MTELearning extends Learning {
    
    public NodeList variables;
    public DataBaseCases cases;
    
    
    /**
     * Method to carry experiments.
     * This method needs a dbc file whose variable y is the continuous one that we want to obtain its
     * conditional density, and the rest of variables in the dbc will be used as parents.
     *
     */
    
    public static void main(String args[]) throws ParseException,IOException {
        
        if(args.length < 3){
            
            System.out.println("too few arguments: Usage: file.dbc intervals numpoints ");
            System.exit(0);
        }
        
        int numpoints,intervals;
        //double length;
        
        FileInputStream f = new FileInputStream(args[0]);
        Node yElim;
        Node xNode;
        double dis,dis2;
        //System.out.println("Me voy a crear el DATABASECASES");
        DataBaseCases cases = new DataBaseCases(f);
        //System.out.println("Ya tengo el databasecases");
        ContinuousCaseListMem sample;
        System.out.println("numero de vbles en el DBC: "+cases.getVariables().size());
        MTELearning ok = new MTELearning(cases);
        //Continuous y = new Continuous();//Esta esta asi para poder ahcer pruebas antes de obtener estructura
        NodeList X = new NodeList();//Igual que y
        NodeList newX = new NodeList();
        ContinuousProbabilityTree T;
        T = new ContinuousProbabilityTree();
        //FiniteStates yDisc;
        
        intervals = Integer.valueOf(args[1]).intValue();
        //length = (Double.valueOf(args[2])).doubleValue();
        numpoints = Integer.valueOf(args[2]).intValue();
        
        X = cases.getVariables();
        
        
        //Estas son las variables
        Node Sex;
        Node Height;
        Node Length;
        Node whole_weight;
        Node Diameter;
        Node Shell_weight;
        Node Shucked_weight;
        Node Viscera_weight;
        Node Rings;
        
        //Ahora voy a poner todas las variables ok;
        Sex = X.getNode("Sex");
        Height = X.getNode("Height");
        Length = X.getNode("Length");
        whole_weight = X.getNode("Whole_weight");
        Diameter = X.getNode("Diameter");
        Shell_weight = X.getNode("Shell_weight");
        Shucked_weight = X.getNode("Shucked_weight");
        Viscera_weight = X.getNode("Viscera_weight");
        Rings = X.getNode("Rings");
        
        //xNode = X.getNode("X");
        //System.out.println("X es: "+X.toString());
        //System.out.println("y es: "+y.toString());
        
        //Ahora pongo sus padres
        NodeList parent = new NodeList();
        parent.insertNode(Height);
        parent.insertNode(Diameter);
        parent.insertNode(whole_weight);
        
        
        //Llamo a learnConditional con todas las variables excepto y como padres.
        T = ok.learnConditional(Shell_weight,parent,cases,intervals,numpoints);
        /*********************
         * Ahora habria que llamar a la funcion que calcule la estructura
         *
         */
                 
        System.out.println("Este es el resultado final");
        T.print();
        
        sample = (ContinuousCaseListMem)((Relation)cases.getRelationList().elementAt(0)).getValues();
        
        dis = ok.ECM(T,sample,Shell_weight);
        
        System.out.println("ECM: "+dis);
        
        dis2 = ok.LogLikelihood(T,sample,Shell_weight);
        
        System.out.println("Verosimilitud: "+dis2);
        
    }//End of main
    
    
    
    // Empty constructor
    public MTELearning(){
        
    }//End of constructor
    
    
    
    //Constructor 2
    public MTELearning(DataBaseCases d){
        
        cases = d;
        
    }//End of constructor
    
    
    //Ha de definir esta funcion al extender a Learning
    //Aqui es donde deberia ir la obtencion de la estructura y para cada densidad condicional
    //llamra a learnConditional
    public void learning(){}
    
    
    /**
     * This function returns a ContinuousProbabilityTree representing
     * f(y|X) a conditional density. <code>y</code> is  continuous.
     * On each branch of the tree must appear
     * only once every variable in <code>X</code>. Once all the variables have appear we stop.
     * If a variable appear it means that we have splitted the tree over that variable.
     * Afterwards this tree (can be quite huge) will (or not) be pruned
     *
     * @param y The continuous variable we want to estimate its conditional density.
     * @param X The set of parent variables left to split over.
     * @param cases The set of cases.
     * @param intervals The number of intervals we will split the domains in.
     * @param numpoints The number of points used in the Derivative method (estimation)
     *
     * @return the ContinuousProbabilityTree representing the conditional density
     */
    
    public ContinuousProbabilityTree learnConditional(Node y, NodeList X, DataBaseCases cases,int intervals, int numpoints) {
        
        int i,j;
        ContinuousProbabilityTree res = new ContinuousProbabilityTree();
        
        Vector vars = new Vector(); //Vector donde guardo las variables del caselistmem
        ContinuousCaseListMem sample; //Lista de casos totales
        boolean nodeLeft = true; //Indica si aun queda algun nodo por partir
        boolean parentLeft = true; //Indica si en el camino hasta ese nodo se han partido ya todas las
        // variables X o aun queda alguna
        int indexY = 0; //El indice que ocupa la variable objetivo en el NodeList del total de
        //variables
        double yValue,xValue;
        NodeList newX = new NodeList();
        Vector yValues = new Vector();//Vector con los valores de la variable objetivo
        
        Vector yValues2 = new Vector();
        //En principio tendria que inicializarlo con un nodelist que solo fuesen y +X
        newX = X.copy();
        newX.insertNode(y);
        sample = new ContinuousCaseListMem(newX);
        //System.out.println("numero de variables en el DataBaseCase "+cases.getVariables().size());
        //Esto deberia mostrar 2 variables, y solo muestra una
        //System.out.println(cases.getVariables().toString());
        
        //Esto deberia mostrar solo la variable X (variable 0)
        //System.out.println(X.toString());
        //Con esto creo que obtengo el continuousCaselistmem
        sample = (ContinuousCaseListMem)((Relation)cases.getRelationList().elementAt(0)).getValues();
        
        //Estas serian las variables que ha cogido el caselistmem
        vars = sample.getVariables();
        //System.out.println("Tama�o de vars: "+vars.size());
        for (i=0 ; i<vars.size() ; i++){
            //System.out.println("Titulo de y: "+y.getTitle());
            //System.out.println("Titutlo de la de X"+i+": "+((Node)vars.elementAt(i)).getTitle());
            if(((Node)vars.elementAt(i)).equals(y)){
                indexY = i;
                //System.out.println("El indexY es :"+indexY);
            }
            
        }
        
        //System.out.println("*************");
        res = new ContinuousProbabilityTree();
        
        //System.out.println("This is the ContinuousCaseListMem:");
        //sample.print();
        //y.print();
        //System.out.println("Ahora voy a hacer la primera estimacion, sin partir nada de nada");
        if(y.getTypeOfVariable() == 0){//The variable to learn is continuous
            //System.out.println("La variable a aprender es continua");
            ((Continuous)y).setMax(((Continuous)vars.elementAt(indexY)).getMax());
            //System.out.println("Max de la y en el nodelist: "+((Continuous)vars.elementAt(indexY)).getMax());
            ((Continuous)y).setMin(((Continuous)vars.elementAt(indexY)).getMin());
            //y.print();
            //Ahora voy a meter en el vector yValues los valores de la variable objetivo.
            //System.out.println("Todos los valores de la variable objetivo");
            for(i=0 ; i<sample.getNumberOfCases() ; i++){
                yValue = sample.getValue(i,indexY);
                //xValue = sample.getValue(i,0);
                yValues.addElement(new Double(yValue));
                //System.out.println(yValue);
            }//End of for
            //System.out.println("\nLlamo al learnUnivariate");
            res = res.learnUnivariate((Continuous)y,yValues, intervals, numpoints);
            //System.out.println("After the first learnUnivariate\n");
            //System.out.println("Variable: "+ y.getName());
            Splitting(res,X,(Continuous)y,sample,intervals,numpoints);
            
        }// End of if to see if the variable is continuous
        
        else{//The variable to learn is discrete (FiniteStates)
            // I use sample instead of a vector becouse CaseListMeme has a method that returns the absolute counts
            // of a given configuration.
            //System.out.println("La variable a aprender es discreta");
            res = res.learnUnivariate((FiniteStates)y,sample);
            Splitting(res,X,(FiniteStates)y,sample,intervals);
        }
        
        return res;
        
    }//End of function learnConditional
    
    
    /**
     * This method is used in order to decide whether to split or not a node to get a conditional
     * MTE density. It is a recursive method. <code>y</code> is the continuous variable we want to
     * get the MTE condtional density for, and <code>X</code> is the set of parents. Each time we split over
     * one of the variables in <code>X</code>, we remove it from <code>X</code>, and we will split the new children
     * we have just created. The recursive method stops when <code>X</code> is empty.
     * The node t that is going to be splitted has assigned on it the continuous variable <code>y</code>
     * and some children attached to it. We will split over a variable Xi belonging to <code>X</code> such that
     * has the biggest error rate according to the last estimation (the one in t). We will split the domain of Xi in
     * intervals of equal length if it is continuous, and it will have as many children as states it has if
     * it is discrte. . If the new density we estimate on any of the new nodes is worst than the one we had
     * in the original node <code>t</code> we put the older one, if not, the new one.
     *
     * AUN FALTA PONERLE UN PAR MAS DE RESTRICCIONES: QUE PARE CUANDO NO HAY 20 PUNTOS PARA HACER LA
     * OBTENCION DE LA MIXTEXPDENSITY, Y QUE PARE CUANDO HAYA MAS DE UN NUMERO DETERMINADO DE HOJAS
     *
     * @param t The node that is gonig to be splitted
     * @param X The set of parent variables left to split over.
     * @param y The continuous variable we want to estimate its conditional density.
     * @param sample The set of cases.
     * @param intervals The number of intervals we will split the domains in.
     * @param numpoints The number of points used in the Derivative method (estimation)
     *
     */
    public void Splitting(ContinuousProbabilityTree t,NodeList X,Continuous y,ContinuousCaseListMem sample,int intervals,int numpoints) {
        
        int k = 0;
        int i= 0;
        int j = 0;
        int indexXi = 0;
        int indexy = 0;
        int indexDi = 0;
        int n = 0;
        int u = 0;
        int h = 0;
        int maxH = 0;
        int DiValue;
        boolean fuera,enough;
        Continuous Xi;
        FiniteStates Di;
        ContinuousProbabilityTree ti;
        Vector cutPoints = new Vector();//Aqui voy a guardar los cutpoints de cada variable
        double min,max,Xivalue,xValue,entropyXi,estimation1,error,XiValue,total,entropyDi;
        double yValue = 0;
        double ei = 0;
        Vector errors = new Vector(); //Aqui voy a guardar los errores que cometo en cada intervalo en cada vble
        Vector errorCollection; //Aqui guardo en cada posicion el vector error. Es decir, la pos 0 del vector
        //errorCollection tiene el vector error correspondiente a la vble i del NodeList X
        Vector entropy = new Vector(); //Aqui voy a guardar las entropias. Posicion 0 se corresponde con
        // variable 0 de X
        Vector yValues = new Vector(); //Vector de reales con los valores de la variable objetivo en cada intervalo
        Vector newcp = new Vector();//VEctor para los cutpoints del densityCopy
        Vector vars;//Vector de Nodes (los que hay en el ContinuousCaseListMem)
        Vector empiric = new Vector(); //Vector de dos posiciones, en la primera un vector con los valores x de la densidad empirica
        // y en la segunda un vector con los valores y de la densidad empirica
        Vector xDensity = new Vector(); // vector con los valores de la x del kerneldensity
        Vector yDensity = new Vector(); //vector con los valores de la y del kerneldensity
        MixtExpDensity MTEh; //MTE que hay en el hijo numero h del nodo t
        ContinuousProbabilityTree densityCopy; //MTE definido por partes, el 'original' de t, lo guardo por si
        // el nuevo que estime es peor.
        ContinuousProbabilityTree childi;
        Vector caseListVector = new Vector();//Vector donde voy a guardar para cada hijo del nodo el ContinuousCaseListMem con el
        // que voy a llamar de nuevo al Splitting
        ContinuousCaseListMem caseNew;
        ContinuousConfiguration config;
        NodeList newX = new NodeList();
        
        //Primero tengo que comprobar si el NodeList X es vacio o no.
        // si es vacio paro
        
        //System.out.println("\nEmpiezo el Splitting");
        
        if ((X.getNodes()).size()==0){
            //System.out.println("No sigo partiendo, ya que no me quedan variables por las que partir");
        }
        else {  //Tengo que partir este nodo
            //primero tengo que ver la variable por la que voy a partir
            //System.out.println("Esta es la vble para la que esta definida el arbol");
            //(t.getVar()).print();
            //System.out.println("Los cutPoints del arbol es: ");
            
            //for(k=0 ; k<t.getNumberOfChildren() ; k++){
            //  System.out.println("k es: "+k+",y el cutPoint es: "+t.getCutPoint(k));
            //  System.out.println("k es: "+k+",y el cutPoint es: "+t.getCutPoint(k+1));
            //}
            
            //System.out.println("Voy a seleccionar la variable por la que partir.");
            newX = X.copy();
            
            //System.out.println("*********************************");
            //System.out.println("*********************************");
            //System.out.println("Hay "+(X.getNodes()).size()+" posibles variables por las que partir");
            errorCollection = new Vector();
            
            vars = new Vector();
            vars = sample.getVariables();
            
            NodeList vars2 = new NodeList();
            
            //Voy a pasar del vector de nodos vars al nodelist vars2
            //para poder buscar el indice de la vble objetivo.
            for(j=0 ; j<vars.size() ; j++){
                //System.out.println("Voy a insertar en el vars2 el nodo:");
                //((Node)vars.elementAt(j)).print();
                vars2.insertNode((Node)vars.elementAt(j));
            }
            
            indexy = vars2.getId(y);
            // System.out.println("Este es el index de y "+indexy);
            
            //Para cada variable calculo su H
            //System.out.println("Now I have to get the H for every possible variable to split over");
            for (i=0 ; i<(X.getNodes()).size() ; i++){//Para cada vable calculo su H
                
               // System.out.println("NOMBRE NODO:" + X.getNodes().elementAt(i).getName());
                //First I have to check whether the variable is discrete or continuous
                if(((Node)(X.getNodes()).elementAt(i)).getTypeOfVariable() == 0){//The variable is continuous
                   // System.out.println("The variable is continuous");
                    Xi = new Continuous();
                    Xi = (Continuous)X.elementAt(i);
                    //System.out.println("Pruebo con:");
                    //Xi.print();
                                        
                    cutPoints = new Vector();
                    min = Xi.getMin();
                    //System.out.println("Este es el minimo de Xi "+min);
                    //System.out.println("Este es el minimo de Y "+y.getMin());
                    max = Xi.getMax();
                    //System.out.println("Este es el maximo de Xi "+max);
                    //System.out.println("Este es el maximo de y "+y.getMax());
                    
                    //Estos son los puntos de corte
                    //System.out.println("Los cutPoints son:");
                    
                    //for (j=0 ; j<=intervals ; j++){
                    // cutPoints.addElement(new Double(min+(j*(max-min)/intervals)));
                    // System.out.println("El "+j+" es: "+(min+(j*(max-min)/intervals)));
                                                            
                    //Aqui escogemos como hacer la particion, por igual frecuencia o por igual longitud.
                    //cutPoints = getCpEqualWidth(max,min,intervals);
                    if(sample.getNumberOfCases() > 29)
                        cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                    else{
                        cutPoints.addElement(new Double(Xi.getMin()));
                        cutPoints.addElement(new Double(Xi.getMax()));
                    }
                    
                    //System.out.println("Los cutpoints son:");
                    //for(k=0 ; k<(cutPoints.size()) ; k++)
                    //   System.out.println("cutpoint "+k+" : "+((Double)cutPoints.elementAt(k)).doubleValue());
                    
                    ti = new ContinuousProbabilityTree(Xi,cutPoints);
                    
                    //Quiero saber cual es el indice de la variable Xi en el ContinuousCaseListMem
                    indexXi = vars2.getId(Xi);
                    //System.out.println("El tama?o del cutPoint es: "+cutPoints.size());
                    for(h=0 ; h<(cutPoints.size()-1) ; h++){
                        //System.out.println("h vale: "+h);
                        //Now I have to collect the values of the dbc that would go to this node
                        //in order to calculate its error.
                        //System.out.println("El inferior es: "+ti.getCutPoint(h));
                        //System.out.println("El superior es: "+ti.getCutPoint(h+1));
                        for(j=0 ; j<sample.getNumberOfCases() ; j++){
                            xValue = sample.getValue(j,indexXi);
                            //System.out.println(xValue);
                            if((xValue >= ti.getCutPoint(h)) & (xValue <= ti.getCutPoint(h+1))){
                                yValue = sample.getValue(j,indexy);
                                yValues.addElement(new Double(yValue));
                            }//End of if
                            
                        }//End of for
                        if(yValues.size() == 0 ){// No hay valores en este intervalo, luego el error es cero
                            ei = 0;
                            errors.addElement(new Double(ei));
                        }else{
                            // tengo que obtener el empiricDensity de los datos que caen
                            // en el nodo este y trabajar sobre el.
                            //System.out.println("Ahora para cada una de las particiones del dominio me ");
                            //System.out.println("me construyo la densidad empirica:");
                            //System.out.println("En la parte "+h+" caen "+yValues.size()+" elementos del dbc");
                             
                            KernelDensity density;
                            if(((Double)yValues.elementAt(0)).doubleValue() == ((Double)yValues.elementAt(yValues.size()-1)).doubleValue()){
                                //System.out.println("All the values are the same. Return one value with one as density");
                                xDensity = new Vector();
                                yDensity = new Vector();
                                xDensity.addElement(yValues.elementAt(0));
                                yDensity.addElement(new Double(1));
                            }else{
                                //instead of considering the sample to estimate the kernel density
                                //I'm going to not consider the first either the last, to avoid problems with
                                //boundary bias
                                //I consider an auxiliary vector, in which I store all the values except first and last
                                //for (i=0 ; i<values.size() ; i++)
                                //    System.out.println("Values "+((Double)values.elementAt(i)).doubleValue());//auxValues = (Vector) yValues.clone();
                                xDensity = new Vector();
                                yDensity = new Vector();
                                for (int m = 0 ; m < yValues.size() ; m++ ){
                                    xDensity.add(yValues.elementAt(m));
                                }
                                t.sort(xDensity);
                             
                                //Como minimo xDensity tiene que tener 4 puntos para 
                                //poder eliminar los extremos, porque de lo contrario
                                //xDensity quedaria vacio o con solo 1 punto.
                                if (xDensity.size()>3){
                                    xDensity.removeElementAt(yValues.size()-1);
                                    xDensity.removeElementAt(0);
                                }
                                //if there are only two values, "for" will not be used
                                density = new KernelDensity(xDensity, 0);
                                empiric = new Vector();
                                empiric = density.getValues(xDensity);
                                xDensity = new Vector();
                                xDensity = (Vector) empiric.elementAt(0);
                                yDensity = (Vector) empiric.elementAt(1);
                                
                            }
                            
                            for (j=0 ; j<yDensity.size() ; j++)
                                if (((Double)yDensity.elementAt(j)).doubleValue() <= 0)
                                    System.out.println("Valor de densidad negativo : "+ ((Double)xDensity.elementAt(j)).doubleValue()+"\t"+(((Double)yDensity.elementAt(j)).doubleValue()));
                            
                            u = 0;
                            ei = 0;//Contador sobre el error
                            
                            //Esta n me indica la cantidad de valores que caen en el intervalo h
                            n = 0;
                            for(j=0 ; j<xDensity.size() ; j++){
                                
                                xValue = ((Double)xDensity.elementAt(j)).doubleValue();
                                yValue = ((Double)yDensity.elementAt(j)).doubleValue();
                                //System.out.println("Valor de x: "+xValue);
                                //System.out.println("Valor de y: "+yValue);
                                fuera = true;
                                u = 0;//Me indica en que hijo de childi cae xValue
                                while(fuera){
                                    //System.out.println("La vble para la que esta definida t es:");
                                    //(t.getVar()).print();
                                    //System.out.println("limit inf: "+t.getCutPoint(u));
                                    //System.out.println("limit sup: "+t.getCutPoint(u+1));
                                    if((xValue >= t.getCutPoint(u)) & (xValue <= t.getCutPoint(u+1))){
                                        fuera = false;
                                        //System.out.println("Esta dentro");
                                        estimation1 = ((t.getChild(u)).getProb()).getValue((Continuous)y,xValue);
                                        ei = ei +(yValue-estimation1)*(yValue-estimation1);
                                        n++;
                                    }//End of if
                                    else{ u++;
                                    //System.out.println("Sigue fuera");
                                    //System.out.println("u vale ya "+u+" y su maximo valor es de "+intervals);
                                    }
                                }//End of while
                            }//End of for(j=0 ; j<xKernel.size() ; j++){
                            
                            
                            //A�ado el error que cometo al vector de errores
                            //System.out.println("Error que cometo en hijo1: "+ei/n);
                            errors.addElement(new Double(ei/n));
                        }// End of else (no hay valoes)
                        //System.out.println("Total hay "+xKernel.size()+" valores del xKernel");
                        //System.out.println("En el intervalo "+h+" caen "+n+" puntos.");
                        //System.out.println("Error de la MTE original en el intervalo "+h+" de la vble Xi: "+ei/n);
                                            
                        // I create new Vectors so that they can be used for the next interval
                        yValues = new Vector();
                        empiric = new Vector();
                        yDensity = new Vector();
                        xDensity = new Vector();
                        
                    }//End of for, de cada intervalo
                    
                    //Ahora me queda calcular la entropia que tengo si escogiese esta variable
                    //segun la formula H(Xi)=suma(ei*log(ei)), pero con los errores normalizados,
                    //luego primero he de normalizarlos.
                    entropyXi = 0;
                    total = 0;
                    
                    //for(j=0 ; j<intervals ; j++){
                    for(j=0 ; j<errors.size() ; j++){
                        //System.out.println("Busco el error "+j);
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        total = total + ei;
                    }//End of for
                    
                    //Now I calculate the entropy.
                    //for(j=0 ; j<intervals ; j++){
                    for(j=0 ; j<errors.size() ; j++){
                        
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        ei = ei/total;
                        //System.out.println("ei normalizado vale: " +ei);
                        entropyXi = entropyXi + ((ei) * Math.log(ei));
                    }
                    //Ahora pongo la entropia en el vector de entropias
                    //System.out.println("La EntropyXi vale: "+entropyXi);
                    entropy.addElement(new Double(entropyXi));
                    
                    //Y ahora pongo el vector con los errores en cada intervalo en el
                    //Vector errorCollection
                    errorCollection.addElement(errors);
                    errors = new Vector();
                }//End of if(continuous)
                
                // THE VARIABLE IS A FINITESTATES
                
                else{//In this case the variable is a FiniteStates
                    //System.out.println("The variable is finiteStates");
                    Di = new FiniteStates();
                    Di = (FiniteStates)X.elementAt(i);
                    //System.out.println("Pruebo con la vble :");
                    //Di.print();
                    ti = new ContinuousProbabilityTree(Di);
                    
                    //System.out.println("Este es el minimo de Y "+y.getMin());
                    //System.out.println("Este es el maximo de y "+y.getMax());
                    //System.out.println("Esta es y:");
                    //y.print();
                    
                    //Quiero saber cual es el indice de la variable Di en el ContinuousCaseListMem
                    indexDi = vars2.getId(Di);
                    
                    //Now for each child (node) we will get the error ei
//-------------------------------------------------------------------------------------------------
                    for(h=0 ; h<Di.getNumStates() ; h++){
                        
                        
                        //The first child corresponds with value 0 of the variable, the second with value 1, etc..
                        
                        //Ya tengo en yValues los valores de la vble objetivo para el intervalo h.
                        //Lo que voy a hacer ahora es calcular el error que comete t en este intervalo
                        
                        //Now I have to collect the values of the dbc that would go to this node
                        //in order to calculate its error.
                        for(j=0 ; j<sample.getNumberOfCases() ; j++){
                            DiValue = (int)sample.getValue(j,indexDi);
                            
                            if(DiValue == h){
                                yValue = sample.getValue(j,indexy);
                                yValues.addElement(new Double(yValue));
                                
                            }//End of if
                            
                        }//End of for
                        
                        // tengo que obtener el empiricDensity de los datos que caen
                        // en el nodo este y trabajar sobre el.
                        
                      //  System.out.println("tama?o de yValues: "+yValues.size());
                        if(yValues.size() == 0){// No hay valores en este intervalo, luego el error es cero
                            ei = 0;
                            errors.addElement(new Double(ei));
                        }else{
                            
                            KernelDensity density;
                            if(((Double)yValues.elementAt(0)).doubleValue() == ((Double)yValues.elementAt(yValues.size()-1)).doubleValue()){
                                System.out.println("Son todos iguales, ha de devolver s?lo un valor con 1 en el y");
                                xDensity = new Vector();
                                yDensity = new Vector();
                                xDensity.addElement(yValues.elementAt(0));
                                yDensity.addElement(new Double(1));
                            }else{
                                //instead of considering the sample to estimate the kernel density
                                //I'm going to not consider the first either the last, to avoid problems with
                                //boundary bias
                                xDensity = new Vector();
                                yDensity = new Vector();
                                for (int m = 0 ; m < yValues.size() ; m++ ){
                                    xDensity.add(yValues.elementAt(m));
                                }
                                t.sort(xDensity);
                                
                               //Como minimo xDensity tiene que tener 4 puntos para 
                               //poder eliminar los extremos, porque de lo contrario
                               //xDensity quedaria vacio o con solo 1 punto.
                                if (xDensity.size()>3){
                                    xDensity.removeElementAt(yValues.size()-1);
                                    xDensity.removeElementAt(0);
                                }
                                
                                //if there are only two values, "for" will not be used
                                density = new KernelDensity(xDensity, 0);
                                empiric = new Vector();
                                empiric = density.getValues(xDensity);
                                xDensity = new Vector();
                                xDensity = (Vector) empiric.elementAt(0);
                                yDensity = (Vector) empiric.elementAt(1);
                                
                             }
                            
                            for (j=0 ; j<yDensity.size() ; j++)
                                if (((Double)yDensity.elementAt(j)).doubleValue() <= 0)
                                    System.out.println("Valor de densidad negativo : "+ ((Double)xDensity.elementAt(j)).doubleValue()+"\t"+(((Double)yDensity.elementAt(j)).doubleValue()));
                           
                            n=0;
                            ei = 0; //Contador sobre el error
                            for(j=0 ; j<xDensity.size() ; j++){
                                //System.out.println("j es: "+j);
                                xValue = ((Double)xDensity.elementAt(j)).doubleValue();
                                yValue = ((Double)yDensity.elementAt(j)).doubleValue();
                                fuera = true;
                                u = 0;//Me indica en que hijo de childi cae xValue
                                while(fuera){
                                    //System.out.println("Punto: "+xValue);
                                    //System.out.println("Extremos del intervalo: "+t.getCutPoint(u)+" -- "+t.getCutPoint(u+1));
                                    if((xValue >= t.getCutPoint(u)) & (xValue <= t.getCutPoint(u+1))){
                                        //System.out.println("Esta dentro");
                                        fuera = false;
                                        estimation1 = ((t.getChild(u)).getProb()).getValue((Continuous)y,xValue);
                                        ei = ei +(yValue-estimation1)*(yValue-estimation1);
                                        n++;
                                    }//End of if
                                    else{ u++;
                                    //System.out.println("Esta fuera");
                                    //System.out.println("El valor de y en "+xValue+" esta fuera del intervalo ["+(childi).getCutPoint(u-1)+","+(childi).getCutPoint(u)+"]");
                                    }
                                }//End of while
                            }//End of for
                            
                            
                            //A�ado el error que cometo al vector de errores
                            errors.addElement(new Double(ei/xDensity.size()));
                        }
                        //System.out.println("En finiteStates n vale: "+n);
                        //System.out.println("Total hay "+xKernel.size()+" valores del xKernel");
                        //System.out.println("En el hijo "+h+" caen "+n+" puntos, de un total de "+cases.getNumberOfCases());
                        //System.out.println("Error de la MTE original en el intervalo "+h+" de la vble Xi: "+ei/n);
                        
                        // I create new Vectors so that they can be used for the next interval
                        yValues = new Vector();
                        empiric = new Vector();
                        yDensity = new Vector();
                        xDensity = new Vector();
                        
                    }//End of for(h=0 ; h<Di.getNumStates() ; h++)
 //-------------------------------------------------------------------------------------------------                   
                    //Ahora me queda calcular la entropia que tengo si escogiese esta variable
                    //segun la formula H(Xi)=suma(ei*log(ei)), pero con los errores normalizados,
                    //luego primero he de normalizarlos.
                    entropyDi = 0;
                    total = 0;
                    
                    for(j=0 ; j<Di.getNumStates() ; j++){
                        //System.out.println("Busco el error "+j);
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        total = total + ei;
                    }//End of for
                    
                    //Now I calculate the entropy.
                    for(j=0 ; j<Di.getNumStates() ; j++){
                        
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        ei = ei/total;
                        //System.out.println("ei normalizado vale: " +ei);
                        entropyDi = entropyDi + ((ei) * Math.log(ei));
                    }
                    //Ahora pongo la entropia en el vector de entropias
                    //System.out.println("La EntropyXi vale: "+entropyDi);
                    entropy.addElement(new Double(entropyDi));
                    
                    //Y ahora pongo el vector con los errores en cada intervalo en el
                    //Vector errorCollection
                    errorCollection.addElement(errors);
                    errors = new Vector();
                    
                }//end of finitestates
                //System.out.println("El i que me indica por donde va mirando las vbles es: "+i);
            }//End of for para cada variable
            
            //System.out.println();
            //System.out.println("Ahora he de seleccionar la variable por la que voy a partir");
            //System.out.println();
            
            // AHORA ES CUANDO PARTO Y CALCULO LOS NODOS HIJOS
            
            //Ahora ya tengo todas las entropias calculadas, solo tengo que quedarme con el maximo, y ver
            //el indice que es, pues se correspondera con el indice de la variable por la que partir.
            //este maxH es el indice de la vble maximo en el NodeList X
            
            //System.out.println("Now I select the maximun entropy. (en splitting 1)");
            maxH = 0;
            for(i=1 ; i<entropy.size() ; i++){
                if(((Double)entropy.elementAt(i)).doubleValue() > ((Double)entropy.elementAt(maxH)).doubleValue())
                    maxH = i;
            }//End of for
            //Asi pues la variable que esta en la posicion maxH es por la que hay que partir
            // la llamo Xi de nuevo.
            
            //Again I have to make a difference if it is discrete or continuous
            if(((Node)X.elementAt(maxH)).getTypeOfVariable() == 0){//It is continuous
                
                Xi = (Continuous)X.elementAt(maxH);
                cutPoints = new Vector();
                min = Xi.getMin();
                //System.out.println("Min de Xi: "+min);
                max = Xi.getMax();
                //System.out.println("Max de Xi: "+max);
                //System.out.println("Parto por la variable: "+Xi.getName()+" (la variable es continua) ");
                //System.out.println("El arbol tiene la vble : ");
                //(t.getVar()).print();
                
                /**
                 * //Primero voy a obtener de nuevo los cutPoints.
                 * for (j=0 ; j<=intervals ; j++)
                 * cutPoints.addElement(new Double(min+(j*(max-min)/intervals)));
                 */
                
                //Aqui escogemos como hacer la particion, por igual frecuencia o por igual longitud.
                //cutPoints = getCpEqualWidth(max,min,intervals);
                //cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                if(sample.getNumberOfCases() > 29)
                    cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                else{
                    cutPoints.addElement(new Double(Xi.getMin()));
                    cutPoints.addElement(new Double(Xi.getMax()));
                }
                //System.out.println("he calculado los cutpoints y hay "+cutPoints.size()+" q son "+cutPoints);
                //Ahora a t deberia ponerle le variabe Xi , y crearle intervals hijos, y en cada hijo poner
                //la variable 'y' y estimarle una densidad, pero sin perder la densidad que ya traia
                // ya que si la que estimo nueva no es mejor dejo la que tenia en ese intervalo.
                //System.out.println("Cambio "+((Double)cutPoints.elementAt(0)).doubleValue()+" por "+min);
                cutPoints.setElementAt(new Double(min),0);
                cutPoints.setElementAt(new Double(max),cutPoints.size()-1);
                densityCopy = new ContinuousProbabilityTree();
                densityCopy = t.copy();
                
                
                //Esta densityCopy deberia tener la variable 'y' y estar partida en intervalos.
                //System.out.println("Ahora al partir por la vble Xi pongo en el arbol esta vble");
                t.assignVar(Xi,cutPoints);
                
                //Ahora pongo un hijo por cada intervalo, y en ese hijo
                // pongo la variable y, le calculo una densidad, y veo si mejora a lo que ya tenia la
                // t original.
                
                for(i=0 ; i<(cutPoints.size()-1) ; i++){
                    //System.out.println("Inserto el hijo: "+i);
                    childi = new ContinuousProbabilityTree();
                    
                    //Necesito los valores de la variable objetivo cuyo Xi esta en el intervalo i actual
                    //Quiero saber cual es el indice de la variable Xi en el ContinuousCaseListMem
                    
                    
                    //Voy a pasar del vector de nodos vars al nodelist vars2
                    vars2 = new NodeList();
                    for(j=0 ; j<vars.size() ; j++){
                        vars2.insertNode((Node)vars.elementAt(j));
                    }
                    
                    indexXi = vars2.getId(Xi);
                    //System.out.println("Este es el index de Xi "+indexXi);
                    
                    //Ahora voy a meter en el vector yValues los valores de la variable objetivo, del intervalo h
                    // j es el caso
                    // indexXi es el indice de la variable Xi en el ContinuousCaseListMem
                    yValues = new Vector();
                    //System.out.println("Indice de X: "+indexXi);
                    //System.out.println("Indice de y: "+indexy);
                    //System.out.println("CutPoint inferior: "+t.getCutPoint(i));
                    //System.out.println("CutPoint superior: "+t.getCutPoint(i+1));
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        XiValue = sample.getValue(j,indexXi);
                        //System.out.println("El valor de X es: "+XiValue);
                        yValue = sample.getValue(j,indexy);
                        if((XiValue >= t.getCutPoint(i)) & (XiValue <= t.getCutPoint(i+1))){
                            yValues.addElement(new Double(yValue));
                            //System.out.print(yValue+"\t");
                            //System.out.println("A�ado "+yValue+" al vector con el que voy a llamar a learnUnivariate para el hijo "+i);
                        }
                    }//End of for
                    //System.out.println();
                    if(yValues.size() == 0){// No hay valores en este intervalo, luego pongo una densidad cero
                       //Asignamos probabilidad de una uniforme
                        Vector cp = new Vector();
                        cp.addElement(new Double(y.getMin()));
                        cp.addElement(new Double(y.getMax()));
                        childi = new ContinuousProbabilityTree(y,cp);
                        ContinuousProbabilityTree grandson = new ContinuousProbabilityTree(1.0/(y.getMax()-y.getMin()));
                        childi.setChild(grandson, 0);
                        error = 0;
                    }else{
                        //Y para calcular la MTE density necesito el empiricDensity de estos valores.
                        empiric = new Vector();
                        //Este empiric no seria con yValues, si no con los que pertenecen a esta particion
                        // Ya que en yValues estan todos los valores de la vble y
                                                
                        KernelDensity density;
                        if(((Double)yValues.elementAt(0)).doubleValue() == ((Double)yValues.elementAt(yValues.size()-1)).doubleValue()){
                            //System.out.println("\nSon todos iguales, ha de devolver solo un valor con 1 en el y");
                            xDensity = new Vector();
                            yDensity = new Vector();
                            xDensity.addElement(yValues.elementAt(0));
                            yDensity.addElement(new Double(1));
                            //System.out.println(((Double)yValues.elementAt(0)).doubleValue());
                        }else{
                            //instead of considering the sample to estimate the kernel density
                            //I'm going to not consider the first either the last, to avoid problems with
                            //boundary bias
                            xDensity = new Vector();
                            yDensity = new Vector();
                            for (int m = 0 ; m < yValues.size() ; m++ ){
                                xDensity.add(yValues.elementAt(m));
                            }
                            t.sort(xDensity);
                            
                            //Como minimo xDensity tiene que tener 4 puntos para 
                            //poder eliminar los extremos, porque de lo contrario
                            //xDensity quedaria vacio o con solo 1 punto.
                            if (xDensity.size()>3){
                                xDensity.removeElementAt(yValues.size()-1);
                                xDensity.removeElementAt(0);
                            }
                            //copy values, sort it and remove min and max
                            density = new KernelDensity(xDensity, 0);
                            empiric = new Vector();
                            empiric = density.getValues(xDensity);
                            xDensity = new Vector();
                            xDensity = (Vector) empiric.elementAt(0);
                            yDensity = (Vector) empiric.elementAt(1);
                            
                        }

                            for (j=0 ; j<yDensity.size() ; j++)
                                if (((Double)yDensity.elementAt(j)).doubleValue() <= 0)
                                    System.out.println("Valor de densidad negativo : "+ ((Double)xDensity.elementAt(j)).doubleValue()+"\t"+(((Double)yDensity.elementAt(j)).doubleValue()));
                           
                          //System.out.println("\nPara el hijo "+i+" van "+yValues.size()+" casos");
                          //System.out.println("Ahora para el hijo "+i+" calculo la MTE density");
                          //System.out.println("Valores del empiric con los que calculo el hijo");
                          //for (j=0 ; j<xDensity.size() ; j++)
                          //    System.out.println(((Double)xDensity.elementAt(j)).doubleValue()+"\t"+((Double)yDensity.elementAt(j)).doubleValue());
                          //System.out.println("Los yValues");
                          //for (j = 0; j < yValues.size(); j++)
                          //    System.out.print(((Double)yValues.elementAt(j)).doubleValue()+"\t");
                          childi = childi.learnUnivariate(y,yValues, intervals, numpoints);
                          //System.out.println("Termino el learnUnivariate\n");
                          //childi.print();
                          //System.out.println("Valores del empiric con los que calculo el hijo");
                          //for (j=0 ; j<xDensity.size() ; j++)
                          //   System.out.println(((Double)xDensity.elementAt(j)).doubleValue()+"\t"+((Double)yDensity.elementAt(j)).doubleValue());
                          
                          //Necesito saber si al hacer el learn pongo los cutpoints directamente o no.
                          // si no me equivoco el childi ahora debe tener intervals hijos, que han partido
                          // el rango de la variable y. Asi primero he de ver el xValue del xDensity a que hijo de
                          // childi corresponde y entonces hacer el getValue
                          //System.out.println("Los cutpoints del childi son :");
                          //for(k=0 ; k < childi.getCutPoints().size() ; k++)
                          //  System.out.println("cutpoint "+k+" : "+childi.getCutPoint(k));

                          u = 0;
                          error = 0;
                          for(j=0 ; j<xDensity.size() ; j++){
                              xValue = ((Double)xDensity.elementAt(j)).doubleValue();
                              yValue = ((Double)yDensity.elementAt(j)).doubleValue();
                              fuera = true;
                              u = 0;//Me indica en que hijo de childi cae xValue
                              while(fuera){
                                  //System.out.println(xValue+" cae en el hijo (u) "+u);
                                  if((xValue >= (childi).getCutPoint(u)) & (xValue <= (childi).getCutPoint(u+1))){
                                      fuera = false;
                                      estimation1 = (((childi).getChild(u)).getProb()).getValue((Continuous)y,xValue);
                                      error = error +(yValue-estimation1)*(yValue-estimation1);
                                  }//End of if
                                  else{ 
                                      u++;
                                      //System.out.println("El valor de y en "+xValue+" esta fuera del intervalo ["+(childi).getCutPoint(u-1)+","+(childi).getCutPoint(u)+"]");
                                  }
                              }//End of while
                          }//End of for
                          error = error/xDensity.size();
                         }// fin del else
                    // He de comparar el error que cometo ahora (error)
                    //con el que cometia con el nodo t con el que se llamo a la funcion.
                    //Para ello he de tener ese error guardado. Para no calcularlo dos veces
                    //deberia haberlo guardado cuando lo calcule antes.
                    
                    //Aqui tengo los errores de la vble Xi en cada intervalo segun la MTE de la t original
                    errors = new Vector();
                    errors = (Vector)errorCollection.elementAt(maxH);
                    //System.out.println("Eror nuevo: "+error);
                    //System.out.println("Antiguo error: "+((Double)errors.elementAt(i)).doubleValue());
                    //i es el intervalo en el que estamos
                    if(error < ((Double)errors.elementAt(i)).doubleValue()){//Es decir, el error que cometo con lo nuevo es mejor
                        t.setChild(childi,i);
                        //System.out.println("Me quedo con lo nuevo que he calculado, ya que mejora el error que tenia");
                    }
                    else{ //El error con lo nuevo es peor, luego me quedo con lo que tenia
                        t.setChild(densityCopy.copy(),i);
                        //System.out.println("No me quedo con lo nuevo que he calculado, ya que empeora el error que tenia");
                        //System.out.println("El que tenia estaba definido sobre la vable :");
                        //(densityCopy.getVar()).print();
                        //System.out.println("Debe ser la vble objetivo");
                    }
                    //AQUI ES DONDE DEBERIA HACER QUE GUARDASE EL CONTINUOUSCASELISTMEM NUEVO
                    //PARA CADA INTERVALO/HIJO SOLO CON AQUELLOS CASOS QUE CUMPLEN LA CONDICION
                    //QUE ACABAMOS DE DEFINIR
                    
                    caseNew = new ContinuousCaseListMem(vars2);
                    //System.out.println("Me acabao de crear el caseNew con vars2, que es:");
                    //vars2.print();
                    config = new ContinuousConfiguration();
                    
                //System.out.println("Voy a crearme el nuevo caselist con el que llamar de nevo a splitting");
                    //System.out.println("repetire lo mismo "+sample.getNumberOfCases()+" veces");
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        //System.out.println("Antes del if");
                        //System.out.println("j: "+j+"     indexXi: "+indexXi);
                        //System.out.println("Valor de la vble Xi en el  caselist: "+sample.getValue(j,indexXi));
                        //config = (ContinuousConfiguration)sample.get(j);
                        //System.out.println("Valor completo de la conf: ");
                        //config.print();
                        //System.out.println("valores del CutPoint: "+t.getCutPoint(i)+"  y  "+t.getCutPoint(i+1));
                        
                        if((sample.getValue(j,indexXi)>=t.getCutPoint(i)) & (sample.getValue(j,indexXi)<=t.getCutPoint(i+1))){
                            //System.out.println("VOy a coger la conf completa");
                            config = (ContinuousConfiguration)sample.get(j);
                            //System.out.println("ya la he cogido");
                            //System.out.println("voy a insertar una nueva conf");
                            caseNew.put(config);
                            //System.out.println("acabo de insertar la nueva conf");
                        }//System.out.println("Acabo de salir del if");
                    }
                    caseListVector.addElement(caseNew);
                    //System.out.println("El nuevo con el que llamo tiene "+caseNew.getNumberOfCases()+" elementos");
                    
                }//End of for (para cada intervalo)
                
                //Ya me he creado todos los hijos, solo me queda para cada hijo volver a llamar a la funcion, de forma que
                //sea recursiva.
                
                //Primero he de quitar el Xi del NodeList
                newX.removeNode(Xi);
                //System.out.println("t tiene ahora "+t.getNumberOfChildren()+" hijos");
                for(j=0 ; j<t.getNumberOfChildren() ; j++){
                    //System.out.println("Casos: "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases());
                    if(((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases() > 2 ){
                        //System.out.println("Ahora llamo a Splitting para el hijo "+j+" con un ContinuousCaseListMem de "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+" elementos");
                        Splitting(t.getChild(j),newX,y,(ContinuousCaseListMem)caseListVector.elementAt(j),intervals,numpoints);
                    }
                    //else System.out.println("No llamo a Splitting para el hijo "+j+" porque caen "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+ " elementos, que son menos de 50");
                }//End of for
                
            }//End of if(continuous)
            
            //   *********** LA VARIABLE ES DISCRETA *************
            //   *********** ESTOY PARTIENDO LA VARIABLE Y CALCULANDO LAS MTE HOJAS ********************
            
            else{ //The variable we have to split over is discrete
                
                Di = new FiniteStates();
                Di = (FiniteStates)X.elementAt(maxH);
                //System.out.println("Parto por : "+Di.getName()+" (la variable es discreta)");
                //Di.print();
                //Ahora a t deberia ponerle le variabe Xi , y crearle intervals hijos, y en cada hijo poner
                //la variable 'y' y estimarle una densidad, pero sin perder la densidad que ya traia
                // ya que si la que estimo nueva no es mejor dejo la que tenia en ese intervalo.
                
                densityCopy = new ContinuousProbabilityTree();
                densityCopy = t.copy();
                
                //System.out.println("Pongo la vble en cuestion como vble del arbol.");
                t.assignVar(Di);
                
                
                //Ahora pongo un hijo por cada intervalo, y en ese hijo
                // pongo la variable y, le calculo una densidad, y veo si mejora a lo que ya tenia la
                // t original.
                for(i=0 ; i<Di.getNumStates() ; i++){//Empieza el for para crear los hijos
                    childi = new ContinuousProbabilityTree();
                    
                    //Necesito los valores de la variable objetivo cuyo Di es el i actual
                    //Quiero saber cual es el indice de la variable Di en el ContinuousCaseListMem
                    vars2 = new NodeList();
                    //Voy a pasar del vector de nodos vars al nodelist vars2
                    for(j=0 ; j<vars.size() ; j++){
                        vars2.insertNode((Node)vars.elementAt(j));
                    }
                    
                    indexDi = vars2.getId(Di);
                    //System.out.println("Este es el index de Di "+indexDi);
                    
                    //Ahora voy a meter en el vector yValues los valores de la variable objetivo, del intervalo h
                    // j es el caso
                    // indexDi es el indice de la variable Xi en el ContinuousCaseListMem
                    yValues = new Vector();
                    //System.out.println("Indice de Di: "+indexDi);
                    //System.out.println("Indice de y: "+indexy);
                    
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        DiValue = (int)sample.getValue(j,indexDi);
                        //System.out.println("El valor de X es: "+XiValue);
                        yValue = sample.getValue(j,indexy);
                        if(DiValue == i){
                            yValues.addElement(new Double(yValue));
                            //System.out.println("A?ado "+yValue+" al vector con el que voy a llamar a learnUnivariate para el hijo "+i);
                        }
                    }//End of for
                    if(yValues.size() == 0){// Si no hay valores devuelvo una densidad igual a cero
                        //Asignamos probabilidad de una uniforme (1/max-min)
                        Vector cp = new Vector();
                        cp.addElement(new Double(y.getMin()));
                        cp.addElement(new Double(y.getMax()));
                        childi = new ContinuousProbabilityTree(y,cp);
                        ContinuousProbabilityTree grandson = new ContinuousProbabilityTree(1.0/(y.getMax()-y.getMin()));
                        childi.setChild(grandson, 0);
                       
                        error = 0;
                    }else{// No devuelvo una densidad cero
                        //Y para calcular la MTE density necesito el empiricDensity de estos valores.
                        empiric = new Vector();
                        //Este empiric no seria con yValues, si no con los que pertenecen a esta particion
                        // Ya que en yValues estan todos los valores de la vble y
                                               
                        t.sort(yValues);
                        KernelDensity density;
                        if(((Double)yValues.elementAt(0)).doubleValue() == ((Double)yValues.elementAt(yValues.size()-1)).doubleValue()){
                            //System.out.println("Son todos iguales, ha de devolver solo un valor con 1 en el y");
                            xDensity = new Vector();
                            yDensity = new Vector();
                            xDensity.addElement(yValues.elementAt(0));
                            yDensity.addElement(new Double(1));
                        }else{
                            //instead of considering the sample to estimate the kernel density
                            //I'm going to not consider the first either the last, to avoid problems with
                            //boundary bias
			    xDensity = new Vector();
                            yDensity = new Vector();
                            for (int m = 0 ; m < yValues.size() ; m++ ){
                                xDensity.add(yValues.elementAt(m));
                            }
                            t.sort(xDensity);
                            
                            //Como minimo xDensity tiene que tener 4 puntos para 
                            //poder eliminar los extremos, porque de lo contrario
                            //xDensity quedaria vacio o con solo 1 punto.
                            if (xDensity.size()>3){
                                xDensity.removeElementAt(yValues.size()-1);
                                xDensity.removeElementAt(0);
                            }
                            //if there are only two values, "for" will not be used
                            density = new KernelDensity(xDensity, 0);
                            empiric = new Vector();
                            empiric = density.getValues(xDensity);
                            xDensity = new Vector();
                            xDensity = (Vector) empiric.elementAt(0);
                            yDensity = (Vector) empiric.elementAt(1);
                            
                        }
                        
                        for (j=0 ; j<yDensity.size() ; j++)
                            if (((Double)yDensity.elementAt(j)).doubleValue() <= 0)
                                System.out.println("Valor de densidad negativo : "+ ((Double)xDensity.elementAt(j)).doubleValue()+"\t"+(((Double)yDensity.elementAt(j)).doubleValue()));

                        
                        //System.out.println("Para el hijo "+i+" van "+yValues.size()+" casos");
                        //System.out.println("Ahora para el hijo "+i+" calculo la MTE density");
                        //System.out.println("Valores del empiric con los que calculo el hijo");
                        //for (j=0 ; j<xKernel.size() ; j++)
                        //System.out.println(((Double)xKernel.elementAt(j)).doubleValue()+"\t"+((Double)yKernel.elementAt(j)).doubleValue());
                        
                        //siempre va a haber suficientes valores?
                        childi = childi.learnUnivariate(y,yValues, intervals, numpoints);
                        //System.out.println("El resultado es: ");
                        //childi.print();
                        
                        //Necesitosaber si al hacer el learn pongo los cutpoints directamente o no.
                        // si no me equivoco el childi ahora debe tener intervals hijos, que han partido
                        // el rango de la variable y. Asi primero he de ver el xValue del xKernel a que hijo de
                        // childi corresponde y entonces hacer el getValue
                        //System.out.println("Los cutpoints del childi son:");
                        //for(k=0 ; k < childi.getCutPoints().size() ; k++)
                        //   System.out.println("cutpoint "+k+" : "+childi.getCutPoint(k));

                        u = 0;
                        error = 0;
                        for(j=0 ; j<xDensity.size() ; j++){
                            xValue = ((Double)xDensity.elementAt(j)).doubleValue();
                            yValue = ((Double)yDensity.elementAt(j)).doubleValue();
                            fuera = true;
                            u = 0;//Me indica en que hijo de childi cae xValue
                            while(fuera){
                                //System.out.println("entro en el while, u vale : "+u+" number of children "+childi.getNumberOfChildren());
                                //System.out.println("el valor: "+xValue+" el primero: "+((childi).getCutPoint(u))+" el segundo: "+((childi).getCutPoint(u+1)));
                                if((xValue >= (childi).getCutPoint(u)) & (xValue <= (childi).getCutPoint(u+1))){
                                    fuera = false;
                                    estimation1 = (((childi).getChild(u)).getProb()).getValue((Continuous)y,xValue);
                                    error = error +(yValue-estimation1)*(yValue-estimation1);
                                }//End of if
                                else{ u++;
                                //System.out.println("El valor de y en "+xValue+" esta fuera del intervalo ["+(childi).getCutPoint(u-1)+","+(childi).getCutPoint(u)+"]");
                                }
                            }//End of while
                        }//End of for
                        error = error/xDensity.size();
                    }//Fin del else
                    // He de comparar el error que cometo ahora (error)
                    //con el que cometia con el nodo t con el que se llamo a la funcion.
                    //Para ello he de tener ese error guardado. Para no calcularlo dos veces
                    //deberia haberlo guardado cuando lo calcule antes.
                    
                    //Aqui tengo los errores de la vble Xi en cada intervalo segun la MTE de la t original
                    // Asi pues he de mirar los de la vble por la que estoy partiendo.
                    errors = new Vector();
                    //System.out.println("Estoy en discreto cuando escogo entre los nuevo y lo anterior. maxH es "+maxH+" y errorCollection tiene "+errorCollection.size()+" elementos.");
                    errors = (Vector)errorCollection.elementAt(maxH);
                    //System.out.println("Eror nuevo: "+error);
                    //System.out.println("Antiguo error: "+((Double)errors.elementAt(i)).doubleValue());
                    //i es el intervalo en el que estamos
                    if(error < ((Double)errors.elementAt(i)).doubleValue()){//Es decir, el error que cometo con lo nuevo es mejor
                        //System.out.println("Error nuevo: "+error+"  Error antiguo: "+((Double)errors.elementAt(i)).doubleValue());
                        t.setChild(childi,i);
                        //System.out.println("Me quedo con lo nuevo que he calculado, ya que mejora el error que tenia");
                        //System.out.println("La vble es: ");
                        //(childi.getVar()).print();
                    }
                    else{ //El error con lo nuevo es peor, luego me quedo con lo que tenia
                        
                        t.setChild(densityCopy.copy(),i);
                        //System.out.println("No me quedo con lo nuevo que he calculado, ya que empeora el error que tenia");
                        //System.out.println("El que tenia estaba definido sobre la vable :");
                        //(densityCopy.getVar()).print();
                        //System.out.println("Debe ser la vble objetivo");
                    }
                    //AQUI ES DONDE HAGO QUE GUARDE EL CONTINUOUSCASELISTMEM NUEVO
                    //PARA CADA INTERVALO/HIJO SOLO CON AQUELLOS CASOS QUE CUMPLEN LA CONDICION
                    //QUE ACABAMOS DE DEFINIR
                    
                    caseNew = new ContinuousCaseListMem(vars2);
                    config = new ContinuousConfiguration();
                    
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        if((sample.getValue(j,indexDi)==i)){
                            config = (ContinuousConfiguration)sample.get(j);
                            caseNew.put(config);
                        }
                    }
                    
                    caseListVector.addElement(caseNew);
                    //System.out.println("El nuevo con el que llamare quizas tiene "+caseNew.getNumberOfCases()+" elementos");
                    
                }//End of for (para cada hijo)
                //System.out.println("Ahira muestra el arbol");
                //t.print();
                //Ya me he creado todos los hijos, solo me queda para cada hijo volver a llamar a la funcion, de forma que
                //sea recursiva.
                
                //Primero he de quitar el Xi del NodeList
                newX.removeNode(Di);
                
                for(j=0 ; j<t.getNumberOfChildren() ; j++){
                    //System.out.println("Casos: "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases());
                    if(((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases() > 2 ){
                        //System.out.println("Ahora llamo a Splitting para el hijo "+j+" con un ContinuousCaseListMem de "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+" elementos");
                        //System.out.println("El hijo tiene como vble definida sobre el:");
                        //((t.getChild(j)).getVar()).print();
                        Splitting(t.getChild(j),newX,y,(ContinuousCaseListMem)caseListVector.elementAt(j),intervals,numpoints);
                    }
                    //else System.out.println("No llamo a Splitting para el hijo "+j+" porque caen "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+ "elementos, que son menos de 50");
                }//End of for
                
            }//End of else (discrete)
            
        }//Fin del else
        
    }//Fin de la funcion Splitting
    
    
    /**
     * Returns the cp vector when splitting the domain of a variable
     * by the method equalfrecuency, that is, it discretizes the domain
     */
    
    public Vector getCpEqualFrecuency(ContinuousCaseListMem sample, Continuous Xi ,int intervals){
        int i,j,n,indexInf,indexSup,rep,indexMore;
        int Points, nPoints, nMore,indexXi,indexInfFormer = 0,repFormer;
        double inf, sup,xValue;
        double medsup = 0;
        double medinf = 0;
        boolean solouno = false;
        Vector cp = new Vector();
        
        //System.out.println("Estoy en el getCpEqualFrecuency");
        n = (int)sample.getNumberOfCases();
        //System.out.println("El tama�o del ContinuousCaseListMem es "+n);
        nPoints = (int)(n/intervals);
        //System.out.println("num de intervals: "+intervals);
        //System.out.println("Hay que poner "+nPoints+" en cada intervalo");
        nMore = n%intervals;
        //System.out.println("Hay "+nMore+" intervalos con "+(nPoints+1)+" puntos");
        indexMore = intervals-nMore;
        indexInf = 0;
        i = 0;
        rep = 0;
        Vector vars = new Vector();
        vars = sample.getVariables();
        
        NodeList vars2 = new NodeList();
        
        //Voy a pasar del vector de nodos vars al nodelist vars2
        //para poder buscar el indice de la vble objetivo.
        for(j=0 ; j<vars.size() ; j++){
            //System.out.println("Voy a insertar en el vars2 el nodo:");
            //((Node)vars.elementAt(j)).print();
            vars2.insertNode((Node)vars.elementAt(j));
        }
        //Quiero saber cual es el indice de la variable Xi en el ContinuousCaseListMem
        indexXi = vars2.getId(Xi);
        
        Vector values = new Vector();
        
        for(j=0 ; j<sample.getNumberOfCases() ; j++){
            xValue = sample.getValue(j,indexXi);
            values.addElement(new Double(xValue));
            //System.out.println(" los x values: "+xValue);
        }
        //System.out.println( "sale del for ++++++++****************** ");
        
        //if ()
        
        sort(values);
        //sort(values);
        //System.out.println("\tras ordenar el vector queda: "+values);
        boolean salir =false;
        if ( values.size()>1){
            //System.out.println("Tama?o de values: "+values.size());
            //si values.size es 3 solo da para 2 intervalos, si es 2 solo da para 1
            //while(i<(Math.min(intervals,values.size()-1))){
            //while((i<intervals)&&(salir)){
            while(i < intervals){
                if(i<indexMore){//Estamos en un intervalo que tendra exactamente nPoints
                    Points = nPoints;
                }else{//Estamos en un intervalo que tendra exactamente nPoints+1
                    Points = nPoints+1;
                }
                //avrofe
                if (Points > 0){
                    //System.out.println("\neste intervalo tiene num de points "+Points);
                    indexSup = indexInf+Points-1;
                    //System.out.println("indexInf: "+indexInf+", indexSup: "+indexSup);
                    //System.out.println("indexSup: "+indexSup);
                    inf = ((Double)values.elementAt(indexInf)).doubleValue();
                    //System.out.println("el inf es: "+inf);
                    sup = ((Double)values.elementAt(indexSup)).doubleValue();
                    //System.out.println("el inf es: "+inf+", el sup es: "+sup);
                    
                    while(inf == sup ){// Si los dos son iguales, significa que he de unir estos valores con los del intervalo
                        // siguiente (habra pues al menos un intervalo menos de los deseados)
                        //System.out.println("Both inf and sup were equal");
                        
                        if(i == (intervals-1)){//Sigue siendo igual y ya no hay mas, asi que lo unimos al anterior
                            if(cp.size() > 0)
                                cp.removeElementAt(cp.size()-1);
                            //System.out.println("i ya vale intervals");
                            indexInf = indexInfFormer;
                            //System.out.println("He cambiado indexInf a "+indexInfFormer);
                            indexSup = values.size()-1;
                            //System.out.println("He cambiado indexSup a "+indexSup);
                            sup = ((Double)values.elementAt(indexSup)).doubleValue();
                            //System.out.println("sup vale  "+sup);
                            inf = ((Double)values.elementAt(indexInf)).doubleValue();
                            //System.out.println("inf vale "+inf);
                            if((indexInf == 0) && (indexSup == (values.size()-1)))// En realidad solo se coge un intervalo
                                solouno = true;
                            if(sup == inf){// Todos los valores son iguales, solo hay un punto, por lo que
                                // se coge todo el intervalo
                                //System.out.println("Aun asi el inf es igual al sup, asi que pongo el max y el min de la variable");
                                inf = Xi.getMin();
                                sup = Xi.getMax();
                            }
                        }else{//fin del if(i == (intervals-1))
                            indexSup += Points;
                            //System.out.println("ahora el indexSup es "+indexSup);
                            sup = ((Double)values.elementAt(indexSup)).doubleValue();
                            //System.out.println("el sup es: "+sup);
                            i++;
                            if(i == (intervals-1)){
                                if((indexInf == 0) && (indexSup == (values.size()-1)))// En realidad solo se coge un intervalo
                                    solouno = true;
                                if(sup == inf){// Todos los valores son iguales, solo hay un punto, por lo que
                                    // se coge todo el intervalo
                                    //System.out.println("Aun asi el inf es igual al sup, asi que pongo el max y el min de la variable");
                                    inf = Xi.getMin();
                                    sup = Xi.getMax();
                                }
                            }
                            rep++;
                        }
                        
                    }
                    //System.out.println("i vale: "+i);
                    //System.out.println("solouno es "+solouno);
                    //Este es el punto medio del inferior
                    if ((i==0) || ((i-rep)==0) || solouno){
                        //medsup = (sup+((Double)values.elementAt(indexSup+1)).doubleValue())/2;
                        //medinf=inf-(medsup-sup);}
                        medinf = inf;}
                    else medinf = (inf+((Double)values.elementAt(indexInf-1)).doubleValue())/2;;
                    
                    if(i==(intervals-1) || (solouno)){ //medsup = sup+ (inf-medinf);
                        medsup = sup;
                    }
                    else{
                        
                        medsup = (sup+((Double)values.elementAt(indexSup+1)).doubleValue())/2;
                    }
                    cp.addElement(new Double((medinf)));
                    //System.out.println("cutpoint "+i+" : "+medinf);
                    indexInfFormer = indexInf;
                    repFormer = rep;
                    indexInf = indexSup+1;
                    i++;
                    rep = 0;
                }//End of if Points > 0
                else{
                    i++;
                }
            }//End of while
            
            
            cp.addElement(new Double(medsup));
            //System.out.println("Ultimo cutpoint : "+medsup);
            //System.out.println("numero de cutpoints "+cp.size()+" q son "+cp);
            //System.out.println("Salgo del getCpEqualFrecuency");
        }
        else {
            cp.addElement(new Double(Xi.getMin()));
            cp.addElement(new Double(Xi.getMax()));
        }
        
        return cp;
        
    }
    
    
    /**
     * This method returns the cp vector when discretising the domain of a continuous variable.
     * It is done by means of the equal with method
     *
     * @param max The maximum value of the variable.
     * @param min The minimum value of the variable.
     * @param intervals The number of intervals to get.
     */
    
    public Vector getCpEqualWidth(double max,double min,int intervals){
        
        int j;
        Vector cutPoints = new Vector();
        for (j=0 ; j<=intervals ; j++){
            cutPoints.addElement(new Double(min+(j*(max-min)/intervals)));
            //  System.out.println("El "+j+" es: "+(min+(j*(max-min)/intervals)));
        }
        
        return cutPoints;
        
        
    }
    
    
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
        
        while (lChanged){
            
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
            
            nIndex++;
            
        }//End of while
        
    }//End of method
    
    /**
     * This method is used in order to decide whether to split or not a node to get a conditional
     * MTE density. It is a recursive method. <code>y</code> is the discrete variable we want to
     * get the MTE condtional density for, and <code>X</code> is the set of parents. Each time we split over
     * one of the variables in <code>X</code>, we remove it from <code>X</code>, and we will split the new children
     * we have just created. The recursive method stops when <code>X</code> is empty.
     * The node t that is going to be splitted has assigned on it the discrete variable <code>y</code>
     * and some children attached to it. We will split over a variable Xi belonging to <code>X</code> such that
     * has the biggest error rate according to the last estimation (the one in t). We will split the domain of Xi in
     * intervals of equal length if it is continuous, and it will have as many children as states it has if
     * it is discrte. . If the new density we estimate on any of the new nodes is worst than the one we had
     * in the original node <code>t</code> we put the older one, if not, the new one.
     *
     * AUN FALTA PONERLE UN PAR MAS DE RESTRICCIONES: QUE PARE CUANDO NO HAY 20 PUNTOS PARA HACER LA
     * OBTENCION DE LA MIXTEXPDENSITY, Y QUE PARE CUANDO HAYA MAS DE UN NUMERO DETERMINADO DE HOJAS
     *
     * @param t The node that is gonig to be splitted
     * @param X The set of parent variables left to split over.
     * @param y The discrete variable we want to estimate its conditional density.
     * @param sample The set of cases.
     * @param intervals The number of intervals we will split the domains in.
     * @param numpoints The number of points used in the Derivative method (estimation)
     *
     */
    public void Splitting(ContinuousProbabilityTree t,NodeList X,FiniteStates y,ContinuousCaseListMem sample,int intervals){
        
        int k = 0;
        int i= 0;
        int j = 0;
        int indexXi = 0;
        int indexy = 0;
        int indexDi = 0;
        int n = 0;
        int u = 0;
        int h = 0;
        int maxH = 0;
        int DiValue;
        Continuous Xi;
        FiniteStates Di;
        ContinuousProbabilityTree ti,tiChildi;
        
        ContinuousProbabilityTree densityCopy;
        Vector cutPoints = new Vector();//Aqui voy a guardar los cutpoints de cada variable
        double min,max,Xivalue,xValue,entropyXi,error,XiValue,total,entropyDi,p1,q1;
        double yValue = 0;
        double ei = 0;
        Vector errors = new Vector(); //Aqui voy a guardar los errores que cometo en cada intervalo en cada vble
        Vector errorCollection; //Aqui guardo en cada posicion el vector error. Es decir, la pos 0 del vector
        //errorCollection tiene el vector error correspondiente a la vble i del NodeList X
        Vector entropy = new Vector(); //Aqui voy a guardar las entropias. Posicion 0 se corresponde con
        // variable 0 de X
        
        Vector newcp = new Vector();//VEctor para los cutpoints del densityCopy
        Vector vars;//Vector de Nodes (los que hay en el ContinuousCaseListMem)
        Vector caseListVector = new Vector();//Vector donde voy a guardar para cada hijo del nodo el ContinuousCaseListMem con el
        // que voy a llamar de nuevo al Splitting
        ContinuousCaseListMem caseNew;
        ContinuousConfiguration config;
        NodeList newX = new NodeList();
        
        //Primero tengo que comprobar si el NodeList X es vacio o no.
        // si es vacio paro
        
        //System.out.println("Empiezo el Splitting (Discreto)");
        
        if ((X.getNodes()).size()==0){
            //System.out.println("No sigo partiendo, ya que no me quedan variables por las que partir");
        }
        else {  //Tengo que partir este nodo
            //primero tengo que ver la variable por la que voy a partir
            
            newX = X.copy();
            
            //System.out.println("*********************************");
            //System.out.println("*********************************");
            //System.out.println("Hay "+(X.getNodes()).size()+" posibles variables por las que partir");
            errorCollection = new Vector();
            
            vars = new Vector();
            vars = sample.getVariables();
            
            NodeList vars2 = new NodeList();
            
            //Voy a pasar del vector de nodos vars al nodelist vars2
            //para poder buscar el indice de la vble objetivo.
            for(j=0 ; j<vars.size() ; j++){
                vars2.insertNode((Node)vars.elementAt(j));
            }
            indexy = vars2.getId(y);
            
            
            //Para cada variable calculo su H
            for (i=0 ; i<(X.getNodes()).size() ; i++){//Para cada vable calculo su H
                
                //First I have to check whether the variable is discrete or continuous
                if(((Node)(X.getNodes()).elementAt(i)).getTypeOfVariable() == 0){//The variable is continuous
                    //System.out.println("The variable is continuous");
                    Xi = new Continuous();
                    Xi = (Continuous)X.elementAt(i);
                    //System.out.println("Pruebo con: "+Xi.getName());
                    //Xi.print();
                    
                    cutPoints = new Vector();
                    min = Xi.getMin();
                    max = Xi.getMax();
                    
                    //Aqui escogemos como hacer la particion, por igual frecuencia o por igual longitud.
                    //cutPoints = getCpEqualWidth(max,min,intervals);
                    //System.out.println("calculo los cutpoints");
                    //cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                    if(sample.getNumberOfCases() > 29)
                        cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                    else{
                        cutPoints.addElement(new Double(Xi.getMin()));
                        cutPoints.addElement(new Double(Xi.getMax()));
                    }
                    //System.out.println("ya he calculado los cutpoints, hay "+cutPoints.size()+" q son "+cutPoints);
                    ti = new ContinuousProbabilityTree(Xi,cutPoints);
                    
                    //Quiero saber cual es el indice de la variable Xi en el ContinuousCaseListMem
                    indexXi = vars2.getId(Xi);
                    
                    caseNew = new ContinuousCaseListMem(vars2);
                    
                    for(h=0 ; h<(cutPoints.size()-1) ; h++){
                        for(j = 0 ; j<sample.getNumberOfCases() ; j++){
                            //I have to get the sub-CaseListMem that would go in this interval
                            if((sample.getValue(j,indexXi)>=ti.getCutPoint(h)) & (sample.getValue(j,indexXi)<=ti.getCutPoint(h+1))){
                                config = (ContinuousConfiguration)sample.get(j);
                                caseNew.put(config);
                            }//End of if
                        }
                        if(caseNew.getNumberOfCases() == 0){// No hay valores en este intervalo, luego pongo
                            tiChildi = new ContinuousProbabilityTree(0.0);
                            ei = 0;
                        }else{
                            // Now I have in caseNew the subset of smple that goes in this interval
                            // The next thing we must do is to obtain the density for this interval
                            
                            tiChildi = new ContinuousProbabilityTree();
                            tiChildi = tiChildi.learnUnivariate(y,caseNew);
                            
                            ei = 0; //Contador sobre el error
                            //To check the error we compare each probability of t and tiChildi
                            
                            for(j = 0 ; j<y.getNumStates() ; j++){
                                
                                p1 = ((t.getChild(j)).getProb()).getIndependent();
                                q1 =  ((tiChildi.getChild(j)).getProb()).getIndependent();
                                ei = ei+((p1-q1)*(p1-q1));
                                
                            }//End of for
                            
                        }// End of if
                        //A�ado el error que cometo al vector de errores
                        //System.out.println("Error que cometo en hijo1: "+ei);
                        errors.addElement(new Double(ei));
                        
                    }//End of for, de cada intervalo
                    
                    //Ahora me queda calcular la entropia que tengo si escogiese esta variable
                    //segun la formula H(Xi)=suma(ei*log(ei)), pero con los errores normalizados,
                    //luego primero he de normalizarlos.
                    entropyXi = 0;
                    total = 0;
                    
                    for(j=0 ; j<errors.size() ; j++){
                        //System.out.println("Busco el error "+j);
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        total = total + ei;
                    }//End of for
                    
                    //Now I calculate the entropy.
                    for(j=0 ; j<errors.size() ; j++){
                        
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        ei = ei/total;
                        //System.out.println("ei normalizado vale: " +ei);
                        entropyXi = entropyXi + ((ei) * Math.log(ei));
                    }
                    //Ahora pongo la entropia en el vector de entropias
                    //System.out.println("La EntropyXi vale: "+entropyXi);
                    entropy.addElement(new Double(entropyXi));
                    
                    //Y ahora pongo el vector con los errores en cada intervalo en el
                    //Vector errorCollection
                    errorCollection.addElement(errors);
                    errors = new Vector();
                }//End of if(continuous)
                
                // THE VARIABLE IS A FINITESTATES
                
                else{//In this case the variable is a FiniteStates
                    //System.out.println("The variable is finiteStates");
                    Di = new FiniteStates();
                    Di = (FiniteStates)X.elementAt(i);
                    //System.out.println("Pruebo con la vble :");
                    //Di.print();
                    ti = new ContinuousProbabilityTree(Di);
                    
                    //Quiero saber cual es el indice de la variable Di en el ContinuousCaseListMem
                    indexDi = vars2.getId(Di);
                    
                    //Now for each child (node) we will get the error ei
                    for(h=0 ; h<Di.getNumStates() ; h++){
                        
                        caseNew = new ContinuousCaseListMem(vars2);
                        //The first child corresponds with value 0 of the variable, the second with value 1, etc..
                        
                        //Now I have to collect the values of the dbc that would go to this node
                        //in order to calculate its error.
                        for(j=0 ; j<sample.getNumberOfCases() ; j++){
                            DiValue = (int)sample.getValue(j,indexDi);
                            if(DiValue == h){
                                config = (ContinuousConfiguration)sample.get(j);
                                caseNew.put(config);
                            }//End of if
                        }//End of for
                        if(caseNew.getNumberOfCases() == 0 ){// No hay valores en esta configuracion, luego no cometo error
                            tiChildi = new ContinuousProbabilityTree(0.0);
                            ei = 0;
                            
                        }else{// Si hay valores en la conf
                            tiChildi = new ContinuousProbabilityTree();
                            tiChildi = tiChildi.learnUnivariate(y,caseNew);
                            
                            ei = 0; //Contador sobre el error
                            //To check the error we compare each probability of t and tiChildi
                            
                            for(j = 0 ; j<y.getNumStates() ; j++){
                                p1 = ((t.getChild(j)).getProb()).getIndependent();
                                q1 =  ((tiChildi.getChild(j)).getProb()).getIndependent();
                                ei = ei+((p1-q1)*(p1-q1));
                            }//End of for
                        }// End of else
                        //A�ado el error que cometo al vector de errores
                        errors.addElement(new Double(ei));
                        
                    }//End of for(h=0 ; h<Di.getNumStates() ; h++)
                    
                    //Ahora me queda calcular la entropia que tengo si escogiese esta variable
                    //segun la formula H(Xi)=suma(ei*log(ei)), pero con los errores normalizados,
                    //luego primero he de normalizarlos.
                    entropyDi = 0;
                    total = 0;
                    
                    for(j=0 ; j<Di.getNumStates() ; j++){
                        //System.out.println("Busco el error "+j);
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        total = total + ei;
                    }//End of for
                    
                    //Now I calculate the entropy.
                    for(j=0 ; j<Di.getNumStates() ; j++){
                        
                        ei = ((Double)errors.elementAt(j)).doubleValue();
                        ei = ei/total;
                        //System.out.println("ei normalizado vale: " +ei);
                        entropyDi = entropyDi + ((ei) * Math.log(ei));
                    }
                    //Ahora pongo la entropia en el vector de entropias
                    //System.out.println("La EntropyXi vale: "+entropyDi);
                    entropy.addElement(new Double(entropyDi));
                    
                    //Y ahora pongo el vector con los errores en cada intervalo en el
                    //Vector errorCollection
                    errorCollection.addElement(errors);
                    errors = new Vector();
                    
                }//end of finitestates
                
            }//End of for para cada variable
            
            //System.out.println();
            //System.out.println("Ahora he de seleccionar la variable por la que voy a partir");
            //System.out.println();
            
            // AHORA ES CUANDO PARTO Y CALCULO LOS NODOS HIJOS
            
            //Ahora ya tengo todas las entropias calculadas, solo tengo que quedarme con el maximo, y ver
            //el indice que es, pues se correspondera con el indice de la variable por la que partir.
            //este maxH es el indice de la vble maximo en el NodeList X
            
            //System.out.println("Now I select the maximun entropy. (en splitting discreto)");
            maxH = 0;
            for(i=1 ; i<entropy.size() ; i++){
                if(((Double)entropy.elementAt(i)).doubleValue() > ((Double)entropy.elementAt(maxH)).doubleValue())
                    maxH = i;
            }//End of for
            //Asi pues la variable que esta en la posicion maxH es por la que hay que partir
            // la llamo Xi de nuevo.
            
            //Again I have to make a difference if it is discrete or continuous
            if(((Node)X.elementAt(maxH)).getTypeOfVariable() == 0){//It is continuous
                
                Xi = (Continuous)X.elementAt(maxH);
                cutPoints = new Vector();
                min = Xi.getMin();
                max = Xi.getMax();
                
                //Aqui escogemos como hacer la particion, por igual frecuencia o por igual longitud.
                //cutPoints = getCpEqualWidth(max,min,intervals);
                //cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                if(sample.getNumberOfCases() > 29)
                    cutPoints = getCpEqualFrecuency(sample,Xi,intervals);
                else{
                    cutPoints.addElement(new Double(Xi.getMin()));
                    cutPoints.addElement(new Double(Xi.getMax()));
                }
                //Ahora a t deberia ponerle le variabe Xi , y crearle intervals hijos, y en cada hijo poner
                //la variable 'y' y estimarle una densidad, pero sin perder la densidad que ya traia
                // ya que si la que estimo nueva no es mejor dejo la que tenia en ese intervalo.
                
                //System.out.println("he calculado los cutpoints y hay "+cutPoints.size()+" q son "+cutPoints);
                //Ahora a t deberia ponerle le variabe Xi , y crearle intervals hijos, y en cada hijo poner
                //la variable 'y' y estimarle una densidad, pero sin perder la densidad que ya traia
                // ya que si la que estimo nueva no es mejor dejo la que tenia en ese intervalo.
                //System.out.println("Cambio "+((Double)cutPoints.elementAt(0)).doubleValue()+" por "+min);
                cutPoints.setElementAt(new Double(min),0);
                cutPoints.setElementAt(new Double(max),cutPoints.size()-1);
                             
                t.assignVar(Xi,cutPoints);
                
                //Necesito los valores de la variable objetivo cuyo Xi esta en el intervalo i actual
                //Quiero saber cual es el indice de la variable Xi en el ContinuousCaseListMem
                
                //Voy a pasar del vector de nodos vars al nodelist vars2
                vars2 = new NodeList();
                for(j=0 ; j<vars.size() ; j++){
                    vars2.insertNode((Node)vars.elementAt(j));
                }
                
                indexXi = vars2.getId(Xi);
                //System.out.println("Este es el index de Xi "+indexXi);
                
                //Ahora pongo un hijo por cada intervalo, y en ese hijo
                // pongo la variable y, le calculo una densidad, y veo si mejora a lo que ya tenia la
                // t original.
                for(i=0 ; i<(cutPoints.size() -1) ; i++){
                    //System.out.println("Inserto el hijo: "+i);
                    
                    
                    caseNew = new ContinuousCaseListMem(vars2);
                    
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        //I have to get the sub-CaseListMem that would go in this interval
                        if((sample.getValue(j,indexXi)>=t.getCutPoint(i)) & (sample.getValue(j,indexXi)<=t.getCutPoint(i+1))){
                            config = (ContinuousConfiguration)sample.get(j);
                            caseNew.put(config);
                        }//End of if
                    }//End of for
                    //System.out.println("A este hijo le corresponden "+caseNew.getNumberOfCases()+" valores de la muestra");
                    
                    // Now I have in caseNew the subset of smple that goes in this interval
                    // The next thing we must do is to obtain the density for this interval
                    if(caseNew.getNumberOfCases() == 0){// No hay valores, luego pongo una densidad cero
                        //Antes poniamos 0, ahora ponemos una uniforme para y
                        double[] d = new double[y.getNumStates()];
                        for (int iii=0;iii<d.length;iii++){
                           d[iii]=1.0/d.length;
                        }
                        tiChildi = new ContinuousProbabilityTree(y,d);
                        
                    }else{
                        tiChildi = new ContinuousProbabilityTree();
                        tiChildi = tiChildi.learnUnivariate(y,caseNew);
                    }
                    t.setChild(tiChildi,i);
                    
                    caseListVector.addElement(caseNew);
                    //System.out.println("El nuevo con el que llamo tiene "+caseNew.getNumberOfCases()+" elementos");
                    
                }//End of for (para cada intervalo)
                
                //Ya me he creado todos los hijos, solo me queda para cada hijo volver a llamar a la funcion, de forma que
                //sea recursiva.
                
                //Primero he de quitar el Xi del NodeList
                newX.removeNode(Xi);
                //System.out.println("t tiene ahora "+t.getNumberOfChildren()+" hijos");
                for(j=0 ; j<t.getNumberOfChildren() ; j++){
                    //System.out.println("Casos: "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases());
                    //System.out.println("Ahora llamo a Splitting para el hijo "+j+" con un ContinuousCaseListMem de "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+" elementos");
                    Splitting(t.getChild(j),newX,y,(ContinuousCaseListMem)caseListVector.elementAt(j),intervals);
                }//End of for
                
            }//End of if(continuous)
            
            //   *********** LA VARIABLE ES DISCRETA *************
            //   *********** ESTOY PARTIENDO LA VARIABLE Y CALCULANDO LAS MTE HOJAS ********************
            
            else{ //The variable we have to split over is discrete
                
                Di = new FiniteStates();
                Di = (FiniteStates)X.elementAt(maxH);
                
                //Ahora a t deberia ponerle le variabe Xi , y crearle intervals hijos, y en cada hijo poner
                //la variable 'y' y estimarle una densidad, pero sin perder la densidad que ya traia
                // ya que si la que estimo nueva no es mejor dejo la que tenia en ese intervalo.
             
                t.assignVar(Di);
                
                
                //Ahora pongo un hijo por cada intervalo, y en ese hijo
                // pongo la variable y, le calculo una densidad, y veo si mejora a lo que ya tenia la
                // t original.
                for(i=0 ; i<Di.getNumStates() ; i++){//Empieza el for para crear los hijos
                    
                    //Necesito los valores de la variable objetivo cuyo Di es el i actual
                    //Quiero saber cual es el indice de la variable Di en el ContinuousCaseListMem
                    vars2 = new NodeList();
                    //Voy a pasar del vector de nodos vars al nodelist vars2
                    for(j=0 ; j<vars.size() ; j++){
                        vars2.insertNode((Node)vars.elementAt(j));
                    }
                    
                    indexDi = vars2.getId(Di);
                    //Now for each child (node) we will get the cpt
                    
                    caseNew = new ContinuousCaseListMem(vars2);
                    //The first child corresponds with value 0 of the variable, the second with value 1, etc..
                    
                    //Now I have to collect the values of the dbc that would go to this node
                    //in order to calculate its error.
                    for(j=0 ; j<sample.getNumberOfCases() ; j++){
                        DiValue = (int)sample.getValue(j,indexDi);
                        if(DiValue == i){
                            config = (ContinuousConfiguration)sample.get(j);
                            caseNew.put(config);
                        }//End of if
                    }//End of for
                    if(caseNew.getNumberOfCases() == 0){
                       //Antes poniamos 0, ahora ponemos una uniforme para y
                        double[] d = new double[y.getNumStates()];
                        for (int iii=0;iii<d.length;iii++){
                           d[iii]=1.0/d.length;
                        }
                        tiChildi = new ContinuousProbabilityTree(y,d);
                        
                
                    }else{
                        tiChildi = new ContinuousProbabilityTree();
                        tiChildi = tiChildi.learnUnivariate(y,caseNew);
                    }
                    t.setChild(tiChildi,i);
                    
                    caseListVector.addElement(caseNew);
                    
                }//End of for (para cada hijo)
                
                //Ya me he creado todos los hijos, solo me queda para cada hijo volver a llamar a la funcion, de forma que
                //sea recursiva.
                
                //Primero he de quitar el Xi del NodeList
                newX.removeNode(Di);
                
                for(j=0 ; j<t.getNumberOfChildren() ; j++){
                    
                    //System.out.println("Ahora llamo a Splitting para el hijo "+j+" con un ContinuousCaseListMem de "+((ContinuousCaseListMem)caseListVector.elementAt(j)).getNumberOfCases()+" elementos");
                    //System.out.println("El hijo tiene como vble definida sobre el:");
                    //((t.getChild(j)).getVar()).print();
                    Splitting(t.getChild(j),newX,y,(ContinuousCaseListMem)caseListVector.elementAt(j),intervals);
                    
                }//End of for
                
            }//End of else (discrete)
            
        }//Fin del else
        
    }//Fin de la funcion Splitting
    
    
    /**
     * Gets of the expected likelihood of a sample corresponding to a conditional distribution, this is, the
     *  sum of the likelihoods in the leaves of the tree
     *
     * @param t The tree to get the likelihood of
     * @param case The caseLisMem to get the sample from
     * @param X The variable the density is defined for
     * @param distance A vector we put all the errors of the leaves (It is modified).
     */
    
    public void LogLikelihoodChild(ContinuousProbabilityTree t, ContinuousCaseListMem cases, Node X, Vector distance){
        
        int i,j,indexXi,xDiscValue;
        double dist,xValue,min,max,yValue;
        Vector vars;
        Node Xi;
        ContinuousConfiguration config;
        Vector caseListVector = new Vector();//Vector donde voy a guardar para cada hijo del nodo el ContinuousCaseListMem con el
        // que voy a llamar de nuevo al LogLikelihood
        ContinuousCaseListMem caseNew;
        ContinuousProbabilityTree childi;
        
        //System.out.println("Cases tiene "+cases.getNumberOfCases()+" elementos");
        
        if (t.getLabel() != 2){// t is a variable, not an MTE function
            vars = new Vector();
            vars = cases.getVariables();
            NodeList vars2 = new NodeList();
            Xi = t.getVar();
            //Voy a pasar del vector de nodos vars al nodelist vars2
            //para poder buscar el indice de la vble objetivo.
            for(j=0 ; j<vars.size() ; j++){
                //System.out.println("Voy a insertar en el vars2 el nodo:");
                //((Node)vars.elementAt(j)).print();
                vars2.insertNode((Node)vars.elementAt(j));
            }
            indexXi = vars2.getId(Xi);// This is the index of Xi in cases
            if (Xi.equals(X)){//We are in the density for X (continuous)
                //System.out.println("La variable es la deseada. Pasamos a calcular sum(log(g(xi)))");
                dist = 0;// Error in this leave.
                
                
                //System.out.println("Cutpoint 0 : "+t.getCutPoint(0));
                //for(j=1 ; j<=t.getNumberOfChildren() ; j++){
                //System.out.println("Cutpoint "+j+" : "+t.getCutPoint(j));
                //}
                
                
                for (i=0 ; i<cases.getNumberOfCases() ; i++){
                    xValue = cases.getValue(i,indexXi);
                    //System.out.println("X es :"+xValue);
                    yValue = t.getValue((Continuous)X,xValue);
                    if (yValue > 0){
                        //System.out.println("xi es: "+xValue+". g(x_i) es: "+yValue);
                        yValue = Math.log(yValue);
                        dist = dist+yValue;}
                }// End of for
                if(cases.getNumberOfCases() > 0){
                    distance.addElement( new Double(-dist/cases.getNumberOfCases())); // We add the dist in this density
                    // To the distance vector
                    //System.out.println("Esta entropia es: "+dist+" falta hacerle el - y dividir por "+cases.getNumberOfCases());
                }
            }//End of if (Xi.equals(X))
            
            else{// The variable is not X, so we must select the cases and call iteratively this method to all the children
                if (Xi.getTypeOfVariable() == 0){// Xi is continuous, we must select the corresponding cutpoints and act
                    //System.out.println("La variables no es la que queremos, asi que partimos es dominio");
                    for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                        min = t.getCutPoint(i);
                        //System.out.println("min del hijo "+i+" :"+min);
                        max = t.getCutPoint(i+1);
                        //System.out.println("max del hijo "+i+" :"+max);
                        caseNew = new ContinuousCaseListMem(vars2);
                        //System.out.println("Me acabao de crear el caseNew con vars2, que es:");
                        //vars2.print();
                        config = new ContinuousConfiguration();
                        // Now for each value of cases I see if it corresponds to this child
                        for (j = 0 ; j < cases.getNumberOfCases() ; j++){
                            xValue = cases.getValue(j,indexXi);
                            if ((min < xValue) & (xValue <= max)){// It corresponds to this child
                                config = (ContinuousConfiguration)cases.get(j);
                                caseNew.put(config);
                            }//End of if ((min < xValue) & (xValue <= max) )
                            
                        }// End of for(j = 0 ; j < cases.getNumberOfCases() ; j++)
                        
                        caseListVector.addElement(caseNew);
                        
                    }//end of for(i = 0 ; i< t.getNumberOfChildren() ; i++)
                    
                }//End of if Xi is continuous
                
                else{// The variable is discrete
                    //System.out.println("La variable no es la que queremos, es otra discreta");
                    for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                        caseNew = new ContinuousCaseListMem(vars2);
                        //System.out.println("Me acabao de crear el caseNew con vars2, que es:");
                        //vars2.print();
                        config = new ContinuousConfiguration();
                        
                        for (j = 0 ; j < cases.getNumberOfCases() ; j++){
                            xValue = cases.getValue(j,indexXi);
                            if (xValue == i){// It corresponds to this child
                                config = (ContinuousConfiguration)cases.get(j);
                                caseNew.put(config);
                            }//End of if
                            
                        }//End of for(j = 0 ; j < cases.getNumberOfCases() ; j++)
                        
                        caseListVector.addElement(caseNew);
                        
                    }//End of for(i = 0 ; i< t.getNumberOfChildren() ; i++)
                    
                }//End of else (The variabe is discrete)
                
                // I call LogLikelihood for each child and each caseNew
                for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                    childi = new ContinuousProbabilityTree();
                    childi = t.getChild(i);
                    LogLikelihoodChild(childi,(ContinuousCaseListMem)(caseListVector.elementAt(i)),X,distance);
                }//End of for
                
            }//End of else, the one that syas that the variable is not X
            
        }// End of if (t.getLabel() != 2)
        
        else{
            System.out.println("The tree is a MTE. Error: This should not happen");
        }
        
        
        
    }//End of  LoglikelihooodChid  method
    
    
    /**
     * This methid gets the expected likelihood of a conditional density with respect to a sample
     * It is the sum of the likelihoods of the leaves (densities)
     */
    public double LogLikelihood(ContinuousProbabilityTree t, ContinuousCaseListMem cases, Node X){
        int i;
        Vector distance = new Vector();
        double res = 0;
        
        LogLikelihoodChild(t,cases,X,distance);
        //System.out.println("El vector con las verosimilitudes de los hijos tiene "+distance.size()+" elementos.");
        
        for (i = 0 ; i < distance.size() ; i++)
            res = res+ ((Double)distance.elementAt(i)).doubleValue();
        
        res = res / distance.size();
        return res;
    }
    
    
    
    /**
     * Gets of the Square erorr of a sample corresponding to a conditional distribution, this is, the
     *  sum of the errors in the leaves of the tree
     *
     * @param t The tree to get the likelihood of
     * @param case The caseLisMem to get the sample from
     * @param X The variable the density is defined for
     * @param distance A vector we put all the errors of the leaves (It is modified).
     * @param numElementVector a vector stores the number of points that are considered
     *
     */
    
    public void ECMChild(ContinuousProbabilityTree t, ContinuousCaseListMem cases, Node X, Vector distance, Vector numElementsVector){
        
        int numDensityElem = 0; 
        int i,j,indexXi,xDiscValue;
        double dist,xValue,min,max,yValue,gx;
        Vector vars;
        Node Xi;
        ContinuousConfiguration config;
        Vector caseListVector = new Vector();//Vector donde voy a guardar para cada hijo del nodo el ContinuousCaseListMem con el
        // que voy a llamar de nuevo al LogLikelihood
        ContinuousCaseListMem caseNew;
        ContinuousProbabilityTree childi;
        Vector values,x,y,empiric;
        
        
        
        //System.out.println("Cases tiene "+cases.getNumberOfCases()+" elementos");
        
        if (t.getLabel() != 2){// t is a variable, not an MTE function
            vars = new Vector();
            vars = cases.getVariables();
            NodeList vars2 = new NodeList();
            Xi = t.getVar();
            //Voy a pasar del vector de nodos vars al nodelist vars2
            //para poder buscar el indice de la vble objetivo.
            //System.out.println("vars tiene tama�o "+vars.size());
            for(j=0 ; j<vars.size() ; j++){
                //System.out.println("Voy a insertar en el vars2 el nodo:");
                //((Node)vars.elementAt(j)).print();
                vars2.insertNode((Node)vars.elementAt(j));
            }
            indexXi = vars2.getId(Xi);// This is the index of Xi in cases
            //System.out.println("IndexXi: "+indexXi);
            //System.out.println("Variable:");
            //Xi.print();
            if (Xi.equals(X)){//We are in the density for X (continuous)
                //System.out.println("\n\nLa variable es la deseada. Pasamos a calcular sum(g(xi)-yi)^2)");
                dist = 0;// Error in this leave.
                x = new Vector();
                y = new Vector();
                empiric = new Vector();
                //System.out.println("Cutpoint 0 : "+t.getCutPoint(0));
                //for(j=1 ; j<=t.getNumberOfChildren() ; j++){
                    //System.out.println("Cutpoint "+j+" : "+t.getCutPoint(j));
                //}
                
                values = new Vector();
                for (i=0 ; i<cases.getNumberOfCases() ; i++){
                    xValue = cases.getValue(i,indexXi);
                    values.addElement(new Double(xValue));
                }
                //System.out.println("values tiene "+values.size()+" elementos");
                
                KernelDensity density = new KernelDensity();
                if(((Double)values.elementAt(0)).doubleValue() == ((Double)values.elementAt(values.size()-1)).doubleValue()){
                    System.out.println("Son todos iguales, ha de devolver solo un valor con 1 en el y");
                    numDensityElem = 1;
                    x = new Vector();
                    y = new Vector();
                    x.addElement(values.elementAt(0));
                    y.addElement(new Double(1)); //y la frecuencia de ese numero sera el numero de values (pq son todos repetidos)
                }else{
                    
                    //instead of considering the sample to estimate the kernel density
                    //I'm going to not consider the first either the last, to avoid problems with
                    //boundary bias
                    x = new Vector();
                    y = new Vector();
                    for (int m = 0 ; m < values.size() ; m++ ){
                        x.add(values.elementAt(m));
                    }
                    t.sort(x);
                   
                    x.removeElementAt(values.size()-1);
                    x.removeElementAt(0);
                    //if there are only two values, "for" will not be used
                    density = new KernelDensity(x, 0);
                    empiric = new Vector();
                    empiric = density.getValues(x);
                    x = new Vector();
                    x = (Vector) empiric.elementAt(0);
                    y = (Vector) empiric.elementAt(1);
                    numDensityElem = x.size();
                }

                
                //System.out.println("x size "+x.size()+" y size "+y.size());
                int negativos = 0;
                for (j=0 ; j<y.size() ; j++)
                    if (((Double)y.elementAt(j)).doubleValue() <= 0){
                        negativos ++;
                        System.out.println("Valor de densidad negativo : "+ ((Double)x.elementAt(j)).doubleValue()+"\t"+(((Double)y.elementAt(j)).doubleValue()));
                    }
                //System.out.println("Numero de valores negativos : "+negativos);
                   
                //es el numero de elementos q considero (contando repetidos)
                int aux = ((Integer)numElementsVector.elementAt(0)).intValue();
                aux = aux + numDensityElem;
                //System.out.println("el num elem: "+aux);
                numElementsVector.insertElementAt(new Integer(aux), 0);
                
                //aqui se hace el ecm
                for(j=0 ; j< x.size() ; j++){
                    xValue = ((Double)x.elementAt(j)).doubleValue();
                    //System.out.println("El valor x del empiricOne es: "+xValue);
                    gx = t.getValue((Continuous)X,xValue);
                    yValue = ((Double)y.elementAt(j)).doubleValue();
                    //System.out.println("El valor y del empiricOne es: "+yValue);
                    //System.out.println("x: "+xValue+" . y: "+yValue+" . g(x) = "+gx);
                    //System.out.println(xValue+"\t"+yValue);
                    dist = dist+(gx-yValue)*(gx-yValue);
                    
                }
                distance.addElement(new Double(dist));
                
                //System.out.println("EL error es : "+dist+" y hay "+xKernel.size()+" valores");
                
                
            }//End of if (Xi.equals(X))
            
            else{// The variable is not X, so we must select the cases and call iteratively this method to all the children
                if (Xi.getTypeOfVariable() == 0){// Xi is continuous, we must select the corresponding cutpoints and act
                    //System.out.println("\n\nLa variables no es la que queremos, asi que partimos el dominio");
                    for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                        min = t.getCutPoint(i);
                        //System.out.println("min del hijo "+i+" :"+min);
                        max = t.getCutPoint(i+1);
                        //System.out.println("max del hijo "+i+" :"+max);
                        caseNew = new ContinuousCaseListMem(vars2);
                        //System.out.println("Me acabao de crear el caseNew con vars2, que es:");
                        //vars2.print();
                        config = new ContinuousConfiguration();
                        // Now for each value of cases I see if it corresponds to this child
                        for (j = 0 ; j < cases.getNumberOfCases() ; j++){
                            xValue = cases.getValue(j,indexXi);
                            if ((min < xValue) & (xValue <= max)){// It corresponds to this child
                                config = (ContinuousConfiguration)cases.get(j);
                                //config.print();
                                caseNew.put(config);
                            }//End of if ((min < xValue) & (xValue <= max) )
                            
                        }// End of for(j = 0 ; j < cases.getNumberOfCases() ; j++)
                        //System.out.println("caseNew tiene "+caseNew.getNumberOfCases()+" casos");
                        caseListVector.addElement(caseNew);
                        
                    }//end of for(i = 0 ; i< t.getNumberOfChildren() ; i++)
                    
                    //System.out.println("caselistvector tiene tama�o "+caseListVector.size());
                    
                }//End of if Xi is continuous
                
                else{// The variable is discrete
                    //System.out.println("La variable no es la que queremos, es otra discreta");
                    for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                        caseNew = new ContinuousCaseListMem(vars2);
                        //System.out.println("Me acabao de crear el caseNew con vars2, que es:");
                        //vars2.print();
                        config = new ContinuousConfiguration();
                        
                        for (j = 0 ; j < cases.getNumberOfCases() ; j++){
                            xValue = cases.getValue(j,indexXi);
                            if (xValue == i){// It corresponds to this child
                                config = (ContinuousConfiguration)cases.get(j);
                                caseNew.put(config);
                            }//End of if
                            
                        }//End of for(j = 0 ; j < cases.getNumberOfCases() ; j++)
                        
                        caseListVector.addElement(caseNew);
                        
                    }//End of for(i = 0 ; i< t.getNumberOfChildren() ; i++)
                    
                }//End of else (The variable is discrete)
                //System.out.println("numero de children: "+t.getNumberOfChildren()+" y el caseListVector tiene "+caseListVector.size());
                
                //t.print();
                
                //I call LogLikelihood for each child and each caseNew
                for (i = 0 ; i< t.getNumberOfChildren() ; i++){
                    //System.out.println("Estoy en el hijo "+i);
                    childi = new ContinuousProbabilityTree();
                    childi = t.getChild(i);
                    int aux = ((Integer)numElementsVector.elementAt(0)).intValue();
                    //aux = aux + numDensityElem;
                    numElementsVector.insertElementAt(new Integer(aux), 0);
                    //System.out.println("Voy a llamar al ECMChild, y ahora mismo el num de elem es "+aux);
                    //System.out.println("caselistvector "+i+" tiene tama�o "+((ContinuousCaseListMem)(caseListVector.elementAt(i))).getNumberOfCases());
                    ECMChild(childi,(ContinuousCaseListMem)(caseListVector.elementAt(i)),X,distance, numElementsVector);
                }//End of for
                
            }//End of else, the one that syas that the variable is not X
            
        }// End of if (t.getLabel() != 2)
        
        else{
            System.out.println("The tree is a MTE. Error: This should not happen");
        }
        
        
    }
    //End of  ECM  method
    
    
    
    
    /**
     * This method gets the Square error of a conditional density with respect to a sample
     * It is the sum of the likelihoods of the leaves (densities)
     *
     */
    
    public double ECM(ContinuousProbabilityTree t, ContinuousCaseListMem cases, Node X){
        
        //System.out.println("Empiezo a calcular el ECM");
        //guardo el numero total de errores que se calculan, para luego dividir
        int numElements = 0;
        int i;
        Vector distance = new Vector();
        Vector numElementsVector = new Vector();
        //tengo q inicializar el vector
        numElementsVector.insertElementAt(new Integer(0), 0);
        double res = 0;
        ECMChild(t,cases,X,distance, numElementsVector);
                       
        numElements = ((Integer)numElementsVector.elementAt(0)).intValue();
        
        for (i = 0 ; i < distance.size() ; i++){
            res = res + ((Double)distance.elementAt(i)).doubleValue();
        }
        //System.out.println("numElements es "+numElements);
        
        res = res / numElements;
        
        //System.out.println("Termino de calcular el ECM");
        return res;
      }
    
    
}//End of class

