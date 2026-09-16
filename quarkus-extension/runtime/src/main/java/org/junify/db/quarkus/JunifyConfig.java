package org.junify.db.quarkus;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.junify.db.config.JunifyDBConfig.StorageEngineType;

import java.util.Optional;

/**
 * Quarkus SmallRye Config mapping for the {@code junifydb.*} configuration prefix.
 */
@ConfigMapping(prefix = "junifydb")
public interface JunifyConfig {

    @WithDefault("IN_MEMORY")
    StorageEngineType engine();

    @WithDefault("data")
    String dataDir();

    @WithDefault("true")
    boolean autoFlush();

    @WithDefault("1000")
    int flushIntervalMs();

    ConsoleConfig console();

    SecurityConfig security();

    interface ConsoleConfig {
        @WithDefault("false")
        boolean enabled();

        @WithDefault("8080")
        int port();

        @WithDefault("/")
        String path();

        @WithDefault("true")
        boolean intelligentPort();
    }

    interface SecurityConfig {
        @WithDefault("false")
        boolean enabled();

        Optional<String> apiKey();

        @WithDefault("admin")
        String adminUsername();

        Optional<String> adminPassword();

        @WithDefault("true")
        boolean corsEnabled();
    }

    default StorageEngineType getEngine() { return engine(); }
    default String getDataDir() { return dataDir(); }
    default boolean isAutoFlush() { return autoFlush(); }
    default int getFlushIntervalMs() { return flushIntervalMs(); }
}
