package elvira.learning;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Date;
import java.util.Vector;
import java.io.*;
import elvira.Node;
import elvira.NodeList;
import elvira.potential.PotentialTable;
import elvira.Bnet;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.database.DataBaseCases;
import elvira.potential.*;
import elvira.parser.*;
import elvira.Graph;
import elvira.tools.Chi2;

/**
 * MITMetrics.java
 * This class implements the metric based on cross-entropy between variable x and Pa(x)
 * compensated by the chi-2 value of the independence test 2*N*cross-entropy.
 *
 * Created: January  2005
 *
 * @author Luis M. de Campos
 * @version  1.0
 */

public class MITMetrics extends Metrics {

    Hashtable[] cache;
    double alpha = 0.9999; //default value for the associated independence tests

    /** Constructors methods. **/

    public MITMetrics() {

	setData(null);
    }

    public MITMetrics(DataBaseCases data){
	setData(data);
	cache = new Hashtable[data.getNodeList().size()];
	for(int i=0 ; i< data.getNodeList().size(); i++)
	    cache[i] = new Hashtable();
    }


       public double score (Bnet b){

	NodeList vars,parentsX,varsXPa;
	int i,j;
	double sum = 0.0;
	double valscore;
	FiniteStates nodeX,nodeY;

	vars = b.getNodeList();

	for(i=0; i< vars.size(); i++){
	    nodeX = (FiniteStates) vars.elementAt(i);
	    parentsX = b.parents(nodeX);
	    varsXPa = new NodeList();
	    varsXPa.insertNode(nodeX);
	    varsXPa.join(parentsX);
	    varsXPa = getData().getNodeList().
		      intersectionNames(varsXPa).sortNames(varsXPa);
	    valscore = score(varsXPa);
	    sum+=valscore;
	}
	return sum;
    }


  //this method computes the local score MIT(x,Pa(x))
    public double score (NodeList vars){

        NodeList varsz,vars2z;
  	PotentialTable px,pz,pxz;
  	int i;
 	long degreesOfFreedom,nconfz,j,nz,nxz;
 	double dxz,dz,dx,chiS,chivalue,mutinf,gxPax;
	FiniteStates x,aux;
	int posx,numdatos;
	Configuration confxz,confz;
	int nStatesx,nStatesz,nStatesmax,k;
	double time,timeInitial;
        Date D;
        D = new Date();
        timeInitial = (double)D.getTime();

	numdatos = getData().getNumberOfCases();
        x = (FiniteStates) vars.elementAt(0);
	nStatesx = x.getNumStates();
	confxz = confz = null;
	//System.out.println("Vars: "+vars.toString2());
	vars2z = vars.copy();
	vars2z.removeNode(x);
	//System.out.println("x: "+x.getName()+" Parents: "+vars2z.toString2());
	posx = getData().getNodeList().getId(x);
	//System.out.println("Posicion: "+posx);
	vars2z.sort(getData().getNodeList());
	//System.out.println("Parents Sorted: "+vars2z.toString2());
	//System.out.println("Clave hash: "+vars2z.hashCode());
	Double valor = (Double) cache[posx].get(vars2z.toString2());
	//System.out.println(cache[posx].toString());
	if(valor == null){
		varsz = vars.copy();
		varsz.removeNode(x);
 		if (varsz.size() == 0){
			//System.out.println("Empty parent set");
	    		valor = new Double(0.0);
			cache[posx].put(vars2z.toString2(),valor);
	    		//try{System.in.read();}catch(IOException e){};
	    		return (valor.doubleValue());
			}
		//System.out.println("x: "+x.getName()+" Parents: "+vars2z.toString2());
		nconfz = (long)FiniteStates.getSize(varsz);
		pxz = getData().getPotentialTable(vars);
		//pxz.normalize();
		pz = null;
  		pz = (PotentialTable)pxz.addVariable(x);
		//System.out.print("probability table of the parent set :\n");
		//pz.print();

		px = (PotentialTable)pxz.copy();
		for(i=0; i< varsz.size(); i++){
	    		aux = (FiniteStates) varsz.elementAt(i);
			px = (PotentialTable)px.addVariable(aux);
		}
		dxz = pxz.entropyPotential();
		dxz = dxz/(double)numdatos+Math.log((double)numdatos);
  		dx = px.entropyPotential();
		dx = dx/(double)numdatos+Math.log((double)numdatos);
  		dz = pz.entropyPotential();
		dz = dz/(double)numdatos+Math.log((double)numdatos);
		mutinf = dx+dz-dxz;
  		chiS = ((double)2.0*(double)numdatos*(double)mutinf);
		//degreesOfFreedom = (nconfz-1)*(nStatesx-1);
 		//System.out.println("Grados de Libertad: "+degreesOfFreedom);
  		//if(degreesOfFreedom <= 0) degreesOfFreedom = 1;
  		//chivalue = computeChi2(degreesOfFreedom);
		chivalue = sumChi2(vars);
		//System.out.println("x: "+x.getName()+" Parents: "+vars2z.toString2());
		//System.out.println("mutual information value: "+mutinf+" statistics: "+chiS+" sum of chi values: "+chivalue);
		gxPax = chiS-chivalue;
  		//System.out.println("Degrees of freedom: "+degreesOfFreedom);
  		//System.out.println("statistics = "+chiS);
            D = new Date();
            time = (double)D.getTime();
    	    totalTime+= (time - timeInitial)/1000;
            timeStEval+=(time - timeInitial)/1000;
            totalSt++;
            tStEval++;
            avStNVar+=(varsz.size()+1);
	    valor = new Double(gxPax);
	    //System.out.println("Value to put: "+valor.toString());
	    cache[posx].put(vars2z.toString2(),valor);
	    //try{System.in.read();}catch(IOException e){};
	    return (valor.doubleValue());

	}
	else{
	    //System.out.println("Retrieved value: "+valor.toString());
            D = new Date();
            time = (double)D.getTime();
            totalTime+= (time - timeInitial)/1000;
            totalSt++;
	    // try{System.in.read();}catch(IOException e){};
	//System.out.println("MIT value found in hash ");
	    return (valor.doubleValue());
	}
    }


  //this method computes the sum of chi values using the Chi2.critchi method
    public double sumChi2 (NodeList vars){

        NodeList varsz,vars2z;
  	int i;
 	long df,sizecond;
	FiniteStates x,z,zmax;
	int nStatesx,nStatesz,factor1,stz,max;
	double sum;
	double augmentedalpha;
	int numparents;

	x = (FiniteStates) vars.elementAt(0);
	nStatesx = x.getNumStates();
	vars2z = vars.copy();
	vars2z.removeNode(x);
	varsz = new NodeList();
	zmax = null;
	//alpha = 0.999; //default level of confidence
	numparents = vars2z.size();
	//this is only to make alpha be dependent of the number of parents
	//augmentedalpha = 1.0-(1.0-alpha)/(double)numparents;
	//ranks var in decreasing order of number of states
	while (vars2z.size() != 0) {
		max = 0;
		for (i=0; i< vars2z.size(); i++){
			z = (FiniteStates) vars2z.elementAt(i);
			nStatesz = z.getNumStates();
			if (nStatesz > max)
			  { max = nStatesz;
			    zmax = (FiniteStates)z;
			  }
		}
		varsz.insertNode(zmax);
		vars2z.removeNode(zmax);
	}
	sum = 0.0;
	sizecond = 1;
	factor1 = nStatesx-1;
	for (i=0; i< numparents; i++){
		z = (FiniteStates) varsz.elementAt(i);
		nStatesz = z.getNumStates();
		df = factor1*(nStatesz-1)*sizecond; //este es el bueno: (x-1)*(y-1)*z
		sizecond *= nStatesz;
		//sum += computeChi2(df); //to compute the sum of chi values using another method (this is only valid for values of alpha 0.90, 0.95, 0.975, 0.99, 0.999
	//	sum += Chi2.critchi(1.0-augmentedalpha,(int)df); //use instead of the following line to take into account the number of parents
		sum += Chi2.critchi(1.0-alpha,(int)df); // si no se considera el numero de padres
	}
	return(sum);
 }
    /*
* This method computes the value z such that the probability of a chi-square variable
* with v degrees of freedom being lesser than or equal to z is equal to either 
* 0.90, 0.95, 0.975, 0.99 or 0.999

* For values of v between 1 and 100 the value is exact.
* For v greater than 100 we use the approximation of Wilson-Hilferty
* The value zAlpha[i][0] only indicates the probability (confidence level) being used.
*/
// 0-> 0.90, 1-> 0.95, 2-> 0.975, 3-> 0.99, 4-> 0.999

public double computeChi2(long degreesOfFreedom){

double z;
int dfselected;
double[][] zAlpha;
double[] norm;
zAlpha = new double[5][101];
norm = new double[5];

norm[0] = 1.282;
norm[1] = 1.645;
norm[2] = 1.960;
norm[3] = 2.326;
norm[4] = 3.090;

zAlpha[0][0]=0.90;
zAlpha[0][1]=2.706;
zAlpha[0][2]=4.605;
zAlpha[0][3]=6.251;
zAlpha[0][4]=7.779;
zAlpha[0][5]=9.236;
zAlpha[0][6]=10.645;
zAlpha[0][7]=12.017;
zAlpha[0][8]=13.362;
zAlpha[0][9]=14.684;
zAlpha[0][10]=15.987;
zAlpha[0][11]=17.275;
zAlpha[0][12]=18.549;
zAlpha[0][13]=19.812;
zAlpha[0][14]=21.064;
zAlpha[0][15]=22.307;
zAlpha[0][16]=23.542;
zAlpha[0][17]=24.769;
zAlpha[0][18]=25.989;
zAlpha[0][19]=27.204;
zAlpha[0][20]=28.412;
zAlpha[0][21]=29.615;
zAlpha[0][22]=30.813;
zAlpha[0][23]=32.007;
zAlpha[0][24]=33.196;
zAlpha[0][25]=34.382;
zAlpha[0][26]=35.563;
zAlpha[0][27]=36.741;
zAlpha[0][28]=37.916;
zAlpha[0][29]=39.087;
zAlpha[0][30]=40.256;
zAlpha[0][31]=41.422;
zAlpha[0][32]=42.585;
zAlpha[0][33]=43.745;
zAlpha[0][34]=44.903;
zAlpha[0][35]=46.059;
zAlpha[0][36]=47.212;
zAlpha[0][37]=48.363;
zAlpha[0][38]=49.513;
zAlpha[0][39]=50.660;
zAlpha[0][40]=51.805;
zAlpha[0][41]=52.949;
zAlpha[0][42]=54.090;
zAlpha[0][43]=55.230;
zAlpha[0][44]=56.369;
zAlpha[0][45]=57.505;
zAlpha[0][46]=58.641;
zAlpha[0][47]=59.774;
zAlpha[0][48]=60.907;
zAlpha[0][49]=62.038;
zAlpha[0][50]=63.167;
zAlpha[0][51]=64.295;
zAlpha[0][52]=65.422;
zAlpha[0][53]=66.548;
zAlpha[0][54]=67.673;
zAlpha[0][55]=68.796;
zAlpha[0][56]=69.919;
zAlpha[0][57]=71.040;
zAlpha[0][58]=72.160;
zAlpha[0][59]=73.279;
zAlpha[0][60]=74.397;
zAlpha[0][61]=75.514;
zAlpha[0][62]=76.630;
zAlpha[0][63]=77.745;
zAlpha[0][64]=78.860;
zAlpha[0][65]=79.973;
zAlpha[0][66]=81.085;
zAlpha[0][67]=82.197;
zAlpha[0][68]=83.308;
zAlpha[0][69]=84.418;
zAlpha[0][70]=85.527;
zAlpha[0][71]=86.635;
zAlpha[0][72]=87.743;
zAlpha[0][73]=88.850;
zAlpha[0][74]=89.956;
zAlpha[0][75]=91.061;
zAlpha[0][76]=92.166;
zAlpha[0][77]=93.270;
zAlpha[0][78]=94.374;
zAlpha[0][79]=95.476;
zAlpha[0][80]=96.578;
zAlpha[0][81]=97.680;
zAlpha[0][82]=98.780;
zAlpha[0][83]=99.880;
zAlpha[0][84]=100.980;
zAlpha[0][85]=102.079;
zAlpha[0][86]=103.177;
zAlpha[0][87]=104.275;
zAlpha[0][88]=105.372;
zAlpha[0][89]=106.469;
zAlpha[0][90]=107.565;
zAlpha[0][91]=108.661;
zAlpha[0][92]=109.756;
zAlpha[0][93]=110.850;
zAlpha[0][94]=111.944;
zAlpha[0][95]=113.038;
zAlpha[0][96]=114.131;
zAlpha[0][97]=115.223;
zAlpha[0][98]=116.315;
zAlpha[0][99]=117.407;
zAlpha[0][100]=118.498;

zAlpha[1][0]=0.95;
zAlpha[1][1]=3.841;
zAlpha[1][2]=5.991;
zAlpha[1][3]=7.815;
zAlpha[1][4]=9.488;
zAlpha[1][5]=11.070;
zAlpha[1][6]=12.592;
zAlpha[1][7]=14.067;
zAlpha[1][8]=15.507;
zAlpha[1][9]=16.919;
zAlpha[1][10]=18.307;
zAlpha[1][11]=19.675;
zAlpha[1][12]=21.026;
zAlpha[1][13]=22.362;
zAlpha[1][14]=23.685;
zAlpha[1][15]=24.996;
zAlpha[1][16]=26.296;
zAlpha[1][17]=27.587;
zAlpha[1][18]=28.869;
zAlpha[1][19]=30.144;
zAlpha[1][20]=31.410;
zAlpha[1][21]=32.671;
zAlpha[1][22]=33.924;
zAlpha[1][23]=35.172;
zAlpha[1][24]=36.415;
zAlpha[1][25]=37.652;
zAlpha[1][26]=38.885;
zAlpha[1][27]=40.113;
zAlpha[1][28]=41.337;
zAlpha[1][29]=42.557;
zAlpha[1][30]=43.773;
zAlpha[1][31]=44.985;
zAlpha[1][32]=46.194;
zAlpha[1][33]=47.400;
zAlpha[1][34]=48.602;
zAlpha[1][35]=49.802;
zAlpha[1][36]=50.998;
zAlpha[1][37]=52.192;
zAlpha[1][38]=53.384;
zAlpha[1][39]=54.572;
zAlpha[1][40]=55.758;
zAlpha[1][41]=56.942;
zAlpha[1][42]=58.124;
zAlpha[1][43]=59.304;
zAlpha[1][44]=60.481;
zAlpha[1][45]=61.656;
zAlpha[1][46]=62.830;
zAlpha[1][47]=64.001;
zAlpha[1][48]=65.171;
zAlpha[1][49]=66.339;
zAlpha[1][50]=67.505;
zAlpha[1][51]=68.669;
zAlpha[1][52]=69.832;
zAlpha[1][53]=70.993;
zAlpha[1][54]=72.153;
zAlpha[1][55]=73.311;
zAlpha[1][56]=74.468;
zAlpha[1][57]=75.624;
zAlpha[1][58]=76.778;
zAlpha[1][59]=77.931;
zAlpha[1][60]=79.082;
zAlpha[1][61]=80.232;
zAlpha[1][62]=81.381;
zAlpha[1][63]=82.529;
zAlpha[1][64]=83.675;
zAlpha[1][65]=84.821;
zAlpha[1][66]=85.965;
zAlpha[1][67]=87.108;
zAlpha[1][68]=88.250;
zAlpha[1][69]=89.391;
zAlpha[1][70]=90.531;
zAlpha[1][71]=91.670;
zAlpha[1][72]=92.808;
zAlpha[1][73]=93.945;
zAlpha[1][74]=95.081;
zAlpha[1][75]=96.217;
zAlpha[1][76]=97.351;
zAlpha[1][77]=98.484;
zAlpha[1][78]=99.617;
zAlpha[1][79]=100.749;
zAlpha[1][80]=101.879;
zAlpha[1][81]=103.010;
zAlpha[1][82]=104.139;
zAlpha[1][83]=105.267;
zAlpha[1][84]=106.395;
zAlpha[1][85]=107.522;
zAlpha[1][86]=108.648;
zAlpha[1][87]=109.773;
zAlpha[1][88]=110.898;
zAlpha[1][89]=112.022;
zAlpha[1][90]=113.145;
zAlpha[1][91]=114.268;
zAlpha[1][92]=115.390;
zAlpha[1][93]=116.511;
zAlpha[1][94]=117.632;
zAlpha[1][95]=118.752;
zAlpha[1][96]=119.871;
zAlpha[1][97]=120.990;
zAlpha[1][98]=122.108;
zAlpha[1][99]=123.225;
zAlpha[1][100]=124.342;

zAlpha[2][0]=0.975;
zAlpha[2][1]=5.024;
zAlpha[2][2]=7.378;
zAlpha[2][3]=9.348;
zAlpha[2][4]=11.143;
zAlpha[2][5]=12.833;
zAlpha[2][6]=14.449;
zAlpha[2][7]=16.013;
zAlpha[2][8]=17.535;
zAlpha[2][9]=19.023;
zAlpha[2][10]=20.483;
zAlpha[2][11]=21.920;
zAlpha[2][12]=23.337;
zAlpha[2][13]=24.736;
zAlpha[2][14]=26.119;
zAlpha[2][15]=27.488;
zAlpha[2][16]=28.845;
zAlpha[2][17]=30.191;
zAlpha[2][18]=31.526;
zAlpha[2][19]=32.852;
zAlpha[2][20]=34.170;
zAlpha[2][21]=35.479;
zAlpha[2][22]=36.781;
zAlpha[2][23]=38.076;
zAlpha[2][24]=39.364;
zAlpha[2][25]=40.646;
zAlpha[2][26]=41.923;
zAlpha[2][27]=43.195;
zAlpha[2][28]=44.461;
zAlpha[2][29]=45.722;
zAlpha[2][30]=46.979;
zAlpha[2][31]=48.232;
zAlpha[2][32]=49.480;
zAlpha[2][33]=50.725;
zAlpha[2][34]=51.966;
zAlpha[2][35]=53.203;
zAlpha[2][36]=54.437;
zAlpha[2][37]=55.668;
zAlpha[2][38]=56.896;
zAlpha[2][39]=58.120;
zAlpha[2][40]=59.342;
zAlpha[2][41]=60.561;
zAlpha[2][42]=61.777;
zAlpha[2][43]=62.990;
zAlpha[2][44]=64.201;
zAlpha[2][45]=65.410;
zAlpha[2][46]=66.617;
zAlpha[2][47]=67.821;
zAlpha[2][48]=69.023;
zAlpha[2][49]=70.222;
zAlpha[2][50]=71.420;
zAlpha[2][51]=72.616;
zAlpha[2][52]=73.810;
zAlpha[2][53]=75.002;
zAlpha[2][54]=76.192;
zAlpha[2][55]=77.380;
zAlpha[2][56]=78.567;
zAlpha[2][57]=79.752;
zAlpha[2][58]=80.936;
zAlpha[2][59]=82.117;
zAlpha[2][60]=83.298;
zAlpha[2][61]=84.476;
zAlpha[2][62]=85.654;
zAlpha[2][63]=86.830;
zAlpha[2][64]=88.004;
zAlpha[2][65]=89.177;
zAlpha[2][66]=90.349;
zAlpha[2][67]=91.519;
zAlpha[2][68]=92.689;
zAlpha[2][69]=93.856;
zAlpha[2][70]=95.023;
zAlpha[2][71]=96.189;
zAlpha[2][72]=97.353;
zAlpha[2][73]=98.516;
zAlpha[2][74]=99.678;
zAlpha[2][75]=100.839;
zAlpha[2][76]=101.999;
zAlpha[2][77]=103.158;
zAlpha[2][78]=104.316;
zAlpha[2][79]=105.473;
zAlpha[2][80]=106.629;
zAlpha[2][81]=107.783;
zAlpha[2][82]=108.937;
zAlpha[2][83]=110.090;
zAlpha[2][84]=111.242;
zAlpha[2][85]=112.393;
zAlpha[2][86]=113.544;
zAlpha[2][87]=114.693;
zAlpha[2][88]=115.841;
zAlpha[2][89]=116.989;
zAlpha[2][90]=118.136;
zAlpha[2][91]=119.282;
zAlpha[2][92]=120.427;
zAlpha[2][93]=121.571;
zAlpha[2][94]=122.715;
zAlpha[2][95]=123.858;
zAlpha[2][96]=125.000;
zAlpha[2][97]=126.141;
zAlpha[2][98]=127.282;
zAlpha[2][99]=128.422;
zAlpha[2][100]=129.561;

zAlpha[3][0]=0.99;
zAlpha[3][1]=6.635;
zAlpha[3][2]=9.210;
zAlpha[3][3]=11.345;
zAlpha[3][4]=13.277;
zAlpha[3][5]=15.086;
zAlpha[3][6]=16.812;
zAlpha[3][7]=18.475;
zAlpha[3][8]=20.090;
zAlpha[3][9]=21.666;
zAlpha[3][10]=23.209;
zAlpha[3][11]=24.725;
zAlpha[3][12]=26.217;
zAlpha[3][13]=27.688;
zAlpha[3][14]=29.141;
zAlpha[3][15]=30.578;
zAlpha[3][16]=32.000;
zAlpha[3][17]=33.409;
zAlpha[3][18]=34.805;
zAlpha[3][19]=36.191;
zAlpha[3][20]=37.566;
zAlpha[3][21]=38.932;
zAlpha[3][22]=40.289;
zAlpha[3][23]=41.638;
zAlpha[3][24]=42.980;
zAlpha[3][25]=44.314;
zAlpha[3][26]=45.642;
zAlpha[3][27]=46.963;
zAlpha[3][28]=48.278;
zAlpha[3][29]=49.588;
zAlpha[3][30]=50.892;
zAlpha[3][31]=52.191;
zAlpha[3][32]=53.486;
zAlpha[3][33]=54.776;
zAlpha[3][34]=56.061;
zAlpha[3][35]=57.342;
zAlpha[3][36]=58.619;
zAlpha[3][37]=59.893;
zAlpha[3][38]=61.162;
zAlpha[3][39]=62.428;
zAlpha[3][40]=63.691;
zAlpha[3][41]=64.950;
zAlpha[3][42]=66.206;
zAlpha[3][43]=67.459;
zAlpha[3][44]=68.710;
zAlpha[3][45]=69.957;
zAlpha[3][46]=71.201;
zAlpha[3][47]=72.443;
zAlpha[3][48]=73.683;
zAlpha[3][49]=74.919;
zAlpha[3][50]=76.154;
zAlpha[3][51]=77.386;
zAlpha[3][52]=78.616;
zAlpha[3][53]=79.843;
zAlpha[3][54]=81.069;
zAlpha[3][55]=82.292;
zAlpha[3][56]=83.513;
zAlpha[3][57]=84.733;
zAlpha[3][58]=85.950;
zAlpha[3][59]=87.166;
zAlpha[3][60]=88.379;
zAlpha[3][61]=89.591;
zAlpha[3][62]=90.802;
zAlpha[3][63]=92.010;
zAlpha[3][64]=93.217;
zAlpha[3][65]=94.422;
zAlpha[3][66]=95.626;
zAlpha[3][67]=96.828;
zAlpha[3][68]=98.028;
zAlpha[3][69]=99.228;
zAlpha[3][70]=100.425;
zAlpha[3][71]=101.621;
zAlpha[3][72]=102.816;
zAlpha[3][73]=104.010;
zAlpha[3][74]=105.202;
zAlpha[3][75]=106.393;
zAlpha[3][76]=107.583;
zAlpha[3][77]=108.771;
zAlpha[3][78]=109.958;
zAlpha[3][79]=111.144;
zAlpha[3][80]=112.329;
zAlpha[3][81]=113.512;
zAlpha[3][82]=114.695;
zAlpha[3][83]=115.876;
zAlpha[3][84]=117.057;
zAlpha[3][85]=118.236;
zAlpha[3][86]=119.414;
zAlpha[3][87]=120.591;
zAlpha[3][88]=121.767;
zAlpha[3][89]=122.942;
zAlpha[3][90]=124.116;
zAlpha[3][91]=125.289;
zAlpha[3][92]=126.462;
zAlpha[3][93]=127.633;
zAlpha[3][94]=128.803;
zAlpha[3][95]=129.973;
zAlpha[3][96]=131.141;
zAlpha[3][97]=132.309;
zAlpha[3][98]=133.476;
zAlpha[3][99]=134.642;
zAlpha[3][100]=135.807;

zAlpha[4][0]=0.999;
zAlpha[4][1]=10.828;
zAlpha[4][2]=13.816;
zAlpha[4][3]=16.266;
zAlpha[4][4]=18.467;
zAlpha[4][5]=20.515;
zAlpha[4][6]=22.458;
zAlpha[4][7]=24.322;
zAlpha[4][8]=26.125;
zAlpha[4][9]=27.877;
zAlpha[4][10]=29.588;
zAlpha[4][11]=31.264;
zAlpha[4][12]=32.910;
zAlpha[4][13]=34.528;
zAlpha[4][14]=36.123;
zAlpha[4][15]=37.697;
zAlpha[4][16]=39.252;
zAlpha[4][17]=40.790;
zAlpha[4][18]=42.312;
zAlpha[4][19]=43.820;
zAlpha[4][20]=45.315;
zAlpha[4][21]=46.797;
zAlpha[4][22]=48.268;
zAlpha[4][23]=49.728;
zAlpha[4][24]=51.179;
zAlpha[4][25]=52.620;
zAlpha[4][26]=54.052;
zAlpha[4][27]=55.476;
zAlpha[4][28]=56.892;
zAlpha[4][29]=58.301;
zAlpha[4][30]=59.703;
zAlpha[4][31]=61.098;
zAlpha[4][32]=62.487;
zAlpha[4][33]=63.870;
zAlpha[4][34]=65.247;
zAlpha[4][35]=66.619;
zAlpha[4][36]=67.985;
zAlpha[4][37]=69.347;
zAlpha[4][38]=70.703;
zAlpha[4][39]=72.055;
zAlpha[4][40]=73.402;
zAlpha[4][41]=74.745;
zAlpha[4][42]=76.084;
zAlpha[4][43]=77.419;
zAlpha[4][44]=78.750;
zAlpha[4][45]=80.077;
zAlpha[4][46]=81.400;
zAlpha[4][47]=82.720;
zAlpha[4][48]=84.037;
zAlpha[4][49]=85.351;
zAlpha[4][50]=86.661;
zAlpha[4][51]=87.968;
zAlpha[4][52]=89.272;
zAlpha[4][53]=90.573;
zAlpha[4][54]=91.872;
zAlpha[4][55]=93.168;
zAlpha[4][56]=94.461;
zAlpha[4][57]=95.751;
zAlpha[4][58]=97.039;
zAlpha[4][59]=98.324;
zAlpha[4][60]=99.607;
zAlpha[4][61]=100.888;
zAlpha[4][62]=102.166;
zAlpha[4][63]=103.442;
zAlpha[4][64]=104.716;
zAlpha[4][65]=105.988;
zAlpha[4][66]=107.258;
zAlpha[4][67]=108.526;
zAlpha[4][68]=109.791;
zAlpha[4][69]=111.055;
zAlpha[4][70]=112.317;
zAlpha[4][71]=113.577;
zAlpha[4][72]=114.835;
zAlpha[4][73]=116.092;
zAlpha[4][74]=117.346;
zAlpha[4][75]=118.599;
zAlpha[4][76]=119.850;
zAlpha[4][77]=121.100;
zAlpha[4][78]=122.348;
zAlpha[4][79]=123.594;
zAlpha[4][80]=124.839;
zAlpha[4][81]=126.083;
zAlpha[4][82]=127.324;
zAlpha[4][83]=128.565;
zAlpha[4][84]=129.804;
zAlpha[4][85]=131.041;
zAlpha[4][86]=132.277;
zAlpha[4][87]=133.512;
zAlpha[4][88]=134.746;
zAlpha[4][89]=135.978;
zAlpha[4][90]=137.208;
zAlpha[4][91]=138.438;
zAlpha[4][92]=139.666;
zAlpha[4][93]=140.893;
zAlpha[4][94]=142.119;
zAlpha[4][95]=143.344;
zAlpha[4][96]=144.567;
zAlpha[4][97]=145.789;
zAlpha[4][98]=147.010;
zAlpha[4][99]=148.230;
zAlpha[4][100]=149.449;
dfselected = 4; //default confidence level is 0.999
if (degreesOfFreedom <= 100) return(zAlpha[dfselected][(int)degreesOfFreedom]);
else {
      z = degreesOfFreedom*Math.pow(1-2.0/(9*degreesOfFreedom)+norm[dfselected]*Math.sqrt(2.0/(9*degreesOfFreedom)),3.0);
      return(z);
      }

 }

public double getAlpha(){
  return alpha;
}

public void setAlpha(double alpha) {
  this.alpha = alpha;
}

///////////////////////////////////////////////////
// these two methods are useless, they are included here only for compatibility reasons

public int getTme(){
return 0;
}

public void setTme(int tme) {

}


} //MITMetrics
