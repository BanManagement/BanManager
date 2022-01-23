import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency


plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
    id("net.kyori.blossom") version "1.2.0"
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

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
    compileOnly("com.github.Eufranio:MagiBridge:api-7-SNAPSHOT") {
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
    expand("internalVersion" to internalVersion)
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
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
