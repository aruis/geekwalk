package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }


    String deployVerticleID;

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route()
                .handler(
                        CorsHandler.create("*")
                                .allowedMethod(HttpMethod.GET));

        router.post("/config")
                .produces("text/plain")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    JsonObject json = routingContext.getBodyAsJson();
                    System.out.println(json.toString());

                    if (deployVerticleID != null)
                        vertx.undeploy(deployVerticleID);

                    vertx.deployVerticle("com.aruistar.geekwalk.ProxyVerticle",
                            new DeploymentOptions().setInstances(4).setConfig(json)
                    )
                            .onSuccess(result -> {
                                deployVerticleID = result;
                                response.end(deployVerticleID);
                            })
                            .onFailure(err -> {
                                response.setStatusCode(500).end(err.getMessage());
                            });


                });

        router.route("/")
                .produces("application/json")
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    HttpServerResponse response = routingContext.response();


                    response.end(new JsonObject().put("test", "ok").toString());


                });

        server.requestHandler(router).listen(8888, event -> {
            if (event.succeeded()) {
                System.out.println("启动在8888端口");
            }
        });
    }
}
