package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder.buildCodesign
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.ZipUtility
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ApplicationPluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SignArchive : DefaultTask() {
    @Internal
    val java = project.convention.getPlugin(JavaPluginConvention::class.java)
    @Internal
    val app = project.convention.getPlugin(ApplicationPluginConvention::class.java)

    init {
        group = "jpkg"
        dependsOn("executableJar")
    }

    @InputFile
    fun getJarFile() : File? = project.file(project.buildDir.absolutePath + "/jpkg/jar").listFiles()?.firstOrNull()


    @TaskAction
    fun action () {
        val original = getJarFile()!!

        original.let { ZipUtility.unzip(it.path, "${project.buildDir.path}/jpkg/jar/tmp") }

        val tmp = File("${project.buildDir.path}/jpkg/jar/tmp")

        tmp
            .listFiles()
            ?.filter { it.extension == "dylib" }
            ?.forEach { project.execute(buildCodesign(it.path, project)) }

        File(tmp, "ws/schild/jave/native").takeIf { it.exists() }
            ?.listFiles()
            ?.filter { it.name.contains("osx") }
            ?.forEach { project.execute(buildCodesign(it.path, project)) }

        ZipUtility.zip("${project.buildDir.path}/jpkg/jar/tmp", original.path ?: "")

        project.execute(buildCodesign(original.path, project))

        tmp.deleteRecursively()

    }
}