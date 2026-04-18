import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

mavenPublishing {
    publishToMavenCentral()
    if (project.hasProperty("signingInMemoryKey")) {
        signAllPublications()
    }

    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))

    pom {
        name.set("BanManagerBukkit")
        description.set("BanManager for Bukkit")
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
        name = "paper"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "ryzr-repo"
        url = uri("https://cdn.rawgit.com/Rayzr522/maven-repo/master/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io/")
    }
    maven {
        name = "Scarsz-Nexus"
        url = uri("https://nexus.scarsz.me/content/groups/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    "api"(project(":BanManagerCommon")) {
        isTransitive = true
    }
    "compileOnly"("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    "compileOnly"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "compileOnly"("net.kyori:adventure-text-serializer-gson:${Versions.ADVENTURE}")
    "compileOnly"("net.kyori:adventure-text-serializer-json:${Versions.ADVENTURE}")
    "compileOnly"("me.clip:placeholderapi:2.12.2")
    "shadeOnly"("org.bstats:bstats-bukkit:3.2.1")
    "shadeOnly"("org.slf4j:slf4j-api:2.0.17")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"] as String

    inputs.property("internalVersion", internalVersion)

    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.bukkit.BMBukkitPlugin")
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

    archiveBaseName.set("BanManagerBukkit")
    archiveClassifier.set("")
    archiveVersion.set("")

    dependencies {
        include(dependency(":BanManagerCommon"))
        include(dependency(":BanManagerLibs"))

        include(dependency("org.bstats:.*:.*"))
        include(dependency("org.slf4j:.*:.*"))
    }

    // Relocate at task level so resource paths and service file contents are
    // also rewritten (the SLF4J 2.x ServiceLoader needs the META-INF/services
    // entry to be relocated to the shaded package).
    relocate("org.bstats", "me.confuser.banmanager.common.bstats")
    relocate("org.slf4j", "me.confuser.banmanager.common.slf4j")

    // Required for SLF4J 2.x service discovery: merges + relocates the
    // META-INF/services/org.slf4j.spi.SLF4JServiceProvider entry.
    mergeServiceFiles()

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    // Keep META-INF/services so the relocated SLF4J 2.x ServiceLoader can find
    // BanManagerSlf4jServiceProvider; shaded deps here ship no service files.
    exclude("META-INF/maven/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("bungeecord.yml")
    exclude("velocity.yml")

    minimize {
        exclude(dependency("org.bstats:.*:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
