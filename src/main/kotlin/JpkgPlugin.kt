package com.xcporter.jpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar

class JpkgPlugin : Plugin<Project> {
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
            java.sourceSets.getByName("main").output.forEach {
                println(it.path)
            }
        }


        if (proj.subprojects.isNotEmpty()) {
            proj.subprojects.forEach { sproj ->
                val ext = sproj.extensions.getByType(JpkgExtension::class.java)
                sproj.plugins.apply(JavaPlugin::class.java)
                val sJava = sproj.convention.getPlugin(JavaPluginConvention::class.java)
                sproj.tasks.register("executableJar", Jar::class.java) {
                    it.group = "jpkg"
                    it.dependsOn("gitVersion")
                    it.dependsOn("jar")
                    println(sproj.version)
                    it.manifest {
                        it.attributes(mapOf(
                            "Main-Class" to (ext.mainClass ?: "")
                        ))
                    }
                    it.from(sJava.sourceSets.getByName("main").output.filter{ it.exists() }.map { if(it.isDirectory) it else sproj.zipTree(it) })
                    it.from(sproj.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else sproj.zipTree(it) })
                }
                sproj.tasks.register("checkVersion") {
                    it.group = "jpkg"
                    it.dependsOn("gitVersion")
                    it.doFirst {
                        println(sproj.version)
                    }
                }
            }
        } else {
            proj.tasks.register("executableJar", Jar::class.java) {
                it.group = "jpkg"
                it.dependsOn("gitVersion")
                it.dependsOn("jar")
                it.manifest {
                    it.attributes(mapOf(
                        "Main-Class" to extension.mainClass
                    ))
                }
                it.from(java.sourceSets.getByName("main").output.filter{ it.exists() }.map { if(it.isDirectory) it else proj.zipTree(it) })
                it.from(proj.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else proj.zipTree(it) })
            }
        }
    }
}
