pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "BanManager"

// Non-Fabric modules (standard includes)
include(":BanManagerCommon")
include(":BanManagerBukkit")
include(":BanManagerBungee")
include(":BanManagerSponge")
include(":BanManagerSponge7")
include(":BanManagerLibs")
include(":BanManagerVelocity")
include(":BanManagerE2E")

project(":BanManagerCommon").projectDir = file("common")
project(":BanManagerBukkit").projectDir = file("bukkit")
project(":BanManagerBungee").projectDir = file("bungee")
project(":BanManagerSponge").projectDir = file("sponge")
project(":BanManagerSponge7").projectDir = file("sponge-api7")
project(":BanManagerLibs").projectDir = file("libs")
project(":BanManagerVelocity").projectDir = file("velocity")
project(":BanManagerE2E").projectDir = file("e2e")

// Fabric module with Stonecutter multi-version support
stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    shared {
        // Define version mappings for Fabric
        version("1.20.1", "1.20.1")
        version("1.21.1", "1.21.1")
        version("1.21.4", "1.21.4")
    }

    create(":fabric")
}

// Set the fabric project directory
project(":fabric").projectDir = file("fabric")
