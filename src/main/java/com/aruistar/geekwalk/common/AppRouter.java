package com.aruistar.geekwalk.common;

import io.vertx.ext.web.Router;

public interface AppRouter {
    void addSubRouter(String path, Router subRouter);

    Router getAppRouter();
}
