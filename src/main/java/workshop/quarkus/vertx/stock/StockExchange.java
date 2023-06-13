package workshop.quarkus.vertx.stock;

import io.smallrye.mutiny.Multi;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.function.Supplier;

public class StockExchange {

    private final static Random random = new Random();

    private final Logger LOG = LoggerFactory.getLogger(StockExchange.class);

    private final Multi<Trade> liveTicker;

    public StockExchange(long tickIntervalMs, Supplier<Trade> generator) {
        this.liveTicker = Multi.createFrom().ticks().every(Duration.of(tickIntervalMs, ChronoUnit.MILLIS))
                .onItem().transform((tick) -> generator.get());
    }

    public StockExchange(long tickIntervalMs) {
        this(tickIntervalMs, StockExchange::generateRandomTrade);
    }

    public StockExchange() {
        this(1000, StockExchange::generateRandomTrade);
    }

    public Multi<Trade> liveTrades() {
        return liveTicker;
    }

    private static Trade generateRandomTrade() {
        var stock = Stock.randomStock();
        var volume = random.nextInt(-10, 10);

        return new Trade(volume,
                stock,
                stock.getPrice(),
                stock.calculateNewPrice(volume));
    }

}