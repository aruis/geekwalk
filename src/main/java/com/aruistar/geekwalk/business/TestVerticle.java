package com.aruistar.geekwalk.business;

import com.aruistar.geekwalk.common.AppRouter;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class TestVerticle extends AbstractVerticle {
    @Inject
    AppRouter appRouter;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        Router router = Router.router(vertx);
        router.route("/a").handler(this::doSomeThing);

        appRouter.addSubRouter("/test", router);
    }

    void doSomeThing(RoutingContext context) {
        context.response().end("a");
    }
}
