package com.aruistar.geekwalk;

import groovy.json.JsonSlurper;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class ProxyVerticleTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        File file = new File("/Users/liurui/develop/workspace-github/geekwalk/src/test/resources/config.json");
        deploymentOptions.setConfig(new JsonObject((Map<String, Object>) new JsonSlurper().parse(file)));

        vertx.deployVerticle(new ServerVerticle(), ar -> {
            if (ar.succeeded()) {
                vertx.deployVerticle(new ProxyVerticle(), deploymentOptions, testContext.succeedingThenComplete());
            }
        });

    }


    @Test
    void testServer(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8080, "127.0.0.1", "/a/hello")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    assertThat(response.bodyAsString()).isEqualTo("hello world");
                    testContext.completeNow();
                }).onFailure(testContext::failNow);

    }

    @Test
    void testProxyServer(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/a/hello")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    assertThat(response.bodyAsString()).isEqualTo("hello world");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb1(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web1")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    System.out.println(response.bodyAsString());
//                    assertThat(response.bodyAsString()).isEqualTo("hello world");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb1Cache(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web1")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    assertThat(response.getHeader("cache-control"))
                            .isEqualTo("public, immutable, max-age=30");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb1Cache2(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web2")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    assertThat(response.headers().getAll("cache-control"))
                            .hasSize(2);
                    assertThat(response.headers().get("cache-control"))
                            .containsSequence("no-");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb2(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web2")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    System.out.println(response.bodyAsString());
//                    assertThat(response.bodyAsString()).isEqualTo("hello world");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb1404(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web1/nopage.html")
                .expect(ResponsePredicate.SC_OK)
                .send()
                .onSuccess(response -> {
                    System.out.println(response.bodyAsString());
//                    assertThat(response.bodyAsString()).isEqualTo("hello world");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testFrontendWeb2404(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(9090, "127.0.0.1", "/web2/nopage.html")
                .expect(ResponsePredicate.SC_NOT_FOUND)
                .send()
                .onSuccess(response -> {
                    System.out.println(response.bodyAsString());
                    assertThat(response.bodyAsString()).isEqualTo("404");
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

    @Test
    void testWebsocket(Vertx vertx, VertxTestContext testContext) {
        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setDefaultHost("127.0.0.1");
        clientOptions.setDefaultPort(9090);

        HttpClient client = vertx.createHttpClient(clientOptions);

        client.webSocket("/websocket").onSuccess(ws -> {
            ws.handler(replyBuffer -> {
                assertThat(replyBuffer.toString()).isEqualTo("hello");
                testContext.completeNow();
            });
//            ws.exceptionHandler(testContext::failNow);
            ws.exceptionHandler(err -> {
                err.printStackTrace();
            });
        }).onFailure(testContext::failNow);

    }

    @Test
    void testLB(Vertx vertx, VertxTestContext testContext) {
        HttpServer server1 = vertx.createHttpServer();
        server1.requestHandler(event -> {
            event.response().send("b1");
        });


        Future<HttpServer> future1 = server1.listen(8081);

        HttpServer server2 = vertx.createHttpServer();
        server2.requestHandler(event -> {
            event.response().send("b2");
        });


        Future<HttpServer> future2 = server2.listen(8082);

        CompositeFuture.all(future1, future2).onSuccess(event -> {

            WebClient client = WebClient.create(vertx);

            int num = 1000;

            List<Future> futures = new ArrayList<>();

            for (int i = 0; i < num; i++) {
                Future<HttpResponse<Buffer>> sendFuture = client.get(9090, "127.0.0.1", "/b")
                        .expect(ResponsePredicate.SC_OK)
                        .send();
                futures.add(sendFuture);
            }

            CompositeFuture.all(futures).onSuccess(all -> {

                List<String> bodys = new ArrayList<>();
                for (int i = 0; i < num; i++) {
                    Object o = all.resultAt(i);
                    bodys.add(((HttpResponse) o).body().toString());
                }

                float b1Count = 0;
                float b2Count = 0;

                for (String body : bodys) {
                    if (body.equals("b1")) b1Count++;
                    if (body.equals("b2")) b2Count++;
                }

                assertThat(b1Count / b2Count).isBetween(0.9F, 1.1F);

                testContext.completeNow();

//                System.out.println(o);
            });


        });
    }

}
