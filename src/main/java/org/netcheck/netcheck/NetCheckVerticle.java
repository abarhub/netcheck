package org.netcheck.netcheck;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.netcheck.netcheck.config.ConfigNetCheck;
import org.netcheck.netcheck.config.ConfigService;
import org.netcheck.netcheck.config.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NetCheckVerticle extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(NetCheckVerticle.class);

    private ConfigService configService;

    private ConfigNetCheck configNetCheck;

    //    private HttpClient client;
    private WebClient client;

    private AtomicLong compteur = new AtomicLong(1);

    public NetCheckVerticle(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        long appNbMs;

//        appNbMs = 1000;

//        appNbMs = 10 * 1000;

//        ConfigRetriever retriever = ConfigRetriever.create(vertx);

        //retriever.getConfig().g


        configNetCheck = initConfig();

        appNbMs = configNetCheck.periodicite();

        LOGGER.info("periodicite {} ms", appNbMs);

//        HttpClientOptions options = new HttpClientOptions()
//            .setKeepAlive(false)
//            .setLogActivity(true);
//        PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(10);
//        client = vertx.createHttpClient(options, poolOptions);

        WebClientOptions options = new WebClientOptions()
            //.setUserAgent("My-App/1.2.3")
            .setKeepAlive(false)
            .setLogActivity(true);
//        options.setKeepAlive(false);
        WebClient client = WebClient.create(vertx, options);

        vertx.setPeriodic(appNbMs, id -> {
            // This handler will get called every second
            //LOGGER.info("timer fired!");
            traitement(id, client, compteur.getAndIncrement());
        });

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (client != null) {
            client.close();
        }
    }

    private ConfigNetCheck initConfig() {
        long appNbMs;

        appNbMs = 1000;

        appNbMs = 10 * 1000;

        String periodicite = configService.get("periodicite.ms");
        if (periodicite != null) {
            appNbMs = Long.parseLong(periodicite);
        }

        List<Host> hostList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String host = configService.get("server.no" + i + ".host");
            if (host != null && host.trim().length() > 0) {
                String postStr = configService.get("server.no" + i + ".port");
                if (postStr != null) {
                    int port = Integer.parseInt(postStr);
                    if (port > 0) {
                        hostList.add(new Host(host, Integer.parseInt(postStr)));
                    }
                }
            } else {
                break;
            }
        }


        return new ConfigNetCheck(appNbMs, List.copyOf(hostList));
    }

    private void traitement(long id, WebClient client, long compteur) {
        String code = id + "-" + compteur;
        LOGGER.atInfo().addKeyValue("code", code).log("timer fired !");

        if (configNetCheck.hostList() != null && !configNetCheck.hostList().isEmpty()) {

//            HttpClientOptions options = new HttpClientOptions()
//                .setKeepAlive(false)
//                .setLogActivity(true);
//            PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(10);
//            HttpClient client2 = vertx.createHttpClient(options, poolOptions);

            List<Future<?>> listeFuture = new ArrayList<>();
            for (Host host : configNetCheck.hostList()) {

//                HttpClientOptions options = new HttpClientOptions()
//                    .setKeepAlive(false)
//                    .setLogActivity(true);
//                PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(10);
//                HttpClient client2 = vertx.createHttpClient(options, poolOptions);

                final var hostname = host.host() + ":" + host.port();
                LOGGER.atInfo().addKeyValue("code", code).log("appel {} {} ...", code, hostname);

                Instant debut = Instant.now();

                Future<?> future = client
                    .get(host.port(), host.host(), "")
                    .as(BodyCodec.string())
                    .send()

                    .onComplete(ar1 -> {
                        Instant fin = Instant.now();
                        Duration duree = Duration.between(debut, fin);
                        if (ar1.succeeded()) {
                            // Connected to the server
                            LOGGER.atInfo().addKeyValue("code", code)
                                .addKeyValue("hostname", hostname)
                                .log("HTTP GET request completed (code status={}, duree={}) !",
                                    ar1.result().statusCode(), duree);

                        } else {
                            Integer statusCode = null;
                            if (ar1.result() != null) {
                                statusCode = ar1.result().statusCode();
                            }
                            LOGGER.atError().addKeyValue("code", code)
                                .addKeyValue("hostname", hostname)
                                .log("HTTP GET request error (code status={}, duree={}) !",
                                    statusCode, duree, ar1.cause());
                        }
                    })
                    .onFailure(thr -> {
                        LOGGER.atInfo().addKeyValue("code", code)
                            .addKeyValue("hostname", hostname)
                            .log("HTTP GET request error !", thr);
                    });

                listeFuture.add(future);

            }

            Future.all(listeFuture).onComplete(ar -> {
                    int nbReussi = 0;
                    int total = 0;
                    if (ar.result() != null) {
                        total = ar.result().size();
                        for (int i = 0; i < total; i++) {
                            if (ar.result().succeeded(i)) {
                                nbReussi++;
                            }
                        }
                    }
                    if (ar.succeeded()) {
                        LOGGER.atInfo().addKeyValue("code", code)
                            .log("HTTP GET all requests completed (succee={}/{}) !", nbReussi, total);
                    } else {
                        LOGGER.atError().addKeyValue("code", code)
                            .log("HTTP GET some request error (succee={}/{}) !", nbReussi, total, ar.cause());
                    }
                })
                .onFailure(x -> {
                    LOGGER.atError().addKeyValue("code", code).log("HTTP request error !", x);
                });

        }


    }

}
