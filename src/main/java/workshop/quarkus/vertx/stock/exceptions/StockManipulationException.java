package workshop.quarkus.vertx.stock.exceptions;

public class StockManipulationException extends RuntimeException {

    public StockManipulationException(final String message) {
        super(message);
    }
}
