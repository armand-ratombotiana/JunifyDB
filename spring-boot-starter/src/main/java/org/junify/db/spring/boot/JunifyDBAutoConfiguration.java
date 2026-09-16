package org.junify.db.spring.boot;

import org.junify.db.JunifyDB;
import org.junify.db.config.ConsoleConfig;
import org.junify.db.config.SecurityConfig;
import org.junify.db.console.http.JunifyDBServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JunifyDBProperties.class)
@ConditionalOnProperty(prefix = "junifydb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JunifyDBAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JunifyDBAutoConfiguration.class);

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public JunifyDB junifyDB(JunifyDBProperties properties) {
        var builder = JunifyDB.embed()
                .storageEngine(properties.getStorageEngine())
                .autoFlush(properties.isAutoFlush())
                .flushIntervalMs(properties.getFlushIntervalMs());

        if (properties.getDataDir() != null && !properties.getDataDir().isBlank()) {
            builder.persistTo(properties.getDataDir());
        }

        // Configure Console
        if (properties.getConsole() != null) {
            var cp = properties.getConsole();
            var consoleConfig = ConsoleConfig.builder()
                    .enabled(cp.isEnabled())
                    .port(cp.getPort())
                    .contextPath(cp.getContextPath())
                    .intelligentPort(cp.isIntelligentPort())
                    .maxPortAttempts(cp.getMaxPortAttempts())
                    .host(cp.getHost())
                    .scheme(cp.getScheme())
                    .minPort(cp.getMinPort())
                    .maxPort(cp.getMaxPort())
                    .failIfPreferredPortUnavailable(cp.isFailIfPreferredPortUnavailable())
                    .startupTimeoutMs(cp.getStartupTimeoutMs())
                    .localhostOnly(cp.isLocalhostOnly())
                    .build();
            builder.console(consoleConfig);
        }

        // Configure Security
        if (properties.getSecurity() != null) {
            var sp = properties.getSecurity();
            var secBuilder = SecurityConfig.builder()
                    .authEnabled(sp.isAuthEnabled())
                    .apiKey(sp.getApiKey())
                    .adminUsername(sp.getAdminUsername())
                    .adminPassword(sp.getAdminPassword())
                    .corsEnabled(sp.isCorsEnabled())
                    .allowedOrigins(sp.getAllowedOrigins())
                    .sessionTtlMs(sp.getSessionTtlMs())
                    .csrfEnabled(sp.isCsrfEnabled())
                    .rateLimitEnabled(sp.isRateLimitEnabled())
                    .rateLimitRequestsPerMinute(sp.getRateLimitRequestsPerMinute())
                    .bruteForceProtectionEnabled(sp.isBruteForceProtectionEnabled())
                    .maxFailedLoginAttempts(sp.getMaxFailedLoginAttempts())
                    .lockoutDurationMs(sp.getLockoutDurationMs())
                    .passwordHashingEnabled(sp.isPasswordHashingEnabled())
                    .auditLoggingEnabled(sp.isAuditLoggingEnabled())
                    .securityHeadersEnabled(sp.isSecurityHeadersEnabled())
                    .minTlsVersion(sp.getMinTlsVersion());
            if (sp.getSslPort() > 0 && sp.getSslKeystorePath() != null) {
                secBuilder.ssl(sp.getSslPort(), sp.getSslKeystorePath(), sp.getSslKeystorePassword());
            }
            builder.security(secBuilder.build());
        }

        JunifyDB db = JunifyDB.create(builder.buildConfig());
        if (db.consoleUrl() != null) {
            logger.info("==========================================================================");
            logger.info("JunifyDB Administration Console: {}", db.consoleUrl());
            logger.info("==========================================================================");
        }
        return db;
    }

    @Bean
    @ConditionalOnMissingBean
    public JunifyDBTemplate junifyDBTemplate(JunifyDB junifyDB) {
        return new JunifyDBTemplate(junifyDB);
    }

    @Bean
    @ConditionalOnMissingBean
    public JunifyDBServer junifyDBServer(JunifyDB junifyDB) {
        return junifyDB.consoleServer();
    }
}
