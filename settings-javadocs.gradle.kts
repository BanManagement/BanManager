pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "BanManagerJavadocs"

include(":BanManagerCommon")
include(":BanManagerLibs")

project(":BanManagerCommon").projectDir = file("common")
project(":BanManagerLibs").projectDir = file("libs")
