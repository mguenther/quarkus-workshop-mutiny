package workshop.quarkus.vertx;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MutinyAdvancedTest {


    @Test
    void task_2_1_fetchDataEverySecond() throws InterruptedException {
        Uni<String> dataUni = Uni.createFrom().item("Fetch Data");

        Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .onItem().transformToUniAndConcatenate(
                        tick -> dataUni.onItem().transform(data -> data + " " + tick))
                .subscribe().with(
                        item -> System.out.println("Received: " + item),
                        failure -> System.err.println("Failed with: " + failure)
                );

        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    void task_2_2_retries() throws InterruptedException {
        Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .log()
                .onItem().transformToUniAndConcatenate(tick -> unreliableUpstreamRequest())
                .onFailure().retry().withBackOff(Duration.ofMillis(100)).atMost(5)
                .subscribe().with(
                        item -> System.out.println("Received item: " + item),
                        failure -> System.err.println("Failed with: " + failure)
                );


        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void task_2_3_failureIsolation() throws InterruptedException {
        Multi<Integer> numbers = Multi.createFrom().range(1, 100)
                .onItem().transformToUniAndConcatenate(n ->
                    Uni.createFrom().item(n)
                            .onItem().transform(MutinyAdvancedTest::toIntegerStuff)
                            .onFailure().recoverWithItem(0)
                ).filter(item -> item != 0);

        numbers
                .subscribe().with(
                        item -> System.out.println("Received: " + item),
                        failure -> System.err.println("Failed with: " + failure)
                );

        TimeUnit.SECONDS.sleep(10);
    }

    private Uni<String> unreliableUpstreamRequest() {
        return Uni.createFrom().item(() -> {
            if (new Random().nextBoolean()) {
                return "Successful Data";
            } else {
                throw new RuntimeException("Temporary Failure");
            }
        });
    }

    private static int toIntegerStuff(int number) {
        if (number % 4 == 0) {
            throw new RuntimeException("Error");
        }

        return number;
    }
}
