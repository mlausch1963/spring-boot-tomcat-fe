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
//EmbeddedServletContainerInitializedEvent
//ContextRefreshedEvent
	@Autowired
	MeterRegistry registry;
	ThreadPoolMetrics metrics;
	
	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		// TODO Auto-generated method stub
		
		TomcatEmbeddedServletContainer tomcat = (TomcatEmbeddedServletContainer) event.getSource();
		System.out.println("Tomcat.service.name = " + tomcat.getTomcat().getService().getName());
		Executor e = tomcat.getTomcat().getConnector().getProtocolHandler().getExecutor();
		Tomcat t = tomcat.getTomcat();
		String domain = t.getService().getDomain();
		ThreadPoolExecutor tpe = (ThreadPoolExecutor)e;
		
		if (tomcat.getTomcat().getService().getDomain().equals("Tomcat")){
			System.out.println("HAHA");
			metrics = new ThreadPoolMetrics(tpe);
			metrics.bindTo(registry);
		}
		else {
			System.out.println("Not Yet");
		}
	}

}

