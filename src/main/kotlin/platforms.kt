package com.xcporter.jpkg

import java.io.File

sealed class Platform {
    var mainIcon: File? = null
    val fileIcon: File? = null
}
