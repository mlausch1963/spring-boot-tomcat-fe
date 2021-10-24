package sample.web.ui;

import java.util.logging.Logger;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
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
    	TomcatConnectorCustomizer c = new MeteredConnectorCustomizer();
    	factory.addConnectorCustomizers(c);
	}
}
