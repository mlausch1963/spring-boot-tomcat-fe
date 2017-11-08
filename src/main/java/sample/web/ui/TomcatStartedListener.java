package sample.web.ui;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
@ConditionalOnClass(TomcatEmbeddedWebappClassLoader.class)
public class TomcatStartedListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	@Autowired
	private MeterRegistry registry;

	@Autowired
    private TomcatThreadPoolMetrics metrics;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {

		TomcatEmbeddedServletContainer tomcat = (TomcatEmbeddedServletContainer) event.getSource();
		Executor e = tomcat.getTomcat().getConnector().getProtocolHandler().getExecutor();
		ThreadPoolExecutor tpe = (ThreadPoolExecutor)e;
		
		if (tomcat.getTomcat().getService().getDomain().equals("Tomcat")){
            //metrics = new TomcatThreadPoolMetrics(tpe);
            metrics.setExecutor(tpe);
            // This is done by spring boot autoconf, before we even arrive here.
			//metrics.bindTo(registry);
		}
	}
}

