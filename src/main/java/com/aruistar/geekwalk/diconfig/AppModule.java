package com.aruistar.geekwalk.diconfig;

import com.aruistar.geekwalk.common.AppRouter;
import com.aruistar.geekwalk.common.AppRouterImpl;
import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;

public class AppModule extends AbstractModule {
    private AppRouter router;

    public AppModule(Vertx vertx) {
        router = new AppRouterImpl(vertx);
    }

    @Override
    protected void configure() {
        bind(AppRouter.class).toInstance(router);
    }
}
