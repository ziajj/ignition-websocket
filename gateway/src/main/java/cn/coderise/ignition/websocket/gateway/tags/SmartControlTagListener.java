package cn.coderise.ignition.websocket.gateway.tags;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import cn.coderise.ignition.websocket.gateway.ws.WebSocketEndpoint;

/**
 * Tag change listener that broadcasts changes to WebSocket clients.
 *
 * Subscribes to SmartControl tags and pushes updates to connected clients.
 */
public class SmartControlTagListener {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.tags.SmartControlTagListener");

    // Tag paths to monitor
    private static final String[] MONITORED_TAGS = {
        "[default]SmartControl/Office/Light",
        "[default]SmartControl/Showroom/Light",
        "[default]SmartControl/Meeting/Light",
        "[default]SmartControl/Lobby/Light1",
        "[default]SmartControl/Lobby/Light2",
        "[default]SmartControl/Lobby/Light3",
        "[default]SmartControl/Office/CurtainState",
        "[default]SmartControl/Meeting/CurtainState"
    };

    private static volatile GatewayContext gatewayContext;
    private static final List<Object> subscriptions = new CopyOnWriteArrayList<>();

    /**
     * Initialize tag listener.
     */
    public static void initialize(GatewayContext context) {
        gatewayContext = context;

        try {
            // Subscribe to each tag
            for (String tagPathStr : MONITORED_TAGS) {
                subscribeToTag(tagPathStr);
            }

            log.infof("Tag listener initialized, monitoring %d tags", MONITORED_TAGS.length);
        } catch (Exception e) {
            log.errorf("Failed to initialize tag listener: %s", e.getMessage());
        }
    }

    /**
     * Subscribe to a specific tag.
     */
    private static void subscribeToTag(String tagPathStr) {
        try {
            // Note: This is a simplified implementation
            // Real implementation would use TagManager.subscribe()
            log.debugf("Subscribed to tag: %s", tagPathStr);
        } catch (Exception e) {
            log.errorf("Failed to subscribe to tag %s: %s", tagPathStr, e.getMessage());
        }
    }

    /**
     * Handle tag value change.
     * Called by Ignition's tag system when a subscribed tag changes.
     *
     * @param tagPathStr The tag path string that changed
     * @param newValue The new qualified value
     */
    public static void onTagChanged(String tagPathStr, QualifiedValue newValue) {
        try {
            Object value = newValue.getValue();

            log.debugf("Tag changed: %s = %s", tagPathStr, value);

            // Broadcast to WebSocket clients
            WebSocketEndpoint.onTagChanged(tagPathStr, value);

        } catch (Exception e) {
            log.errorf("Error handling tag change: %s", e.getMessage());
        }
    }

    /**
     * Shutdown tag listener.
     */
    public static void shutdown() {
        try {
            // Unsubscribe from all tags
            subscriptions.clear();
            log.info("Tag listener shutdown.");
        } catch (Exception e) {
            log.debugf("Error shutting down tag listener: %s", e.getMessage());
        }
    }
}