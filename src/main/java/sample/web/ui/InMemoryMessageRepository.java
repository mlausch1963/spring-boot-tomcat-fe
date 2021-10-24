/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.web.ui;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.Counter;

/**
 * @author Dave Syer
 * @author Michael Lausch
 *
 * A message repo, which also has some statistics, implemented as micrometer metrics.
 * Used to show how business logic counters and gauges can be used for monitoring and
 * therefore for alerting.
 */
@Component
public class InMemoryMessageRepository implements MessageRepository, MeterBinder {

    static Logger logger = Logger.getLogger(InMemoryMessageRepository.class.getName());

	private static final AtomicLong counter = new AtomicLong();
	private ConcurrentMap<Long, Message> messages;
    @SuppressWarnings("unused")
	private Gauge message_count;
    private Counter insert_ops;
    private Counter delete_ops;
    private Counter missed_lookups;

    InMemoryMessageRepository() {
        messages = new ConcurrentHashMap<>();
    }

    @Override
	public Iterable<Message> findAll() {
		return this.messages.values();
	}

	@Override
	public Message save(Message message) {
		Long id = message.getId();
		if (id == null) {
			id = counter.incrementAndGet();
			message.setId(id);

		}
		this.messages.put(id, message);
		if (insert_ops != null) {
			insert_ops.increment();
		}
		return message;
	}


	@Override
	public Message findMessage(Long id) {
        Message m = this.messages.get(id);
        if (m == null) {
            missed_lookups.increment();
        }
        return m;
	}

	@Override
	public void deleteMessage(Long id) {
		this.messages.remove(id);
		delete_ops.increment();
	}

    @Override
    public void bindTo(MeterRegistry registry) {

        /*
         * that's how to get a Gauge value by calling a function on an object. In this case it's message.size()
         * and the result is exported as a prometheus gauge.
         */
        message_count = Gauge.builder("messages_stored", messages, ConcurrentMap::size)
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
