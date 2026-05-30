plugins {
    `java-library`
    id("com.gradleup.shadow")
}

description = "Sample consumer plugin that exercises every public BanManager API service"

applyCommonConfiguration()

repositories {
    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    compileOnly(project(":BanManagerAPI"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
}

tasks.named<Copy>("processResources") {
    val pluginVersion = project.version.toString()
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("plugin.yml") {
        expand("pluginVersion" to pluginVersion)
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("BanManagerSamplePlugin")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}
