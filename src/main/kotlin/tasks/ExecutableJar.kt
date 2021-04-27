package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar

open class ExecutableJar : Jar() {
    val extension = project.jpkgExtension()
    @Internal
    val java = project.convention.getPlugin(JavaPluginConvention::class.java)
    @Internal
    val app = project.convention.getPlugin(ApplicationPluginConvention::class.java)
    private val versionSetting = if (extension.useVersionFromGit && extension.gitVersion != null) {
        "-${extension.gitVersion}"
    } else {
        if (project.version != "unspecified") "-${project.version}" else ""
    }

    init {
        group = "jpkg"
        dependsOn("execJarConfig")
        archiveFileName.set(("${project.name}$versionSetting") + ".jar")
        destinationDirectory.set(project.file(project.buildDir.absolutePath + "/jpkg/jar"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            it.attributes(mapOf(
                "Main-Class" to (app.mainClassName.takeUnless { it.isNullOrBlank() } ?: extension.mainClass)
            ))
        }
        from(project.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else project.zipTree(it) })
    }
}