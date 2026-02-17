plugins {
    base
}

description = "E2E tests for BanManager using Docker and Mineflayer"

// Fabric version configurations
data class FabricVersion(val mcVersion: String, val javaImage: String, val fabricLoader: String)

val fabricVersions = listOf(
    FabricVersion("1.20.1", "java17", "0.16.10"),
    FabricVersion("1.21.1", "java21", "0.16.9"),
    FabricVersion("1.21.4", "java21", "0.16.9"),
    FabricVersion("1.21.11", "java21", "0.17.3")
)

// Sponge version configurations
// Note: SPONGEVERSION format matches Sponge download API versions
// See: https://dl-api.spongepowered.org/v2/groups/org.spongepowered/artifacts/spongevanilla/versions
data class SpongeVersion(val mcVersion: String, val javaImage: String, val spongeVersion: String)

val spongeVersions = listOf(
    SpongeVersion("1.20.6", "java21", "1.20.6-11.0.0"),
    SpongeVersion("1.21.1", "java21", "1.21.1-12.0.2"),
    SpongeVersion("1.21.3", "java21", "1.21.3-13.0.0")
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
            providers.exec {
                workingDir = file("platforms/$platformDir")
                commandLine("docker", "compose", "down", "-v")
                isIgnoreExitValue = true
            }.result.get()
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

// Fabric E2E tests - runs latest version (1.21.11)
createPlatformTestTask(
    "testFabric",
    "fabric",
    ":fabric:1.21.11:remapJar",
    "Run Fabric E2E tests in Docker (latest: 1.21.11)",
    mapOf(
        "MC_VERSION" to "1.21.11",
        "JAVA_IMAGE" to "java21",
        "FABRIC_LOADER" to "0.17.3"
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

// Sponge version-specific E2E tests
spongeVersions.forEach { version ->
    val versionSuffix = version.mcVersion.replace(".", "_")

    createPlatformTestTask(
        "testSponge_${versionSuffix}",
        "sponge",
        ":BanManagerSponge:shadowJar",
        "Run Sponge ${version.mcVersion} E2E tests in Docker",
        mapOf(
            "MC_VERSION" to version.mcVersion,
            "JAVA_IMAGE" to version.javaImage,
            "SPONGEVERSION" to version.spongeVersion
        )
    )
}

// Sponge E2E tests - runs default version (1.20.6 / API 11)
createPlatformTestTask(
    "testSponge",
    "sponge",
    ":BanManagerSponge:shadowJar",
    "Run Sponge E2E tests in Docker (default: 1.20.6 / API 11)",
    mapOf(
        "MC_VERSION" to "1.20.6",
        "JAVA_IMAGE" to "java21",
        "SPONGEVERSION" to "1.20.6-11.0.0"
    )
)

// Test all Sponge versions
tasks.register("testSpongeAll") {
    group = "verification"
    description = "Run Sponge E2E tests for all supported API versions (11/12/13)"

    spongeVersions.forEach { version ->
        val versionSuffix = version.mcVersion.replace(".", "_")
        dependsOn("testSponge_${versionSuffix}")
    }
}

// Sponge7 (Legacy API 7 / MC 1.12.2) E2E tests
createPlatformTestTask(
    "testSponge7",
    "sponge7",
    ":BanManagerSponge7:shadowJar",
    "Run Sponge7 (legacy API 7 / MC 1.12.2) E2E tests in Docker",
    mapOf(
        "MC_VERSION" to "1.12.2",
        "JAVA_IMAGE" to "java8"
    )
)

// Velocity Proxy E2E tests
createPlatformTestTask(
    "testVelocity",
    "velocity",
    ":BanManagerVelocity:shadowJar",
    "Run Velocity proxy E2E tests in Docker"
)

// BungeeCord Proxy E2E tests
createPlatformTestTask(
    "testBungee",
    "bungee",
    ":BanManagerBungee:shadowJar",
    "Run BungeeCord proxy E2E tests in Docker"
)

tasks.register("testAll") {
    group = "verification"
    description = "Run E2E tests for all platforms"

    dependsOn("testBukkit", "testFabric", "testSponge", "testVelocity", "testBungee")
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
    description = "Start the Fabric test server without running tests (for debugging) - latest: 1.21.11"

    dependsOn(":fabric:1.21.11:remapJar")

    workingDir = file("platforms/fabric")
    environment("MC_VERSION", "1.21.11")
    environment("JAVA_IMAGE", "java21")
    environment("FABRIC_LOADER", "0.17.3")
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

// Helper function to create versioned Sponge debug tasks
fun createSpongeDebugTasks(mcVersion: String, javaImage: String, spongeVersion: String) {
    val versionSuffix = mcVersion.replace(".", "_")
    val envVars = mapOf(
        "MC_VERSION" to mcVersion,
        "JAVA_IMAGE" to javaImage,
        "SPONGEVERSION" to spongeVersion
    )

    tasks.register<Exec>("startSponge_${versionSuffix}") {
        group = "verification"
        description = "Start the Sponge $mcVersion test server without running tests (for debugging)"

        dependsOn(":BanManagerSponge:shadowJar")

        workingDir = file("platforms/sponge")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "up", "-d", "mariadb", "sponge")
    }

    tasks.register<Exec>("stopSponge_${versionSuffix}") {
        group = "verification"
        description = "Stop the Sponge $mcVersion test server"

        workingDir = file("platforms/sponge")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "down", "-v")
        isIgnoreExitValue = true
    }

    tasks.register<Exec>("logsSponge_${versionSuffix}") {
        group = "verification"
        description = "Show Sponge $mcVersion server logs"

        workingDir = file("platforms/sponge")
        envVars.forEach { (key, value) -> environment(key, value) }
        commandLine("docker", "compose", "logs", "-f", "sponge")
    }
}

// Create debug tasks for each Sponge version
spongeVersions.forEach { version ->
    createSpongeDebugTasks(version.mcVersion, version.javaImage, version.spongeVersion)
}

// Default Sponge debug tasks (API 11)
tasks.register<Exec>("startSponge") {
    group = "verification"
    description = "Start the Sponge test server without running tests (for debugging) - default: 1.20.6"

    dependsOn(":BanManagerSponge:shadowJar")

    workingDir = file("platforms/sponge")
    environment("MC_VERSION", "1.20.6")
    environment("JAVA_IMAGE", "java21")
    environment("SPONGEVERSION", "1.20.6-11.0.0")
    commandLine("docker", "compose", "up", "-d", "mariadb", "sponge")
}

tasks.register<Exec>("stopSponge") {
    group = "verification"
    description = "Stop the Sponge test server"

    workingDir = file("platforms/sponge")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsSponge") {
    group = "verification"
    description = "Show Sponge server logs"

    workingDir = file("platforms/sponge")
    commandLine("docker", "compose", "logs", "-f", "sponge")
}

// Sponge7 debug tasks
tasks.register<Exec>("startSponge7") {
    group = "verification"
    description = "Start the Sponge7 (legacy) test server without running tests (for debugging)"

    dependsOn(":BanManagerSponge7:shadowJar")

    workingDir = file("platforms/sponge7")
    commandLine("docker", "compose", "up", "-d", "mariadb", "sponge7")
}

tasks.register<Exec>("stopSponge7") {
    group = "verification"
    description = "Stop the Sponge7 test server"

    workingDir = file("platforms/sponge7")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsSponge7") {
    group = "verification"
    description = "Show Sponge7 server logs"

    workingDir = file("platforms/sponge7")
    commandLine("docker", "compose", "logs", "-f", "sponge7")
}

// Velocity debug tasks
tasks.register<Exec>("startVelocity") {
    group = "verification"
    description = "Start the Velocity proxy test environment without running tests (for debugging)"

    dependsOn(":BanManagerVelocity:shadowJar")

    workingDir = file("platforms/velocity")
    commandLine("docker", "compose", "up", "-d", "mariadb", "paper", "velocity")
}

tasks.register<Exec>("stopVelocity") {
    group = "verification"
    description = "Stop the Velocity proxy test environment"

    workingDir = file("platforms/velocity")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsVelocity") {
    group = "verification"
    description = "Show Velocity proxy logs"

    workingDir = file("platforms/velocity")
    commandLine("docker", "compose", "logs", "-f", "velocity")
}

// BungeeCord debug tasks
tasks.register<Exec>("startBungee") {
    group = "verification"
    description = "Start the BungeeCord proxy test environment without running tests (for debugging)"

    dependsOn(":BanManagerBungee:shadowJar")

    workingDir = file("platforms/bungee")
    commandLine("docker", "compose", "up", "-d", "mariadb", "paper", "bungee")
}

tasks.register<Exec>("stopBungee") {
    group = "verification"
    description = "Stop the BungeeCord proxy test environment"

    workingDir = file("platforms/bungee")
    commandLine("docker", "compose", "down", "-v")
    isIgnoreExitValue = true
}

tasks.register<Exec>("logsBungee") {
    group = "verification"
    description = "Show BungeeCord proxy logs"

    workingDir = file("platforms/bungee")
    commandLine("docker", "compose", "logs", "-f", "bungee")
}

tasks.named("clean") {
    doLast {
        // Clean up all platform Docker resources
        listOf("bukkit", "fabric", "sponge", "sponge7", "velocity", "bungee").forEach { platform ->
            providers.exec {
                workingDir = file("platforms/$platform")
                commandLine("docker", "compose", "down", "-v", "--rmi", "local")
                isIgnoreExitValue = true
            }.result.get()
        }
    }
}
