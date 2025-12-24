plugins {
    base
}

description = "E2E tests for BanManager using Docker and Mineflayer"

// Fabric version configurations
data class FabricVersion(val mcVersion: String, val javaImage: String, val fabricLoader: String)

val fabricVersions = listOf(
    FabricVersion("1.20.1", "java17", "0.16.10"),
    FabricVersion("1.21.1", "java21", "0.16.9"),
    FabricVersion("1.21.4", "java21", "0.16.9")
)

// Helper function to create platform test tasks
fun createPlatformTestTask(
    taskName: String,
    platformDir: String,
    pluginTask: String,
    description: String,
    environment: Map<String, String> = emptyMap()
) {
    tasks.register<Exec>(taskName) {
        group = "verification"
        this.description = description

        dependsOn(pluginTask)

        workingDir = file("platforms/$platformDir")

        // Set environment variables for docker-compose
        environment.forEach { (key, value) ->
            environment(key, value)
        }

        commandLine(
            "docker", "compose", "up",
            "--build",
            "--abort-on-container-exit",
            "--exit-code-from", "tests"
        )

        doLast {
            exec {
                workingDir = file("platforms/$platformDir")
                commandLine("docker", "compose", "down", "-v")
                isIgnoreExitValue = true
            }
        }
    }
}

// Bukkit/Paper E2E tests
createPlatformTestTask(
    "testBukkit",
    "bukkit",
    ":BanManagerBukkit:shadowJar",
    "Run Bukkit E2E tests in Docker"
)

// Fabric version-specific E2E tests
fabricVersions.forEach { version ->
    val versionSuffix = version.mcVersion.replace(".", "_")

    createPlatformTestTask(
        "testFabric_${versionSuffix}",
        "fabric",
        ":fabric:${version.mcVersion}:remapJar",
        "Run Fabric ${version.mcVersion} E2E tests in Docker",
        mapOf(
            "MC_VERSION" to version.mcVersion,
            "JAVA_IMAGE" to version.javaImage,
            "FABRIC_LOADER" to version.fabricLoader
        )
    )
}

// Fabric E2E tests - runs latest version (1.21.4)
createPlatformTestTask(
    "testFabric",
    "fabric",
    ":fabric:1.21.4:remapJar",
    "Run Fabric E2E tests in Docker (latest: 1.21.4)",
    mapOf(
        "MC_VERSION" to "1.21.4",
        "JAVA_IMAGE" to "java21",
        "FABRIC_LOADER" to "0.16.9"
    )
)

// Test all Fabric versions
tasks.register("testFabricAll") {
    group = "verification"
    description = "Run Fabric E2E tests for all supported MC versions"

    fabricVersions.forEach { version ->
        val versionSuffix = version.mcVersion.replace(".", "_")
        dependsOn("testFabric_${versionSuffix}")
    }
}

tasks.register("testAll") {
    group = "verification"
    description = "Run E2E tests for all platforms"

    dependsOn("testBukkit", "testFabric")
}

// Backward compatibility - "test" now runs Bukkit tests
tasks.register("test") {
    group = "verification"
    description = "Run Bukkit E2E tests (alias for testBukkit)"
    dependsOn("testBukkit")
}

// Helper tasks for debugging
tasks.register<Exec>("startBukkit") {
    group = "verification"
    description = "Start the Bukkit test server without running tests (for debugging)"

    dependsOn(":BanManagerBukkit:shadowJar")

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "up", "-d", "mariadb", "paper")
}

tasks.register<Exec>("stopBukkit") {
    group = "verification"
    description = "Stop the Bukkit test server"

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsBukkit") {
    group = "verification"
    description = "Show Bukkit server logs"

    workingDir = file("platforms/bukkit")
    commandLine("docker", "compose", "logs", "-f", "paper")
}

// Helper tasks for Fabric debugging (version-aware)
// Helper function to create versioned Fabric debug tasks
fun createFabricDebugTasks(mcVersion: String, javaImage: String, fabricLoader: String) {
    val versionSuffix = mcVersion.replace(".", "_")
    val envVars = mapOf(
        "MC_VERSION" to mcVersion,
        "JAVA_IMAGE" to javaImage,
        "FABRIC_LOADER" to fabricLoader
    )

    tasks.register<Exec>("startFabric_${versionSuffix}") {
        group = "verification"
        description = "Start the Fabric $mcVersion test server without running tests (for debugging)"

        dependsOn(":fabric:${mcVersion}:remapJar")

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "up", "-d", "mariadb", "fabric")
    }

    tasks.register<Exec>("stopFabric_${versionSuffix}") {
        group = "verification"
        description = "Stop the Fabric $mcVersion test server"

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "down", "-v")
        isIgnoreExitValue = true
    }

    tasks.register<Exec>("logsFabric_${versionSuffix}") {
        group = "verification"
        description = "Show Fabric $mcVersion server logs"

        workingDir = file("platforms/fabric")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "logs", "-f", "fabric")
    }
}

// Create debug tasks for each Fabric version
fabricVersions.forEach { version ->
    createFabricDebugTasks(version.mcVersion, version.javaImage, version.fabricLoader)
}

// Default Fabric debug tasks (latest version)
tasks.register<Exec>("startFabric") {
    group = "verification"
    description = "Start the Fabric test server without running tests (for debugging) - latest: 1.21.4"

    dependsOn(":fabric:1.21.4:remapJar")

    workingDir = file("platforms/fabric")
    environment("MC_VERSION", "1.21.4")
    environment("JAVA_IMAGE", "java21")
    environment("FABRIC_LOADER", "0.16.9")
    commandLine("docker", "compose", "up", "-d", "mariadb", "fabric")
}

tasks.register<Exec>("stopFabric") {
    group = "verification"
    description = "Stop the Fabric test server"

    workingDir = file("platforms/fabric")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsFabric") {
    group = "verification"
    description = "Show Fabric server logs"

    workingDir = file("platforms/fabric")
    commandLine("docker", "compose", "logs", "-f", "fabric")
}

tasks.named("clean") {
    doLast {
        // Clean up all platform Docker resources
        listOf("bukkit", "fabric").forEach { platform ->
            exec {
                workingDir = file("platforms/$platform")
                commandLine("docker", "compose", "down", "-v", "--rmi", "local")
                isIgnoreExitValue = true
            }
        }
    }
}
