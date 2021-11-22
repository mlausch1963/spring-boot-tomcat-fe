package sample.web.ui;

import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class MessageMetrics implements MeterBinder{
	private Gauge message_count;
	private Counter insert_ops;
	private Counter delete_ops;
	private Counter missed_lookups;

	private MessageRepository messages;
	
	public MessageMetrics(final MessageRepository messages) {
		this.messages = messages;
	}

	
	@Override
    public void bindTo(MeterRegistry registry) {

        /*
         * that's how to get a Gauge value by calling a function on an object. In this case it's message.size()
         * and the result is exported as a prometheus gauge.
         */
        message_count = Gauge.builder("messages_stored", messages, MessageRepository::size)
                .baseUnit("count")
                .description("Number of messages stored in repository")
                .register(registry);

        insert_ops = Counter.builder("messages_inserted")
                .baseUnit("count")
                .description("Number of messages inserted")
                .register(registry);

        delete_ops = Counter.builder("messages_deleted")
                .baseUnit("count")
                .description("count of delete operations")
                .register(registry);

        missed_lookups = Counter.builder("messages_lookup_failed")
                .baseUnit("count")
                .description("count of failed message lookups")
                .register(registry);
    }
}
