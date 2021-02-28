package com.xcporter.jpkg

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File

object CmdBuilder {
    fun Project.execute(cmd: List<String>) {
        this.exec {
            it.commandLine = cmd
            it.standardOutput = System.out
            it.errorOutput = System.err
        }
        println(cmd.joinToString(" "))
    }

    fun getJpackage() : String {
        var home = System.getenv("JAVA_HOME") ?: throw Exception("Java Home is not set")
        println("java_home: $home")
        if (!home.endsWith(File.separator)) home = "$home${File.separator}"
        return with(home + "bin${File.separator}${if(OperatingSystem.current().isWindows) "jpackage.exe" else "jpackage"}") {
            if(File(this).exists()) this
            else throw Exception("jpackage not found: is java home set to jdk 14 or higher?")
        }
    }

    fun checkJpackage() = try { getJpackage(); true} catch(e: Throwable) { println(e); false }

    fun buildCodesignCommand(path: String, project: Project) : List<String> {
        val acc = mutableListOf<String>()
        val ext = project.extensions.getByType(JpkgExtension::class.java)
        acc.addAll(
            listOf(
                "codesign",
                "--preserve-metadata=entitlements",
                "-f",
                "--options",
                "runtime",
                "-s",
                ext.mac.signingIdentity ?: "",
                path
            )
        )
        return acc
    }

    fun buildJpackageImageCommand(project: Project) : List<String> {
        val ext = project.extensions.getByType(JpkgExtension::class.java)
        val acc = mutableListOf<String>(getJpackage())
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
            ext.vendor?.let {
                add(JPackageArgs.VENDOR.arg)
                add(it)
            }
            ext.resourceDir?.let {
                add(JPackageArgs.RESOURCE_DIR.arg)
                add(it)
            }
            if (ext.verbose) add("--verbose")
        }

        acc.addAll(
            listOf(
                JPackageArgs.TYPE.arg, JpkgExtension.DistType.DMG.arg,
                JPackageArgs.VERSION.arg, (ext.appVersion ?: project.version as String),
                JPackageArgs.DESTINATION.arg, ext.destination!!,
                JPackageArgs.Mac.APP_IMAGE.arg, "${project.buildDir.path}/jpkg/mac/${ext.packageName}.app"
            )
        )
        return acc
    }

    fun buildJpackageJarCommand(project: Project) : List<String> {
        val ext = project.extensions.getByType(JpkgExtension::class.java)
        val acc = mutableListOf<String>(getJpackage())
//        Mandatory config
        acc.addAll(
            listOf(
                JPackageArgs.TYPE.arg, ext.type!!.arg,
                JPackageArgs.VERSION.arg, (ext.appVersion ?: project.version as String),
                JPackageArgs.INPUT.arg, project.file(project.buildDir.absolutePath + "/jpkg/jar").absolutePath,
                JPackageArgs.MAIN_JAR.arg, project.file(project.buildDir.absolutePath + "/jpkg/jar").listFiles()?.first()?.name ?: "",
                JPackageArgs.DESTINATION.arg, ext.destination!!
            )
        )
//        General Config
        with (acc) {
            ext.packageName?.let {
                add(JPackageArgs.NAME.arg)
                acc.add(it)
            }
            ext.copyright?.let {
                add(JPackageArgs.COPYRIGHT.arg)
                add("\"$it\"")
            }
            ext.description?.let {
                add(JPackageArgs.DESCRIPTION.arg)
                add("\"$it\"")
            }
            ext.icon?.let {
                add(JPackageArgs.ICON.arg)
                add(it)
            }
            ext.vendor?.let {
                add(JPackageArgs.VENDOR.arg)
                add("\"$it\"")
            }
            ext.fileAssociations?.let {
                add(JPackageArgs.FILE_ASSOCIATIONS.arg)
                add(it)
            }
            ext.resourceDir?.let {
                add(JPackageArgs.RESOURCE_DIR.arg)
                add(it)
            }
            if (ext.verbose) add("--verbose")
        }
//        Platform Config
        when {
            OperatingSystem.current().isMacOsX -> {
                ext.mac.name?.let {
                    acc.add(JPackageArgs.Mac.NAME.arg)
                    acc.add("$it")
                } ?: ext.packageName?.let {
                    acc.add(JPackageArgs.Mac.NAME.arg)
                    acc.add("$it")
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
                    acc.add("\"$it\"")
                } ?: ext.menuGroup?.let {
                    acc.add(JPackageArgs.Windows.MENU_GROUP.arg)
                    acc.add("\"$it\"")
                }
            }
            OperatingSystem.current().isLinux -> {
                ext.linux.name?.let {
                    acc.add(JPackageArgs.Linux.NAME.arg)
                    acc.add("\"$it\"")
                }
                ext.linux.maintainer?.let {
                    acc.add(JPackageArgs.Linux.MAINTAINER.arg)
                    acc.add("\"$it\"")
                } ?: ext.vendor?.let {
                    acc.add(JPackageArgs.Linux.MAINTAINER.arg)
                    acc.add("\"$it\"")
                }
                ext.linux.menuGroup?.let {
                    acc.add(JPackageArgs.Linux.MENU_GROUP.arg)
                    acc.add("\"$it\"")
                } ?: ext.menuGroup?.let {
                    acc.add(JPackageArgs.Linux.MENU_GROUP.arg)
                    acc.add("\"$it\"")
                }
                ext.linux.packageDependencies?.let {
                    acc.add(JPackageArgs.Linux.DEPENDENCIES.arg)
                    acc.add(it.joinToString(", "))
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
                    acc.add("\"$it\"")
                }
                if (ext.linux.shortcut == true || (ext.linux.shortcut == null && ext.shortcut == true))
                    acc.add(JPackageArgs.Linux.SHORTCUT.arg)
            }
        }
        return acc
    }
}