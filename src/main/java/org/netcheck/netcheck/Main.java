package org.netcheck.netcheck;

import io.vertx.core.Vertx;
import org.netcheck.netcheck.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("start");
        ConfigService configService;
        configService = new ConfigService();
        Vertx vertx = Vertx.vertx();
        LOGGER.info("deploiement MainVerticle");
        vertx.deployVerticle(new MainVerticle());
        LOGGER.info("deploiement NetCheckVerticle");
        vertx.deployVerticle(new NetCheckVerticle(configService));
    }

}
