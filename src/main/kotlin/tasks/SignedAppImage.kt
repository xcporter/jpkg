package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder
import com.xcporter.jpkg.CmdBuilder.buildCodesignCommand
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.JpkgExtension
import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SignedAppImage : DefaultTask() {
    init {
        group = "jpkg"
        dependsOn("signArchive")
    }

    @TaskAction
    fun action() {
        project.jpkgExtension().type = JpkgExtension.DistType.MAC_APP
        project.jpkgExtension().destination?.let { File(it).mkdirs() }
        val cmd = CmdBuilder.buildJpackageJarCommand(project)
        println(cmd.joinToString(" "))
        project.execute(cmd)
        val app = project.file(project.buildDir.absolutePath + "/jpkg/mac/").listFiles()?.firstOrNull { it.extension == "app"}

        app
            ?.let { outer ->
                File(outer, "/Contents/runtime/Contents/MacOS/").listFiles()
                    ?.filter { it.extension == "dylib" }
                    ?.forEach { project.execute(buildCodesignCommand(it.path, project)) }
                File(outer, "/Contents/MacOS/").listFiles()
                    ?.forEach { project.execute(buildCodesignCommand(it.path, project)) }
                project.execute(buildCodesignCommand(outer.path, project))
            }

    }
}