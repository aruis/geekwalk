package com.aruistar.geekwalk.domain;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Upstream {

    String prefix;
    String url;
    String path;

    public String getPrefix() {
        return prefix;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public HttpClient getClient() {
        return client;
    }

    HttpClient client;

    public Upstream(JsonObject json, Vertx vertx) {
        this.prefix = json.getString("prefix");
        this.url = json.getString("url");


        try {
            String host = new URL(url).getHost();
            int port = new URL(url).getPort();

            this.path = new URL(url).getPath();

            HttpClientOptions clientOptions = new HttpClientOptions();
            clientOptions.setDefaultHost(host);
            clientOptions.setDefaultPort(port);
//        clientOptions.setSsl(true);
//        clientOptions.setTrustAll(true);
//        clientOptions.setKeepAlive(true);

            this.client = vertx.createHttpClient(clientOptions);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }
}
