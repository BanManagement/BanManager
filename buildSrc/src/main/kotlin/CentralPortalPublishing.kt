import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Configures publishing to Maven Central via the Sonatype Central Portal.
 * Uses the vanniktech/gradle-maven-publish-plugin.
 *
 * Requires the following Gradle properties or environment variables:
 * - mavenCentralUsername / ORG_GRADLE_PROJECT_mavenCentralUsername
 * - mavenCentralPassword / ORG_GRADLE_PROJECT_mavenCentralPassword
 * - signingInMemoryKey / ORG_GRADLE_PROJECT_signingInMemoryKey (optional, for CI)
 * - signingInMemoryKeyPassword / ORG_GRADLE_PROJECT_signingInMemoryKeyPassword (optional, for CI)
 */
fun Project.applyCentralPortalPublishing() {
    apply(plugin = "com.vanniktech.maven.publish")

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()
    }
}

