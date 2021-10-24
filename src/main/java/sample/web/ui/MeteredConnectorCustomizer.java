package sample.web.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Component
public class MeteredConnectorCustomizer implements TomcatConnectorCustomizer, MeterBinder  {
    static Logger logger = Logger.getLogger(SampleWebUiApplication.class.getName());
	
    private Gauge   threads_active_count;
    private Gauge   queue_active_count;
    private ThreadPoolExecutor executor;
    
	//@Override
	public void customize(Connector connector) {
		// TODO Auto-generated method stub
		ProtocolHandler handler = connector.getProtocolHandler();
		this.executor = (ThreadPoolExecutor) handler.getExecutor();
		if (this.executor == null) {
			BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(77);
			this.executor = new ThreadPoolExecutor(11, 23, 1, TimeUnit.SECONDS, q);
			handler.setExecutor(this.executor);
			logger.info("Customize connector to use custom executor");
		}
	}

	private double getActiveThreads() {
		return (double)this.executor.getActiveCount();
		
	}
	private double getActiveQueue() {
		return (double) this.executor.getQueue().size();
	}
	@Override
	public void bindTo(MeterRegistry registry) {
		// TODO Auto-generated method stub
		
		queue_active_count = Gauge.builder("AAAA_queue_active", this, MeteredConnectorCustomizer::getActiveQueue)
				.baseUnit("count")
				.description("currently busy queue entry")
				.register(registry);
		
		threads_active_count = Gauge.builder("AAAAA_threads_active", this, MeteredConnectorCustomizer::getActiveThreads)
                .baseUnit("count")
                .description("currently busy threads")
                .register(registry);

 
	}
}
