package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.GitParser
import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GitVersion : DefaultTask() {
    init {
        group = "jpkg"
    }

    @TaskAction
    fun action () {
        if (project.jpkgExtension().useVersionFromGit) {
            project.version = try {
                GitParser(project.file("./.git")).formatVersion()
            } catch(e: Throwable) {
                GitParser(project.rootProject.file("./.git")).formatVersion()
            }
        }
    }
}