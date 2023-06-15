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
        return exchange.liveTrades()
                .select().first(100)
                .collect()
                .asList()
                .onItem().transformToUni(
                        list -> Uni.createFrom().item(list.stream().mapToLong(Trade::volume).sum())
                );
    }

    public Multi<Trade> tradesForStockInDuration(Stock stock, Duration duration) {
        return exchange.liveTrades()
                .select().first(duration)
                .filter(trade -> trade.stock() == stock);
    }

    public Multi<Trade> validatedTrades() {
        return exchange.liveTrades()
                .onItem().transformToUniAndMerge(TradeInspector::inspectTrade)
                .onFailure().retry().indefinitely();
    }


    public Multi<IllegalTrade> illegalTrades() {
        return exchange.liveTrades()
                .onItem().transformToUniAndConcatenate((trade) -> TradeInspector.inspectTrade(trade)
                        .onItem().transform(item -> IllegalTrade.fromTradeWithReason(trade, null))
                        .onFailure().recoverWithItem(throwable -> IllegalTrade.fromTradeWithReason(trade, throwable.getMessage()))
                )
                .filter(trade -> trade.reason() != null);
    }
}
