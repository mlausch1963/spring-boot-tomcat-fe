package sample.web.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ThreadPoolHealthIndicator implements HealthIndicator {

    @Autowired
    private ThreadPoolMetrics metrics;

    @Override
    public Health health() {
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.status("OVERLOAD")
                    .withDetail("Error Code", errorCode)
                    .withDetail("QueueLength", metrics.getQueueLength())
                    .withDetail("ActiveCount", metrics.getActiveCount())
                    .withDetail("MaximumPoolSize", metrics.getMaximumPoolSize())
                    .build();
        }
        return Health.up().build();
    }

    private int check() {
        if (metrics.getQueueLength() > 50) {
            return -2;
        }
        if (metrics.getActiveCount() > ((metrics.getMaximumPoolSize() - 10))) {
            return -1;
        }
        return 0;
    }
}
