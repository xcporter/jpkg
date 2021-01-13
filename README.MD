# JPKG
> Warning: This project is not complete, use at your own risk

### Gradle plugin for creating executable distributions

- Executable "fat" jar archive
- Platform specific packages with JPackage (JDK 14 or higher required)
- use local toolset to codesign and notarize apple packages for gatekeeper

## Installation

```groovy
// Not yet published to a public repository!
plugins {
    id 'com.xcporter.jpkg' version '0.0.1'
}
```

## Tasks

### `executableJar`
Creates jar file with runtime classpath dependencies included.
This task is not added to a project unless `mainClassName` is defined in 
the project, or a value is supplied for `mainClass` in the configuration block. 

### `gitVersion`

Apply last tag from git repo (plus beginning of current commit hash if different than tagged commit) to project version

### `jpackageRun`

Run jpackage command for current platform

### `signArchive`

unzips, signs libraries, then rezips results of `executableJar` task

### `signedAppImage`

Builds and signs mac app image from signed jar file. 

### `signedDmg`

Builds and signs dmg from the results of `signedAppImage`

## Configuration

No package tasks will be created for your project unless a `mainClassName` is specified 
at the project level, or else `mainClass` in the jpkg configuration closure is specified.

Additionally `signingIdentity` must be present for codesigning tasks to be available, and 
`userName`, `password`, and `appBundle` must be listed to notarize.

```groovy
//default values
jpkg {
//    When true, jpkg looks for a git repo
//    and takes the version from the most recent tag it finds
//    (similar to git describe --tags)
    useVersionFromGit = false
    
//    override file name for artifacts
    packageName: String = null
    
//    needed for all tasks; can be set here or at project level
    mainClass: String = null

    packageName = "YourAppName"
    vendor = "Your Name"
    copyright = "2021"
    description = "Your App Description"
    
//    platform level configuration
//    each can specify custom type, icon, or name per platform
//    to configure type use function type() which accepts a string
//    that's also a valid jpackage argument, otherwise platform defaults are:
//    mac : dmg
//    windows : msi
//    linux : deb
    mac {
        name = "Mac Package Name (how it appears in menu bar)"
        icon = "path to icon"
//        code sign and notarization configuration
        signingIdentity = "Your dev certificate identity"
        bundleName = "mac-package-identifier for notarization"
        userName: String
        password: String
    }
    
    windows {
        winDirChooser: Boolean = true
        winPerUser: Boolean = false
        winMenu: Boolean = true
        menuGroup: String? = null
        shortcut: Boolean? = true
    }
    
    linux {
        menuGroup: String? = null
        shortcut: Boolean? = true
        maintainer: String? = null
        packageDependencies: MutableList<String>? = null
        release: String? = null
        category: String? = null
    }
    
}
```
