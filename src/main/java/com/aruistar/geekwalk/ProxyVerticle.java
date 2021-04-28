package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
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

            Promise<Void> promise = Promise.promise();

            req.bodyHandler(body -> {
                client.request(req.method(), req.uri())
                        .onFailure(error -> promise.fail(error.getMessage()))
                        .onSuccess(req2 -> {
                            req.headers().forEach(entry -> {
                                if (entry.getKey().equals("Content-Type")) {
                                    req2.putHeader(entry.getKey(), entry.getValue());
                                }
                            });
                            req2.send(body)
                                    .onFailure(error -> promise.fail(error.getMessage())
                                    )
                                    .onSuccess(resp2 -> resp2.body()
                                            .onFailure(error ->
                                                    promise.fail(error.getMessage())
                                            )
                                            .onSuccess(buffer -> {
                                                System.out.println(buffer);
                                                resp.setStatusCode(resp2.statusCode());
                                                resp.write(buffer);
                                                promise.complete();
                                            })
                                    );
                        });

            });

            promise.future().onComplete(ar -> {
                if (ar.failed()) {
                    System.out.println(ar.cause().getMessage());
                    resp.setStatusCode(500);
                    resp.write(ar.cause().getMessage());
                }
                resp.end();
            });
        }).listen(9090, event -> {
            if (event.succeeded()) {
                System.out.println("启动在9090端口");
            }
        });
    }

    private Future<HttpClientResponse> requestServer(HttpClient client, HttpServerRequest serverRequest, HttpServerResponse serverResponse) {
        Promise<HttpClientResponse> promise = Promise.promise();

        client.request(serverRequest.method(), serverRequest.uri(), ar -> {
            if (ar.succeeded()) {
                HttpClientRequest clientRequest = ar.result();

                clientRequest.setChunked(true);
                serverRequest.headers().forEach(entry -> {
                    if (entry.getKey().equals("Content-Type")) {
                        clientRequest.putHeader(entry.getKey(), entry.getValue());
                    }
                });

                clientRequest.response(ar2 -> {
                    if (ar2.succeeded()) {
                        HttpClientResponse resp2 = ar2.result();
                        promise.complete(resp2);
                    } else {
                        ar2.cause().printStackTrace();
                        promise.fail(ar2.cause().getMessage());
                    }
                });

            } else {
                ar.cause().printStackTrace();
                promise.fail(ar.cause().getMessage());
            }
        });

        return promise.future();
    }
}
