package org.junify.db.config;

import java.util.logging.Logger;

/**
 * Resolves effective administration console and security configurations
 * according to strict precedence rules:
 * <ol>
 *   <li>Explicit Programmatic Configuration</li>
 *   <li>Java System Properties (e.g. {@code junifydb.console.port})</li>
 *   <li>Operating System Environment Variables (e.g. {@code JUNIFYDB_CONSOLE_PORT})</li>
 *   <li>Framework Defaults</li>
 * </ol>
 */
public final class ConfigurationResolver {

    private static final Logger logger = Logger.getLogger(ConfigurationResolver.class.getName());

    private ConfigurationResolver() {
    }

    /**
     * Resolves the effective {@link ConsoleConfig} using system properties and environment variables
     * over the provided base configuration.
     */
    public static ConsoleConfig resolveConsoleConfig(ConsoleConfig base) {
        if (base == null) {
            base = ConsoleConfig.disabled();
        }

        ConsoleConfig.Builder builder = ConsoleConfig.builder()
                .enabled(base.enabled())
                .port(base.port())
                .contextPath(base.contextPath())
                .intelligentPort(base.intelligentPort())
                .maxPortAttempts(base.maxPortAttempts())
                .host(base.host())
                .scheme(base.scheme())
                .portRange(base.minPort(), base.maxPort())
                .failIfPreferredPortUnavailable(base.failIfPreferredPortUnavailable())
                .startupTimeoutMs(base.startupTimeoutMs())
                .localhostOnly(base.localhostOnly());

        // Check system properties & env vars
        String enabledVal = getPropertyOrEnv("junifydb.console.enabled", "JUNIFYDB_CONSOLE_ENABLED");
        if (enabledVal != null && !enabledVal.isBlank()) {
            builder.enabled(Boolean.parseBoolean(enabledVal.trim()));
        }

        String portVal = getPropertyOrEnv("junifydb.console.port", "JUNIFYDB_CONSOLE_PORT");
        if (portVal != null && !portVal.isBlank()) {
            try {
                builder.port(Integer.parseInt(portVal.trim()));
            } catch (NumberFormatException e) {
                logger.warning("Invalid port configuration in system property/env: " + portVal);
            }
        }

        String contextPathVal = getPropertyOrEnv("junifydb.console.context-path", "JUNIFYDB_CONSOLE_CONTEXT_PATH");
        if (contextPathVal == null) {
            contextPathVal = getPropertyOrEnv("junifydb.console.contextPath", "JUNIFYDB_CONSOLE_CONTEXTPATH");
        }
        if (contextPathVal != null && !contextPathVal.isBlank()) {
            builder.contextPath(contextPathVal.trim());
        }

        String hostVal = getPropertyOrEnv("junifydb.console.host", "JUNIFYDB_CONSOLE_HOST");
        if (hostVal != null && !hostVal.isBlank()) {
            builder.host(hostVal.trim());
        }

        String schemeVal = getPropertyOrEnv("junifydb.console.scheme", "JUNIFYDB_CONSOLE_SCHEME");
        if (schemeVal != null && !schemeVal.isBlank()) {
            builder.scheme(schemeVal.trim());
        }

        String minPortVal = getPropertyOrEnv("junifydb.console.min-port", "JUNIFYDB_CONSOLE_MIN_PORT");
        if (minPortVal == null) {
            minPortVal = getPropertyOrEnv("junifydb.console.minPort", "JUNIFYDB_CONSOLE_MINPORT");
        }
        if (minPortVal != null && !minPortVal.isBlank()) {
            try {
                builder.minPort(Integer.parseInt(minPortVal.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String maxPortVal = getPropertyOrEnv("junifydb.console.max-port", "JUNIFYDB_CONSOLE_MAX_PORT");
        if (maxPortVal == null) {
            maxPortVal = getPropertyOrEnv("junifydb.console.maxPort", "JUNIFYDB_CONSOLE_MAXPORT");
        }
        if (maxPortVal != null && !maxPortVal.isBlank()) {
            try {
                builder.maxPort(Integer.parseInt(maxPortVal.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String intelligentVal = getPropertyOrEnv("junifydb.console.intelligent-port", "JUNIFYDB_CONSOLE_INTELLIGENT_PORT");
        if (intelligentVal == null) {
            intelligentVal = getPropertyOrEnv("junifydb.console.intelligentPort", "JUNIFYDB_CONSOLE_INTELLIGENTPORT");
        }
        if (intelligentVal != null && !intelligentVal.isBlank()) {
            builder.intelligentPort(Boolean.parseBoolean(intelligentVal.trim()));
        }

        String maxAttemptsVal = getPropertyOrEnv("junifydb.console.max-port-attempts", "JUNIFYDB_CONSOLE_MAX_PORT_ATTEMPTS");
        if (maxAttemptsVal == null) {
            maxAttemptsVal = getPropertyOrEnv("junifydb.console.maxPortAttempts", "JUNIFYDB_CONSOLE_MAXPORTATTEMPTS");
        }
        if (maxAttemptsVal != null && !maxAttemptsVal.isBlank()) {
            try {
                builder.maxPortAttempts(Integer.parseInt(maxAttemptsVal.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String failOnUnavailableVal = getPropertyOrEnv("junifydb.console.fail-if-preferred-port-unavailable",
                "JUNIFYDB_CONSOLE_FAIL_IF_PREFERRED_PORT_UNAVAILABLE");
        if (failOnUnavailableVal != null && !failOnUnavailableVal.isBlank()) {
            builder.failIfPreferredPortUnavailable(Boolean.parseBoolean(failOnUnavailableVal.trim()));
        }

        String localhostOnlyVal = getPropertyOrEnv("junifydb.console.localhost-only", "JUNIFYDB_CONSOLE_LOCALHOST_ONLY");
        if (localhostOnlyVal != null && !localhostOnlyVal.isBlank()) {
            builder.localhostOnly(Boolean.parseBoolean(localhostOnlyVal.trim()));
        }

        return builder.build();
    }

    /**
     * Resolves the effective {@link SecurityConfig} using system properties and environment variables
     * over the provided base configuration.
     */
    public static SecurityConfig resolveSecurityConfig(SecurityConfig base) {
        if (base == null) {
            base = SecurityConfig.disabled();
        }

        SecurityConfig.Builder builder = SecurityConfig.builder()
                .authEnabled(base.authEnabled())
                .apiKey(base.apiKey())
                .adminUsername(base.adminUsername())
                .adminPassword(base.adminPassword())
                .corsEnabled(base.corsEnabled())
                .allowedOrigins(base.allowedOrigins())
                .sessionTtlMs(base.sessionTtlMs())
                .ssl(base.sslPort(), base.sslKeystorePath(), base.sslKeystorePassword())
                .csrfEnabled(base.csrfEnabled())
                .rateLimitEnabled(base.rateLimitEnabled())
                .rateLimitRequestsPerMinute(base.rateLimitRequestsPerMinute())
                .bruteForceProtectionEnabled(base.bruteForceProtectionEnabled())
                .maxFailedLoginAttempts(base.maxFailedLoginAttempts())
                .lockoutDurationMs(base.lockoutDurationMs())
                .passwordHashingEnabled(base.passwordHashingEnabled())
                .auditLoggingEnabled(base.auditLoggingEnabled())
                .securityHeadersEnabled(base.securityHeadersEnabled())
                .minTlsVersion(base.minTlsVersion());

        String authVal = getPropertyOrEnv("junifydb.security.auth-enabled", "JUNIFYDB_SECURITY_AUTH_ENABLED");
        if (authVal == null) {
            authVal = getPropertyOrEnv("junifydb.security.authEnabled", "JUNIFYDB_SECURITY_AUTHENABLED");
        }
        if (authVal != null && !authVal.isBlank()) {
            builder.authEnabled(Boolean.parseBoolean(authVal.trim()));
        }

        String apiKeyVal = getPropertyOrEnv("junifydb.security.api-key", "JUNIFYDB_SECURITY_API_KEY");
        if (apiKeyVal == null) {
            apiKeyVal = getPropertyOrEnv("junifydb.security.apiKey", "JUNIFYDB_SECURITY_APIKEY");
        }
        if (apiKeyVal != null && !apiKeyVal.isBlank()) {
            builder.apiKey(apiKeyVal.trim());
        }

        String userVal = getPropertyOrEnv("junifydb.security.admin-username", "JUNIFYDB_SECURITY_ADMIN_USERNAME");
        if (userVal == null) {
            userVal = getPropertyOrEnv("junifydb.security.adminUsername", "JUNIFYDB_SECURITY_ADMINUSERNAME");
        }
        if (userVal != null && !userVal.isBlank()) {
            builder.adminUsername(userVal.trim());
        }

        String passVal = getPropertyOrEnv("junifydb.security.admin-password", "JUNIFYDB_SECURITY_ADMIN_PASSWORD");
        if (passVal == null) {
            passVal = getPropertyOrEnv("junifydb.security.adminPassword", "JUNIFYDB_SECURITY_ADMINPASSWORD");
        }
        if (passVal != null && !passVal.isBlank()) {
            builder.adminPassword(passVal.trim());
        }

        String corsVal = getPropertyOrEnv("junifydb.security.cors-enabled", "JUNIFYDB_SECURITY_CORS_ENABLED");
        if (corsVal == null) {
            corsVal = getPropertyOrEnv("junifydb.security.corsEnabled", "JUNIFYDB_SECURITY_CORSENABLED");
        }
        if (corsVal != null && !corsVal.isBlank()) {
            builder.corsEnabled(Boolean.parseBoolean(corsVal.trim()));
        }

        String originsVal = getPropertyOrEnv("junifydb.security.allowed-origins", "JUNIFYDB_SECURITY_ALLOWED_ORIGINS");
        if (originsVal == null) {
            originsVal = getPropertyOrEnv("junifydb.security.allowedOrigins", "JUNIFYDB_SECURITY_ALLOWEDORIGINS");
        }
        if (originsVal != null && !originsVal.isBlank()) {
            builder.allowedOrigins(originsVal.trim());
        }

        String csrfVal = getPropertyOrEnv("junifydb.security.csrf-enabled", "JUNIFYDB_SECURITY_CSRF_ENABLED");
        if (csrfVal != null && !csrfVal.isBlank()) {
            builder.csrfEnabled(Boolean.parseBoolean(csrfVal.trim()));
        }

        String rateLimitVal = getPropertyOrEnv("junifydb.security.rate-limit-enabled", "JUNIFYDB_SECURITY_RATE_LIMIT_ENABLED");
        if (rateLimitVal != null && !rateLimitVal.isBlank()) {
            builder.rateLimitEnabled(Boolean.parseBoolean(rateLimitVal.trim()));
        }

        String rpmVal = getPropertyOrEnv("junifydb.security.rate-limit-rpm", "JUNIFYDB_SECURITY_RATE_LIMIT_RPM");
        if (rpmVal != null && !rpmVal.isBlank()) {
            try {
                builder.rateLimitRequestsPerMinute(Integer.parseInt(rpmVal.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String bruteForceVal = getPropertyOrEnv("junifydb.security.brute-force-enabled", "JUNIFYDB_SECURITY_BRUTE_FORCE_ENABLED");
        if (bruteForceVal != null && !bruteForceVal.isBlank()) {
            builder.bruteForceProtectionEnabled(Boolean.parseBoolean(bruteForceVal.trim()));
        }

        String maxFailedVal = getPropertyOrEnv("junifydb.security.max-failed-attempts", "JUNIFYDB_SECURITY_MAX_FAILED_ATTEMPTS");
        if (maxFailedVal != null && !maxFailedVal.isBlank()) {
            try {
                builder.maxFailedLoginAttempts(Integer.parseInt(maxFailedVal.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String secHeadersVal = getPropertyOrEnv("junifydb.security.headers-enabled", "JUNIFYDB_SECURITY_HEADERS_ENABLED");
        if (secHeadersVal != null && !secHeadersVal.isBlank()) {
            builder.securityHeadersEnabled(Boolean.parseBoolean(secHeadersVal.trim()));
        }

        return builder.build();
    }

    private static String getPropertyOrEnv(String propKey, String envKey) {
        String val = System.getProperty(propKey);
        if (val != null && !val.isBlank()) {
            return val;
        }
        return System.getenv(envKey);
    }
}
