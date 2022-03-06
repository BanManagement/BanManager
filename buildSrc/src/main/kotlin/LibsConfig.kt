import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.*
import org.gradle.kotlin.dsl.*
import javax.inject.Inject

fun Project.applyLibrariesConfiguration() {
    apply(plugin = "java-base")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        maven { url = uri("https://ci.frostcast.net/plugin/repository/everything") }
    }

    configurations {
        create("shade")
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().setSourceCompatibility("1.8")
        the<JavaPluginExtension>().setTargetCompatibility("1.8")
    }

    group = "${rootProject.group}.BanManagerLibs"

    tasks.register<ShadowJar>("jar") {
        configurations = listOf(project.configurations["shade"])
        archiveClassifier.set("")
    }
    val altConfigFiles = { artifactType: String ->
        val deps = configurations["shade"].incoming.dependencies
                .filterIsInstance<ModuleDependency>()
                .map { it.copy() }
                .map { dependency ->
                    dependency.artifact {
                        name = dependency.name
                        type = artifactType
                        extension = "jar"
                        classifier = artifactType
                    }
                    dependency
                }

        files(configurations.detachedConfiguration(*deps.toTypedArray())
                .resolvedConfiguration.lenientConfiguration.artifacts
                .filter { it.classifier == artifactType }
                .map { zipTree(it.file) })
    }

    tasks.register<Jar>("sourcesJar") {
        from({
            altConfigFiles("sources")
        })
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        from({
            altConfigFiles("javadoc")
        })
        archiveClassifier.set("javadoc")
    }

    tasks.named("assemble").configure {
        dependsOn("jar", "sourcesJar", "javadocJar")
    }

    project.apply<LibsConfigPluginHack>()

    val libsComponent = project.components["libs"] as AdhocComponentWithVariants

    val apiElements = project.configurations.register("apiElements") {
        isVisible = false
        description = "API elements for libs"
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_API))
            attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
            attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
        }
        outgoing.artifact(tasks.named("jar"))
    }

    val runtimeElements = project.configurations.register("runtimeElements") {
        isVisible = false
        description = "Runtime elements for libs"
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
            attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
        }
        outgoing.artifact(tasks.named("jar"))
    }

    val sourcesElements = project.configurations.register("sourcesElements") {
        isVisible = false
        description = "Source elements for libs"
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.DOCUMENTATION))
            attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.objects.named(DocsType.SOURCES))
        }
        outgoing.artifact(tasks.named("sourcesJar"))
    }

    libsComponent.addVariantsFromConfiguration(apiElements.get()) {
        mapToMavenScope("compile")
    }

    libsComponent.addVariantsFromConfiguration(runtimeElements.get()) {
        mapToMavenScope("runtime")
    }

    libsComponent.addVariantsFromConfiguration(sourcesElements.get()) {
        mapToMavenScope("runtime")
    }

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                from(libsComponent)

                pom {
                    name.set("BanManagerLibs")
                    description.set("BanManager shared libs")
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

    configure<SigningExtension> {
        val pubExt = checkNotNull(extensions.findByType(PublishingExtension::class.java))
        val publication = pubExt.publications["maven"]

        sign(publication)
    }
}

internal open class LibsConfigPluginHack @Inject constructor(
        private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        val libsComponents = softwareComponentFactory.adhoc("libs")
        project.components.add(libsComponents)
    }
}
