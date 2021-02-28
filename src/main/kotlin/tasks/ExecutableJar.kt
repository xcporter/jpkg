package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.GitParser
import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar

open class ExecutableJar : Jar() {
    val extension = project.jpkgExtension()
    val java = project.convention.getPlugin(JavaPluginConvention::class.java)
    val app = project.convention.getPlugin(ApplicationPluginConvention::class.java)
    private val versionSetting = if (extension.useVersionFromGit) {
        try {
            "-${GitParser(project.file("./.git")).formatVersion()}"
        } catch(e: Throwable) {
           "-${ GitParser(project.rootProject.file("./.git")).formatVersion()}"
        }
    } else {
        if (project.version != "unspecified") "-${project.version}" else ""
    }

    init {
        group = "jpkg"
        dependsOn("classes")
        archiveFileName.set(("${project.name}$versionSetting") + ".jar")
        destinationDirectory.set(project.file(project.buildDir.absolutePath + "/jpkg/jar"))
        manifest {
            it.attributes(mapOf(
                 "Main-Class" to (app.mainClassName.takeUnless { it.isNullOrBlank() } ?: extension.mainClass)
            ))
        }
        from(java.sourceSets.getByName("main").output.filter{ it.exists() }.map { if(it.isDirectory) it else project.zipTree(it) })
        from(project.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else project.zipTree(it) })
    }
}