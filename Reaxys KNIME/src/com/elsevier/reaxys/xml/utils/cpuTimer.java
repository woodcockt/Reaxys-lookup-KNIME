package com.elsevier.reaxys.xml.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class cpuTimer {
	
	private long start = 0;
	private long cpuTime = 0;
	private long userTime = 0;
	private long systemTime = 0;
	private ThreadMXBean bean = null;
	final public double MULTIPLIER = 1.0e-9; // divisor to get seconds

	
	public static void main(String[] arg) {
		cpuTimer t = new cpuTimer();
		System.out.println(t.toString());
		
		for (long i = 0; i < 10000000000L; i++) {
			double a = Math.sqrt(Math.PI * Math.E);
			a = Math.sqrt(a);
		}
		
		System.out.println(t.toString());
		t.reset();
		System.out.println(t.toString());
		
	}
	public cpuTimer() {
		start = System.nanoTime();
		bean = ManagementFactory.getThreadMXBean( );
	}
	
	/**
	 * convert a value to seconds
	 * @param nanos - time in nanoseconds
	 * @return time in seconds
	 * 
	 */
	public double toSeconds(long nanos) {
		return nanos * MULTIPLIER;
	}
	
	/**
	 * reset values so that they start counting from now for all times
	 * 
	 */
	public void reset() {
		start = System.nanoTime();
		cpuTime += getCpuTime();
		userTime += getUserTime();
		systemTime += getSystemTime();
	}
	
	
	/**
	 * get elapsed "wall clock" time
	 * @return time from last reset in nanoseconds
	 */
	public long getElapsedTime() {
		return System.nanoTime() - start;
	}
	
	/**
	 * get the cpu time used in this thread since the last reset.
	 * 
	 * @return cpu time in nanoseconds
	 */
	public long getCpuTime() {
	    return bean.isThreadCpuTimeSupported() && bean.isThreadCpuTimeEnabled() ?
	        bean.getCurrentThreadCpuTime( ) - cpuTime :  0L;
	}
	 
	/**
	 * get the user time used in this thread since th elast reset.
	 * 
	 * @return user time in nanoseconds
	 */
	public long getUserTime() {
	    return bean.isThreadCpuTimeSupported() && bean.isThreadCpuTimeEnabled() ?
	        bean.getCurrentThreadUserTime( ) - userTime: 0L;
	}
	
	
	/**
	 * get the system time used in this thread since the last reset.
	 * 
	 * @return system time in nanoseconds
	 */
	public long getSystemTime() {
	    return bean.isThreadCpuTimeSupported() && bean.isThreadCpuTimeEnabled() ?
	        (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime() - systemTime) : 0L;
	} 

	public String elapsedSeconds() {
		return String.format("%5.2f", toSeconds(getElapsedTime()));
	}
	
	/**
	 * return a summary of the cpu and elapsed time, and %cpu time based on those values
	 * @return string with this information
	 */
	public String toString() {
		return String.format("elapsed: %3.2f  user: %3.2f system: %3.2f  %3.2f%%", toSeconds(getElapsedTime()),
				toSeconds(getUserTime()), toSeconds(getSystemTime()), 
				Math.min(100.0,toSeconds(getCpuTime())/toSeconds(getElapsedTime() + 1)*100.0));
	}
}
