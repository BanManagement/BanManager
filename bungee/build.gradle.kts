import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
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
                name.set("BanManagerBungee")
                description.set("BanManager for BungeeCord")
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
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

configurations {
    compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
    api(project(":BanManagerCommon"))
    compileOnly("net.md-5:bungeecord-api:1.14-SNAPSHOT")
    "shadeOnly"("org.bstats:bstats-bungeecord:2.2.1")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]

    inputs.property("internalVersion", internalVersion)

    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion, "mainPath" to "me.confuser.banmanager.bungee.BMBungeePlugin")
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

    archiveBaseName.set("BanManagerBungeeCord")
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
