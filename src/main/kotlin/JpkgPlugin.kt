package com.xcporter.jpkg

import com.xcporter.jpkg.tasks.Configure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Repository

class JpkgPlugin : Plugin<Project> {
    companion object {
        var version: String? = null
        var vhash: String? = null
        var hash: String? = null
        var multiProjectBuild = false
    }
    override fun apply(proj: Project) {
        proj.plugins.apply(JavaPlugin::class.java)
        val java = proj.convention.getPlugin(JavaPluginConvention::class.java)

        proj.allprojects.forEach {
            it.extensions.create("jpkg", JpkgExtension::class.java)
            it.tasks.register("gitVersion") {
                val ext = it.project.extensions.getByType(JpkgExtension::class.java)
                if (ext.gitVersion) {
                    it.project.version = try {
                        GitParser(it.project.file("./.git")).formatVersion()
                    } catch(e: Throwable) {
                        GitParser(it.project.rootProject.file("./.git")).formatVersion()
                    }
                }
            }
        }

        val extension = proj.extensions.getByType(JpkgExtension::class.java)

        proj.tasks.register("listConfigurations") {
            it.group = "jpkg"
        }

//        proj.tasks.create("add tasks") {
//            multiProjectBuild = proj.subprojects.isNotEmpty()
//            if (multiProjectBuild) {
//                println("Multiproject build detected: applying package directives to subprojects")
//                proj.subprojects.forEach {
//                    it.tasks.create("testTTT") {
//                        println("THIS TASK REALLY WORKS!")
//                        it.group = "jpkg"
//                    }
//                }
//            } else {
//                println("Single project build detected")
//                proj.tasks.create("testTTT") {
//                    println("THIS TASK REALLY WORKS!")
//                    it.group = "jpkg"
//                }
//            }
//        }
    }
}
