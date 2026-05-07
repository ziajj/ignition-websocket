package cn.coderise.ignition.websocket.gateway.config;

import com.inductiveautomation.ignition.gateway.config.PersistentResourceConfig;
import com.inductiveautomation.ignition.gateway.config.ResourceConfigMeta;

/**
 * WebSocket module settings.
 */
public record WebSocketSettings(
    int port,
    int maxConnections,
    int sessionTimeoutMinutes,
    boolean requireAuthentication,
    String allowedOrigins
) implements PersistentResourceConfig {

    public static final WebSocketSettings DEFAULT = new WebSocketSettings(
        8088,        // port (same as Gateway HTTP port)
        100,         // maxConnections
        30,          // sessionTimeoutMinutes
        true,        // requireAuthentication
        "*"          // allowedOrigins (all origins for development)
    );

    public static final ResourceConfigMeta<WebSocketSettings> META = ResourceConfigMeta.newBuilder(WebSocketSettings.class)
        .typeId("cn.coderise.ignition.websocket.settings")
        .name("WebSocketSettings")
        .description("WebSocket Module Configuration")
        .build();
}