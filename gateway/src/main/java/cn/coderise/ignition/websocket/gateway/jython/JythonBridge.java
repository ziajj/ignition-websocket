package cn.coderise.ignition.websocket.gateway.jython;

import java.util.Arrays;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import cn.coderise.ignition.websocket.gateway.WebSocketGatewayHook;

/**
 * Bridge between Java WebSocket and Jython business logic.
 *
 * Allows Jython scripts to handle:
 * - onMessage(session, message) - process control commands
 * - onConnect(session) - return initial status
 * - onTagChanged(tagPath, value) - return broadcast message
 */
public class JythonBridge {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.jython.JythonBridge");

    // Script module path
    private static final String SCRIPT_MODULE = "ws_handlers";

    // Cached PythonInterpreter
    private static volatile PythonInterpreter interpreter;

    /**
     * Initialize the Jython interpreter.
     */
    public static void initialize() {
        try {
            if (interpreter == null) {
                interpreter = new PythonInterpreter();
                log.info("Jython interpreter initialized.");
            }
        } catch (Exception e) {
            log.errorf("Failed to initialize Jython interpreter: %s", e.getMessage());
        }
    }

    /**
     * Shutdown the Jython interpreter.
     */
    public static void shutdown() {
        if (interpreter != null) {
            try {
                interpreter.close();
            } catch (Exception e) {
                log.debugf("Error closing interpreter: %s", e.getMessage());
            }
            interpreter = null;
        }
    }

    /**
     * Invoke a Jython handler function.
     *
     * @param functionName The function name (e.g., "onMessage", "onConnect")
     * @param args The arguments to pass
     * @return The result from Jython, or null if error/not found
     */
    public static Object invokeHandler(String functionName, Object... args) {
        try {
            if (interpreter == null) {
                initialize();
            }

            // Import the module
            interpreter.exec("import " + SCRIPT_MODULE);

            // Get the function
            PyObject func = interpreter.get(SCRIPT_MODULE + "." + functionName);

            if (func == null || !func.isCallable()) {
                log.debugf("Jython function %s not found or not callable", functionName);
                return null;
            }

            // Convert args to PyObjects
            PyObject[] pyArgs = Arrays.stream(args)
                .map(Py::java2py)
                .toArray(PyObject[]::new);

            // Call the function
            PyObject result = func.__call__(pyArgs);

            // Convert result back to Java String
            return result.__str__().toString();

        } catch (Exception e) {
            log.errorf("Error invoking Jython handler %s: %s", functionName, e.getMessage());
            return null;
        }
    }

    /**
     * Process a control command via Jython.
     *
     * @param deviceId The device ID (e.g., "officeLight")
     * @param action The action (e.g., "on", "off")
     * @return Result dict from Jython, or default result if error
     */
    public static Object processCommand(String deviceId, String action) {
        Object result = invokeHandler("onMessage", deviceId, action);
        if (result == null) {
            // Return default result if Jython handler not found
            return createDefaultResult(deviceId, action);
        }
        return result;
    }

    /**
     * Get initial status on connect via Jython.
     *
     * @return Initial status dict from Jython, or empty dict if error
     */
    public static Object getInitialStatus() {
        Object result = invokeHandler("onConnect");
        if (result == null) {
            return "{}";
        }
        return result;
    }

    /**
     * Process tag change via Jython.
     *
     * @param tagPath The tag path that changed
     * @param newValue The new value
     * @return Broadcast message dict from Jython, or null if no broadcast needed
     */
    public static Object processTagChange(String tagPath, Object newValue) {
        return invokeHandler("onTagChanged", tagPath, newValue);
    }

    /**
     * Create default result when Jython handler is not available.
     */
    private static Object createDefaultResult(String deviceId, String action) {
        return String.format(
            "{\"type\":\"command_result\",\"deviceId\":\"%s\",\"action\":\"%s\",\"success\":true,\"message\":\"Jython handler not found, using default\"}",
            deviceId, action
        );
    }
}