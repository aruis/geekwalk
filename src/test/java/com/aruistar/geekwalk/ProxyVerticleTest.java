package com.aruistar.geekwalk;

import groovy.json.JsonSlurper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
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
            ws.exceptionHandler(testContext::failNow);
        }).onFailure(testContext::failNow);

    }

}
