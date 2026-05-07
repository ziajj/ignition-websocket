package cn.coderise.ignition.websocket.gateway.ws;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 * WebSocket Servlet that registers our WebSocket endpoint.
 *
 * This servlet is registered to /ws/smart-control path.
 */
public class SmartControlWebSocketServlet extends WebSocketServlet {

    private static final LoggerEx log = LoggerEx.newBuilder().build("websocket.gateway.ws.SmartControlWebSocketServlet");

    @Override
    public void configure(WebSocketServletFactory factory) {
        // Register our WebSocket endpoint class
        factory.register(WebSocketEndpoint.class);
        log.info("WebSocket endpoint registered: " + WebSocketEndpoint.class.getName());
    }
}