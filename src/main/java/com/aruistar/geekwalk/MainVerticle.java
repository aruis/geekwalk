package com.aruistar.geekwalk;

import com.aruistar.geekwalk.asyncresponse.ProductBackend;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.deployVerticle(ProductBackend.class.getName());
        vertx.deployVerticle(HttpVerticle.class.getName());
    }
}
