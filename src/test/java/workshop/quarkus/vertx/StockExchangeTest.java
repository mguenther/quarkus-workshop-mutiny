package workshop.quarkus.vertx;

import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import workshop.quarkus.vertx.stock.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class StockExchangeTest {

    private final static List<Trade> staticTrades = List.of(
            new Trade(100, Stock.AMD, 1.0, 1.0),
            new Trade(50, Stock.AAPL, 100.0, 100.0),
            new Trade(25, Stock.GE, 10.0, 10.0),
            new Trade(1000, Stock.TWTR, 1.0, 1.0),
            new Trade(20, Stock.TSLA, 1.0, 1.0),
            new Trade(-50, Stock.TWTR, 1.0, 1.0),
            new Trade(-10, Stock.AAPL, 50.0, 25.0),
            new Trade(100, Stock.GE, 50.0, 40.0),
            new Trade(0, Stock.VWAGY, 1.0, 1.0),
            new Trade(10_000, Stock.VWAGY, 10.0, 8.0),
            new Trade(-5_000, Stock.GE, 10.0, 10.0)
    );

    private final AtomicInteger offset = new AtomicInteger(0);

    @BeforeEach
    void resetOffset() {
        this.offset.set(0);
    }

    @Test
    void task3_1_subscribeTradeVolume() {
        final var volumePerTrade = new Random().nextLong(10);
        var sut = new StockService(new StockExchange(10, () -> new Trade(volumePerTrade, Stock.GE, 1.0, 1.0)));

        UniAssertSubscriber<Long> subscriber = sut.tradeVolumeOfNextHundredTrades()
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitItem()
                .assertItem(volumePerTrade * 100);

    }

    @Test
    void task3_2_subscribeStockTimeDuration() {
        final var stock = Stock.GE;
        final var tickInterval = 200;
        var sut = new StockService(new StockExchange(tickInterval, supplyTestTrades()));

        AssertSubscriber<Trade> subscriber =
                sut.tradesForStockInDuration(stock, Duration.of((long) staticTrades.size() * tickInterval, ChronoUnit.MILLIS) )
                .subscribe()
                .withSubscriber(AssertSubscriber.create());

        var items = subscriber
                .assertSubscribed()
                .request(staticTrades.size())
                .awaitCompletion()
                .getItems();

        Assertions.assertEquals(3, items.size());
        Assertions.assertTrue(items.stream().allMatch(trade -> trade.stock() == stock));

    }

    @Test
    void task3_3_onlyValidatedTrades() {
        var sut = new StockService(new StockExchange(10, supplyTestTrades()));

        AssertSubscriber<Trade> subscriber = sut.validatedTrades()
                .subscribe()
                .withSubscriber(AssertSubscriber.create());

        var items = subscriber.assertSubscribed()
                .request(100)
                .awaitItems(100)
                .getItems();

        Assertions.assertEquals(100, items.size());
        Assertions.assertTrue(items.stream().allMatch(
                trade -> TradeInspector.inspectTrade(trade).onFailure().recoverWithItem(() -> null).await().indefinitely() != null)
        );
    }

    @Test
    void task4_4_onlyIllegalTrades() {
        var sut = new StockService(new StockExchange(10, supplyTestTrades()));

        AssertSubscriber<IllegalTrade> subscriber = sut.illegalTrades()
                .subscribe()
                .withSubscriber(AssertSubscriber.create());

        var items = subscriber.assertSubscribed()
                .request(4)
                .awaitItems(4)
                .getItems();

        Assertions.assertEquals(4, items.size());
        Assertions.assertEquals(items.get(0), new IllegalTrade(-10, Stock.AAPL, 50.0, 25.0, "The price difference is too high."));
        Assertions.assertEquals(items.get(1), new IllegalTrade(0, Stock.VWAGY, 1.0, 1.0, "Trade volume of 0 is not allowed!"));
        Assertions.assertEquals(items.get(2), new IllegalTrade(10000, Stock.VWAGY, 10.0, 8.0, "10000 exceeds the allowed volume of 1000 stocks"));
        Assertions.assertEquals(items.get(3), new IllegalTrade(-5000, Stock.GE, 10.0, 10.0, "-5000 exceeds the allowed volume of 1000 stocks"));
    }

    private Supplier<Trade> supplyTestTrades() {
        return () -> staticTrades.get(offset.getAndIncrement() % staticTrades.size());
    }
}
