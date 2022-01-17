import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyLibrariesConfiguration()

dependencies {
    "shade"("net.kyori:text-api:${Versions.ADVENTURE}")
    "shade"("net.kyori:text-serializer-gson:${Versions.ADVENTURE}")
    "shade"("net.kyori:text-serializer-legacy:${Versions.ADVENTURE}")

    "shade"("com.j256.ormlite:ormlite-core:5.1")
    "shade"("com.j256.ormlite:ormlite-jdbc:5.1")

    "shade"("com.zaxxer:HikariCP:4.0.3")
    "shade"("org.mariadb.jdbc:mariadb-java-client:2.7.4")
    "shade"("mysql:mysql-connector-java:8.0.27")
    "shade"("com.googlecode.concurrent-trees:concurrent-trees:2.4.0")
    "shade"("com.maxmind.db:maxmind-db-gson:2.0.3")

    "shade"("org.yaml:snakeyaml:1.29")
    "shade"("com.google.code.gson:gson:2.3.1")
    "shade"("com.github.spullara.cli-parser:cli-parser:1.1.5")
    "shade"("com.google.guava:guava:21.0")

    "shade"("org.apache.commons:commons-compress:1.19")
    "shade"("com.github.seancfoley:ipaddress:5.3.3")
    "shade"("com.h2database:h2:1.4.200")
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<ShadowJar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN

    dependencies {
        relocate("net.kyori.text", "me.confuser.banmanager.common.kyori.text") {
            include(dependency("net.kyori:text-api"))
            include(dependency("net.kyori:text-serializer-gson"))
            include(dependency("net.kyori:text-serializer-legacy"))
        }

        relocate("org.yaml.snakeyaml", "me.confuser.banmanager.common.snakeyaml") {
            include(dependency("org.yaml:snakeyaml"))
        }

        relocate("inet.ipaddr", "me.confuser.banmanager.common.ipaddr") {
            include(dependency("com.github.seancfoley:ipaddress"))
        }

        relocate("org.h2", "me.confuser.banmanager.common.h2") {
            include(dependency("com.h2database:h2"))
        }

        relocate("com.j256.ormlite", "me.confuser.banmanager.common.ormlite") {
            include(dependency("com.j256.ormlite:ormlite-core:5.1"))
            include(dependency("com.j256.ormlite:ormlite-jdbc:5.1"))
        }

        relocate("com.zaxxer.hikari", "me.confuser.banmanager.common.hikari") {
            include(dependency("com.zaxxer:HikariCP"))
        }

        relocate("org.mariadb.jdbc", "me.confuser.banmanager.common.mariadb") {
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
        }

        relocate("com.mysql", "me.confuser.banmanager.common.mysql") {
            include(dependency("mysql:mysql-connector-java"))
        }

        relocate("com.google.gson", "me.confuser.banmanager.common.gson") {
            include(dependency("com.google.code.gson:gson"))
        }

        relocate("org.apache.commons", "me.confuser.banmanager.common.apachecommons") {
            include(dependency("org.apache.commons:commons-compress"))
        }

        relocate("com.google.common", "me.confuser.banmanager.common.google.guava") {
            include(dependency("com.google.guava:guava"))
        }

        relocate("com.googlecode.concurrenttrees", "me.confuser.banmanager.common.google.concurrenttrees") {
            include(dependency("com.googlecode.concurrent-trees:concurrent-trees"))
        }

        relocate("com.sampullara.cli", "me.confuser.banmanager.common.cli") {
            include(dependency("com.github.spullara.cli-parser:cli-parser"))
        }

        relocate("com.maxmind", "me.confuser.banmanager.common.maxmind") {
            include(dependency("com.maxmind.db:maxmind-db-gson"))
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
