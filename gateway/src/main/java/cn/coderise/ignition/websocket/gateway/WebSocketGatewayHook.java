package cn.coderise.ignition.websocket.gateway;

import java.util.Optional;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.config.SingletonResourceHandler;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import cn.coderise.ignition.websocket.WebSocketModule;
import cn.coderise.ignition.websocket.gateway.config.WebSocketSettings;
import cn.coderise.ignition.websocket.gateway.ws.SmartControlWebSocketServlet;
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
    private static volatile SingletonResourceHandler<WebSocketSettings> settingsHandler;

    private ServletContextHandler wsContextHandler;

    @Override
    public void setup(GatewayContext context) {
        gatewayContext = context;

        log.info("Setting up Ignition WebSocket module.");

        // Register localized properties
        BundleUtil.get().addBundle("WebSocket", getClass(), "WebSocket");

        // Setup settings resource handler for configuration persistence
        settingsHandler = SingletonResourceHandler.newBuilder(WebSocketSettings.META)
                .context(context)
                .onChange(settings -> {
                    log.info("WebSocket settings changed, applying new configuration");
                    WebSocketEndpoint.applySettings(settings);
                })
                .build();

        // Register WebSocket servlet
        registerWebSocketServlet(context);

        log.info("Ignition WebSocket module setup completed.");
    }

    /**
     * Register WebSocket servlet to Jetty.
     */
    private void registerWebSocketServlet(GatewayContext context) {
        try {
            // Create ServletContextHandler for WebSocket
            wsContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            wsContextHandler.setContextPath("/ws");

            // Create WebSocket servlet holder
            ServletHolder wsHolder = new ServletHolder("smart-control", new SmartControlWebSocketServlet());

            // Add servlet to handler
            wsContextHandler.addServlet(wsHolder, "/smart-control");

            // Register with Ignition's WebResourceManager
            context.getWebResourceManager().addContextHandler(wsContextHandler);

            log.info("WebSocket servlet registered at /ws/smart-control");
        } catch (Exception e) {
            log.errorf("Failed to register WebSocket servlet: %s", e.getMessage());
        }
    }

    @Override
    public void startup(LicenseState activationState) {
        log.info("Starting up Ignition WebSocket Gateway Hook!");

        // Start the settings handler
        if (settingsHandler != null) {
            settingsHandler.startup();
        }

        // Initialize WebSocket endpoint
        WebSocketEndpoint.startup(gatewayContext);

        // Apply current settings
        if (settingsHandler != null) {
            WebSocketSettings currentSettings = settingsHandler.getResource();
            log.infof("Applying WebSocket settings: maxConnections=%d, requireAuth=%s",
                currentSettings.maxConnections(),
                currentSettings.requireAuthentication());
            WebSocketEndpoint.applySettings(currentSettings);
        }

        log.info("Ignition WebSocket Module started successfully.");
    }

    @Override
    public void shutdown() {
        log.info("Shutting down Ignition WebSocket module.");

        // Shutdown WebSocket endpoint
        WebSocketEndpoint.shutdown();

        // Remove WebSocket context handler
        if (wsContextHandler != null && gatewayContext != null) {
            try {
                gatewayContext.getWebResourceManager().removeContextHandler(wsContextHandler);
            } catch (Exception e) {
                log.debugf("Error removing context handler: %s", e.getMessage());
            }
        }

        // Shutdown settings handler
        if (settingsHandler != null) {
            settingsHandler.shutdown();
        }

        BundleUtil.get().removeBundle("WebSocket");
    }

    @Override
    public void mountRouteHandlers(RouteGroup routeGroup) {
        // WebSocket endpoint is registered via Jetty ServletContextHandler, not RouteGroup
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

    /**
     * Get current WebSocket settings.
     */
    public static WebSocketSettings getSettings() {
        if (settingsHandler == null) {
            return WebSocketSettings.DEFAULT;
        }
        return settingsHandler.getResource();
    }
}