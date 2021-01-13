package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder.buildCodesignCommand
import com.xcporter.jpkg.CmdBuilder.buildJpackageImageCommand
import com.xcporter.jpkg.CmdBuilder.execute
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SignedDmg : DefaultTask() {

    init {
        group = "jpkg"
        dependsOn("signedAppImage")
    }

    @TaskAction
    fun action () {
        project.execute(buildJpackageImageCommand(project))
        project.file(project.buildDir.path + "/jpkg/mac/").listFiles()
            ?.firstOrNull { it.extension == "dmg" }
            ?.let {
                project.execute(buildCodesignCommand(it.path, project))
            }

    }
}