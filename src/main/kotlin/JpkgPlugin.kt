package com.xcporter.jpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Repository

class JpkgPlugin : Plugin<Project> {
    companion object {
        var repo: Git? = null
        var multiProjectBuild = false
    }
    override fun apply(proj: Project) {
        proj.tasks.register("configure project") {
            it.doFirst {

            }
        }

        proj.tasks.create("add tasks") {
            multiProjectBuild = proj.subprojects.isNotEmpty()
            if (multiProjectBuild) {
                println("Multiproject build detected: applying package directives to subprojects")
                proj.subprojects.forEach {
                    it.tasks.create("testTTT") {
                        println("THIS TASK REALLY WORKS!")
                        it.group = "jpkg"
                    }
                }
            } else {
                println("Single project build detected")
                proj.tasks.create("testTTT") {
                    println("THIS TASK REALLY WORKS!")
                    it.group = "jpkg"
                }
            }
        }
    }
}
