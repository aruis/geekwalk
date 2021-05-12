package com.aruistar.geekwalk;

import com.aruistar.geekwalk.domain.Backend;
import com.aruistar.geekwalk.domain.Frontend;
import com.aruistar.geekwalk.domain.Upstream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

public class ProxyVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        int port = config().getInteger("port");

        List<Backend> backendList = new ArrayList<>();
        List<Frontend> frontendList = new ArrayList<>();

        config().getJsonArray("backend").forEach(json -> {
            backendList.add(new Backend((JsonObject) json, vertx));
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
                    .handler(rc -> {
                        if (!frontend.isCachingEnabled()) {
                            MultiMap headers = rc.response().headers();
                            headers.add("Cache-Control", "no-store")
                                    .add("Cache-Control", "no-cache");
                        }

                        rc.next();
                    })
                    .handler(StaticHandler.create()
                            .setAllowRootFileSystemAccess(true)
                            .setWebRoot(frontend.getDir())
                            .setCachingEnabled(frontend.isCachingEnabled())
                            .setMaxAgeSeconds(frontend.getMaxAgeSeconds())
                    );
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

//        server.webSocketHandler(websocket -> {
//            System.out.println(websocket.path());
//        });

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

            for (Backend backend : backendList) {
                if (path.startsWith(backend.getPrefix())) {
                    Upstream upstream = backend.getUpstream();
                    String uri = req.uri().replaceFirst(backend.getPrefix(), upstream.getPath());

                    HttpClient upstreamClient = upstream.getClient();

                    String upgrade = req.getHeader("Upgrade");
                    if (upgrade != null && upgrade.equals("websocket")) {
                        Future<ServerWebSocket> fut = req.toWebSocket();
                        fut.onSuccess(ws -> {
                            upstreamClient.webSocket(uri).onSuccess(clientWS -> {
                                ws.frameHandler(clientWS::writeFrame);
                                ws.closeHandler(x -> {
                                    clientWS.close();
                                });
                                clientWS.frameHandler(ws::writeFrame);
                                clientWS.closeHandler(x -> {
                                    ws.close();
                                });
                            }).onFailure(err -> {
                                error(resp, err);
                            });
                        }).onFailure(err -> {
                            error(resp, err);
                        });
                        return;
                    }

                    upstreamClient.request(req.method(), uri, ar -> {
                        if (ar.succeeded()) {
                            HttpClientRequest reqUpstream = ar.result();
                            reqUpstream.headers().setAll(req.headers());

                            reqUpstream.send(req).onSuccess(respUpstream -> {
                                resp.setStatusCode(respUpstream.statusCode());
                                resp.headers().setAll(respUpstream.headers());
                                resp.headers().set("Server", "nginx/1.19.10");
                                resp.headers().set("Connection", "keep-alive");
                                resp.send(respUpstream);
                            }).onFailure(err -> {
                                err.printStackTrace();
                                error(resp, err);
                            });

                        } else {
                            ar.cause().printStackTrace();
                            error(resp, ar.cause());
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

    void error(HttpServerResponse resp, Throwable err) {
        resp.setStatusCode(500).end(err.getMessage());
    }
}
