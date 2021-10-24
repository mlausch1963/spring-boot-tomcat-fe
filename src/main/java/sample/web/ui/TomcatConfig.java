package sample.web.ui;

import java.util.logging.Logger;

import org.apache.catalina.LifecycleListener;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;



@Component
public class TomcatConfig implements 
  WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    static Logger logger = Logger.getLogger(SampleWebUiApplication.class.getName());

	
	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		// TODO Auto-generated method stub
    	TomcatProtocolHandlerCustomizer<Http11NioProtocol> cp = new MeteredProcolCustomizer();    	
    	TomcatConnectorCustomizer c = new MeteredConnectorCustomizer();
    	LifecycleListener ctx = new MeteredLifecycleListener();
    			
    	factory.addConnectorCustomizers(c);
    	logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAA  customizer added");
    	logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAA  connector customers are" + factory.getTomcatConnectorCustomizers());


	}
}
