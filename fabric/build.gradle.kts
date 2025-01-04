import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
  `java-library`
  `maven-publish`
  `signing`
  `fabric-loom`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("BanManagerFabric")
                description.set("BanManager for Fabric")
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
    }
}

signing {
    if (project.findProperty("signingKey")?.toString()?.toBoolean() == true) {
        useInMemoryPgpKeys(findProperty("signingKey")?.toString(), findProperty("signingPassword")?.toString())

        sign(publishing.publications["mavenJava"])
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
    minecraft("com.mojang:minecraft:1.21.4")
    mappings("net.fabricmc:yarn:1.21.4+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.9")

    val modules = listOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-events-interaction-v0",
        "fabric-lifecycle-events-v1",
        "fabric-message-api-v1",
        "fabric-networking-api-v1",
        "fabric-entity-events-v1"
    )

    modules.forEach {
        modImplementation(fabricApi.module(it, "0.111.0+1.21.4"))
    }

    modImplementation("me.lucko:fabric-permissions-api:0.3.1")

    api(project(":BanManagerCommon"))
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

    inputs.property("internalVersion", internalVersion)

    filesMatching(listOf("plugin.yml", "fabric.mod.json")) {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.fabric.BMFabricPlugin")
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
    archiveClassifier.set("")
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
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
