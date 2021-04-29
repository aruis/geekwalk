package com.aruistar.geekwalk.launcher;

import io.vertx.core.Launcher;

public class GeekLauncher extends Launcher {
    public static void main(String[] args) {
        new GeekLauncher().dispatch(args);
    }
}
