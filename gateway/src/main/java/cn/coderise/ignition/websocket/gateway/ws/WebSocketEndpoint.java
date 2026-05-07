package cn.coderise.ignition.websocket.gateway.ws;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import cn.coderise.ignition.websocket.gateway.config.WebSocketSettings;

/**
 * WebSocket Endpoint for real-time device control.
 *
 * Handles:
 * - Client connections
 * - Control commands from mobile frontend
 * - Tag change broadcasts to all connected clients
 */
@WebSocket
public class WebSocketEndpoint {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.ws.WebSocketEndpoint");

    // All active WebSocket connections
    private static final Set<Session> connections = ConcurrentHashMap.newKeySet();

    // Gateway context for Tag access
    private static volatile GatewayContext gatewayContext;

    // Current settings
    private static volatile WebSocketSettings settings = WebSocketSettings.DEFAULT;

    // Current session (for this endpoint instance)
    private Session session;

    /**
     * Initialize the WebSocket endpoint.
     */
    public static void startup(GatewayContext context) {
        gatewayContext = context;
        log.info("WebSocket endpoint initialized.");
    }

    /**
     * Shutdown the WebSocket endpoint.
     */
    public static void shutdown() {
        // Close all connections
        for (Session s : connections) {
            try {
                s.close();
            } catch (Exception e) {
                log.debugf("Error closing session: %s", e.getMessage());
            }
        }
        connections.clear();
        log.info("WebSocket endpoint shutdown.");
    }

    /**
     * Apply new settings.
     */
    public static void applySettings(WebSocketSettings newSettings) {
        settings = newSettings;
        log.infof("Applied settings: maxConnections=%d, requireAuth=%s",
            newSettings.maxConnections(),
            newSettings.requireAuthentication());
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        this.session = session;

        // Check connection limit
        if (connections.size() >= settings.maxConnections()) {
            log.warn("Max connections reached, rejecting new connection.");
            session.close(1008, "Max connections reached");
            return;
        }

        // TODO: Authentication check
        // if (settings.requireAuthentication()) {
        //     String token = session.getUpgradeRequest().getParameter("token");
        //     if (!validateToken(token)) {
        //         session.close(1008, "Unauthorized");
        //         return;
        //     }
        // }

        connections.add(session);
        log.infof("WebSocket connected: %s (total: %d)", session.getRemoteAddress(), connections.size());

        // Send initial status
        sendTo(session, "{\"type\":\"connected\",\"message\":\"Welcome to Ignition WebSocket\"}");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        log.debugf("Received message: %s", message);

        try {
            // TODO: Call Jython handler
            // Object result = JythonBridge.invokeHandler("onMessage", session, message);

            // For now, just echo back
            String response = processMessage(message);
            sendTo(session, response);
        } catch (Exception e) {
            log.errorf("Error processing message: %s", e.getMessage());
            sendTo(session, "{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        connections.remove(session);
        log.infof("WebSocket closed: %s (code: %d, reason: %s, remaining: %d)",
            session.getRemoteAddress(), statusCode, reason, connections.size());
    }

    /**
     * Send message to a specific session.
     */
    private static void sendTo(Session session, String json) {
        if (session != null && session.isOpen()) {
            session.getRemote().sendStringByFuture(json);
        }
    }

    /**
     * Broadcast message to all connected sessions.
     */
    public static void broadcast(String json) {
        for (Session s : connections) {
            sendTo(s, json);
        }
    }

    /**
     * Process incoming message (placeholder for Jython integration).
     */
    private String processMessage(String message) {
        // TODO: Implement Jython call
        return "{\"type\":\"echo\",\"message\":\"" + message + "\"}";
    }

    /**
     * Get current connection count.
     */
    public static int getConnectionCount() {
        return connections.size();
    }
}