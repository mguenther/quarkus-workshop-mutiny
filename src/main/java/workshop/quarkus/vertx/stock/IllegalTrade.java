package workshop.quarkus.vertx.stock;

import java.util.Objects;

public record IllegalTrade(long volume, Stock stock, double priceBefore, double priceAfter, String reason) {
    @Override
    public String toString() {
        return "IllegalTrade{" +
                "volume=" + volume +
                ", stock=" + stock +
                ", priceBefore=" + priceBefore +
                ", priceAfter=" + priceAfter +
                ", reason='" + reason + '\'' +
                '}';
    }

    public static IllegalTrade fromTradeWithReason(Trade trade, String reason) {
        return new IllegalTrade(trade.volume(), trade.stock(), trade.priceBefore(), trade.priceAfter(), reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IllegalTrade that = (IllegalTrade) o;
        return volume == that.volume && Double.compare(that.priceBefore, priceBefore) == 0 && Double.compare(that.priceAfter, priceAfter) == 0 && stock == that.stock && Objects.equals(reason, that.reason);
    }
}
