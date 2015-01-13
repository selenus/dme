package de.dariah.schereg.util;

/**
 * @author Tobias Gradl
 * @created 12.12.2011 11:25:48
 * @version 1
 *
 */
public class StopWatch {
	private long start = 0;
	private long stop = 0;
	private boolean running = false;

	public void start() {
		this.start = System.currentTimeMillis();
		this.running = true;
	}

	public void reset() {
		this.start = System.currentTimeMillis();
	}
	
	public void stop() {
		this.stop = System.currentTimeMillis();
		this.running = false;
	}

	public long getElapsedTime() {
		if (running) {
			return (System.currentTimeMillis() - start);
		} else {
			return (stop - start);
		}
	}

	public long getElapsedTimeSecs() {
		return getElapsedTime() / 1000;
	}
}
