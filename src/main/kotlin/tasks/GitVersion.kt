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
        project.jpkgExtension().gitVersion = try {
            GitParser(project.file("./.git")).formatVersion()
        } catch(e: Throwable) {
            try {
                GitParser(project.rootProject.file("./.git")).formatVersion()
            } catch(e: Throwable) {
                println("No git repository found")
                null
            }
        }
        if (project.jpkgExtension().useVersionFromGit) {
            project.jpkgExtension().gitVersion?.let {
                project.version = it
            } ?: run { println("Warning: No git repo found")}
        }
    }
}