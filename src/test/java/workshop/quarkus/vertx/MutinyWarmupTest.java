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

        numbers.group().intoLists().of(10)
                .subscribe().with(
                        batch -> System.out.println("Received batch: " + batch)
                );
    }
    @Test
    void task_1_2_multipleSubscribers() throws InterruptedException {
        // Subscribe to the stockExchange.liveTrades() method two times and log / print the trades - what's going on?

        stockExchange.liveTrades().subscribe()
                .with(
                        System.out::println
                );


        stockExchange.liveTrades().subscribe()
                .with(
                        System.out::println
                );

        // Nothing to solve - you should notice that the Multi does not broadcast automatically, so you need to either
        // make the Multi into a broadcast().toAllSubscribers() or toHotStream() - though the last option has some
        // consequences.
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void task_1_3_backpressureStrategies() throws InterruptedException {
        Multi<Long> fast = Multi.createFrom().ticks().every(Duration.ofMillis(20));
        Multi<Long> slow = Multi.createFrom().ticks().every(Duration.ofMillis(100));

        Multi.createBy().combining().streams(fast.select().first(100), slow).asTuple()
                .select().first(100)
                .subscribe().with(
                        item -> System.out.println("Received: " + item + ": " + ( item.getItem1() + item.getItem2())),
                        failure -> System.err.println("Failed with: " + failure)
                );


        TimeUnit.SECONDS.sleep(12);
    }
}
