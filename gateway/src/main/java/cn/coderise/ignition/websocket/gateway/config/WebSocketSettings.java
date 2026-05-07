package cn.coderise.ignition.websocket.gateway.config;

/**
 * WebSocket module settings (simplified).
 */
public class WebSocketSettings {

    public static final int DEFAULT_MAX_CONNECTIONS = 100;
    public static final int DEFAULT_TIMEOUT_MINUTES = 30;
    public static final boolean DEFAULT_REQUIRE_AUTH = true;

    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private int sessionTimeoutMinutes = DEFAULT_TIMEOUT_MINUTES;
    private boolean requireAuthentication = DEFAULT_REQUIRE_AUTH;
    private String allowedOrigins = "*";

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public boolean isRequireAuthentication() {
        return requireAuthentication;
    }

    public void setRequireAuthentication(boolean requireAuthentication) {
        this.requireAuthentication = requireAuthentication;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}