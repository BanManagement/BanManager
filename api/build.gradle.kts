import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
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
        name.set("BanManagerAPI")
        description.set("Stable, dependency-light public API for BanManager v8+. Plugin authors integrate against this artifact.")
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
    // The single non-JDK dependency. Unshaded so the IPAddress type the API
    // exposes carries the canonical inet.ipaddr.* package, not the BM-shaded
    // me.confuser.banmanager.common.ipaddr.* prefix.
    api("com.github.seancfoley:ipaddress:5.5.1")
}
