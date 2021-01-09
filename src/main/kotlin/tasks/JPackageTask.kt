package com.xcporter.jpkg.tasks

import com.xcporter.jpkg.JPackageArgs
import com.xcporter.jpkg.JpkgExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import java.io.ByteArrayOutputStream
import java.io.File

open class JPackageTask : DefaultTask() {
    val ext = project.extensions.getByType(JpkgExtension::class.java)

    init {
        group = "jpkg"
        dependsOn.add(project.tasks.getByName("executableJar"))
    }

    @TaskAction
    fun execute() {
        val cmd = buildCommand()
        println(cmd.joinToString(" "))
        project.exec {
            it.commandLine = cmd
            it.standardOutput = System.out
        }
    }

    companion object {
        fun getJpackage() : String {
            val home = System.getProperty("java.home") ?: throw Exception("Java Home is not set")
            return with(home + File.separator + "bin" + File.separator + "jpackage") {
                if(File(this).exists()) this
                else throw Exception("jpackage not found: is java home set to jdk 14 or higher?")
            }
        }

        fun checkJpackage() = try { getJpackage(); true} catch(e: Throwable) { false }
    }

    fun buildCommand() : List<String> {
        val acc = mutableListOf<String>(getJpackage())
//        Mandatory config
        acc.addAll(
            listOf(
                JPackageArgs.TYPE.arg, ext.type!!.arg,
                JPackageArgs.VERSION.arg, (ext.appVersion ?: project.version as String),
                JPackageArgs.INPUT.arg, project.file(project.buildDir.absolutePath + "/jpkg/jar").absolutePath,
                JPackageArgs.MAIN_JAR.arg, project.file(project.buildDir.absolutePath + "/jpkg/jar").listFiles().first().name,
                JPackageArgs.DESTINATION.arg, ext.destination!!
            )
        )
//        General Config
        with (acc) {
            ext.packageName?.let {
                add(JPackageArgs.NAME.arg)
                add(it)
            }
            ext.copyright?.let {
                add(JPackageArgs.COPYRIGHT.arg)
                add(it)
            }
            ext.description?.let {
                add(JPackageArgs.DESCRIPTION.arg)
                add(it)
            }
            ext.icon?.let {
                add(JPackageArgs.ICON.arg)
                add(it)
            }
            ext.vendor?.let {
                add(JPackageArgs.VENDOR.arg)
                add(it)
            }
            ext.fileAssociations?.let {
                add(JPackageArgs.FILE_ASSOCIATIONS.arg)
                add(it)
            }
        }
//        Platform Config
        when {
            OperatingSystem.current().isMacOsX -> {
                ext.mac.name?.let {
                    acc.add(JPackageArgs.Mac.NAME.arg)
                    acc.add(it)
                } ?: ext.packageName?.let {
                    acc.add(JPackageArgs.Mac.NAME.arg)
                    acc.add(it)
                }
            }
            OperatingSystem.current().isWindows -> {
                if (ext.win.winDirChooser) acc.add(JPackageArgs.Windows.DIR_CHOOSER.arg)
                if (ext.win.winPerUser) acc.add(JPackageArgs.Windows.PER_USER.arg)
                if (ext.win.winMenu) acc.add(JPackageArgs.Windows.MENU.arg)
                if (ext.win.shortcut == true || (ext.win.shortcut == null && ext.shortcut == true))
                    acc.add(JPackageArgs.Windows.SHORTCUT.arg)
                ext.win.menuGroup?.let {
                    acc.add(JPackageArgs.Windows.MENU_GROUP.arg)
                    acc.add(it)
                } ?: ext.menuGroup?.let {
                    acc.add(JPackageArgs.Windows.MENU_GROUP.arg)
                    acc.add(it)
                }
            }
            OperatingSystem.current().isLinux -> {
                ext.linux.name?.let {
                    acc.add(JPackageArgs.Linux.NAME.arg)
                    acc.add(it)
                } ?: ext.packageName?.let {
                    acc.add(JPackageArgs.Linux.NAME.arg)
                    acc.add(it)
                }
                ext.linux.maintainer?.let {
                    acc.add(JPackageArgs.Linux.MAINTAINER.arg)
                    acc.add(it)
                } ?: ext.vendor?.let {
                    acc.add(JPackageArgs.Linux.MAINTAINER.arg)
                    acc.add(it)
                }
                ext.linux.menuGroup?.let {
                    acc.add(JPackageArgs.Linux.MENU_GROUP.arg)
                    acc.add(it)
                } ?: ext.menuGroup?.let {
                    acc.add(JPackageArgs.Linux.MENU_GROUP.arg)
                    acc.add(it)
                }
                ext.linux.packageDependencies?.let {
                    acc.add(JPackageArgs.Linux.DEPENDENCIES.arg)
                    acc.addAll(it)
                }
                ext.linux.release?.let {
                    acc.add(JPackageArgs.Linux.RELEASE.arg)
                    acc.add(it)
                } ?: ext.project.version.takeUnless { it == "unspecified" }?.let {
                    acc.add(JPackageArgs.Linux.RELEASE.arg)
                    acc.add(it as String)
                }
                ext.linux.category?.let {
                    acc.add(JPackageArgs.Linux.CATEGORY.arg)
                    acc.add(it)
                }
                if (ext.linux.shortcut == true || (ext.linux.shortcut == null && ext.shortcut == true))
                    acc.add(JPackageArgs.Linux.SHORTCUT.arg)
            }
        }
        return acc
    }


}