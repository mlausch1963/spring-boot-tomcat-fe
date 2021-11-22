package sample.web.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
/* 
 * This class is used as a special purpose executor. See comments in 
 * the class TomcatConfig, why we are doing this.  
 */
@Component
public class CustomizeBoundResourcesExecutor implements MeterBinder, TomcatConnectorCustomizer, RejectedExecutionHandler, ExecutorMetrics  {
    static Logger logger = Logger.getLogger(SampleWebUiApplication.class.getName());
	
    private ThreadPoolExecutor executor;

    @Value("${executor.queue_capacity:100}")
    private int queueCapacity;
    
    @Value("${executor.thread_pool_core_size:10}")
    private int threadCoreSize;
    
    @Value("${executor.thread_pool_max_size:100}")
	private int threadMaxSize;
	
	@Value("${executor_thread_keepalive_millisecs:5000}")
	private int threadKeepAlive;

	@SuppressWarnings("unused")
	private Gauge queueActiveCount;
	@SuppressWarnings("unused")
	private Gauge queueCapacityCount;
	@SuppressWarnings("unused")
	private Gauge threadsActiveCount;
	@SuppressWarnings("unused")
	private Gauge threadPoolCoreCount;
	@SuppressWarnings("unused")
	private Gauge threadPoolMaxCount;

	private Counter threadPoolRejectedCount;
	

	
	//@Override
	public void customize(Connector connector) {
		logger.info("Instrumenting connector with micrometer");
		connector.getExecutorName();
		ProtocolHandler handler = connector.getProtocolHandler();
		executor = (ThreadPoolExecutor) handler.getExecutor();
		if (executor == null) {
			BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(queueCapacity);
			executor = new ThreadPoolExecutor(threadCoreSize, threadMaxSize, threadKeepAlive, TimeUnit.MILLISECONDS, q, this);
			handler.setExecutor(executor);
		}
	}
	
	public double getActiveCount() {
		return (double)this.executor.getActiveCount();
	}

	public double getCoreCount() {
		return (double)this.executor.getCorePoolSize();
	}
	
	public double getMaxCount() {
		return (double)this.executor.getMaximumPoolSize();
	}
	
	public double getQueueLength() {
		return (double) executor.getQueue().size();
	}
	
	public double getQueueCapacity() {
		return (double) queueCapacity;
	}
	
	public double getRejectedCount() {
		return threadPoolRejectedCount.count();
	}
	
	@Override
	public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
		threadPoolRejectedCount.increment();
		logger.warning("Incoming http request rejected.");
	}
	@Override
	public void bindTo(MeterRegistry registry) {
		
		queueActiveCount = Gauge.builder("executor_queue_active_count", this, CustomizeBoundResourcesExecutor::getQueueLength)
				.baseUnit("count")
				.description("currently busy queue entry")
				.register(registry);

		queueCapacityCount = Gauge.builder("executor_queue_capacity_count", this, CustomizeBoundResourcesExecutor::getQueueCapacity)
				.baseUnit("count")
				.description("capacity of queue")
				.register(registry);
		
		threadsActiveCount = Gauge.builder("executor_threads_active_count", this, CustomizeBoundResourcesExecutor::getActiveCount)
                .baseUnit("count")
                .description("currently busy threads")
                .register(registry);
		
		threadPoolCoreCount = Gauge.builder("executor_threads_core_count", this, CustomizeBoundResourcesExecutor::getCoreCount)
                .baseUnit("count")
                .description("threads pool core size")
                .register(registry);
		
		threadPoolMaxCount = Gauge.builder("tomcat_threads_max_count", this, CustomizeBoundResourcesExecutor::getMaxCount)
                .baseUnit("count")
                .description("threads pool max size")
                .register(registry);
		
		threadPoolRejectedCount = Counter.builder("tomcat_threads_rejected_count")
                .baseUnit("count")
                .description("incoming requests rejected")
                .register(registry);
	}
}
