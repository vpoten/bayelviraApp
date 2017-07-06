package elvira.probabilisticDecisionGraph.tools;

import java.util.*;

public class VectorOps {

	/** Randomly generates a distribution for a discrete variable with the given number of states (see Caprile 1999).
	 * @param states - the number of states the distribution should be defined over
	 * @return - the distribution drawn from the uniform distribution over all distributions
	 */
	public static double[] randomDiscreteDistribution(int states, Random rnd) {
		double[] result=new double[states];
		double r=1.0;
		double power=1.0/(states-1);
		for (int i=0; i<states-1; i++) {
			result[i]=r*(1.0-Math.pow(1.0-rnd.nextDouble(),power));
			r-=result[i];
		}
		result[states-1]=r;
		return result;
	}
	
	public static double[] getNewDoubleArray(int size, double val){
		double[] retval = new double[size];
		for(int i=0;i<size;i++) retval[i] = val;
		return retval;
	}
	
	public static String doubleVectorToString(Vector<Double> v){
			String retval = "[";
			for(Double d : v){
				retval += d+" ";
			}
			return retval;
	}
	
	public static String doubleArrayToString(double[] v){
		String retval = "[";
		for(int i=0;i<v.length;i++){
			retval+=v[i]+" ";
		}
		return retval+"]";
	}
	
	public static String doubleArrayToSimpleString(double[] v){
		String retval = "";
		for(int i=0;i<v.length;i++) retval += v[i]+" ";
		return retval;
	}
	
	public static void printDoubleVector(Vector v){
		System.out.print("[");
		for(Enumeration e = v.elements(); e.hasMoreElements(); )
			System.out.print(e.nextElement()+" ");
		System.out.println("]");
	}

	public final static double[] getNormalisedUniformDoubleArray(int size){
		if(size < 0){
			System.out.println("can not create array of negative size (in getNormalisedDoubleArray("+size+"))");
			System.exit(1);
		}
		double[] retval = new double[size];
		for(int i=0;i<size;i++) retval[i] = 1/size;
		return retval;
	}

//	public final static Vector<Attribute> attributeEnumerationToVector(Enumeration<Attribute> theEnum){
//		Vector<Attribute> retval = new Vector<Attribute>();
//		while(theEnum.hasMoreElements())
//			retval.add(theEnum.nextElement());
//		return retval;
//	}

	public final static String intArrayToString(int[] ia){
		String str = "";
		for(int i : ia){
			str += i+" ";
		}
		return "["+str+"]";
	}
	
	public final static double sum(final double[] p){
		double sum = 0.0;
		for(int i=0;i<p.length;i++) sum += p[i];
		return sum;
	}
	
//	public static void main(String[] args){
//		for(int i=2;i< Integer.parseInt(args[0]); i++){
//			System.out.println(StringOps.arrayToString(randomDiscreteDistribution(i)));
//		}
//	}
	
	public static int arrayContains(Object[] a, Object e){
		for(int i=0;i<a.length;i++){
			if(a[i].equals(e)) return i;
		}
		return -1;
	}
	
	public static final int getIndexOfMaxValue(Double[] da){
		int idx = 0;
		for(int i=1;i<da.length;i++) if(da[i] > da[idx]) idx = i;
		return idx;
	}
	
	public static final int getIndexOfMaxValue(double[] da){
		int idx=0;
		for(int i = 1;i<da.length;i++){
			if(da[i]>da[idx]) idx = i;
		}
		return idx;
	}
	
	public static final double[] copyAndNormalise(final double[] a){
		double retval[] = new double[a.length];
		double s=sum(a);
		if(s!=0){
			for(int i=0;i<a.length;i++){
				retval[i] = a[i] / s;
			}
		} else {
			retval = getNormalisedUniformDoubleArray(retval.length);
		}
		return retval;
	}
	
	/**
	 * This method normalises an array of double values.
	 * 
	 * @param a - the array to be normalised.
	 */
	public static final void normalise(double[] a){
		double s = sum(a);
		if(s!=0.0){
			for(int i=0;i<a.length;i++)
				a[i] = a[i] / s;
		} else {
			for(int i=0;i<a.length;i++){
				a[i] = 1.0/a.length;
			}
		}
	}
	
	public static final double[] getWeightedAverageArray(double[] a1, double w1, double[] a2, double w2) throws VectorOpsException{
		if(a1.length != a2.length){
			throw new VectorOpsException("can not construct weighted average of histograms of different arity.");
		}
		double[] avg = new double[a1.length];
		for(int i=0;i<a1.length;i++)
			avg[i] = a1[i]*w1 + a2[i]*w2;
		VectorOps.normalise(avg);
		return avg;
	} 

	public static final double sampleMean(double[] counts){
		return sum(counts) / counts.length;
	}
	
	public static final double sumOfSquaredErrors(double[] counts){
		return sumOfSquaredErrors(counts, sampleMean(counts));
	}
	
	public static final double sumOfSquaredErrors(double[] counts, double mean){
		double sse = 0.0;
		for(double x : counts) sse += Math.pow((x - mean), 2);
		return sse;
	}
	
	public static final double sampleVariance(double[] counts){
		return sampleVariance(counts, sampleMean(counts));
	}
	
	public static final double sampleVariance(double[] counts, double mean){
		double sse = sumOfSquaredErrors(counts, mean);
		return (counts.length > 1 ? (sse / counts.length) : 0.0);
	}
	
	public static final double KLDivergence(double[] p, double[] q){
		double kl = 0.0;
		for(int i=0;i<p.length;i++){
			if(p[i] == 0.0) continue;
			if(q[i] == 0.0){ kl = Double.POSITIVE_INFINITY; break;}
			kl += p[i]*MathUtils.log2(p[i] / q[i]);
		}
		return kl;
	}
	
	public static final void printMeanVarSD(double[] c, boolean horizontal){
		double mean = sampleMean(c);
		double var = sampleVariance(c,mean);
		double sd = Math.sqrt(var);
		if(!horizontal){
			System.out.print("mean     : "+mean+"\n" +
					"variance : "+var+"\n" +
					"std.dev  : "+sd+"\n");
		} else {
			System.out.print(mean+"\t"+var+"\t"+sd);
		}
	}

	/**
	 * This method looks for duplicates in a Vector. If all elements in the argument
	 * Vector refers to different Objects, this method returns false. Otherwise, it returns true.
	 * 
	 * @param v - the Vector to inspect for duplicate elements.
	 * @return true if different indexes contains references to the same Object and false otherwise.
	 */
	public static boolean containsDuplicates(Vector v){
		int i;
		for(Object o : v){
			i = v.indexOf(o);
			if(v.indexOf(o, i+1) != -1){
				return true;
			}
		}
		return false;
	}
	
/*	public static void vectorIntersection(Vector<? extends Object> v1, Vector<? extends Object> elementsToSubtract){
		Vector<Object> toRemove = new Vector<Object>();
		for(Object element : v1){
			if(!elementsToSubtract.contains(element)) toRemove.add(element);
		}
		v1.removeAll(toRemove);
	}*/
}
