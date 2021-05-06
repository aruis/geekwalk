package com.aruistar.geekwalk;

import com.aruistar.geekwalk.domain.Frontend;
import com.aruistar.geekwalk.domain.Upstream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

public class ProxyVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        int port = config().getInteger("port");

        List<Upstream> upstreamList = new ArrayList<>();
        List<Frontend> frontendList = new ArrayList<>();

        config().getJsonArray("upstream").forEach(json -> {
            upstreamList.add(new Upstream((JsonObject) json, vertx));
        });

        config().getJsonArray("frontend").forEach(json -> {
            frontendList.add(new Frontend((JsonObject) json));
        });

        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setTcpKeepAlive(true);
        HttpServer server = vertx.createHttpServer(serverOptions);

        Router router = Router.router(vertx);
        for (Frontend frontend : frontendList) {
            router.route(frontend.getPrefix())
                    .handler(StaticHandler.create().setAllowRootFileSystemAccess(true)
                            .setWebRoot(frontend.getDir()));
        }

        router.errorHandler(404, err -> {
            String path = err.request().path();
            for (Frontend frontend : frontendList) {
                if (path.startsWith(frontend.getPrefix()) && frontend.getReroute404() != null) {
                    err.reroute(frontend.getReroute404());
                    return;
                }
            }

            err.response().setStatusCode(404).end("404");
        });

        server.requestHandler(req -> {
            String path = req.path();
            HttpServerResponse resp = req.response();

            for (Frontend frontend : frontendList) {
                if (path.startsWith(frontend.getPrefix())) {
                    router.handle(req);
                    return;
                }
            }

            req.pause();

            for (Upstream upstream : upstreamList) {
                if (path.startsWith(upstream.getPrefix())) {
                    String uri = req.uri().replaceFirst(upstream.getPrefix(), upstream.getPath());

                    upstream.getClient().request(req.method(), uri, ar -> {
                        if (ar.succeeded()) {
                            HttpClientRequest reqUpstream = ar.result();
                            reqUpstream.headers().setAll(req.headers());

                            reqUpstream.send(req).onSuccess(respUpstream -> {
                                resp.setStatusCode(respUpstream.statusCode());
                                resp.headers().setAll(respUpstream.headers());
                                resp.send(respUpstream);
                            }).onFailure(err -> {
                                err.printStackTrace();
                                resp.setStatusCode(500).end(err.getMessage());
                            });

                        } else {
                            ar.cause().printStackTrace();
                            resp.setStatusCode(500).end(ar.cause().getMessage());
                        }
                    });
                    break;
                }
            }


        }).listen(port, event -> {
            if (event.succeeded()) {
                System.out.println("启动在" + port + "端口");
            }
        });
    }
}
