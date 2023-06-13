# Lab Assignment

## Task #1: Mutiny Basics

The goal of Task 1 is to get familiar with the API of the Mutiny primitives and get a solid understanding how things work. To that end you will find three Unit tests in the class `MutinyWarmupTest.java`. The tests are named accordingly to the description in the assignment.


1. Batching

The Test provides a simple `Multi<Integer>` that will generate all values between 1 and 100. Your task is to subscribe to this Multi in a way that allows you to process items in batches of 10. Your output in the end should look something like this:

```
Received batch: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
Received batch: [11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
Received batch: [21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
Received batch: [31, 32, 33, 34, 35, 36, 37, 38, 39, 40]
Received batch: [41, 42, 43, 44, 45, 46, 47, 48, 49, 50]
Received batch: [51, 52, 53, 54, 55, 56, 57, 58, 59, 60]
Received batch: [61, 62, 63, 64, 65, 66, 67, 68, 69, 70]
Received batch: [71, 72, 73, 74, 75, 76, 77, 78, 79, 80]
Received batch: [81, 82, 83, 84, 85, 86, 87, 88, 89, 90]
Received batch: [91, 92, 93, 94, 95, 96, 97, 98, 99, 100]
```

2Multiple Subscriptions

An `StockExchange` object is available as part of the test class. This class only exposes one method `liveTrades` that returns a `Multi<Trade>` and continously generated imaginary stock trades. 

Your task is to subscribe twice to this Multi and print the trades received to the console. Take a look at those trades as your subscribers will output them simultaneously - is this what you expected and if not, what could be the problem here?

3. Combining into backpressure

Mutiny handles the Flow Subscription for us and gives us a sane way to configure and use backpressure. This will be necessary in Task 1.3, as we have a slow and fast producer of Integers. We are interested in the first 100 Integers of each producer and want to add them together pairwise using `combining()`.

Implement this the naive way and see what happens - how can we mitigate this thinking about what we've learned about backpressure? Try out several ways how to mitigate this issue. Think about the pro and cons of each way to mitigate this issue, we will discuss this after the lab.

## Task #2: More Mutiny

Task 2 delves deeper into the things we will have to manage / know if we want to use Mutiny effectively. Again we have some Unit-Tests, this time in the file `MutinyAdvancesTest.java`.

1. It is quite common that we have to use the result of a `Multi` or `Uni` as a signal to trigger some other non-blocking operation and merge those things. In this task you must create a Multi that ticks every second, then gets the result of the `dataUni` and merges the tick with the retrieved string to output the resulting string on the console.

    Your output should look like this:

```
Received: Fetch Data 0
Received: Fetch Data 1
Received: Fetch Data 2
Received: Fetch Data 3
Received: Fetch Data 4
Received: Fetch Data 5
Received: Fetch Data 6
Received: Fetch Data 7
Received: Fetch Data 8
Received: Fetch Data 9
Received: Fetch Data 10
```

2. Not every data source or operation is reliable, we have to think about how to mitigate (temporary) failures and issues. One way to mitigate is a retry mechanism. In this task you have to subscribe to a flaky `Uni<String>` that you can get via the `unreliableUpstreamRequest()` method. Again: subscribe in an interval of one second, merge the result of the Uni with the Tick oh the Multi and try to print an output string with both concatenated on the console.

You'll notice that quite often the Uni will yield an error instead of a message - mitigate this with a retry of your choice and backoff. Think about in which cases a retry is actually useful and when they should be avoided.   


3. Sometimes it's not the upstream source, sometimes it's us. In this test-case we have a Multi that sadly is not able to process one of its elements. We own this multi, so you can change it (without removing the error condition obviously!). Try to mitigate this issue in several ways:

a.) With a back-off - what does this yield and is this what you expected?

b.) With a `onFailure()` strategy that provides a recovery-item - this should still not be what we expect/want

c.) Try to isolate and wrap the error in a way that enables us to see all the produced values. 

## Task 3. Developing a Service

It's fair to say, that most time we have to supply the intermediate layer between a (hopefully reactive) datasource like a database or a remote API and our own presentation layer, e.g. a RESTful API or a frontend.

We get some "raw" data from the source and want to provide the appropriate data types and constructs to display/deliver to the customer. This is exactly what this task is about. We will now have to make good use of our `StockExchange` datasource, that provides us with a never ending list of `Trade` objects.

Again we have provided tests in the `StockExchangeTest` class, but this time you must implement the missing business logic in the `StockService.java` file, located in the `workshop.quarkus.vertx.stock` package.

1. We have a imaginary statistics component that relies on getting the trade volumes of each 100 trades performed. For that we need to be able to observe the trades as they happen and return the trade volume as we've collected 100 trades.

The method to implement is the `tradeVolumeOfNextHundredTrades` in the `StockService` - it should return a long value representing the amount of traded stocks in the next 100 trades after it is calles.

2. Another prominent flavor of batching is the classical bucketing in time frames. This time we want to be able to get all trades in a limited amount of time for later analysis/consumption. The method to implement is `tradesForStockInDuration(Stock stock, Duration duration)` in the `StockService` - this time no transformation to a Uni needs to be done, just gather all the Trades and pass them on.

3. We have to adhere to some imaginary trade restrictions. The `TradeInspector` class exposes a singular function `inspectTrade(Trade trade)` which returns a Uni - this Uni will just return the Trade if it passes all checks but will cause a failure with a cause otherwise.

Your task is to implement the method `validatedTrades()`, that has to use the mentioned TradeInspector to just pass on the Trades that pass all tests. Make sure that your Multi does not terminate on errors, remember, that even a `onFailure()` handler will cancel the upstream subscription!

4. Last, but not least we also need a method to get all the _invalid_ trades. The method to implement is the `illegalTrades()` within the StockService. We _only_ want those trades that were rejected by the `TradeInspector` and we also need a transformation for those trades. Take a look at the `IllegalTrade` class - it provides a simple static helper method to create an instance from a `Trade` object with the cause why this trade is being rejected. 

You must get the cause from the message of the failure that is being propagated by the `TradeInspector`. 


## That's it! You've done great!

You have completed all assignments. If you have any further questions or need clarification, please don't hesitate to reach out to us. We're here to help.