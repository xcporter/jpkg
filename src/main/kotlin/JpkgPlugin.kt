package com.xcporter.jpkg

import com.xcporter.jpkg.CmdBuilder.checkJpackage
import com.xcporter.jpkg.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import java.io.File

// todo build all task
class JpkgPlugin : Plugin<Project> {
    override fun apply(proj: Project) {
        proj.plugins.apply(JavaPlugin::class.java)
        proj.plugins.apply(ApplicationPlugin::class.java)
        val java = proj.convention.getPlugin(JavaPluginConvention::class.java)
        val app = proj.convention.getPlugin(ApplicationPluginConvention::class.java)

        proj.allprojects.forEach { p ->
            p.extensions.create("jpkg", JpkgExtension::class.java, p)
            p.plugins.apply(JavaPlugin::class.java)
            p.plugins.apply(ApplicationPlugin::class.java)

            p.parseEnv()

            p.tasks.register("gitVersion", GitVersion::class.java)
            p.tasks.register("execJarConfig", ExecJarConfigure::class.java)
            p.tasks.register("checkConfiguration") {
                it.group = "jpkg"
                it.doFirst {
                    println("is JPackage: ${checkJpackage()}")
                    println(p.jpkgExtension().type?.arg)
                    println(p.jpkgExtension().mainClass)
                    proj.configurations.forEach { println(it.name) }
                    java.sourceSets.getByName("main").output.forEach { println("$it  ${it.exists()}") }
                    println("\n\nEnvironment:")
                    p.jpkgExtension().env.forEach { t, u -> println("$t : $u") }
                }
            }
        }

        val extension = proj.jpkgExtension()

        if (proj.subprojects.isNotEmpty()) {
            proj.subprojects.forEach { subproj ->
                val ext = subproj.extensions.getByType(JpkgExtension::class.java)
                val sApp = subproj.convention.getPlugin(ApplicationPluginConvention::class.java)

                subproj.afterEvaluate { sub ->
                    if (!ext.mainClass.isNullOrBlank() || !sApp.mainClassName.isNullOrBlank()) {
                        sub.tasks.register("executableJar", ExecutableJar::class.java)
                    }
                    if (checkJpackage() && (!ext.mainClass.isNullOrBlank() || !sApp.mainClassName.isNullOrBlank()) && (ext.type != null)) {
                        sub.tasks.register("jpackageBuild", JPackageTask::class.java)
                    }
                    if(!ext.mac.signingIdentity.isNullOrBlank()) {
                        sub.tasks.register("signArchive", SignArchive::class.java)
                        sub.tasks.register("signedAppImage", SignedAppImage::class.java)
                        sub.tasks.register("signedDmg", SignedDmg::class.java)
                    }
                }
            }
        } else {
            proj.afterEvaluate { main ->
                if (!extension.mainClass.isNullOrBlank() || !app.mainClassName.isNullOrBlank()) {
                    main.tasks.register("executableJar", ExecutableJar::class.java)
                }
                if (checkJpackage() && (!extension.mainClass.isNullOrBlank() || !app.mainClassName.isNullOrBlank()) && (extension.type != null)) {
                    main.tasks.register("jpackageBuild", JPackageTask::class.java)
                }
                if(!extension.mac.signingIdentity.isNullOrBlank()) {
                    main.tasks.register("signArchive", SignArchive::class.java)
                    main.tasks.register("signedAppImage", SignedAppImage::class.java)
                    main.tasks.register("signedDmg", SignedDmg::class.java)
                }
            }
        }
    }

    fun Project.parseEnv() {
        val ext = jpkgExtension()
        File(rootProject.projectDir.path, ".env")
            .takeIf { it.exists() }
            ?.let {
                it.readLines()
                    .map { it.split("=") }
                    .map { it.first() to it.last() }
                    .toMap()
                    .forEach {
                        ext.env.put(it.key, it.value)
                    }

            }
    }

}

internal fun Project.jpkgExtension() : JpkgExtension = this.extensions.getByType(JpkgExtension::class.java)