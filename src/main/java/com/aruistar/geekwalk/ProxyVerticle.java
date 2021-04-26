package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;

public class ProxyVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setDefaultHost("127.0.0.1");
        clientOptions.setDefaultPort(8080);
        clientOptions.setKeepAlive(true);

        HttpClient client = vertx.createHttpClient(clientOptions);

        server.requestHandler(req -> {
            HttpServerResponse resp = req.response();

//            resp.end("test");
//
//            req.handler(x -> {
//                System.out.println(x.toString());
//            });


            resp.setChunked(true);
            client.request(req.method(), req.uri(), ar -> {
                if (ar.succeeded()) {
                    HttpClientRequest req2 = ar.result();

                    req.handler(x -> {
                        System.out.println(x.toString());
                        req2.write(x);
                    });

                    req.endHandler(x -> {
                        req2.end();
                    });

                    req2.send(ar2 -> {
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
                        }
                    });
                }
            });


        }).listen(9090, event -> {
            if (event.succeeded()) {
                System.out.println("启动在9090端口");
            }
        });
    }
}
