package sample.web.ui;

import java.util.logging.Logger;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Create a customizer to use a new special purpose Executor. This executor serves two purposes.
 *
 * 1. It limits the length of the incoming queue. Unbounded incoming queue lead to unbounded
 *    latencies, which can bring a system down. For the overall performance it's much better to 
 *    reject incoming requests, if there is no capacity. In connection with an implementation of
 *    request lifetime and request cancellation, this prevents system from overload and erratic, 
 *    undefined behaviour: https://thenewstack.io/an-introduction-to-queue-theory-why-disaster-happens-at-the-edges/
 *    
 * 2. Normally the metrics about thread pool utilization and queue length are only available 
 *    via JMX beams. JMX may impose a performance hit and therefore it might be disabled, 
 *    deleting thiose metrics. But they are important to find out if there is an overload
 *    situation and more important as an input to the readyness K8s probe, which will redirect
 *    traffic to the pod to another pod. If a pod returns != 200 for the readyness probe,
 *    as soon as all threads in the threadpool are busy, overflowing a pod will be prevented. 
 *     
 */

@Component
public class TomcatConfig implements 
  WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    static Logger logger = Logger.getLogger(SampleWebUiApplication.class.getName());

	
	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		// TODO Auto-generated method stub
    	TomcatConnectorCustomizer c = new CustomizeBoundResourcesExecutor();
    	factory.addConnectorCustomizers(c);
	}
}
