package sample.web.ui;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public interface ExecutorMetrics extends RejectedExecutionHandler {
    void setExecutor(ThreadPoolExecutor executor);

    Double getMaximumPoolSize();

    Double getCorePoolSize();

    Double getActiveCount();

    Double getQueueLength();

    void bindTo(MeterRegistry registry);

    void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor);
}
