package workshop.quarkus.vertx.stock.exceptions;

public class TradeVolumeExceededException extends RuntimeException {

    public TradeVolumeExceededException(final String message) {
        super(message);
    }
}
