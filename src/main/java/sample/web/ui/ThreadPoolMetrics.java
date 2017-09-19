package sample.web.ui;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.springframework.beans.factory.annotation.Autowired;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.prometheus.client.GaugeMetricFamily;


public class ThreadPoolMetrics implements MeterBinder {

	private class ConstOne{
		public long value() { return 1;}
	};

	final String jmxDomain = "Tomcat";
	final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	private ThreadPoolExecutor executor;
	private ConstOne one = new ConstOne();


	private class ThreadPoolBean {
		ObjectInstance bean;
		
		ThreadPoolBean(ObjectInstance bean) {
			this.bean = bean;
		}

		public long getBusyThreadCount() {
			Integer v = -1;
			try {
				v = (Integer)server.getAttribute(bean.getObjectName(), "currentThreadsBusy");
			} catch (InstanceNotFoundException | AttributeNotFoundException | ReflectionException | MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("currentThreadsBusy: v = " + v.toString());
			return v;
		}
		
		public long getCurrentThreadCount() {
			Integer v = -1;
			try {
				v = (Integer)server.getAttribute(bean.getObjectName(), "currentThreadCount");
				System.out.println("getCurrentThreadCount: v = " + v.toString());
				
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return v;
		}
	}
	
	public ThreadPoolMetrics(ThreadPoolExecutor tpe) {
		this.executor = tpe;
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		Gauge.builder("tomcat_threads_max_pool_size", executor, (e) -> e.getMaximumPoolSize()).register(registry);
		Gauge.builder("tomcat_threads_core_pool_size", executor, (e) -> e.getCorePoolSize()).register(registry);
		Gauge.builder("tomcat_threads_active_count", executor, (e) -> e.getActiveCount()).register(registry);
		Gauge.builder("tomcat_threads_queue_size", executor, (e) -> e.getQueue().size()).register(registry);

		Gauge.builder("version", one, one-> one.value())
				.tags("branch", "rel-1.0.0")
				.register(registry);
	}
}
