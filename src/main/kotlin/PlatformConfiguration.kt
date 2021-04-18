package com.xcporter.jpkg

import org.gradle.api.Project
import java.util.*

sealed class PlatformConfiguration(val project: Project) {
    var type: JpkgExtension.DistType? = null
    var icon: String? = null
    var name: String? = null
    var fileAssociations: String? = null
    var resourceDir: String? = null

    fun type(new: String) {
        JpkgExtension.DistType.values()
            .firstOrNull { it.arg == new }
            ?.let {
                type = it
            }
    }

    fun env(name: String) : String? = project.jpkgExtension().env[name]

    class Mac(project: Project) : PlatformConfiguration(project) {
        var sign: Boolean = false
        var signingIdentity: String? = null
        var bundleName: String? = null
        var userName: String? = null
        var password: String? = null
        var notarization : UUID? = null
    }

    class Linux(project: Project) : PlatformConfiguration(project) {
        var menuGroup: String? = null
        var shortcut: Boolean? = true
        var maintainer: String? = null
        var packageDependencies: MutableList<String>? = null
        var release: String? = null
        var category: String? = null
    }

    class Windows(project: Project) : PlatformConfiguration(project) {
        var winDirChooser: Boolean = true
        var winPerUser: Boolean = false
        var winMenu: Boolean = true
        var menuGroup: String? = null
        var shortcut: Boolean? = true
    }
}