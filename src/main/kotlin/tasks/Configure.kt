package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.JpkgPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Configure : DefaultTask() {
    @TaskAction
    fun configure() {
        val x = getTagVersionFromGit()
        JpkgPlugin.version = x?.first
        JpkgPlugin.vhash = x?.second
        JpkgPlugin.hash = getHashFromHead()
        setProjectVersionUnlessDefined()
    }

    private fun getTagVersionFromGit () : Pair<String, String>? {
        return project.projectDir.listFiles()
            ?.firstOrNull { it.isDirectory && it.name == ".git" }
            ?.let {
                File(it.path + "/refs/tags")
                    .listFiles()?.firstOrNull()?.let {
                        it.name to it.readText(Charsets.UTF_8)
                    }
            }
    }

    private fun getHashFromHead () : String? {
        return File(project.projectDir.path).listFiles()
            ?.firstOrNull { it.isDirectory && it.name == ".git" }
            ?.let {
                val cur = it.listFiles()
                    ?.firstOrNull { it.name == "HEAD" }
                    ?.readText(Charsets.UTF_8)
                File( "${it.absolutePath}/${cur?.split(" ")?.last()?.trim()}").readText(Charsets.UTF_8)
            }
    }

    private fun setProjectVersionUnlessDefined() {
        if (project.version.toString() == "unspecified" && JpkgPlugin.hash != null) {
            if (JpkgPlugin.hash == JpkgPlugin.vhash) {
                project.version = "${JpkgPlugin.version}"
            } else {
                project.version = "${JpkgPlugin.version}-${JpkgPlugin.hash?.take(5)}"
            }
        }
        println("project version is set to: ${project.version}")
        println("get tag version ${JpkgPlugin.version}")
        println("current: ${JpkgPlugin.hash}")
        println("tag: ${JpkgPlugin.vhash}")
    }

}