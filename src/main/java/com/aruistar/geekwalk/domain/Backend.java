package com.aruistar.geekwalk.domain;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Backend {

    String prefix;

    List<Upstream> upstreamList = new ArrayList<>();

    public String getPrefix() {
        return prefix;
    }


    //TODO 搞定负载均衡傻瓜版，明天看情况再说喽。
    public Upstream getUpstream() {
        return upstreamList.get(0);
    }


    public Backend(JsonObject json, Vertx vertx) {
        this.prefix = json.getString("prefix");
        json.getJsonArray("upstream")
                .forEach(up -> {
                    upstreamList.add(new Upstream((JsonObject) up, vertx));
                });


    }
}
