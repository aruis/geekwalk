package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class MyProxyVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setTcpKeepAlive(true);
        HttpServer server = vertx.createHttpServer(serverOptions);

        WebClientOptions options = new WebClientOptions()
                .setDefaultHost("127.0.0.1")
                .setDefaultPort(8080);

        options.setKeepAlive(false);
        WebClient client = WebClient.create(vertx, options);

        server.requestHandler(serverReq -> {
            HttpServerResponse resp = serverReq.response();
            resp.setChunked(true);

            Promise<Void> promise = Promise.promise();
            serverReq.bodyHandler(body ->
                            client.request(serverReq.method(), serverReq.uri())
                                    .putHeaders(serverReq.headers())
                                    .sendBuffer(body)
                                    .onSuccess(clientResp -> {
//                                        resp.headers().add("content-type", clientResp.getHeader("content-type"));
//                            resp.headers().add("charset", clientResp.getHeader("charset"));
                                        resp.setStatusCode(clientResp.statusCode());
                                        resp.write(clientResp.body());

                                        promise.complete();
                                    })
                                    .onFailure(error -> {
                                        resp.setStatusCode(500);
                                        resp.write(error.getMessage());

                                        promise.fail(error.getMessage());
                                    })
            );

            promise.future()
                    .onComplete(ar -> resp.end());

        }).listen(9090, event -> {
            if (event.succeeded()) {
                System.out.println("启动在9090端口");
            }
        });
    }
}
