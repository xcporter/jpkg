package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar

open class ExecutableJar : Jar() {
    val extension = project.jpkgExtension()
    val java = project.convention.getPlugin(JavaPluginConvention::class.java)
    val app = project.convention.getPlugin(ApplicationPluginConvention::class.java)
    init {
     group = "jpkg"
     dependsOn("gitVersion")
     dependsOn("classes")
     archiveFileName.set((extension.packageName ?: "${project.name}-${project.version}") + ".jar")
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