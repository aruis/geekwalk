package com.aruistar.geekwalk.common;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Set;

public class AppRouterImpl implements AppRouter {

    private Router router;

    public AppRouterImpl(Vertx vertx) {
        router = Router.router(vertx);

        CorsHandler corsHandler = CorsHandler.create("*");
        corsHandler.allowedMethods(Set.of(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE));
        router.route().handler(corsHandler);
    }

    @Override
    public void addSubRouter(String path, Router subRouter) {
        router.mountSubRouter(path, subRouter);
    }

    @Override
    public Router getAppRouter() {
        return router;
    }
}
