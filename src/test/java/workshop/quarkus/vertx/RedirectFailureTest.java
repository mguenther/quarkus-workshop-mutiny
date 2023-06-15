package workshop.quarkus.vertx;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

public class RedirectFailureTest {

    @Test
    void redirectRecoveryTest() {
        Uni<String> primarySource = fetchFromPrimarySource();

        primarySource
                .onFailure().recoverWithUni(RedirectFailureTest::fetchFromFailureHandler)
                .subscribe().with(
                        item -> System.out.println("Received item: " + item),
                        failure -> System.err.println("Failed with: " + failure)
                );
    }

    // Simulate fetching data from the primary source
    private static Uni<String> fetchFromPrimarySource() {
        return Uni.createFrom().failure(new RuntimeException("Primary source unavailable"));
    }

    // Simulate fetching data from the backup source
    private static Uni<String> fetchFromBackupSource() {
        return Uni.createFrom().item("Data from backup source");
    }


    private static Uni<String> fetchFromFailureHandler(Throwable throwable) {
        return Uni.createFrom().failure(throwable)
                .onItem().transform((item) -> "")
                .onFailure().recoverWithItem("something");
    }
}
