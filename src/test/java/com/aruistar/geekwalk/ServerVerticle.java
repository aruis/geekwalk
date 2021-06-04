package com.aruistar.geekwalk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class ServerVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new ServerVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route("/websocket").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            HttpServerResponse response = routingContext.response();

            request.toWebSocket().onSuccess(ws -> {
                ws.writeTextMessage("hello");
            });

        });


        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());


        EventBus eb = vertx.eventBus();

        vertx.setPeriodic(1000, t -> {
            // Create a timestamp string
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));

            eb.send("feed", new JsonObject().put("now", timestamp));
        });

        router.get("/a/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            HttpServerRequest request = routingContext.request();
            response.end("hello world");
        });

        router.post("/hello")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    JsonObject json = routingContext.getBodyAsJson();
                    routingContext.response().end(json.getString("name"));
                });

        router.errorHandler(500, rc -> {
            rc.failure().printStackTrace();
            rc.response().setStatusCode(500).end("i am wrong :" + rc.failure().getMessage());
        });

        router.errorHandler(404, rc -> {
            rc.response().setStatusCode(404).end("no page");
        });

        server.requestHandler(router).listen(8080, event -> {
            if (event.succeeded()) {
                System.out.println("启动在8080端口");
            }
            startPromise.complete();
        });

    }
}
