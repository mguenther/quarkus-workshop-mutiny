package workshop.quarkus.vertx.verticles;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class WebVerticle extends AbstractVerticle {

    private final static Logger LOG = LoggerFactory.getLogger(WebVerticle.class);
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Uni<Void> asyncStart() {
        return vertx.createHttpServer()
                .requestHandler(req -> req.response().endAndForget("@" + counter.incrementAndGet()))
                .listen(8080)
                .onItem().invoke(() -> LOG.info("See http://localhost:8080"))
                .onFailure().invoke(Throwable::printStackTrace)
                .replaceWithVoid();
    }
}
