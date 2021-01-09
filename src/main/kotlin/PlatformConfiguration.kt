package com.xcporter.jpkg

sealed class PlatformConfiguration {
    var type: JpkgExtension.DistType? = null
    var icon: String? = null
    var name: String? = null

    class Mac() : PlatformConfiguration() {
        var signingIdentity: String? = null
        var userName: String? = null
        var password: String? = null
    }

    class Linux() : PlatformConfiguration() {
        var menuGroup: String? = null
        var shortcut: Boolean? = true
        var maintainer: String? = null
        var packageDependencies: MutableList<String>? = null
        var release: String? = null
        var category: String? = null
    }

    class Windows() : PlatformConfiguration() {
        var winDirChooser: Boolean = true
        var winPerUser: Boolean = false
        var winMenu: Boolean = true
        var menuGroup: String? = null
        var shortcut: Boolean? = true
    }
}