package sample.web.ui;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import io.micrometer.core.instrument.Counter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.Gauge;


public class TomcatThreadPoolMetrics implements MeterBinder, ExecutorMetrics {

    static Logger logger = Logger.getLogger("ThreadPoolMetric");
    private MeterRegistry registry;
    private ThreadPoolExecutor executor;

    private Counter rejectCount;


    public TomcatThreadPoolMetrics() {

    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        this.executor.setRejectedExecutionHandler(this);
        this.executor.setCorePoolSize(5);
        this.executor.setMaximumPoolSize(200);
    }

    // We need this getters also for the healthz checks.
    public Double getMaximumPoolSize() {
        if (executor != null) {
            return (double) executor.getMaximumPoolSize();
        }
        return -1.0;
    }

    public Double getCorePoolSize() {
        if (executor != null) {
            return (double) executor.getCorePoolSize();
        }
        return -1.0;
    }

    public Double getActiveCount() {
        if (executor != null) {
            return (double) executor.getActiveCount();
        }
        return -1.0;
    }

    public Double getQueueLength() {
        if (executor != null) {
            return (double) executor.getQueue().size();
        }
        return -1.0;
    }

	@Override
    public void bindTo(MeterRegistry registry) {
        Gauge maxPoolSize = Gauge.builder("tomcat_threads_max_pool_size", this, TomcatThreadPoolMetrics::getMaximumPoolSize)
                .baseUnit("count")
                .description("The maximum allowed number of threads.")
                .register(registry);

        Gauge corePoolSize = Gauge.builder("tomcat_threads_core_pool_size", this, TomcatThreadPoolMetrics::getCorePoolSize)
                .baseUnit("count")
                .description("The number of threads to keep in the pool, even if they are idle.")
                .register(registry);

        Gauge activeCount = Gauge.builder("tomcat_threads_active_count", this, TomcatThreadPoolMetrics::getActiveCount)
                .baseUnit("count")
                .description("The approximate number of threads that are actively executing tasks.")
                .register(registry);

        Gauge queueSize = Gauge.builder("tomcat_threads_queue_size", this, TomcatThreadPoolMetrics::getQueueLength)
                .baseUnit("count")
                .description("The number of tasks queued, because no thread available")
                .register(registry);

        Gauge version = Gauge.builder("version", 1, Number::doubleValue)
                .tags("branch", "rel-1.0.0")
                .register(registry);

        rejectCount = Counter.builder("tomcat_threads_rejected")
                .baseUnit("count")
                .description("The number of thread executions dropped")
                .register(registry);
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        rejectCount.increment();
    }
}
