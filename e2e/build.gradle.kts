plugins {
    base
}

description = "E2E tests for BanManager using Docker and Mineflayer"

// Helper function to create platform test tasks
fun createPlatformTestTask(
    taskName: String,
    platformDir: String,
    pluginTask: String,
    description: String
) {
    tasks.register<Exec>(taskName) {
        group = "verification"
        this.description = description

        dependsOn(pluginTask)

        workingDir = file("platforms/$platformDir")
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

tasks.register("testAll") {
    group = "verification"
    description = "Run E2E tests for all working platforms (currently Bukkit only)"

    dependsOn("testBukkit")
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

tasks.named("clean") {
    doLast {
        // Clean up all platform Docker resources
        listOf("bukkit").forEach { platform ->
            exec {
                workingDir = file("platforms/$platform")
                commandLine("docker", "compose", "down", "-v", "--rmi", "local")
                isIgnoreExitValue = true
            }
        }
    }
}
