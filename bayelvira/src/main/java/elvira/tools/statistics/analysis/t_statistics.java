/*
 * t_statistics.java
 *
 * Created on 9 de junio de 2004, 11:28
 */

package elvira.tools.statistics.analysis;

/**
 *
 * @author  andrew
 */
public class t_statistics {
    double [] samp1;
    int sample1;
    double [] samp2;
    int sample2;
    double t;
    /** Creates a new instance of t_statistics */
    public t_statistics(double [] samp1_, int sample1_, double [] samp2_, int sample2_) {
        samp1=samp1_;
        sample1=sample1_;
        samp2=samp2_;
        sample2=sample2_;
    }

    /**
     * Return the t-satistics for the two observations set.
     */
    public double calc_t() {	
		
		// Stand alone function to return t when given two arrays and their lengths
		
		double sum=0, sumsqu=0, ave=0;
		double ave1, ave2;
		double nh;
		double ssq1, ssq2;
		double MSE;
			

		for (int i=0;i<sample1;i++) {
			sum += samp1[i];	
			sumsqu += samp1[i]*samp1[i];
		}	
		ave1 = sum/sample1;
		ssq1 = sumsqu - sum*sum/sample1;
						
		sum=0; sumsqu=0; 
		for (int i=0;i<sample2;i++) {
			sum += samp2[i];	
			sumsqu += samp2[i]*samp2[i];
		}		
		ave2 = sum/sample2;
		ssq2 = sumsqu - sum*sum/sample2;	

		MSE = (ssq1 + ssq2)/(sample1+sample2-2);	
		nh = 2/(1/(double)sample1+1/(double)sample2);
		t = (ave1-ave2)/Math.sqrt(2*MSE/nh);		
	
		return t;
        }    
}
