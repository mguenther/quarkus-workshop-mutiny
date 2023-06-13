package workshop.quarkus.vertx.stock;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.time.Duration;

public class StockService {

    private final StockExchange exchange;

    private final Logger LOG = LoggerFactory.getLogger(StockExchange.class);

    public StockService(StockExchange exchange) {
        this.exchange = exchange;
    }


    // This should return the amount of stocks traded within the next 100 trades
    public Uni<Long> tradeVolumeOfNextHundredTrades() {
        // Replace this
        return Uni.createFrom().item(0L);
    }

    public Multi<Trade> tradesForStockInDuration(Stock stock, Duration duration) {
        // Replace this
        return Multi.createFrom().empty();
    }

    public Multi<Trade> validatedTrades() {
        // Replace this
        return Multi.createFrom().empty();
    }


    public Multi<IllegalTrade> illegalTrades() {
        // Replace this
        return Multi.createFrom().empty();
    }
}
