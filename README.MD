# JPKG
> Warning: This project is not complete, use at your own risk

### Gradle plugin for creating executable distributions

- Executable "fat" jar archive
- Platform specific packages with JPackage (JDK 14 or higher required)

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

## Configuration

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
}
```

