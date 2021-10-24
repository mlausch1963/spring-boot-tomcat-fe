package sample.web.ui;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

import org.springframework.stereotype.Component;

/**
 * @author Michael Lausch
 *
 * A sample class to hold various statistics with labels.
 *
 */

@Component
public class VersionInfo {
	
	Gauge version = Gauge.builder("version", 1, Number::doubleValue)
			.description("Version information")
			.tags("branch", "rel-1.1.0", "app", "fe").
			register(Metrics.globalRegistry);
}
