package com.xcporter.jpkg

import groovy.lang.Closure
import groovy.lang.Closure.DELEGATE_FIRST
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

open class JpkgExtension(val project: Project) {
    var useVersionFromGit: Boolean = false
    var packageName: String? = null
    var mainClass: String? = null

    val env = mutableMapOf<String, String>()
//    Jpackage Args
    var appVersion: String? = null
    var description: String? = null
    var destination: String? = null
    var copyright: String? = null
    var vendor: String? = null
    var fileAssociations: String? = null
    var icon: String? = null
    var type: DistType? = null
    var resourceDir: String? = null
    var menuGroup: String? = null
    var shortcut: Boolean? = null
    var verbose: Boolean = false

//    Platform Parameters
    val mac = PlatformConfiguration.Mac(project)
    val win = PlatformConfiguration.Windows(project)
    val linux = PlatformConfiguration.Linux(project)

    fun mac(op: Closure<Unit>) {
        if (OperatingSystem.current().isMacOsX) {
            op.apply {
                resolveStrategy = DELEGATE_FIRST
                delegate = mac
                call()
            }
            mac.type?.let { type = it } ?: run { type = DistType.DMG }
            destination = project.file(project.buildDir.absolutePath + "/jpkg/mac").absolutePath
            mac.icon?.let { icon = it }
            mac.fileAssociations?.let { fileAssociations = it }
            mac.resourceDir?.let { resourceDir = it }
        }
    }

    fun windows(op: Closure<Unit>) {
        if (OperatingSystem.current().isWindows) {
            op.apply {
                resolveStrategy = DELEGATE_FIRST
                delegate = win
                call()
            }
            win.type?.let { type = it } ?: run { type = DistType.MSI }
            destination = project.file(project.buildDir.absolutePath + "/jpkg/win").absolutePath
            win.icon?.let { icon = it }
            win.fileAssociations?.let { fileAssociations = it }
            win.resourceDir?.let { resourceDir = it }
        }
    }

    fun linux(op: Closure<Unit>) {
        if (OperatingSystem.current().isLinux) {
            op.apply {
                resolveStrategy = DELEGATE_FIRST
                delegate = linux
                call()
            }
            linux.type?.let { type = it } ?: run { type = DistType.DEB }
            destination = project.file(project.buildDir.absolutePath + "/jpkg/linux").absolutePath
            linux.icon?.let { icon = it }
            linux.fileAssociations?.let { fileAssociations = it }
            linux.resourceDir?.let { resourceDir = it }
        }
    }

    fun env(name: String) : String? = env[name]

    enum class DistType (val arg: String) {
        MAC_APP("app-image"),
        EXE("exe"),
        MSI("msi"),
        RPM("rpm"),
        DEB("deb"),
        PKG("pkg"),
        DMG("dmg")
    }
}