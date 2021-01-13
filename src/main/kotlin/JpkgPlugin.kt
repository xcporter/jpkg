package com.xcporter.jpkg

import com.xcporter.jpkg.CmdBuilder.checkJpackage
import com.xcporter.jpkg.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention

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
            p.tasks.register("gitVersion", GitVersion::class.java)
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
                    if (checkJpackage() && (!ext.mainClass.isNullOrBlank() || !sApp.mainClassName.isNullOrBlank()) && (extension.type != null)) {
                        sub.tasks.register("jpackageBuild", JPackageTask::class.java)
                    }
                    if(!extension.mac.signingIdentity.isNullOrBlank()) {
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
                    main.tasks.register("jpackageRun", JPackageTask::class.java)
                }
                if(!extension.mac.signingIdentity.isNullOrBlank()) {
                    main.tasks.register("signArchive", SignArchive::class.java)
                    main.tasks.register("signedAppImage", SignedAppImage::class.java)
                    main.tasks.register("signedDmg", SignedDmg::class.java)
                }
            }
        }
    }


}

internal fun Project.jpkgExtension() : JpkgExtension = this.extensions.getByType(JpkgExtension::class.java)