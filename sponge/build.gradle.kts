import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

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

    api("com.github.seancfoley:ipaddress:5.3.3")
    implementation("com.github.seancfoley:ipaddress:5.3.3")
    api(project(":BanManagerCommon")) {
        isTransitive = true
    }
    compileOnly("com.github.Eufranio:MagiBridge:api-7-SNAPSHOT") {
        exclude(group = "net.dv8tion", module = "JDA")
    }
    "shadeOnly"("org.bstats:bstats-sponge:2.2.1")
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
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
