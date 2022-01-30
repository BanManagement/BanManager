import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    api(project(":BanManagerCommon"))
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.0.1")
    "shadeOnly"("org.bstats:bstats-velocity:2.2.1")
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

    archiveBaseName.set("BanManagerVelocity")
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

    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
