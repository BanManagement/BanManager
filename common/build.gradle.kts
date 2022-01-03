plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

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
    onlyIf{ false }
}
/*
tasks.withType<Test>().configureEach {
    // useJUnit()
}*/

// "shade"("ch.vorburger.mariaDB4j:mariaDB4j:2.4.0")