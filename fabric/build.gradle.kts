import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
  `java-library`
  `fabric-loom`
  id("com.vanniktech.maven.publish")
}

// Read version-specific properties
val minecraftVersion: String by project.extra { property("minecraft_version") as String }
val yarnMappings: String by project.extra { property("yarn_mappings") as String }
val fabricLoaderVersion: String by project.extra { property("fabric_loader") as String }
val fabricApiVersion: String by project.extra { property("fabric_api") as String }
val javaVersion: String by project.extra { property("java_version") as String }

// Stonecutter version check helper
val mcVersion = minecraftVersion.split(".").let { parts ->
    val major = parts.getOrElse(0) { "1" }.toIntOrNull() ?: 1
    val minor = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0
    val patch = parts.getOrElse(2) { "0" }.toIntOrNull() ?: 0
    Triple(major, minor, patch)
}
val isPreV21 = mcVersion.second < 21

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

// Configure Java toolchain based on MC version
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

// Stonecutter 0.7.11 handles source sets automatically with the "shared sources" fix
// No custom source set configuration needed

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))

    coordinates(artifactId = "BanManagerFabric-mc$minecraftVersion")

    pom {
        name.set("BanManagerFabric")
        description.set("BanManager for Fabric - Minecraft $minecraftVersion")
        url.set("https://github.com/BanManagement/BanManager/")
        licenses {
            license {
                name.set("Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales")
                url.set("https://github.com/BanManagement/BanManager/blob/master/LICENCE")
            }
        }
        developers {
            developer {
                id.set("confuser")
                name.set("James Mortemore")
                email.set("jamesmortemore@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/BanManagement/BanManager.git")
            developerConnection.set("scm:git:ssh://git@github.com/BanManagement/BanManager.git")
            url.set("https://github.com/BanManagement/BanManager/")
        }
    }
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // Base modules available in all versions
    val baseModules = listOf(
        "fabric-api-base",
        "fabric-events-interaction-v0",
        "fabric-lifecycle-events-v1",
        "fabric-message-api-v1",
        "fabric-networking-api-v1",
        "fabric-entity-events-v1"
    )

    baseModules.forEach {
        modImplementation(fabricApi.module(it, fabricApiVersion))
    }

    // Command API: v1 for 1.20.1, v2 for 1.21+
    if (isPreV21) {
        modImplementation(fabricApi.module("fabric-command-api-v1", fabricApiVersion))
    } else {
        modImplementation(fabricApi.module("fabric-command-api-v2", fabricApiVersion))
    }

    modImplementation("me.lucko:fabric-permissions-api:0.3.1")

    api(project(":BanManagerCommon"))
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    val internalVersion = project.ext["internalVersion"]
    val mixinJavaVersion = "JAVA_$javaVersion"
    val commandApiModule = if (isPreV21) "fabric-command-api-v1" else "fabric-command-api-v2"

    inputs.property("internalVersion", internalVersion)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("mixinJavaVersion", mixinJavaVersion)
    inputs.property("commandApiModule", commandApiModule)

    filesMatching(listOf("plugin.yml", "fabric.mod.json")) {
        expand(
            "internalVersion" to internalVersion,
            "mainPath" to "me.confuser.banmanager.fabric.BMFabricPlugin",
            "minecraftVersion" to minecraftVersion,
            "commandApiModule" to commandApiModule
        )
    }

    filesMatching("banmanager.mixins.json") {
        expand("mixinJavaVersion" to mixinJavaVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

    archiveBaseName.set("BanManagerFabric")
    archiveClassifier.set("mc$minecraftVersion")
    archiveVersion.set("")

    dependencies {
        include(dependency(":BanManagerCommon"))
        include(dependency(":BanManagerLibs"))
    }
    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("/mappings/*")

    minimize()
}

tasks.named<RemapJarTask>("remapJar") {
    dependsOn(tasks.named<ShadowJar>("shadowJar"))

    inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
    archiveBaseName.set("BanManagerFabric")
    archiveClassifier.set("mc$minecraftVersion")
    archiveVersion.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
