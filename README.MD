# JPKG
> Warning: This project is not thoroughly tested, use at your own risk

### Gradle plugin for creating executable distributions

- Executable "fat" jar archive
- Platform specific packages with JPackage (JDK 14 or higher required)
- use local toolset to codesign and notarize apple packages for gatekeeper

## Installation

```groovy
plugins {
    id 'com.xcporter.jpkg' version '0.0.7'
}
```

## Tasks

### `executableJar`
Creates jar file with runtime classpath dependencies included.
This task is not added to a project unless `mainClassName` is defined in 
the project, or a value is supplied for `mainClass` in the configuration block. 

### `gitVersion`

Apply last tag from git repo (plus beginning of current commit hash if different than tagged commit) to project version

### `jpackageBuild`

Run jpackage command for current platform

### `signArchive`

unzips, signs libraries, then rezips results of `executableJar` task

### `signedAppImage`

Builds and signs mac app image from signed jar file. 

### `signedDmg`

Builds and signs dmg from the results of `signedAppImage`

### `notarize`

Submits to apple notarization server and staples if successful. Because this task waits 
for the apple server before stapling, it may be idle for more than an hour. For this reason 
I've left it independent of execution order, so you must manually make sure the `signedDmg` or 
`signedAppImage` task has already run. 

Secrets must be entered in build file or specified via a .env file and the `env()` function

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
    
    runtimeImage = "path/to/custom/jre"

//    global menu-group; applies only to windows and linux
    menuGroup = "Your Group"
    
//    print all commands for outside utilities
    verbose: Boolean = false
    
//    print all commands and pass verbose flag to all utilities
    deeplyVerbose: Boolean = false

//    Platform level configuration
//    each can specify custom type, icon, or name per platform
//    to configure type use function type() which accepts a string
//    that's also a valid jpackage argument, otherwise platform defaults are:
//    mac : dmg
//    windows : msi
//    linux : deb
    mac {
        name = "Mac Package Name (how it appears in menu bar)"
        icon = "path to icon"

//      code sign and notarization configuration
        signingIdentity = "Your dev certificate identity"
        bundleName = "mac-package-identifier for notarization"

//      A function env() is available to parse in secrets stored in a file
//        named '.env' stored in the project root, one per line with the form
//        'USERNAME=*********'
        userName = env('USERNAME')
        password: env('PASSWORD')
    
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

Note: When working with the `resourcesDir` parameter, perhaps for the purpose of customizing a mac disk image,  filenames must match the project name,
following the format laid out in jpackage docs: https://docs.oracle.com/en/java/javase/15/jpackage/packaging-tool-user-guide.pdf
