import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyLibrariesConfiguration()

dependencies {
    "shade"("net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-gson:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-json:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-plain:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-commons:${Versions.ADVENTURE}")
    "shade"("net.kyori:examination-api:1.3.0")
    "shade"("net.kyori:examination-string:1.3.0")
    "shade"("net.kyori:option:1.1.0")

    "shade"("com.j256.ormlite:ormlite-core:6.1")
    "shade"("com.j256.ormlite:ormlite-jdbc:6.1")

    "shade"("com.zaxxer:HikariCP:6.3.3")
    "shade"("org.mariadb.jdbc:mariadb-java-client:3.5.7")
    "shade"("com.mysql:mysql-connector-j:8.4.0")
    "shade"("com.googlecode.concurrent-trees:concurrent-trees:2.4.0")
    "shade"("com.maxmind.db:maxmind-db-gson:2.0.3")

    "shade"("org.yaml:snakeyaml:2.4")
    "shade"("com.google.code.gson:gson:2.11.0")
    "shade"("com.github.spullara.cli-parser:cli-parser:1.1.5")
    "shade"("com.google.guava:guava:33.4.8-jre")

    "shade"("org.apache.commons:commons-compress:1.27.1")
    "shade"("com.github.seancfoley:ipaddress:5.5.1")
    "shade"("com.h2database:h2:1.4.200")
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<ShadowJar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN

    dependencies {
        relocate("net.kyori.adventure", "me.confuser.banmanager.common.kyori") {
            include(dependency("net.kyori:adventure-text-serializer-legacy"))
            include(dependency("net.kyori:adventure-text-serializer-gson"))
            include(dependency("net.kyori:adventure-text-serializer-json"))
            include(dependency("net.kyori:adventure-text-serializer-plain"))
            include(dependency("net.kyori:adventure-text-serializer-commons"))
            include(dependency("net.kyori:adventure-text-minimessage"))
            include(dependency("net.kyori:adventure-api"))
            include(dependency("net.kyori:adventure-key"))
        }

        relocate("net.kyori.examination", "me.confuser.banmanager.common.kyori.examination") {
            include(dependency("net.kyori:examination-api"))
            include(dependency("net.kyori:examination-string"))
        }

        relocate("net.kyori.option", "me.confuser.banmanager.common.kyori.option") {
            include(dependency("net.kyori:option"))
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
            include(dependency("com.j256.ormlite:ormlite-core"))
            include(dependency("com.j256.ormlite:ormlite-jdbc"))
        }

        relocate("com.zaxxer.hikari", "me.confuser.banmanager.common.hikari") {
            include(dependency("com.zaxxer:HikariCP"))
        }

        relocate("org.mariadb.jdbc", "me.confuser.banmanager.common.mariadb") {
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
        }

        relocate("com.mysql", "me.confuser.banmanager.common.mysql") {
            include(dependency("com.mysql:mysql-connector-j"))
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

        relocate("com.google.thirdparty", "me.confuser.banmanager.common.google.thirdparty") {
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
