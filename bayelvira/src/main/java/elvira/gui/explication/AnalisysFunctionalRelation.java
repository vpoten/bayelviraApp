package elvira.gui.explication;

import java.text.NumberFormat;
import java.util.Random;
import java.util.Vector;

import elvira.Bnet;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.gui.InferencePanel;
import elvira.potential.PotentialTable;

public class AnalisysFunctionalRelation {
	
	//Variables de Instancia
	private String resultado;
	private double parametro1;
	private double parametro2;
	private double parametro3;
	private double parametro_estudio;
	private Random aleatorio= new Random(3816);//Genera numeros aleatorios en base al tiempo actual
	private String ind_estado;					   // de esta forma la semilla no es la misma siempre
	private Case caso_propagar= new Case();
	private String nombre_parametro;
	private Node nodo_parametro;
	private Bnet red;
	private Bnet red1;
	private Bnet red2;
	private Bnet red3;
	private Relation relacion;
	private Relation relacion1;
	private Relation relacion2;
	private Relation relacion3;
	private int p_interes;
	private int s_interes;
	private String nombre_interes;
	
	
	//Constructor
	public AnalisysFunctionalRelation(Case c,String parametro_est,
			                  String name_parametro,String estado,Bnet red_b,
			                  int p, int s,String name_interes){
		parametro1=aleatorio.nextDouble();//Valor del parametro auxiliar 1
		parametro2=aleatorio.nextDouble();//Valor del parametro auxiliar 2
		parametro3=aleatorio.nextDouble();//Valor del parametro auxiliar 3
		parametro_estudio=Double.valueOf(parametro_est).doubleValue();//Valor nuevo del parametro en estudio
		caso_propagar=c; //Caso en estudio
		nombre_parametro=name_parametro; //Nombre del parametro en estudio
		ind_estado=estado;//Indice del estado del parametro en estudio
		red=red_b;//red en estudio
		p_interes=p;//Sirven para indexar la probabilidad de interes id nodo
		s_interes=s;//Sirven para indexar la probabilidad de interes  id estado
		nombre_interes=name_interes;// Titulo del nodo de interés
	}
	//Method to calculate the relation without Evidence  
	/**
	 * Pr(Vr,0)= a*x +b 
	 * @throws Throwable 
	 */
	public String Sin_Evidencia(InferencePanel inf,InferencePanel inf2) throws Throwable{
		double prog1=0.0;
		double prog2=0.0;
		double a=0.0;
		double b=0.0;

		parametro1=roundNum(parametro1);
		parametro2=roundNum(parametro2);
		red1=red.copyBnetIncludingRelations();
		red2=red.copyBnetIncludingRelations();
		
		//Propagacion PRIMERA con valor de Parametro1
		relacion1=red1.getRelation(red1.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot1=(PotentialTable)relacion1.getValues();
		System.out.println("Propagacion PRIMERA");
		System.out.println("parametro 1 :"+ parametro1);
		pot1.setValue(calcular_decimal(ind_estado,red1.getNodeList().getNodeString(nombre_parametro, true)),parametro1);
		FiniteStates fs1=(FiniteStates)red1.getNodeList().getNodeString(nombre_parametro,true);
		pot1.normalizeOver(fs1);
		//pot1.normalize(pot1);
		//pot1.normalize();
		
		
		
		red1.getRelation(red1.getNodeList().getNodeString(nombre_parametro,true)).print();
		
		//System.out.println("Seismo Fuerte antes de que compile la red..valor del parametro1: "+pot1.getValue(Integer.valueOf(ind_estado).intValue()));
		//parametro1=pot1.getValue(Integer.valueOf(ind_estado).intValue());
		parametro1=pot1.getValue(calcular_decimal(ind_estado,red1.getNodeList().getNodeString(nombre_parametro, true)));
		
		red1.compile(3, null,null,null);		
		red1.getCompiledPotentialList();//
		System.out.println("Compilando la red..."+red1.getIsCompiled());
		
		Case uno=new Case(red1);
		//System.out.println("Seismo Fuerte despues de que compile la red..valor del parametro1: "+pot1.getValue(Integer.valueOf(ind_estado).intValue()));
		prog1=uno.getProbOfStateNode(p_interes, s_interes);
		uno.showCase();
		//System.out.println("Tsunami No despues de que compile la red prog1: "+ prog1);
		
		//Propagacion SEGUNDA con valor de Parametro2
		
		System.out.println("PARTE DOS");
		System.out.println("");
		relacion2=red2.getRelation(red2.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot2=(PotentialTable)relacion2.getValues();
		System.out.println("parametro 2: "+ parametro2);
		pot2.print();
		
		pot2.setValue(calcular_decimal(ind_estado,red2.getNodeList().getNodeString(nombre_parametro, true)),parametro2);
		FiniteStates fs2=(FiniteStates)red2.getNodeList().getNodeString(nombre_parametro,true);
		pot2.normalizeOver(fs2);
		//pot2.normalize(pot2);
		//pot2.normalize();
		
		red2.getRelation(red2.getNodeList().getNodeString(nombre_parametro,true)).print();
		//parametro2=pot2.getValue(Integer.valueOf(ind_estado).intValue());
		parametro2=pot2.getValue(calcular_decimal(ind_estado,red2.getNodeList().getNodeString(nombre_parametro, true)));
		//System.out.println("Seismo Fuerte antes de que compile la red..valor del parametro1: "+pot2.getValue(Integer.valueOf(ind_estado).intValue()));

		red2.compile(3, null,null,null);
		red2.getCompiledPotentialList();
		System.out.println("Compilado red 2..."+red2.getIsCompiled());
		
		Case two= new Case(red2);
		System.out.println("Seismo FUerte despues de que compile la red..valor del parametro2: "+pot2.getValue(Integer.valueOf(ind_estado).intValue()));
		prog2=two.getProbOfStateNode(p_interes, s_interes);
		two.showCase();
		System.out.println("Tsunami NO despues de que compile la red prog2 :" + prog2);
		
		
		
		prog1=roundNum(prog1);
		prog2=roundNum(prog2);
		
		
		//Calculos de las constantes
		b=(((prog2*parametro1)-(prog1*parametro2))/(parametro1 - parametro2));
		a=(prog1 -b)/parametro1;
		System.out.println("a :"+ a +" b: "+ b);
		System.out.println("Parametro en estudio :"+parametro_estudio/1000 );
		System.out.println("Seismo Fuerte Inicial:"+ caso_propagar.getProbOfStateNode(0,0));
		
		
		//Propagación Final con el valor del parametro en estudio
		resultado=String.valueOf((a*(parametro_estudio/1000)) + b);
		
		return resultado ;
	}
    //Method to calculate the relation with Evidence  
	/**
	 *           a*x +  b 
	 * Pr(Vr,0)= --------
	 *             x + c
	 */ 

	public String Con_Evidencia()throws Throwable{{
		double prog1=0.0;
		double prog2=0.0;
		double prog3=0.0;
		double inicial=0.0;
		parametro1=roundNum(parametro1);
		parametro2=roundNum(parametro2);
		parametro3=roundNum(parametro3);
		//GAUSS
		int numero_ecuaciones=3;
		double [][] matriz= new double[3][4];
		

		
		red1=red.copyBnetIncludingRelations();
		red2=red.copyBnetIncludingRelations();
		red3=red.copyBnetIncludingRelations();
		
		relacion=red.getRelation(red.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot=(PotentialTable)relacion.getValues();
		inicial=pot.getValue(calcular_decimal(ind_estado,red.getNodeList().getNodeString(nombre_parametro, true)));
		System.out.println("ANALISIS DE SENSIBILDAD.....EJECUTANDOSE");
		System.out.println("Valor Inicial del Parametro :"+ inicial);
	
//		Propagacion PRIMERA con valor de Parametro1
		System.out.println("Propagacion PRIMERA");
		relacion1=red1.getRelation(red1.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot1=(PotentialTable)relacion1.getValues();
		System.out.println("parametro 1 sin normalizar: "+ parametro1);
		pot1.print();
		pot1.setValue(calcular_decimal(ind_estado,red1.getNodeList().getNodeString(nombre_parametro, true)),parametro1);
		FiniteStates fs1=(FiniteStates)red1.getNodeList().getNodeString(nombre_parametro,true);
		pot1.normalizeOver(fs1);
		
		
		red1.getRelation(red1.getNodeList().getNodeString(nombre_parametro,true)).print();
		
		//System.out.println("Seismo Fuerte antes de que compile la red..valor del parametro1: "+pot1.getValue(Integer.valueOf(ind_estado).intValue()));
		//parametro1=pot1.getValue(Integer.valueOf(ind_estado).intValue());
		parametro1=pot1.getValue((calcular_decimal(ind_estado,red1.getNodeList().getNodeString(nombre_parametro, true))));
		System.out.println("parametro 1 con normalizar: "+ parametro1);
		
		red1.compile(0, null,null,null);		
		red1.getCompiledPotentialList();//
		System.out.println("Compilando la red..."+red1.getIsCompiled());
		
		Case uno=new Case(red1,caso_propagar.getEvidence());
		uno.propagate();
		prog1=uno.getProbOfStateNode(p_interes, s_interes);
		uno.showCase();
		System.out.println("Valor de la propagacion 1 de Vestudio: "+ prog1);
		
		
//		Propagacion SEGUNDA con valor de Parametro2
		System.out.println("");
		System.out.println("PARTE DOS");
		relacion2=red2.getRelation(red2.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot2=(PotentialTable)relacion2.getValues();
		System.out.println("parametro 2 sin normalizar: "+ parametro2);
		pot2.print();
		
		pot2.setValue(calcular_decimal(ind_estado,red2.getNodeList().getNodeString(nombre_parametro, true)),parametro2);
		FiniteStates fs2=(FiniteStates)red2.getNodeList().getNodeString(nombre_parametro,true);
		pot2.normalizeOver(fs2);
		
		red2.getRelation(red2.getNodeList().getNodeString(nombre_parametro,true)).print();
		//System.out.println("Seismo Fuerte antes de que compile la red..valor del parametro1: "+pot2.getValue(Integer.valueOf(ind_estado).intValue()));
		//parametro2=pot2.getValue(Integer.valueOf(ind_estado).intValue());
		parametro2=pot2.getValue((calcular_decimal(ind_estado,red2.getNodeList().getNodeString(nombre_parametro, true))));
		System.out.println("parametro 2 con normalizar: "+ parametro2);
		
		red2.compile(0, null,null,null);
		red2.getCompiledPotentialList();
		System.out.println("Compilado red 2..."+red2.getIsCompiled());
		
		Case two= new Case(red2,caso_propagar.getEvidence());
		two.propagate();
		prog2=two.getProbOfStateNode(p_interes, s_interes);
		two.showCase();
		System.out.println("Valor de la propagacion 2 de Vestudio :" + prog2);
				
			
		//Propagacion TERCERA con valor de Parametro3
		System.out.println("");
		System.out.println("PARTE TRES");
		relacion3=red3.getRelation(red3.getNodeList().getNodeString(nombre_parametro, true));
		PotentialTable pot3=(PotentialTable)relacion3.getValues();
		System.out.println("parametro 3 sin normalizar: "+ parametro3);
		pot3.print();
		
		pot3.setValue(calcular_decimal(ind_estado,red3.getNodeList().getNodeString(nombre_parametro, true)),parametro3);
		FiniteStates fs3=(FiniteStates)red3.getNodeList().getNodeString(nombre_parametro,true);
		pot3.normalizeOver(fs3);
		
		red3.getRelation(red3.getNodeList().getNodeString(nombre_parametro,true)).print();
		//System.out.println("Seismo Fuerte antes de que compile la red..valor del parametro3: "+pot3.getValue(Integer.valueOf(ind_estado).intValue()));
		//parametro3=pot3.getValue(Integer.valueOf(ind_estado).intValue());
		parametro3=pot3.getValue((calcular_decimal(ind_estado,red3.getNodeList().getNodeString(nombre_parametro, true))));
		System.out.println("parametro 3 con normalizar: "+ parametro3);
		
		red3.compile(0, null,null,null);
		red3.getCompiledPotentialList();
		System.out.println("Compilado red 3..."+red3.getIsCompiled());
		
		Case three= new Case(red3,caso_propagar.getEvidence());
		three.propagate();
		prog3=three.getProbOfStateNode(p_interes, s_interes);
		three.showCase();
		System.out.println("Valor de la propagacion 3 de Vestudio :" + prog3);		
		
		
		//Calculos de las constantes
		double termino_independiente1=prog1*parametro1;
		double termino_independiente2=prog2*parametro2;
		double termino_independiente3=prog3*parametro3;
		
		//Redondear a 8 decimales que es el máximo soportado por ELVIRA
		
		prog1=roundNum(prog1);
		prog2=roundNum(prog2);
		prog3=roundNum(prog3);
		termino_independiente1=roundNum(termino_independiente1);
		termino_independiente2=roundNum(termino_independiente2);
		termino_independiente3=roundNum(termino_independiente3);
		
				
		matriz[0][0]=parametro1;
		matriz[0][1]=1.0;
		matriz[0][2]=-prog1;
		matriz[0][3]=termino_independiente1;
		matriz[1][0]=parametro2;
		matriz[1][1]=1.0;
		matriz[1][2]=-prog2;
		matriz[1][3]=termino_independiente2;
		matriz[2][0]=parametro3;
		matriz[2][1]=1.0;
		matriz[2][2]=-prog3;
		matriz[2][3]=termino_independiente3;
		
				
		System.out.println("ESTADÍSTICAS");
		System.out.println("Parámetros: ");
		System.out.println("1: "+ matriz[0][0]);
		System.out.println("2: "+ matriz[1][0]);
		System.out.println("3: "+ matriz[2][0]);
		System.out.println("Propagaciones");
		System.out.println("1: "+ matriz[0][2]);
		System.out.println("2: "+ matriz[1][2]);
		System.out.println("3: "+ matriz[2][2]);
		System.out.println("Terminos Independientes");
		System.out.println("1: "+ matriz[0][3]);
		System.out.println("2: "+ matriz[1][3]);
		System.out.println("3: "+ matriz[2][3]);
		
		double [] sol=getSolucion(numero_ecuaciones,matriz);
		
		
		System.out.println("a :"+ sol[0] +" b: "+ sol[1]+ " c: "+ sol[2]);
		
		
		//Propagación Final con el valor del parametro en estudio
		resultado=String.valueOf ((( ( (sol[0]*(parametro_estudio/1000)) + sol[1]) ) / ((parametro_estudio/1000) + sol[2])));
		
		return resultado ;
	}
}	
	/**
	 * 
	 * @param String binario
	 * @param Node nodo
	 * @return integer
	 */
	
	public  int calcular_decimal(String binario,Node nodo){
		NodeList listapadres=nodo.getParentNodes();
		Vector listapadres2= listapadres.getNodes();
		FiniteStates fsnodo=(FiniteStates)nodo;
		Vector variables= new Vector();
		variables.add(nodo);
		int decimal=0;
		for(int o=0;o<listapadres.size();o++){
			variables.add(listapadres2.get(o));
		}
		int dimension=1;
		int numero_var=1+listapadres.size();
		for(int u=0;u<listapadres.size();u++){
			FiniteStates fn=(FiniteStates)listapadres2.get(u);
			dimension=dimension*fn.getNumStates();
		}
		String[]buscar= new String[(fsnodo.getNumStates()*dimension)+1];
		for(int r=0;r<buscar.length;r++){
			buscar[r]="";
		}
		buscar[0]="";
		int lon=0;
		int i=0;
		boolean primero=true;
		do{
			Node aux=(Node)variables.get(0);
			FiniteStates fsaux=(FiniteStates )aux;
			variables.remove(0);
			if(primero){
				lon=(buscar.length-1)/fsaux.getNumStates();
			}else{
				lon=lon/fsaux.getNumStates();
			}
				do{
					for(int l=0;l<fsaux.getNumStates();l++){
						for(int q=0;q<lon;q++){
							buscar[i]=buscar[i]+ String.valueOf(l);
							i++;
						}
					}
				}while(i!=buscar.length-1);	
				numero_var--;
				primero=false;
				i=0;
		}while(numero_var!=0);
				
		
		
		for(int d=0;d<buscar.length;d++){
			if(buscar[d].compareTo(binario)==0){
				decimal=d;
			}
		}
		return decimal;
	}
	//Method to resolve the ecuations
	//throw gauss technique
	public double[] getSolucion(int mNumEcs,double mCoef[][]) { 
		double x;
		double y;
		int j;
		int i;
		int k;

		for(j=0; j<mNumEcs; j++) {
			//Encontramos la primera ecuacion con un coeficiente no cero 
			//en la columna (ecuación) que estemos mirando (j)
			for(i=j; i<mNumEcs; i++){
				if(mCoef[i][j] != 0D) {
					break;
				}
			}
			//(+) Movemos esa ecuación a la primera fila
			for(k=0; k<mNumEcs+1; k++){
				x = mCoef[j][k];
				mCoef[j][k] = mCoef[i][k];
				mCoef[i][k] = x;
			}
			
			//(+) Obtenemos un coeficiente unidad en la primera columna no cero
			y = 1/mCoef[j][j];
			for(k=0; k<mNumEcs+1; k++){
				mCoef[j][k]=y*mCoef[j][k];
			}

			for(i=0; i<mNumEcs; i++){
				y = -mCoef[i][j];
				for(k=0; k<mNumEcs+1; k++){
					if(i==j) break;
					mCoef[i][k]=mCoef[i][k]+y*mCoef[j][k];
				}
			}
		}
		double dRet[] = new double[mNumEcs];
		for(i=0; i<mNumEcs; i++){
			double dRes1 = mCoef[i][mNumEcs]*1000+0.5;
			int iRes = (int)dRes1;
			double dRes2 = iRes/1000D;
			dRet[i] = dRes2;
		}
		
		return dRet;
	}
	//Metodo para truncar y redondear
	public static double roundNum(double num) throws Exception
	{
	double valor = 0;

	valor = num;

	valor = valor*100000;
	valor = java.lang.Math.round(valor);
	valor = valor/100000;

	return valor;

	}


}
