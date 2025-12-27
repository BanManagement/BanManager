import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

applyPlatformAndCoreConfiguration()

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))

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

dependencies {
    api(project(":BanManagerLibs"))

    testImplementation("junit:junit:4.13")
    testImplementation("org.hamcrest:hamcrest-library:1.2.1")
    testImplementation("org.powermock:powermock-module-junit4:2.0.2")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.2")
    testImplementation("com.github.javafaker:javafaker:1.0.2")
    testImplementation("org.awaitility:awaitility:4.0.1")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.6.0")
}

tasks.withType<Test>().configureEach {
    useJUnit()
    maxHeapSize = "512m"
    forkEvery = 1  // Fork a new JVM for each test class to prevent memory accumulation
}
