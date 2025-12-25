logger.lifecycle("""
*******************************************
 You are building BanManager!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, ask on Discord! Further information https://banmanagement.com/support

 Output files will be in [subproject]/build/libs
*******************************************
""")

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("io.freefair.aggregate-javadoc") version "8.11"
}

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
