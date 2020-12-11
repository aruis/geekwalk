package com.aruistar.geekwalk;

import com.aruistar.geekwalk.business.TestVerticle;
import com.aruistar.geekwalk.diconfig.AppModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        AppModule module = new AppModule(vertx);
        Injector injector = Guice.createInjector(module);
        TestVerticle testVerticle = injector.getInstance(TestVerticle.class);
        HttpVerticle appVerticle = injector.getInstance(HttpVerticle.class);


        vertx.deployVerticle(testVerticle);
        vertx.deployVerticle(appVerticle);
    }
}
