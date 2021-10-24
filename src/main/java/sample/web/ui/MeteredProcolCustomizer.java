package sample.web.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class MeteredProcolCustomizer implements  TomcatProtocolHandlerCustomizer, MeterBinder {
    static Logger logger = Logger.getLogger(SampleWebUiApplication.class.getName());

	@Override
	public void customize(ProtocolHandler protocolHandler) {
		// TODO Auto-generated method stub
		logger.info("XXXXXXXXXXXXXXXXXXX ph customizer called");	
		var x = protocolHandler.getId();
		logger.info("XXXXXXXXXXXXXXXXXXX ph id = " + x);
		var e = protocolHandler.getExecutor();
		logger.info("XXXXXXXXXXXXXXXXXXX executor = " + e);	
		BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(27);
		e = new ThreadPoolExecutor(9, 17, 1, TimeUnit.SECONDS, q);
		protocolHandler.setExecutor(e);
		logger.info("XXXXXXXXXXXXXXXX New executor: core size = 11, max sze = 23, class = " + protocolHandler.getClass());
	}
	
	@Override
	public void bindTo(MeterRegistry registry) {
		logger.info("XXXXXXXXXXXXXX bind to called, registry = " + registry);
	}
}