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
import io.prometheus.client.GaugeMetricFamily;


public class ThreadPoolMetrics implements MeterBinder {
	
	
	final String jmxDomain = "Tomcat";
	final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	private ThreadPoolExecutor executor;
	
	
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
		registry.gaugeBuilder("tomcat_threads_max_pool_size", executor, (e) -> e.getMaximumPoolSize()).create();
		registry.gaugeBuilder("tomcat_threads_core_pool_size", executor, (e) -> e.getCorePoolSize()).create();
		registry.gaugeBuilder("tomcat_threads_active_count", executor, (e) -> e.getActiveCount()).create();
		registry.gaugeBuilder("tomcat_threads_queue_size", executor, (e) -> e.getQueue().size()).create();
		
		
		
		// TODO Auto-generated method stub
		/*
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName filterName;
		try {
			filterName = new ObjectName(jmxDomain + ":type=ThreadPool,name=*");
		
			Set<ObjectInstance> mBeans = server.queryMBeans(filterName, null);

			if (mBeans.size() > 0) {
            
				List<String> labelList = Collections.singletonList("name");
            
				for (final ObjectInstance mBean : mBeans) {
					List<String> labelValueList = Collections.singletonList(mBean.getObjectName().getKeyProperty("name").replaceAll("[\"\\\\]", ""));
					ThreadPoolBean tpb = new ThreadPoolBean(mBean);
					registry.gaugeBuilder("tomcat_threads_total", tpb, (c) -> c.getCurrentThreadCount()).create();
					
					registry.gaugeBuilder("tomcat_threads_count", tpb, (c) -> c.getBusyThreadCount()).create();
				}
			}
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
}
