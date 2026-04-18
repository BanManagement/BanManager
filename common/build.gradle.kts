import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
    jacoco
}

applyPlatformAndCoreConfiguration()

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

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("net.datafaker:datafaker:2.5.4")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:3.3.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxHeapSize = "512m"
    // Fork a new JVM per test class. Several tests (storage, configs, MariaDB4j) leak
    // static state - native handles, jdbc drivers, daemon executors - that hangs the
    // suite if a single JVM keeps accumulating fixtures. Slower, but reliable.
    forkEvery = 1
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}
