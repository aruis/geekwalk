package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) throws InterruptedException {
        Vertx.vertx().deployVerticle(new HttpVerticle());
//        Thread.sleep(5000);
        Vertx.vertx().deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
//        vertx.deployVerticle(ProductBackend.class.getName());
//        vertx.deployVerticle(HttpVerticle.class.getName());

        HttpServer httpServer = vertx.createHttpServer();

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setTrustAll(true);
        httpClientOptions.setDefaultPort(8080);
        httpClientOptions.setDefaultHost("127.0.0.1");

        HttpClient client = vertx.createHttpClient(httpClientOptions);


        httpServer.requestHandler(sr -> {

            HttpServerResponse res = sr.response();
            res.setChunked(true);


            client.request(sr.method(), sr.uri(), ar1 -> {
                if (ar1.succeeded()) {
                    HttpClientRequest request = ar1.result();

                    // Send the request and process the response
                    request.send(ar -> {
                        if (ar.succeeded()) {
                            HttpClientResponse response = ar.result();
                            response.handler(res::write);
                            res.setStatusCode(response.statusCode());


                            response.endHandler(event -> {
                                res.end();
                            });
//

                            System.out.println("Received response with status code " + response.statusCode());
                        } else {
                            System.out.println("Something went wrong " + ar.cause().getMessage());
                        }
                    });

//                    res.hand
                    sr.handler(request::write);
                    sr.endHandler(event -> {
                        request.end();
                    });

                }
            });


        }).listen(8081);

    }
}
