package workshop.quarkus.vertx.stock;

import io.smallrye.mutiny.Uni;
import workshop.quarkus.vertx.stock.exceptions.IllegalTradeException;
import workshop.quarkus.vertx.stock.exceptions.StockManipulationException;
import workshop.quarkus.vertx.stock.exceptions.TradeVolumeExceededException;

public class TradeInspector {

    public static Uni<Trade> inspectTrade(Trade trade) {
        return Uni.createFrom().item(trade)
                .onItem().transformToUni(TradeInspector::inspectTradeVolume)
                .onItem().transformToUni(TradeInspector::inspectPriceDifference)
                .onItem().transformToUni(TradeInspector::inspectMaximumVolume);
    }

    private static Uni<Trade> inspectMaximumVolume(Trade trade) {
        return Uni.createFrom().item(trade)
                .onItem().invoke(t -> {
                    if (Math.abs(t.volume()) > 1000) {
                        throw new TradeVolumeExceededException(t.volume() + " exceeds the allowed volume of 1000 stocks");
                    }
                });
    }

    private static Uni<Trade> inspectPriceDifference(Trade trade) {
        return Uni.createFrom().item(trade)
                .onItem().invoke(t -> {
                    if (t.priceAfter() > t.priceBefore() * 1.25 || t.priceAfter() < t.priceBefore() * 0.75) {
                        throw new StockManipulationException("The price difference is too high.");
                    }
                });
    }

    private static Uni<Trade> inspectTradeVolume(Trade trade) {
        return Uni.createFrom().item(trade)
                .onItem().invoke(t -> {
                    if (t.volume() == 0) {
                        throw new IllegalTradeException("Trade volume of 0 is not allowed!");
                    }
                });
    }

    private static Uni<Trade> randomErrorChance(Trade trade) {
        return Uni.createFrom().item(trade)
                .onItem().invoke(t -> {
                    if (Math.random() <= 0.5) {
                        throw new IllegalStateException("Something went wrong.");
                    }
                });
    }

}
