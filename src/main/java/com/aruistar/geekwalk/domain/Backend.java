package com.aruistar.geekwalk.domain;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Backend {

    String prefix;

    List<Upstream> upstreamList = new ArrayList<>();
    List<Upstream> _upstreamList = new ArrayList<>();

    public String getPrefix() {
        return prefix;
    }

    Random random = new Random();

    public Upstream getUpstream() {
        if (upstreamList.size() == 1) {
            return upstreamList.get(0);
        }
        return _upstreamList.get(random.nextInt(_upstreamList.size()));
    }


    public Backend(JsonObject json, Vertx vertx) {
        this.prefix = json.getString("prefix");

        Object value = json.getValue("upstream");

        if (value instanceof JsonArray) {
            JsonArray array = json.getJsonArray("upstream");
            array.forEach(up -> {
                Upstream upstream = new Upstream((JsonObject) up, vertx);
                upstreamList.add(upstream);
                for (int i = 0; i < upstream.weight; i++) {
                    _upstreamList.add(upstream);
                }
            });
        } else {
            Upstream upstream = new Upstream(json.getString("upstream"), 1, vertx);
            upstreamList.add(upstream);
            _upstreamList.add(upstream);
        }
    }
}
