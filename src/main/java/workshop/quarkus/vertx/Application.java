package workshop.quarkus.vertx;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mutiny.core.Vertx;
import workshop.quarkus.vertx.verticles.MutinyVerticle;
import workshop.quarkus.vertx.verticles.WebVerticle;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Use one instance with a singular worker
        var deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(1);
        deploymentOptions.setWorkerPoolSize(1);

        LOG.info("Deployment Starting");
        vertx.deployVerticleAndAwait(new WebVerticle(), deploymentOptions);
        vertx.deployVerticleAndAwait(new MutinyVerticle(), deploymentOptions);
        LOG.info("Deployment completed");
    }
}
