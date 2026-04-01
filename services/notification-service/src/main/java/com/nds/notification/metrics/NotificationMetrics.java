package com.nds.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class NotificationMetrics {

    private final MeterRegistry registry;
    private final AtomicLong queueDepth = new AtomicLong(0);

    public NotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("notifications_queue_depth", queueDepth, AtomicLong::get);
    }

    public void incrementSent(String channel, String status) {
        Counter.builder("notifications_sent_total")
                .tag("channel", channel)
                .tag("status", status)
                .register(registry)
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void recordDuration(Timer.Sample sample, String channel) {
        sample.stop(Timer.builder("notification_processing_duration_seconds")
                .tag("channel", channel)
                .register(registry));
    }

    public void incrementQueueDepth() {
        queueDepth.incrementAndGet();
    }

    public void decrementQueueDepth() {
        queueDepth.decrementAndGet();
    }
}
