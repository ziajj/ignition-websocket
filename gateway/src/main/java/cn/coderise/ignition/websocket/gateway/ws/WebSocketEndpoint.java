package cn.coderise.ignition.websocket.gateway.ws;

import java.io.BufferedReader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.inductiveautomation.ignition.common.gson.Gson;
import com.inductiveautomation.ignition.common.gson.JsonObject;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod;
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext;
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WebSocket Endpoint for real-time device control.
 *
 * Handles:
 * - Client connections via REST API
 * - Control commands from mobile frontend
 * - Tag change broadcasts to all connected clients
 */
public class WebSocketEndpoint {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.ws.WebSocketEndpoint");
    private static final Gson GSON = new Gson();

    // All active sessions
    private static final Set<String> sessions = ConcurrentHashMap.newKeySet();

    // Gateway context for Tag access
    private static volatile GatewayContext gatewayContext;

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
        sessions.clear();
        log.info("WebSocket endpoint shutdown.");
    }

    /**
     * Mount REST routes for WebSocket-like communication.
     */
    public static void mountRoutes(RouteGroup routes, GatewayContext context) {
        // POST /command - Send control command
        routes.newRoute("/command")
            .type(RouteGroup.TYPE_JSON)
            .method(HttpMethod.POST)
            .handler(WebSocketEndpoint::handleCommand)
            .mount();

        // GET /status - Get current device status
        routes.newRoute("/status")
            .type(RouteGroup.TYPE_JSON)
            .method(HttpMethod.GET)
            .handler(WebSocketEndpoint::getStatus)
            .mount();

        // GET /connections - Get connection count
        routes.newRoute("/connections")
            .type(RouteGroup.TYPE_JSON)
            .method(HttpMethod.GET)
            .handler(WebSocketEndpoint::getConnections)
            .mount();

        log.info("WebSocket routes mounted at /data/ws-module/");
    }

    /**
     * Handle control command from frontend.
     */
    private static Object handleCommand(RequestContext request, HttpServletResponse response) throws Exception {
        HttpServletRequest req = request.getRequest();
        String body = readRequestBody(req);
        log.debugf("Received command: %s", body);

        try {
            JsonObject cmd = GSON.fromJson(body, JsonObject.class);
            String deviceId = cmd.get("deviceId").getAsString();
            String action = cmd.get("action").getAsString();

            // TODO: Call Jython handler
            JsonObject result = processCommand(deviceId, action);
            return GSON.toJson(result);
        } catch (Exception e) {
            log.errorf("Error processing command: %s", e.getMessage());
            response.setStatus(400);
            JsonObject error = new JsonObject();
            error.addProperty("type", "error");
            error.addProperty("message", e.getMessage());
            return GSON.toJson(error);
        }
    }

    /**
     * Get current device status.
     */
    private static Object getStatus(RequestContext request, HttpServletResponse response) throws Exception {
        JsonObject status = new JsonObject();
        status.addProperty("type", "status");
        status.addProperty("message", "Status endpoint ready");
        status.addProperty("connectionCount", sessions.size());
        return GSON.toJson(status);
    }

    /**
     * Get connection count.
     */
    private static Object getConnections(RequestContext request, HttpServletResponse response) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("count", sessions.size());
        return GSON.toJson(result);
    }

    /**
     * Read request body as string.
     */
    private static String readRequestBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Broadcast message to all connected sessions.
     */
    public static void broadcast(String eventType, JsonObject data) {
        log.debugf("Broadcasting to %d sessions: %s", sessions.size(), eventType);
    }

    /**
     * Process incoming command (placeholder for Jython integration).
     */
    private static JsonObject processCommand(String deviceId, String action) {
        JsonObject result = new JsonObject();
        result.addProperty("type", "command_result");
        result.addProperty("deviceId", deviceId);
        result.addProperty("action", action);
        result.addProperty("success", true);
        result.addProperty("message", "Command processed (Jython integration pending)");
        return result;
    }

    /**
     * Get current connection count.
     */
    public static int getConnectionCount() {
        return sessions.size();
    }
}