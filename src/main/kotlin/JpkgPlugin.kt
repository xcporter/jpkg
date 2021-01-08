package com.xcporter.jpkg

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import java.io.File

class JpkgPlugin : Plugin<Project> {
    override fun apply(proj: Project) {
        proj.plugins.apply(JavaPlugin::class.java)
        proj.plugins.apply(ApplicationPlugin::class.java)
        val java = proj.convention.getPlugin(JavaPluginConvention::class.java)
        val app = proj.convention.getPlugin(ApplicationPluginConvention::class.java)

        proj.allprojects.forEach { p ->
            p.extensions.create("jpkg", JpkgExtension::class.java)
            p.tasks.register("gitVersion") {
                val ext = it.project.extensions.getByType(JpkgExtension::class.java)

                if (ext.useVersionFromGit) {
                    it.project.version = try {
                        GitParser(it.project.file("./.git")).formatVersion()
                    } catch(e: Throwable) {
                        GitParser(it.project.rootProject.file("./.git")).formatVersion()
                    }
                }
            }
        }

        val extension = proj.extensions.getByType(JpkgExtension::class.java)

        if (proj.subprojects.isNotEmpty()) {
            proj.subprojects.forEach { sproj ->
                val ext = sproj.extensions.getByType(JpkgExtension::class.java)
                sproj.plugins.apply(JavaPlugin::class.java)
                sproj.plugins.apply(ApplicationPlugin::class.java)
                val sJava = sproj.convention.getPlugin(JavaPluginConvention::class.java)
                val sApp = sproj.convention.getPlugin(ApplicationPluginConvention::class.java)

                sproj.afterEvaluate { sub ->
                    if (!ext.mainClass.isNullOrBlank() || !sApp.mainClassName.isNullOrBlank()) {
                        sub.tasks.register("executableJar", Jar::class.java) {
                            it.group = "jpkg"
                            it.dependsOn(sub.tasks.getByName("gitVersion"))
                            it.dependsOn(sub.tasks.getByName("classes"))
                            it.archiveFileName.set((ext.packageName ?: "${sub.rootProject.name}-${sub.name}${if ((sub.version as String) != "unspecified") "-${sub.version}" else ""}") + ".jar")
                            it.destinationDirectory.set(sproj.file(sproj.buildDir.absolutePath + "/jpkg"))
                            it.manifest {
                                it.attributes(mapOf(
                                    "Main-Class" to (sApp.mainClassName.takeUnless { it.isNullOrBlank() } ?: ext.mainClass)
                                ))
                            }
                            it.from(sJava.sourceSets.getByName("main").output.filter { it.exists() }.map { if(it.isDirectory) it else sproj.zipTree(it) })
                            it.from(sproj.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else sproj.zipTree(it) })
                            it.doFirst { println("${sub.name} version: ${sub.version}") }
                        }
                    }
                }
            }
        } else {
            proj.afterEvaluate { main ->
                if (!extension.mainClass.isNullOrBlank() || !app.mainClassName.isNullOrBlank()) {
                    main.tasks.register("executableJar", Jar::class.java) {
                        it.group = "jpkg"
                        it.dependsOn("gitVersion")
                        it.dependsOn("classes")
                        it.archiveFileName.set((extension.packageName ?: "${main.name}-${main.version}") + ".jar")
                        it.destinationDirectory.set(proj.file(proj.buildDir.absolutePath + "/jpkg"))
                        it.manifest {
                            it.attributes(mapOf(
                                "Main-Class" to (app.mainClassName.takeUnless { it.isNullOrBlank() } ?: extension.mainClass)
                            ))
                        }
                        it.from(java.sourceSets.getByName("main").output.filter{ it.exists() }.map { if(it.isDirectory) it else proj.zipTree(it) })
                        it.from(proj.configurations.getByName("runtimeClasspath").map { if(it.isDirectory) it else proj.zipTree(it) })
                    }
                }
            }
        }
    }
}
