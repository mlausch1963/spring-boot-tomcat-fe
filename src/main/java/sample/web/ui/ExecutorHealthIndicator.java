package sample.web.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ExecutorHealthIndicator implements HealthIndicator {

    static private Logger logger = Logger.getLogger(HealthIndicator.class.getName());

    @Autowired
    private ExecutorMetrics metrics;
    @Value("${health.executor.queue_margin_percent:10}")
    private int queueMarginPercent;

    @Value("${health.executor.threads_margin_percent:10}")
    private int threadsMarginPercent;
    
    @Override
    public Health health() {
        Health.Builder status = Health.up();
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            status = Health.down(); //status("WARNING");
        }
        
        return status
        		.withDetail("Error Code", errorCode)
        		.withDetail("MaxQueueLength", metrics.getQueueCapacity())
        		.withDetail("CurQueueLength", metrics.getQueueLength())
        		.withDetail("MaxThreadCount", metrics.getMaxCount())
        		.withDetail("CurThreadCount", metrics.getActiveCount())
        		.build();
    }

    private int check() {
    	var maxEntries = metrics.getQueueCapacity();
    	var maxThreads = metrics.getMaxCount();
    	
    	maxEntries = maxEntries - ((maxEntries/100) * queueMarginPercent);
    	maxThreads = maxThreads - ((maxThreads/100) * threadsMarginPercent);
    	logger.info("health: checking margins: maxEntries = " + maxEntries + ", maxThreads = " + maxThreads);
        if (metrics.getActiveCount() > maxThreads) {
    		logger.info("Threads count exceeded");	
        	if (metrics.getQueueLength() > maxEntries) {
        		logger.info("Queue length exceeded");	
        		return -1;
            }
        }
        return 0;
    }
}
