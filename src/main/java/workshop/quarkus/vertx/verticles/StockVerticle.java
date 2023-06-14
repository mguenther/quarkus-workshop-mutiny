package workshop.quarkus.vertx.verticles;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import workshop.quarkus.vertx.stock.StockExchange;

public class StockVerticle extends AbstractVerticle {

    private StockExchange stockExchange = new StockExchange();

    private final Logger LOG = LoggerFactory.getLogger(StockVerticle.class);

    @Override
    public Uni<Void> asyncStart() {
        stockExchange.liveTrades().subscribe()
                .with(LOG::info);

        stockExchange.liveTrades().subscribe()
                .with(LOG::info);

        return Uni.createFrom().voidItem();
    }
}
