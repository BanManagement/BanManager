import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

fun Project.applyCommonConfiguration() {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        maven { url = uri("https://ci.frostcast.net/plugin/repository/everything") }
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok:1.18.22")
        "annotationProcessor"("org.projectlombok:lombok:1.18.22")

        "testCompileOnly"("org.projectlombok:lombok:1.18.22")
        "testAnnotationProcessor"("org.projectlombok:lombok:1.18.22")
    }

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "MINUTES")
        }
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().setSourceCompatibility("1.8")
        the<JavaPluginExtension>().setTargetCompatibility("1.8")
    }
}
