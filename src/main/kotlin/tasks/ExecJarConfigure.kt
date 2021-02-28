package com.xcporter.jpkg.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction

/**
 * ExecutableJar task must be configured *after* Classes task
 * or else ExecutableJar will always fail on a clean build
 *
 */
open class ExecJarConfigure : DefaultTask() {
    init {
        group = "jpkg"
        dependsOn("classes")
    }

    val java = project.convention.getPlugin(JavaPluginConvention::class.java)

    @TaskAction
    fun configure () {
        project.tasks.withType(ExecutableJar::class.java).configureEach {
            with(it) {
                from(java.sourceSets.getByName("main").output.filter{ it.exists() }.map { if(it.isDirectory) it else project.zipTree(it) })
            }
        }
    }
}