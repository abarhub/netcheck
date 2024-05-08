package org.netcheck.netcheck;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NetCheckVerticle extends AbstractVerticle {


    private static final Logger LOGGER = LoggerFactory.getLogger(NetCheckVerticle.class);

    private ConfigService configService;

    private ConfigNetCheck configNetCheck;

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

        HttpClientOptions options = new HttpClientOptions()
            .setKeepAlive(false)
            .setLogActivity(true);
        PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(1);
        HttpClient client = vertx.createHttpClient(options, poolOptions);

        vertx.setPeriodic(appNbMs, id -> {
            // This handler will get called every second
            //LOGGER.info("timer fired!");
            traitement(id, client);
        });

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

    private void traitement(long id, HttpClient client) {
        LOGGER.info("timer fired {} !", id);

        if (configNetCheck.hostList() != null && !configNetCheck.hostList().isEmpty()) {
            for (Host host : configNetCheck.hostList()) {

                LOGGER.info("appel {}:{} ...", host.host(), host.port());

                client
                    .request(HttpMethod.GET, host.port(), host.host(), "")
                    .onComplete(ar1 -> {
                        if (ar1.succeeded()) {
                            // Connected to the server
                            LOGGER.info("HTTP GET request completed {} !", id);
                        } else {
                            LOGGER.info("HTTP GET request error {} !", id, ar1.cause());
                        }
                    });

            }
        }


    }

}
