package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.JPackageArgs
import com.xcporter.jpkg.JpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import java.io.ByteArrayOutputStream
import java.io.File

open class JPackageTask : DefaultTask() {
    val ext = project.extensions.getByType(JpkgExtension::class.java)

    init {
        group = "jpkg"
        dependsOn.add(project.tasks.getByName("executableJar"))
    }

    @TaskAction
    fun execute() {
        val cmd = CmdBuilder.buildJpackageJarCommand(project)
        println(cmd.joinToString(" "))
        project.execute(cmd)
    }
}