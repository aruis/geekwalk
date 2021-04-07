package com.aruistar.geekwalk;

import com.aruistar.geekwalk.asyncresponse.Controller;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.jboss.resteasy.plugins.server.vertx.VertxRegistry;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;


public class HttpVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Build the Jax-RS hello world deployment
        VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.start();
        VertxRegistry registry = deployment.getRegistry();
        registry.addPerInstanceResource(HelloWorldService.class);
        registry.addPerInstanceResource(Controller.class);

        // Start the front end server using the Jax-RS controller
        vertx.createHttpServer()
                .requestHandler(new VertxRequestHandler(vertx, deployment))
                .listen(8080, ar -> {
                    System.out.println("Server started on port " + ar.result().actualPort());
                });
    }

}
