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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * @author Dave Syer
 * @author Michael Lausch
 *
 * A message repo, which also has some statistics, implemented as micrometer metrics.
 * Used to show how business logic counters and gauges can be used for monitoring and
 * therefore for alerting.
 * 
 * Implementing the "MeterBinder" interface with the bindTo function, initializes the 
 * metrics. 
 * This class holds 2 examples of metrics, Counter and Gauges to demonatrate the 
 * difference in implementation and usage.
 * 
 * "Counter" is used for counting events, the only important function is the increment() 
 * function. It just increments the counter (thread-safe) by one, when passing a double 
 * parameter, it adds the parameter. Be careful to only increment a counter, never pass a
 * negative value. Counters are monotone growing. 
 * 
 * "Gauge" is just a number, like the number of elements in the repository. Or the state 
 * of a healthcheck. The value of a gauge can go up and down. Best practice to implement 
 * a gauge is to fetch the value from the real thing, in this example it is the number 
 * of elements in he repository. Threadsafeness is left as an exercise for the reader.
 * 
 * Because the class is annotated as a "Component" the bindTo function is called 
 * automagically by the spring boot machinery. It is also possible to get the global registry
 * https://micrometer.io/docs/concepts#_global_registry
 * 
 */
@Component
public class InMemoryMessageRepository implements MessageRepository , MeterBinder {

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
	
	public int size() {
		return messages.size();
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
