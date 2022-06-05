import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency


plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
    id("net.kyori.blossom") version "1.2.0"
    `maven-publish`
    signing
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("BanManagerSponge")
                description.set("BanManager for Sponge")
                url.set("https://github.com/BanManagement/BanManager/")
                licenses {
                    license {
                        name.set("Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales")
                        url.set("https://github.com/BanManagement/BanManager/blob/master/LICENCE")
                    }
                }
                developers {
                    developer {
                        id.set("confuser>")
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

blossom {
    replaceToken("@projectVersion@", project.ext["internalVersion"])
}

sponge {
    apiVersion("7.2.0")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }

    license("Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales")

    plugin("banmanager") {
        displayName("BanManager")
        entrypoint("me.confuser.banmanager.sponge.BMSpongePlugin")
        description("A database driven punishment system")
        links {
            homepage("https://banmanagement.com/")
            source("https://github.com/BanManagment/BanManager")
            issues("https://github.com/BanManagment/BanManager")
        }
        contributor("confuser") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
            version("7.2.0")
        }
        dependency("magibridge") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(true)
            version("api-7-SNAPSHOT")
        }
    }
}

repositories {
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io/")
        metadataSources {
            artifact() //Look directly for artifact
        }
    }
    maven {
        name = "jcenter"
        url = uri("https://jcenter.bintray.com/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:7.2.0")

    api(project(":BanManagerCommon")) {
        isTransitive = true
    }
    compileOnly("com.github.Eufranio:MagiBridge:api-7-6ec024d1be-1") {
        exclude(group = "net.dv8tion", module = "JDA")
    }
    "shadeOnly"("org.bstats:bstats-sponge:2.2.1")
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

    inputs.property("internalVersion", internalVersion)

    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.sponge.BMSpongePlugin")
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

    archiveBaseName.set("BanManagerSponge")
    archiveClassifier.set("")
    archiveVersion.set("")

    dependencies {
        include(dependency(":BanManagerCommon"))
        include(dependency(":BanManagerLibs"))
        relocate("org.bstats", "me.confuser.banmanager.common.bstats") {
            include(dependency("org.bstats:"))
        }
    }

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("**/module-info.class")
    exclude("*.yml")
    exclude("assets/banmanager/bungeecord.yml")

    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
