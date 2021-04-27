package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;

public class ProxyVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setTcpKeepAlive(true);
        HttpServer server = vertx.createHttpServer(serverOptions);

        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setDefaultHost("127.0.0.1");
        clientOptions.setDefaultPort(8080);
        clientOptions.setKeepAlive(true);

        HttpClient client = vertx.createHttpClient(clientOptions);

        server.requestHandler(req -> {
            HttpServerResponse resp = req.response();

            resp.setChunked(true);

//            client.request(req.method(), req.uri()).compose(req2 -> {
//                System.out.println(req.isEnded());
//                return null;
//            });

            client.request(req.method(), req.uri(), ar -> {
                if (ar.succeeded()) {
                    HttpClientRequest req2 = ar.result();

                    req2.setChunked(true);

                    req.headers().forEach(entry -> {
                        if (entry.getKey().equals("Content-Type")) {
                            req2.putHeader(entry.getKey(), entry.getValue());
                        }
                    });

                    req2.response(ar2 -> {
                        if (ar2.succeeded()) {
                            HttpClientResponse resp2 = ar2.result();

                            resp.setStatusCode(resp2.statusCode());
                            resp2.handler(x -> {
                                System.out.println(x.toString());
                                resp.write(x);
                            });

                            resp2.endHandler(x -> {
                                resp.end();
                            });
                        } else {
                            ar2.cause().printStackTrace();
                            resp.setStatusCode(500).end(ar2.cause().getMessage());
                        }
                    });

                    if (!req.isEnded()) {
                        req.handler(x -> {
                            System.out.println(x.toString());
                            req2.write(x);
                        });

                        req.endHandler(x -> {
                            req2.end();
                        });
                    } else {
                        req2.end();
                    }
                } else {
                    ar.cause().printStackTrace();
                    resp.setStatusCode(500).end(ar.cause().getMessage());
                }
            });


        }).listen(9090, event -> {
            if (event.succeeded()) {
                System.out.println("启动在9090端口");
            }
        });
    }
}
