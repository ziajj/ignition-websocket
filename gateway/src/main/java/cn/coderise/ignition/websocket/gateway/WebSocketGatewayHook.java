package cn.coderise.ignition.websocket.gateway;

import java.util.Optional;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import cn.coderise.ignition.websocket.WebSocketModule;
import cn.coderise.ignition.websocket.gateway.ws.WebSocketEndpoint;

/**
 * Gateway hook for Ignition WebSocket Module.
 *
 * This module provides WebSocket endpoint for real-time device control:
 * - Mobile frontend connects via WebSocket
 * - Jython scripts handle business logic
 * - Tag changes are pushed to connected clients
 */
public class WebSocketGatewayHook extends AbstractGatewayModuleHook {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.WebSocketGatewayHook");

    private static volatile GatewayContext gatewayContext;

    @Override
    public void setup(GatewayContext context) {
        gatewayContext = context;

        log.info("Setting up Ignition WebSocket module.");

        // Register localized properties
        BundleUtil.get().addBundle("WebSocket", getClass(), "WebSocket");

        log.info("Ignition WebSocket module setup completed.");
    }

    @Override
    public void startup(LicenseState activationState) {
        log.info("Starting up Ignition WebSocket Gateway Hook!");

        // Initialize WebSocket endpoint
        WebSocketEndpoint.startup(gatewayContext);

        log.info("Ignition WebSocket Module started successfully.");
    }

    @Override
    public void shutdown() {
        log.info("Shutting down Ignition WebSocket module.");

        // Shutdown WebSocket endpoint
        WebSocketEndpoint.shutdown();

        BundleUtil.get().removeBundle("WebSocket");
    }

    @Override
    public void mountRouteHandlers(RouteGroup routeGroup) {
        log.info("Mounting WebSocket API routes.");
        // WebSocket endpoint is registered via Jetty ServletContextHandler
        WebSocketEndpoint.mountRoutes(routeGroup, gatewayContext);
    }

    @Override
    public Optional<String> getMountPathAlias() {
        return Optional.of(WebSocketModule.URL_ALIAS);
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }

    /**
     * Get the GatewayContext for Jython script execution.
     */
    public static GatewayContext getGatewayContext() {
        return gatewayContext;
    }
}