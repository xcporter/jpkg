package com.xcporter.jpkg

enum class JPackageArgs(val arg: String) {
    TYPE("--type"),
    VERSION("--app-version"),
    COPYRIGHT("--copyright"),
    DESCRIPTION("--description"),
    NAME("--name"),
    DESTINATION("--dest"),
    TEMP("--temp"),
    VENDOR("--vendor"),
    ICON("--icon"),
    INPUT("--input"),
    MAIN_JAR("--main-jar"),
    FILE_ASSOCIATIONS("--file-associations");

    enum class Mac(val arg: String) {
        NAME("--mac-package-name")
    }
    enum class Windows(val arg: String) {
        DIR_CHOOSER("--win-dir-chooser"),
        MENU("--win-menu"),
        MENU_GROUP("--win-menu-group"),
        PER_USER("--win-per-user-install"),
        SHORTCUT("--win-shortcut")
    }
    enum class Linux(val arg: String) {
        NAME("--linux-package-name"),
        MAINTAINER("--linux-deb-maintainer"),
        MENU_GROUP("--linux-menu-group"),
        DEPENDENCIES("--linux-package-deps"),
        SHORTCUT("--linux-shortcut"),
        RELEASE("--linux-app-release"),
        CATEGORY("--linux-app-category")
    }
}