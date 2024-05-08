package org.netcheck.netcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigService {

    private Map<String, String> config = new HashMap<>();

    public ConfigService() throws IOException {
        init();
    }

    private void init() throws IOException {
        Path p;
        config.clear();
        p = Path.of("config/config.properties");
        if (Files.exists(p)) {
            Properties p1 = new Properties();
            try (var reader = Files.newBufferedReader(p)) {
                p1.load(reader);
            }
            for (String key : p1.stringPropertyNames()) {
                config.put(key, p1.getProperty(key));
            }
        }
    }

    public String get(String key) {
        return config.get(key);
    }
}
