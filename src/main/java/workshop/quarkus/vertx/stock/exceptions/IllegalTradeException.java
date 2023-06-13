package workshop.quarkus.vertx.stock.exceptions;

public class IllegalTradeException extends RuntimeException{

    public IllegalTradeException(String message) {
        super(message);
    }
}
