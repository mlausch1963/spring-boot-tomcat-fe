package sample.web.ui;

import com.fasterxml.jackson.databind.ser.impl.MapEntrySerializer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michael Lausch
 *
 * A sample class to hold various statistics with labels.
 *
 */

@Component
public class Metrics implements MeterBinder {

    public void bindTo(MeterRegistry registry) {

        Gauge version = Gauge.builder("version", 1, Number::doubleValue)
                .tags("branch", "rel-1.1.0", "app", "fe")
                .register(registry);
    }
}
