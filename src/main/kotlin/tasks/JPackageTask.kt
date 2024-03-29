package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.JpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class JPackageTask : DefaultTask() {
    @Internal
    val ext = project.extensions.getByType(JpkgExtension::class.java)

    init {
        group = "jpkg"
        dependsOn.add(project.tasks.getByName("executableJar"))
    }

    @TaskAction
    fun execute() {
        ext.destination?.let { File(it).mkdirs() }
        val cmd = CmdBuilder.buildJpackageJar(project)
        project.execute(cmd)
    }
}