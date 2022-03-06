import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
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
    "compileOnly"("com.discordsrv:discordsrv:1.24.0") {
        isTransitive  = false
    }
    "compileOnly"("me.clip:placeholderapi:2.10.9")
    "shadeOnly"("org.bstats:bstats-bukkit:2.2.1")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

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
        relocate("org.bstats", "me.confuser.banmanager.common.bstats") {
            include(dependency("org.bstats:"))
        }
    }

    exclude("GradleStart**")
    exclude(".cache");
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("bungeecord.yml")

    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
