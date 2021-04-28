package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
        Vertx.vertx().deployVerticle(new ServerVerticle());
        Vertx.vertx().deployVerticle(new MyProxyVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("hello");
    }
}
