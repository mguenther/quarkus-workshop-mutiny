package workshop.quarkus.vertx.stock;

public record Trade(long volume, Stock stock, double priceBefore, double priceAfter) {
    @Override
    public String toString() {
        return "Trade{" +
                "volume=" + volume +
                ", stock=" + stock +
                ", priceBefore=" + priceBefore +
                ", priceAfter=" + priceAfter +
                '}';
    }
}
