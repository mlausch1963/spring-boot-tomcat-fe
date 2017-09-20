package sample.web.ui;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class TomcatStartedListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	@Autowired
	private MeterRegistry registry;

	@Autowired
	private ThreadPoolMetrics metrics;
	
	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {

		TomcatEmbeddedServletContainer tomcat = (TomcatEmbeddedServletContainer) event.getSource();
		Executor e = tomcat.getTomcat().getConnector().getProtocolHandler().getExecutor();
		ThreadPoolExecutor tpe = (ThreadPoolExecutor)e;
		
		if (tomcat.getTomcat().getService().getDomain().equals("Tomcat")){
			//metrics = new ThreadPoolMetrics(tpe);
			metrics.setExecutor(tpe);
			// This is done by spring boot autoconf, before we even arrive here.
			//metrics.bindTo(registry);
		}
	}
}

