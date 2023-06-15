package workshop.quarkus.vertx;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

public class MutinyNullTypeTest {

    @Test
    void multiWithNull() {
        Multi.createFrom().items("a", "b", "c")
                .onItem().transformToUniAndMerge(item -> Uni.createFrom().item(item.equals("b") ? null : item))
                .onItem().invoke(item -> System.out.println("Intermediate step with item: " + item))
                .subscribe()
                .with(
                        System.out::println,
                        failure -> System.out.println("Encountered failure: " + failure.getMessage())
                );
    }
}
