package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.JpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AppleCodeSign : DefaultTask() {
    val ext = project.extensions.getByType(JpkgExtension::class.java)
    init {
        group = "jpkg"
        dependsOn.add(project.tasks.getByName("jpackageRun"))
    }

    @TaskAction
    fun action () {

    }

}