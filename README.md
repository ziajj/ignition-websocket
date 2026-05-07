# Ignition WebSocket Module

WebSocket Module for Ignition Gateway, enabling real-time device control with Jython scripting.

## Features

- **WebSocket Endpoint**: Real-time bidirectional communication
- **Jython Integration**: Business logic in Python scripts
- **Tag Monitoring**: Push Tag changes to connected clients
- **Authentication**: Token-based connection validation

## Architecture

```
Mobile Frontend
     │ WebSocket (ws://gateway:8088/ws/smart-control)
     ▼
Ignition Gateway (Jetty)
     │
     ├── WebSocket Module
     │     └── WebSocket Endpoint (Java)
     │           ├── onOpen → Connection management
     │           ├── onMessage → Jython handler
     │           └── Tag listener → Broadcast updates
     │
     └── Tag System ←→ PLC/Devices
```

## Build

```bash
# Build the module
./gradlew build

# Output: build/IgnitionWebSocket.modl
```

## Install

1. Copy `IgnitionWebSocket.modl` to Gateway's `modules/` directory
2. Restart Gateway or use module manager to install

## Usage

Connect to WebSocket endpoint:
```javascript
const ws = new WebSocket('ws://gateway:8088/ws/smart-control');

ws.onopen = () => {
    console.log('Connected');
    ws.send(JSON.stringify({
        deviceId: 'officeLight',
        action: 'on'
    }));
};

ws.onmessage = (event) => {
    console.log('Received:', event.data);
};
```

## Project Structure

```
ignition-websocket/
├── build.gradle.kts      # Root build config
├── settings.gradle.kts   # Project settings
├── gradle/
│   └── libs.versions.toml # Dependency versions
├── common/               # Shared code
│   └── src/main/java/
├── gateway/              # Gateway module
│   └── src/main/java/
│       └── cn/coderise/ignition/websocket/gateway/
│           ├── WebSocketGatewayHook.java
│           ├── config/
│           └── ws/
│               ├── WebSocketEndpoint.java
│               └── SmartControlWebSocketServlet.java
└── license.html
```

## License

MIT License - See [license.html](license.html)