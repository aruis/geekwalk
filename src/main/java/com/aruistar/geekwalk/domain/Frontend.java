package com.aruistar.geekwalk.domain;

import io.vertx.core.json.JsonObject;

public class Frontend {

    String prefix;
    String dir;
    String reroute404;
    long maxAgeSeconds;
    boolean cachingEnabled;

    public Frontend(JsonObject json) {
        this.dir = json.getString("dir");
        this.prefix = json.getString("prefix");
        this.cachingEnabled = json.getBoolean("cachingEnabled", true);
        this.maxAgeSeconds = json.getLong("maxAgeSeconds", 24 * 60 * 60L);
        if (!json.getString("reroute404", "").isBlank()) {
            this.reroute404 = json.getString("reroute404").trim();
        }
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public String getReroute404() {
        return reroute404;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDir() {
        return dir;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }
}
