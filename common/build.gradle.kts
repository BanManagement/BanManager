plugins {
    `java-library`
    `maven-publish`
    signing
}

applyPlatformAndCoreConfiguration()

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("BanManagerCommon")
                description.set("Common BanManager code needed for all platforms")
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
    sign(publishing.publications["mavenJava"])
}

dependencies {
    api(project(":BanManagerLibs"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.hamcrest:hamcrest-library:1.2.1")
    testImplementation("org.powermock:powermock-module-junit4:2.0.2")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.2")
    testImplementation("com.github.javafaker:javafaker:1.0.2")
    testImplementation("org.awaitility:awaitility:4.0.1")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.4.0")
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
