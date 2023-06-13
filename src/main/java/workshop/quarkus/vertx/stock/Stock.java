package workshop.quarkus.vertx.stock;

import java.util.Random;

public enum Stock {
    AMD("Advanced Micro Devices", 4.0),
    GE("General Electric", 10.0),
    VWAGY("Volkswagen", 100.0),
    TWTR("Twitter", 200.0),
    TSLA("Tesla", 400.0),
    AAPL("Apple", 1000.0);

    private final String stockName;

    private double price;

    Stock(String name, double startPrice) {
        this.stockName = name;
        this.price = startPrice;
    }

    public String getStockName() {
        return stockName;
    }

    public double getPrice() {
        return price;
    }

    public static Stock randomStock() {
        return Stock.values()[new Random().nextInt(Stock.values().length)];
    }

    public synchronized double calculateNewPrice(int volume) {
        if ( volume < 0 ) {
            this.price *= 0.99;
        } else {
            this.price *= 1.01;
        }

        return this.price;
    }
}
