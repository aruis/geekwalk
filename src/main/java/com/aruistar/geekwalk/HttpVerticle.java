package com.aruistar.geekwalk;

import com.aruistar.geekwalk.common.AppRouter;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;


public class HttpVerticle extends AbstractVerticle {
    @Inject
    private AppRouter appRouter;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router rootRouter = appRouter.getAppRouter();
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(rootRouter);
        server.listen(8080, listen -> {
            if (listen.succeeded()) {
                System.out.println("Server listening on http://localhost:8080/");
            } else {
                listen.cause().printStackTrace();
                System.exit(1);
            }
        });
    }

}
