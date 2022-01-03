plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.0")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")
    implementation("org.spongepowered:SpongeGradle:0.11.5")
}