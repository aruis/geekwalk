package com.aruistar.geekwalk.domain;

import io.vertx.core.json.JsonObject;

public class Frontend {

    String prefix;
    String dir;

    public String getReroute404() {
        return reroute404;
    }

    public void setReroute404(String reroute404) {
        this.reroute404 = reroute404;
    }

    String reroute404;

    public Frontend(JsonObject json) {
        this.dir = json.getString("dir");
        this.prefix = json.getString("prefix");
        if (!json.getString("reroute404", "").isBlank()) {
            this.reroute404 = json.getString("reroute404").trim();
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

}
