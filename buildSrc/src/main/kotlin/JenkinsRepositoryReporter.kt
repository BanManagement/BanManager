import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Reports Maven artifacts to Jenkins Maven Repository Plugin.
 * This allows the plugin to serve Gradle-built artifacts as a Maven repository.
 */
abstract class ReportArtifactsToJenkinsTask : DefaultTask() {

    @get:Input
    abstract val groupId: Property<String>

    @get:Input
    abstract val artifactId: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:InputFile
    abstract val pomFile: RegularFileProperty

    @get:InputFile
    abstract val mainArtifact: RegularFileProperty

    @get:Input
    abstract val artifactType: Property<String>

    @get:InputFile
    @get:Optional
    abstract val sourcesJar: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val javadocJar: RegularFileProperty

    init {
        group = "publishing"
        description = "Reports artifacts to Jenkins Maven Repository Plugin"

        // Only run on Jenkins
        onlyIf {
            val jenkinsUrl = System.getenv("JENKINS_URL")
            val buildNumber = System.getenv("BUILD_NUMBER")
            // Check for any valid job name source
            val jobName = (project.findProperty("jenkinsRepositoryProject") as String?)
                ?: System.getenv("JENKINS_REPOSITORY_PROJECT")
                ?: System.getenv("JOB_BASE_NAME")
                ?: System.getenv("JOB_NAME")

            if (jenkinsUrl.isNullOrBlank() || buildNumber.isNullOrBlank() || jobName.isNullOrBlank()) {
                logger.info("Skipping Jenkins artifact reporting - not running on Jenkins")
                false
            } else {
                true
            }
        }
    }

    @TaskAction
    fun reportArtifacts() {
        val jenkinsUrl = System.getenv("JENKINS_URL")?.trimEnd('/')
        val buildNumber = System.getenv("BUILD_NUMBER")

        // Project name resolution: prefer explicit override, then JOB_BASE_NAME, then JOB_NAME
        // The maven-repository-plugin expects the leaf job name (not folder paths)
        val jobName = (project.findProperty("jenkinsRepositoryProject") as String?)
            ?: System.getenv("JENKINS_REPOSITORY_PROJECT")
            ?: System.getenv("JOB_BASE_NAME")
            ?: System.getenv("JOB_NAME")

        if (jenkinsUrl == null || buildNumber == null || jobName == null) {
            logger.warn("Jenkins environment variables not set, skipping artifact reporting")
            return
        }

        val endpoint = "$jenkinsUrl/plugin/repository/add_info"
        logger.lifecycle("Reporting artifacts to Jenkins: $endpoint")

        val data = buildReportData(jobName, buildNumber)
        logger.debug("Report data:\n$data")

        try {
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain")

            connection.outputStream.use { os ->
                os.write(data.toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                logger.lifecycle("Successfully reported artifacts to Jenkins")
            } else {
                logger.warn("Failed to report artifacts to Jenkins: HTTP $responseCode")
                val errorStream = connection.errorStream?.bufferedReader()?.readText()
                if (errorStream != null) {
                    logger.warn("Error response: $errorStream")
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to report artifacts to Jenkins: ${e.message}")
        }
    }

    private fun buildReportData(jobName: String, buildNumber: String): String {
        val sb = StringBuilder()

        // Project reference
        sb.appendLine(jobName)
        sb.appendLine(buildNumber)

        // POM artifact
        val pom = pomFile.get().asFile
        sb.appendLine("[pom]")
        sb.appendLine(pom.absolutePath)
        sb.appendLine(groupId.get())
        sb.appendLine(artifactId.get())
        sb.appendLine(version.get())
        sb.appendLine("") // classifier (empty for pom)
        sb.appendLine("pom")
        sb.appendLine(pom.name)
        sb.appendLine(md5(pom))

        // Main artifact
        val main = mainArtifact.get().asFile
        sb.appendLine("[main]")
        sb.appendLine(main.absolutePath)
        sb.appendLine(groupId.get())
        sb.appendLine(artifactId.get())
        sb.appendLine(version.get())
        sb.appendLine("") // classifier
        sb.appendLine(artifactType.get())
        sb.appendLine(main.name)
        sb.appendLine(md5(main))

        // Sources jar (if present)
        if (sourcesJar.isPresent && sourcesJar.get().asFile.exists()) {
            val sources = sourcesJar.get().asFile
            sb.appendLine("[artifact]")
            sb.appendLine(sources.absolutePath)
            sb.appendLine(groupId.get())
            sb.appendLine(artifactId.get())
            sb.appendLine(version.get())
            sb.appendLine("sources")
            sb.appendLine("jar")
            sb.appendLine(sources.name)
            sb.appendLine(md5(sources))
        }

        // Javadoc jar (if present)
        if (javadocJar.isPresent && javadocJar.get().asFile.exists()) {
            val javadoc = javadocJar.get().asFile
            sb.appendLine("[artifact]")
            sb.appendLine(javadoc.absolutePath)
            sb.appendLine(groupId.get())
            sb.appendLine(artifactId.get())
            sb.appendLine(version.get())
            sb.appendLine("javadoc")
            sb.appendLine("jar")
            sb.appendLine(javadoc.name)
            sb.appendLine(md5(javadoc))
        }

        return sb.toString()
    }

    private fun md5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(file.readBytes())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Configures Jenkins artifact reporting for a project with maven-publish.
 */
fun Project.configureJenkinsArtifactReporting() {
    plugins.withId("maven-publish") {
        afterEvaluate {
            val publishing = extensions.findByType(org.gradle.api.publish.PublishingExtension::class.java)
            val mavenPub = publishing?.publications?.findByName("maven") as? org.gradle.api.publish.maven.MavenPublication

            if (mavenPub != null) {
                val reportTask = tasks.register<ReportArtifactsToJenkinsTask>("reportArtifactsToJenkins") {
                    groupId.set(mavenPub.groupId)
                    artifactId.set(mavenPub.artifactId)
                    version.set(mavenPub.version)
                    artifactType.set("jar")

                    // POM file from generatePomFileForMavenPublication
                    val pomTask = tasks.findByName("generatePomFileForMavenPublication")
                    if (pomTask != null) {
                        dependsOn(pomTask)
                        pomFile.set(layout.buildDirectory.file("publications/maven/pom-default.xml"))
                    }

                    // Main jar
                    val jarTask = tasks.findByName("jar") as? org.gradle.jvm.tasks.Jar
                    if (jarTask != null) {
                        dependsOn(jarTask)
                        mainArtifact.set(jarTask.archiveFile)
                    }

                    // Sources jar
                    val sourcesTask = tasks.findByName("sourcesJar") as? org.gradle.jvm.tasks.Jar
                        ?: tasks.findByName("kotlinSourcesJar") as? org.gradle.jvm.tasks.Jar
                    if (sourcesTask != null) {
                        dependsOn(sourcesTask)
                        sourcesJar.set(sourcesTask.archiveFile)
                    }

                    // Javadoc jar
                    val javadocTask = tasks.findByName("javadocJar") as? org.gradle.jvm.tasks.Jar
                        ?: tasks.findByName("plainJavadocJar") as? org.gradle.jvm.tasks.Jar
                    if (javadocTask != null) {
                        dependsOn(javadocTask)
                        javadocJar.set(javadocTask.archiveFile)
                    }
                }

                // Make publishToMavenLocal trigger the report
                tasks.named("publishToMavenLocal").configure {
                    finalizedBy(reportTask)
                }
            } else {
                logger.info("No 'maven' publication found in ${project.name}, skipping Jenkins reporting setup")
            }
        }
    }
}
