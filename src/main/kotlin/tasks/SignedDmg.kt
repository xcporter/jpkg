package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder.buildCodesign
import com.xcporter.jpkg.CmdBuilder.buildJpackageImage
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
        project.execute(buildJpackageImage(project))
        project.file(project.buildDir.path + "/jpkg/mac/").listFiles()
            ?.firstOrNull { it.extension == "dmg" }
            ?.let {
                project.execute(buildCodesign(it.path, project))
            }

    }
}