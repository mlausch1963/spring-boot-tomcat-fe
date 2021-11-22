package sample.web.ui;


public interface ExecutorMetrics {
	
	public double getMaxCount();


    public double getActiveCount();
    public double getCoreCount();
    public double getQueueLength();
    public double getQueueCapacity();
}