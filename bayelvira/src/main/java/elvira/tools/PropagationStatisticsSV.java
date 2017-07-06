package elvira.tools;

/*
 * Created on 25-jun-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author Manolo
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PropagationStatisticsSV extends PropagationStatistics{

	//Standard deviation of the times of the evaluation of a diagram for
	//a lot of times
	double deviationTimes;
	
	double maximumTotalSize;
	
	double averageTime;
	
	double minimumTime;
	
	double maximumTime;
	

	/**
	 * @return
	 */
	public double getDeviationTimes() {
		return deviationTimes;
	}

	/**
	 * @param d
	 */
	public void setDeviationTimes(double d) {
		deviationTimes = d;
	}

	/**
	 * @return
	 */
	public double getMaximumTotalSize() {
		return maximumTotalSize;
	}

	/**
	 * @param d
	 */
	public void setMaximumTotalSize(double d) {
		maximumTotalSize = d;
	}

	/**
	 * @return Returns the averageTime.
	 */
	public double getAverageTime() {
		return averageTime;
	}
	/**
	 * @param averageTime The averageTime to set.
	 */
	public void setAverageTime(double averageTime) {
		this.averageTime = averageTime;
	}
	/**
	 * @return Returns the maximumTime.
	 */
	public double getMaximumTime() {
		return maximumTime;
	}
	/**
	 * @param maximumTime The maximumTime to set.
	 */
	public void setMaximumTime(double maximumTime) {
		this.maximumTime = maximumTime;
	}
	/**
	 * @return Returns the minimumTime.
	 */
	public double getMinimumTime() {
		return minimumTime;
	}
	/**
	 * @param minimumTime The minimumTime to set.
	 */
	public void setMinimumTime(double minimumTime) {
		this.minimumTime = minimumTime;
	}
}
