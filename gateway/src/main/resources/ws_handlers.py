# WebSocket Handlers - Jython Business Logic
#
# This module provides the business logic for WebSocket commands.
# Modify these handlers to customize device control behavior.
#
# Functions:
#   onMessage(deviceId, action) -> dict
#   onConnect() -> dict
#   onTagChanged(tagPath, newValue) -> dict or None

import json
import system.tag

# Device ID to Tag path mapping
TAG_MAP = {
    "officeLight":    "[default]SmartControl/Office/Light",
    "showroomLight":  "[default]SmartControl/Showroom/Light",
    "meetingLight":   "[default]SmartControl/Meeting/Light",
    "lobbyLight1":    "[default]SmartControl/Lobby/Light1",
    "lobbyLight2":    "[default]SmartControl/Lobby/Light2",
    "lobbyLight3":    "[default]SmartControl/Lobby/Light3",
    "officeCurtain":  "[default]SmartControl/Office/CurtainCmd",
    "meetingCurtain": "[default]SmartControl/Meeting/CurtainCmd",
}

# Action to Tag value mapping
VALUE_MAP = {
    "on": True,
    "off": False,
    "open": 1,
    "close": 2,
    "stop": 0
}

# Reverse mapping: Tag path to Device ID
REVERSE_TAG_MAP = {
    "[default]SmartControl/Office/Light":      "officeLight",
    "[default]SmartControl/Showroom/Light":    "showroomLight",
    "[default]SmartControl/Meeting/Light":     "meetingLight",
    "[default]SmartControl/Lobby/Light1":      "lobbyLight1",
    "[default]SmartControl/Lobby/Light2":      "lobbyLight2",
    "[default]SmartControl/Lobby/Light3":      "lobbyLight3",
    "[default]SmartControl/Office/CurtainState": "officeCurtain",
    "[default]SmartControl/Meeting/CurtainState": "meetingCurtain",
}


def onMessage(deviceId, action):
    """
    Handle control command from mobile frontend.

    Args:
        deviceId: The device ID (e.g., "officeLight")
        action: The action (e.g., "on", "off")

    Returns:
        dict with result status
    """
    tagPath = TAG_MAP.get(deviceId)
    value = VALUE_MAP.get(action)

    if tagPath is None:
        return {
            "type": "command_result",
            "deviceId": deviceId,
            "action": action,
            "success": False,
            "error": "Unknown device: %s" % deviceId
        }

    if value is None:
        return {
            "type": "command_result",
            "deviceId": deviceId,
            "action": action,
            "success": False,
            "error": "Unknown action: %s" % action
        }

    try:
        # Write to Tag
        system.tag.writeBlocking([tagPath], [value])

        return {
            "type": "command_result",
            "deviceId": deviceId,
            "action": action,
            "success": True,
            "message": "Command executed"
        }
    except Exception as e:
        return {
            "type": "command_result",
            "deviceId": deviceId,
            "action": action,
            "success": False,
            "error": str(e)
        }


def onConnect():
    """
    Get initial status when client connects.

    Returns:
        dict with all device states
    """
    tagPaths = list(TAG_MAP.values())
    results = system.tag.readBlocking(tagPaths)

    devices = {}
    for i, tagPath in enumerate(tagPaths):
        # Find device ID for this tag
        deviceId = REVERSE_TAG_MAP.get(tagPath)
        if deviceId and i < len(results):
            devices[deviceId] = results[i].value

    return {
        "type": "init",
        "devices": devices
    }


def onTagChanged(tagPath, newValue):
    """
    Handle Tag value change - return broadcast message.

    Args:
        tagPath: The tag path that changed
        newValue: The new value

    Returns:
        dict to broadcast to all clients, or None to skip broadcast
    """
    deviceId = REVERSE_TAG_MAP.get(str(tagPath))

    if deviceId is None:
        # Not a device tag we care about
        return None

    return {
        "type": "status",
        "deviceId": deviceId,
        "value": newValue
    }
