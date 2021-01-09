package com.xcporter.jpkg

import groovy.lang.Closure
import groovy.lang.Closure.DELEGATE_FIRST
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.util.ConfigureUtil

open class JpkgExtension(val project: Project) {
    var useVersionFromGit: Boolean = false
    var packageName: String? = null
    var mainClass: String? = null

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

//    Platform Parameters
    val mac = PlatformConfiguration.Mac()
    val win = PlatformConfiguration.Windows()
    val linux = PlatformConfiguration.Linux()

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
        }
    }

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