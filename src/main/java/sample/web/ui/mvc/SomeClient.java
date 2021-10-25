package sample.web.ui.mvc;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Component
public class SomeClient implements MeterBinder{
	
    @SuppressWarnings("unused")
	private MultiTaggedCounter client_requests;
	
	@Override
    public void bindTo(MeterRegistry registry) {
		client_requests = new MultiTaggedCounter("smtp_client_requests", registry,
				"status", "server");
	}
	
	public void success() {
		client_requests.increment(
				"2XX",
				"smtp");
	}

	public void fail() {
		client_requests.increment(
				"5XX",
				"smtp");
	}
}
