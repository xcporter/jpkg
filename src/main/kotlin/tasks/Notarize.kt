package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.CmdBuilder
import com.xcporter.jpkg.CmdBuilder.execute
import com.xcporter.jpkg.JpkgExtension
import com.xcporter.jpkg.jpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

open class Notarize : DefaultTask() {
    @Internal
    var target : File? = null
    @Internal
    val ext = project.jpkgExtension()

    init {
        group = "jpkg"
        when(ext.type) {
            JpkgExtension.DistType.DMG -> {
                target =
                    project.file("${project.buildDir.path}/jpkg/mac").listFiles()
                        ?.firstOrNull { it.name.endsWith(".dmg") }
            }
            JpkgExtension.DistType.MAC_APP -> {
                target =
                    project.file("${project.buildDir.path}/jpkg/mac").listFiles()
                        ?.firstOrNull { it.name.endsWith(".app") }
            }
        }
    }

    @InputFiles
    fun getInputFiles(): Array<File>? = project.file(project.buildDir.absolutePath + "/jpkg/mac").listFiles()

    @TaskAction
    fun notarize() {
        var retry = true
        var attempts = 0
        println("[Notary] uploading...")
        submit()?.let {
            ext.mac.notarization = UUID.fromString(it.split('=')[1].trim())
            println("[Notary] Package successfully uploaded: ${ext.mac.notarization}")
        }
        while(retry) {
            Thread.sleep(30000)
            check()?.let {
                NotarizationStatus.fromString(it).also {
                    print("\nattempt: $attempts\n$it\n")
                }.let { res ->
                    when(res.status) {
                        "success" -> {
                            retry = false
                            println("[Notary] Notarization successful! Stapling...")
                            println(staple())
                        }
                        "in progress" -> {}
                        else -> {
                            retry = false
                            println("[Notary] Error: \n$res")
                        }
                    }
                }
            } ?: run {
                retry = false
                println("[Notary] status check failed, cancelling")
            }
            attempts++
        }
    }

    fun submit() : String? {
        return target?.let {
            try {
                project.execute(CmdBuilder.buildNotarizeSubmit(it.path, project))
            } catch(e: Throwable) { e.message }
        } ?: null.also { println("[Notary] Warning: Target file not found; will not be notarized") }
    }

    fun check() : String? {
        return ext.mac.notarization?.let {
            try {
                project.execute(CmdBuilder.buildNotarizeStatus(project))
            } catch(e: Throwable) { println("[Notary] Warning: Notarization ID not found, cancelling\n"); e.message }
        } ?: null.also { println("[Notary] Warning: Notarization ID not found, cancelling") }
    }

    fun staple() : String? {
        return target?.let {
            try {
                project.execute(CmdBuilder.buildStaple(it.path, project))
            } catch(e: Throwable) { println("[Notary] Warning: Unable to staple notarization\n"); e.message }
        } ?: null.also { println("[Notary] Warning: Unable to staple notarization") }
    }

    class NotarizationStatus (val status: String?, val message: String?, val logLink: String?) {
        companion object {
            fun fromString(str: String) : NotarizationStatus = NotarizationStatus(
                str.parse("Status:"),
                str.parse("Status Message:"),
                str.parse("LogFileURL:")
            )

            private fun String.parse(label: String) : String? = Regex("($label\\s)(.+)(\\n|$)").find(this)?.groupValues?.get(2)

        }

        override fun toString(): String =
            """
                Status: ${status}
                ${message?.let {"Message: $it"} ?: ""}
                ${logLink?.let {"LogURL: $it"} ?: ""}
            """.trimIndent()

    }

}