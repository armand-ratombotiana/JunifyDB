package org.junify.db.micronaut;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import org.junify.db.JunifyDB;
import org.junify.db.config.ConsoleConfig;
import org.junify.db.config.JunifyDBConfig;
import org.junify.db.config.SecurityConfig;
import org.junify.db.console.http.JunifyDBServer;

import jakarta.inject.Singleton;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Micronaut {@link Factory} that creates and manages the {@link JunifyDB} singleton.
 */
@Factory
public class JunifyDBFactory {

    @Nullable
    private JunifyDB junifyDB;

    @Property(name = "junifydb.enabled", defaultValue = "true")
    private boolean enabled = true;

    @Property(name = "junifydb.engine", defaultValue = "IN_MEMORY")
    private String engine = "IN_MEMORY";

    @Property(name = "junifydb.data-dir", defaultValue = "./data/junifydb")
    private String dataDir = "./data/junifydb";

    @Property(name = "junifydb.auto-flush", defaultValue = "true")
    private boolean autoFlush = true;

    @Property(name = "junifydb.flush-interval-ms", defaultValue = "1000")
    private int flushIntervalMs = 1000;

    @Property(name = "junifydb.console.enabled", defaultValue = "false")
    private boolean consoleEnabled = false;

    @Property(name = "junifydb.console.port", defaultValue = "8080")
    private int consolePort = 8080;

    @Property(name = "junifydb.console.path", defaultValue = "/")
    private String consolePath = "/";

    @Property(name = "junifydb.console.intelligent-port", defaultValue = "true")
    private boolean intelligentPort = true;

    @Property(name = "junifydb.security.enabled", defaultValue = "false")
    private boolean securityEnabled = false;

    @Nullable
    @Property(name = "junifydb.security.api-key")
    private String apiKey;

    @Property(name = "junifydb.security.admin-username", defaultValue = "admin")
    private String adminUsername = "admin";

    @Nullable
    @Property(name = "junifydb.security.admin-password")
    private String adminPassword;

    @Property(name = "junifydb.security.cors-enabled", defaultValue = "true")
    private boolean corsEnabled = true;

    @PostConstruct
    void initialize() {
        if (enabled) {
            var builder = JunifyDBConfig.builder()
                    .storageEngine(parseEngine(engine))
                    .persistTo(dataDir)
                    .autoFlush(autoFlush)
                    .flushIntervalMs(flushIntervalMs);

            if (consoleEnabled) {
                builder.console(ConsoleConfig.builder()
                        .enabled(true)
                        .port(consolePort)
                        .contextPath(consolePath)
                        .intelligentPort(intelligentPort)
                        .build());
            }

            if (securityEnabled || apiKey != null || adminPassword != null) {
                var secBuilder = SecurityConfig.builder()
                        .authEnabled(securityEnabled)
                        .adminUsername(adminUsername)
                        .corsEnabled(corsEnabled);
                if (apiKey != null) secBuilder.apiKey(apiKey);
                if (adminPassword != null) secBuilder.adminPassword(adminPassword);
                builder.security(secBuilder.build());
            }

            junifyDB = JunifyDB.create(builder.buildConfig());
        }
    }

    @Singleton
    @NonNull
    public JunifyDB junifyDB() {
        if (junifyDB == null) {
            throw new IllegalStateException("JunifyDB is not initialized. Check that junifydb.enabled=true.");
        }
        return junifyDB;
    }

    @Singleton
    @Nullable
    public JunifyDBServer junifyDBServer() {
        return junifyDB != null ? junifyDB.consoleServer() : null;
    }

    @PreDestroy
    void stop() {
        if (junifyDB != null && junifyDB.isOpen()) {
            junifyDB.close();
        }
    }

    private JunifyDBConfig.StorageEngineType parseEngine(String engine) {
        try {
            return JunifyDBConfig.StorageEngineType.valueOf(engine.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JunifyDBConfig.StorageEngineType.IN_MEMORY;
        }
    }
}