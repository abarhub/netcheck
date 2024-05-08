package org.netcheck.netcheck.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    private Map<String, String> config = new HashMap<>();

    public ConfigService() throws IOException {
        init();
    }

    private void init() throws IOException {
        Path p;
        config.clear();
        p = Path.of("config/config.properties");
        if (Files.exists(p)) {
            LOGGER.info("Loading config from {}", p.toAbsolutePath());
            Properties p1 = new Properties();
            try (var reader = Files.newBufferedReader(p)) {
                p1.load(reader);
            }
            for (String key : p1.stringPropertyNames()) {
                config.put(key, p1.getProperty(key));
            }
            LOGGER.info("{} config parameters", config.size());
        } else {
            LOGGER.warn("No config file found");
        }
    }

    public String get(String key) {
        return config.get(key);
    }
}
