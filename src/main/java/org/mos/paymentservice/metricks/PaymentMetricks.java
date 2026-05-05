package org.mos.paymentservice.metricks;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class PaymentMetricks {
    private final Counter paymentsFetched;
    private final Counter paymentsSaved;
    private final Counter paymentsRead;
    private final Counter fetchErrors;
    private final Counter saveErrors;

    private final Timer fetchTimer;
    private final Timer saveTimer;
    private final Timer readTimer;

    private final AtomicLong totalSavedGauge = new AtomicLong(0);

    public PaymentMetricks(MeterRegistry registry){
        paymentsFetched = Counter.builder("payments.fetched.total")
                .description("Total payments successfully fetched from stub")
                .register(registry);

        paymentsSaved = Counter.builder("payments.saved.total")
                .description("Total payments successfully saved to db")
                .register(registry);

        paymentsRead = Counter.builder("payments.read.total")
                .description("Total payments successfully read from API")
                .register(registry);

        fetchErrors = Counter.builder("payments.fetched.errors.total")
                .description("The total number of errors successfully caught while receiving data from the stub")
                .register(registry);

        saveErrors = Counter.builder("payments.saved.errors.total")
                .description("The total number of errors successfully caught while saving data into the db")
                .register(registry);

        fetchTimer = Timer.builder("payments.fetch.duration")
                .description("Latency of fetching payment from stub service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        saveTimer = Timer.builder("payments.save.duration")
                .description("Latency of saving payment to database")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        readTimer = Timer.builder("payments.read.duration")
                .description("Latency of reading latest payments from database")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        registry.gauge("payments.db.total", totalSavedGauge, AtomicLong::get);
    }

    public void incrementFetched()    { paymentsFetched.increment(); }
    public void incrementSaved()      { paymentsSaved.increment(); totalSavedGauge.incrementAndGet(); }
    public void incrementRead()       { paymentsRead.increment(); }
    public void incrementFetchError() { fetchErrors.increment(); }
    public void incrementSaveError()  { saveErrors.increment(); }

    public Timer getFetchTimer() { return fetchTimer; }
    public Timer getSaveTimer()  { return saveTimer; }
    public Timer getReadTimer()  { return readTimer; }
}
