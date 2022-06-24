import com.jetbrains.plugin.structure.intellij.version.IdeVersion
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel

val name: String by project
val changenotesFile: String by project
val descriptionFile: String by project
val ideaVersion: String by project
val javaVersion: String by project
val pluginVerifierIdeVersions: String by project

plugins {
    id("org.jetbrains.intellij") version "1.4.0"
    java
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    testImplementation("com.sksamuel.scrimage", "scrimage-core", "4.0.22")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set(ideaVersion)
    pluginName.set(name)
    downloadSources.set(true)
    updateSinceUntilBuild.set(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    register("genDocs", JavaExec::class) {
        group = "naruto-progress"
        description = "generate documentation"
        classpath = java.sourceSets["test"].runtimeClasspath
        mainClass.set("com.lawmillenium.intellij.plugins.narutoprogress.DocumentationGenerator")
    }

    register("testProgressBar", JavaExec::class) {
        group = "naruto-progress"
        description = "test progress bar"
        classpath = java.sourceSets["test"].runtimeClasspath
        mainClass.set("com.lawmillenium.intellij.plugins.narutoprogress.TestProgressBar")
    }

    register("indexColors", DefaultTask::class) {
        group = "naruto-progress"
        description = "create index file for color schemes"
        doFirst {
            File("src/main/resources/com/lawmillenium/intellij/plugins/narutoprogress/colors").let { dir ->
                if (dir.exists() && dir.isDirectory && dir.canRead()) {
                    val strb = StringBuilder()
                    dir.listFiles()
                            ?.filter { it.isFile }
                            ?.filter { it.name.endsWith(".csv") }
                            ?.forEach { strb.append(it.name).append("\n") }
                    val index = File(dir, ".cscheme.index")
                    index.delete()
                    index.createNewFile()
                    index.writeText(strb.toString())
                } else throw IllegalStateException("unable to read color schemes")
            }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        dependsOn("indexColors")
    }

    patchPluginXml {
        untilBuild.set(null as String?)
        sinceBuild.convention(project.provider {
            val ideVersion = IdeVersion.createIdeVersion(setupDependencies.get().idea.get().buildNumber)
            "${ideVersion.baselineVersion}.${ideVersion.build}"
        })

        File(changenotesFile).let {
            if (it.exists() && it.isFile && it.canRead()) changeNotes.set(it.readText())
            else throw IllegalStateException("unable to read $changenotesFile")
        }
        File(descriptionFile).let {
            if (it.exists() && it.isFile && it.canRead()) pluginDescription.set(it.readText())
            else throw IllegalStateException("unable to read $descriptionFile")
        }
    }

    buildPlugin {
        dependsOn(patchPluginXml)
    }

    runPluginVerifier {
        ideVersions.set(pluginVerifierIdeVersions.split(",").map { it.trim() }.toList())
        failureLevel.set(listOf(FailureLevel.COMPATIBILITY_PROBLEMS,
                FailureLevel.NOT_DYNAMIC))
    }

    signPlugin {
        (File("certs/private.pem").readText(Charsets.UTF_8)).let { privateKey.set(it) }
        System.getenv("JETBRAINS_REPO_SIGNING_KEY_PASSWORD")?.let { password.set(it) }
        (File("certs/chain.crt").readText(Charsets.UTF_8)).let { certificateChain.set(it) }
    }

    publishPlugin {
        System.getenv("JETBRAINS_REPO_TOKEN")?.let { token.set(it) }
        System.getenv("PLUGIN_DEPLOYMENT_CHANNELS")?.let { channels.set(it.split(",").map { s -> s.trim() }.toList()) }
    }
}
