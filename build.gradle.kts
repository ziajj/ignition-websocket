import java.util.concurrent.TimeUnit

plugins {
    base
    id("io.ia.sdk.modl") version("0.5.0")
}

allprojects {
    version = "1.0.0"
    group = "cn.coderise.ignition.websocket"
}

val moduleVendorName = "Coderise IOT Technology (Suzhou) Co., Ltd"

ignitionModule {
    fileName.set("IgnitionWebSocket")

    name.set("Ignition WebSocket Module")
    id.set("cn.coderise.ignition.websocket")
    moduleVersion.set("${project.version}")
    moduleDescription.set("WebSocket Module for real-time device control with Jython scripting")
    requiredIgnitionVersion.set("8.1.0")
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":gateway" to "G",
            ":common" to "G"
        )
    )

    hooks.putAll(
        mapOf(
            "cn.coderise.ignition.websocket.gateway.WebSocketGatewayHook" to "G"
        )
    )
    skipModlSigning.set(true)
}

// Custom task to inject vendor information
val injectVendorInfo by tasks.registering {
    dependsOn(":writeModuleXml")
    doLast {
        val moduleXmlFile = file("build/moduleContent/module.xml")
        if (moduleXmlFile.exists()) {
            var content = moduleXmlFile.readText()
            if (!content.contains("<vendor>")) {
                content = content.replace(
                    "</description>",
                    "</description>\n\t\t<vendor>$moduleVendorName</vendor>"
                )
            }
            content = content.replace("<freeModule>false</freeModule>", "<freeModule>true</freeModule>")
            moduleXmlFile.writeText(content)
        }
    }
}

tasks.named("zipModule") {
    dependsOn(injectVendorInfo)
}

val deepClean by tasks.registering {
    dependsOn(allprojects.map { "${it.path}:clean" })
    description = "Executes clean tasks and remove plugin caches."
    doLast {
        delete(file(".gradle"))
    }
}
