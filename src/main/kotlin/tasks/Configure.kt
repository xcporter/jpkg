package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.JpkgPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.storage.file.FileRepositoryBuilder

open class Configure : DefaultTask() {
    @TaskAction
    fun configure() {
        doFirst {
            gitSetup()
            JpkgPlugin.repo?.describe()?.setMatch("*.")
        }
    }

    fun gitSetup() {
        JpkgPlugin.repo = Git (
            with(FileRepositoryBuilder()) {
            gitDir = project.projectDir
            readEnvironment()
            findGitDir()
            build()
        })
    }
}