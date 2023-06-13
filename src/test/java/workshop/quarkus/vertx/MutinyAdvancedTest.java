package workshop.quarkus.vertx;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MutinyAdvancedTest {


    @Test
    void task_2_1_fetchDataEverySecond() throws InterruptedException {
        Uni<String> dataUni = Uni.createFrom().item("Fetch Data");

        // Add your code here

        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    void task_2_2_retries() throws InterruptedException {
        // Add your code here

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    void task_2_3_failureIsolation() throws InterruptedException {
        Multi<Integer> numbers = Multi.createFrom().range(1, 1000)
                .onItem().transform(n -> {
                    if (n % 4 == 0) {
                        throw new RuntimeException("Error");
                    }
                    return n;
                });

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
}
