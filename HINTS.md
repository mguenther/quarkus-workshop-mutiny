# Hints

**Spoiler Alert**

We encourage you to work on the assignment yourself or together with your peers. However, situations may present themselves to you where you're stuck on a specific assignment. Thus, this document contains a couple of hints/solutions that ought to guide you through a specific task of the lab assignment.

In any case, don't hesitate to talk to us if you're stuck on a given problem!

## Task 1.1

One way to do this is to tell mutiny to group the resulting data into lists of different sizes. Different data structures are also supported, but for our case a list is what we want. Code-wise this can look like this:

```java 
numbers.group().intoLists().of(10)
        .subscribe().with(
                batch -> System.out.println("Received batch: " + batch)
        );
```

## Task 1.2 

The subscribers get different events, even though they are subscribed to the same (singleton) `Multi`. `Uni` and `Multi` cold streams - like Java streams they're lazily evaluated and start their operation only on subscription. And each subscription starts - out of the box - a new flow of data. So every request by a subscriber is handled internally by different calls to the data source, which yields (in our case, this can be done differently) different results.

There was nothing to solve - you should notice that the `Multi` does not broadcast automatically, so you need to either make the `Multi` into a `broadcast().toAllSubscribers()` or `toHotStream()` - though the last option has some implications.

## Task 1.3

The issue is, that the fast producer insists on producing in an interval of 20 ms, the slow producer in an interval of 100ms - this will lead to an overflow of the default buffer (128 items) pretty quickly.

One way to mitigate this is to increase the buffer size, either to a rather high fixed value or to unbounded. It works, but is very dangerous because in terms of memory footprint. Do this often or long enough, and you'll be in trouble.

Another way is to use a drop strategy `onOverflow` for the fast producer. We know that we'll only need the first 100 items, so we can drop everything as soon as the overflow starts to occur. This keeps the subscription alive and the buffer will be constantly full of new items as soon as we've made some room by consuming some.

The third way is to limit the subscription to the amount of items we actually need. Personally I prefer this solution as we make our intent clear "I want to subscribe to 100 items". Mutiny will automatically cancel the subscription after it has received the necessary amount of items. The default buffer now also is large enough. This can be done like this:

```java
Multi.createBy().combining().streams(fast.select().first(100), slow).asTuple()
        .select().first(100)
        .subscribe().with(
                item -> System.out.println("Received: " + item + ": " + ( item.getItem1() + item.getItem2())),
                failure -> System.err.println("Failed with: " + failure)
        );
```

A fun stretch goal (if you're interested) is: what happens if you use a retry with a backoff and **WHY** does that happen? It helps understanding what's going on under the hood.

## Task 2.1 

You can create a Multi with a 1-second interval via the `createFrom().ticks()` method. Afterward you have to merge the tick-items with the `Uni` and concatenate the strings. This then can be merged with the `Multi` stream and propagated downstream as new item. 

```java
Multi.createFrom().ticks().every(Duration.ofSeconds(1))
        .onItem().transformToUniAndConcatenate(tick -> dataUni.onItem().transform(data -> data + " " + tick))
        .subscribe().with(
                item -> System.out.println("Received: " + item),
                failure -> System.err.println("Failed with: " + failure)
        );
```


## Task 2.2

The code is the easy part as we've to do the same as before pretty much, just with an added `onFailure().retry().withBackoff()` part.

```java
Multi.createFrom().ticks().every(Duration.ofSeconds(1))
        .onItem().transformToUniAndConcatenate(tick -> unreliableUpstreamRequest())
        .onFailure().retry().withBackOff(Duration.ofMillis(100)).atMost(5)
        .subscribe().with(
                item -> System.out.println("Received item: " + item),
                failure -> System.err.println("Failed with: " + failure)
        );
```

The interesting part is the retry itself. Retry does a re-subscribe to the upstream. So if you have a static data source (e.g 1,2,3,4) and the value 3 will crash every time you will see 1 and 2 over and over again until you decide to give up (or don't). So a retry only makes sense if:

a.) There is hope that this is a temporary issue.
b.) The data flow is inherently designed in a way that it won't start over just because you re-subscribed. b. is kind of a weak condition as it _might_ still be useful to see all values, but you need to be aware.

Classical scenario is a remote call that sometimes fails (due to network issues or timeouts) but usually no more than one time. In that case -> just retry a couple of times, and it will work at some point.

## Task 2.3

The issue is, that a failure - even if you catch and handle it - will cancel the subscription to the upstream.

So strategy a. will give us the same values over and over again and never finish (as explained in task 2.2), strategy b. will give us an alternative item but stop the consumption. Both not ideal.

The intended way to do this is to make sure, that the faulty operation itself provides a recovery-item and fails gracefully, thereby not propagating its error to the upstream. So we have to change the invoke-part into a `Uni` itself, so the `Multi` looks something like this in the end:

```java
Multi<Integer> numbers = Multi.createFrom().range(1, 1000)
        .onItem().transformToUniAndConcatenate(n -> 
                Uni.createFrom().item(n)
                .onItem().invoke(v -> {
                    if (v == 7) {
                        throw new IllegalArgumentException("We don't like seven!");
                    }
                })
                .onFailure().recoverWithItem(4)
        );
```

## Task 3.1

Mutiny provides a way to cap the subscription at a limit with `select().first(number)` - we can then just collect the items, do the necessary computation and transform that to an `Uni`.

```java
return exchange.liveTrades()
        .select().first(100)
        .collect()
        .asList()
        .onItem()
        .transformToUni(
                list -> Uni.createFrom().item(list.stream().mapToLong(Trade::volume).sum())
        );
```

## Task 3.2

Pretty much the same - even easier to some degree, as we don't have to collect and transform anything. We can just use Mutiny to do the time-boxed collection for us and filter on the Stock we want to focus on:

```java 
public Multi<Trade> tradesForStockInDuration(Stock stock, Duration duration) {
    return exchange.liveTrades()
            .select().first(duration)
            .filter(trade -> trade.stock() == stock);
}
```

## Task 3.3

The tricky part here is how to handle the failure of the `TradeInspector` as part of the pipeline. It returns a `Uni`, so can't just use `filter(TradeInspector.doStuff)` but have to  build a pipeline and act accordingly. 

Two strategies come to mind straight away how to do this:
a.) Providing a recovery item for the `TradeInspector` and filtering for that. The issue is, that `Multi` is not allowed to emit `null` values, so what do we use without canceling the upstream? Can be done but looks cumbersome.
b.) We know that we just don't want to use items that yield errors, we pretty much want to drop them but there is no specific method for doing this. But what we do know/have are retries. So the easiest solution is to just retry indefinitely, because the way our data source now works the next item will be the next item, as we've changed the source to a broadcasting source that just patiently emits one item after the next to all subscribers.

```java
return exchange.liveTrades()
    .onItem().transformToUniAndMerge(TradeInspector::inspectTrade)
    .onFailure().retry().indefinitely();
```

A better version of this would be to make sure that we only retry the errors propagated by the `TradeInspector`. Otherwise, we'd casually ignore errors that might come from the `liveTrades()` itself. So if you've seen and done this: great job!

## Task 3.3

This looks easy at first glance, but the mapping to a different entity has some implications. The fluent API won't let you map to another type as part of the `onFailure().recoverWithItem()` handlers. Those are "locked in" to the type of the item expected.

So the easiest way in my opinion to do this is to transform all items to the `IllegalTrade` type and filter the ones without an error reason later.

```java
return exchange.liveTrades()
        .onItem().transformToUniAndConcatenate((trade) -> TradeInspector.inspectTrade(trade)
                .onItem().transform(item -> IllegalTrade.fromTradeWithReason(trade, null))
                .onFailure().recoverWithItem(throwable -> IllegalTrade.fromTradeWithReason(trade, throwable.getMessage()))
        )
        .filter(trade -> trade.reason() != null)
        .onFailure().retry().indefinitely();
```