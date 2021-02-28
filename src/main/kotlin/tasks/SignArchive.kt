package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder.buildCodesignCommand
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.ZipUtility
import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SignArchive : DefaultTask() {
    val ext = project.jpkgExtension()

    val java = project.convention.getPlugin(JavaPluginConvention::class.java)
    val app = project.convention.getPlugin(ApplicationPluginConvention::class.java)

    init {
        group = "jpkg"
        dependsOn("executableJar")
    }


    @TaskAction
    fun action () {
        val original = File("${project.buildDir.path}/jpkg/jar/").listFiles()
            ?.firstOrNull { it.extension == "jar" }!!

        original.let { ZipUtility.unzip(it.path, "${project.buildDir.path}/jpkg/jar/tmp") }

        val tmp = File("${project.buildDir.path}/jpkg/jar/tmp")
        tmp
            .listFiles()
            ?.filter { it.extension == "dylib" }
            ?.forEach { project.execute(buildCodesignCommand(it.path, project)) }

        File(tmp, "ws/schild/jave/native").takeIf { it.exists() }
            ?.listFiles()
            ?.filter { it.name.contains("osx") }
            ?.forEach { project.execute(buildCodesignCommand(it.path, project)) }

        ZipUtility.zip("${project.buildDir.path}/jpkg/jar/tmp", original.path ?: "")

        project.execute(buildCodesignCommand(original.path, project))

        tmp.deleteRecursively()

    }
}