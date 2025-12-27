plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.0.0-beta4")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:5.2.5")
    implementation("org.spongepowered:spongegradle-plugin-development:2.3.0")
    implementation("fabric-loom:fabric-loom.gradle.plugin:1.9.2")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.30.0")
}
