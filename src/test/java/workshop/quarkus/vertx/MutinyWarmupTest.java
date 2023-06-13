package workshop.quarkus.vertx;

import io.smallrye.mutiny.Multi;
import org.junit.jupiter.api.Test;
import workshop.quarkus.vertx.stock.StockExchange;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class MutinyWarmupTest {

    private final StockExchange stockExchange = new StockExchange(1_000);


    @Test
    void task_1_1_batching() {
        // Group the numbers into batches of 10 items per Batch and print them out
        Multi<Integer> numbers = Multi.createFrom().range(1, 101);
    }

    @Test
    void task_1_2_multipleSubscribers() throws InterruptedException {
        // Subscribe to the stockExchange.liveTrades() method two times and log / print the trades - what's going on?

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void task_1_3_backpressureStrategies() throws InterruptedException {
        Multi<Long> fast = Multi.createFrom().ticks().every(Duration.ofMillis(20));
        Multi<Long> slow = Multi.createFrom().ticks().every(Duration.ofMillis(100));

        TimeUnit.SECONDS.sleep(10);
    }
}
