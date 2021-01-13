package com.xcporter.jpkg

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtility {

    fun unzip(source: String, destination: String) {
        val dest = File(destination)
        if (!dest.exists()) { dest.mkdir() }
        ZipInputStream(File(source).inputStream()).use { zip ->
            var entry = zip.nextEntry
            while(entry != null) {
                val path = destination + File.separator + entry.name
                if (!entry.isDirectory) extractFile(zip, path)
                else File(path).mkdirs()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    fun zip (source: String, destination: String) {
        val sourceFile = File(source)
        ZipOutputStream(File(destination).outputStream()).use {
            archiveFile(sourceFile, "", it)
        }
    }

    fun extractFile (source: ZipInputStream, destination: String) = File(destination).outputStream().buffered().use { sink ->
        source.copyTo(sink)
    }

    fun archiveFile (source: File, name: String, sink: ZipOutputStream) {
        if(source.isDirectory) {
            if(name.endsWith("/")) {
                sink.putNextEntry(ZipEntry(name))
                sink.closeEntry()
            } else {
                ZipEntry(name + File.separator).apply {
                    time = source.lastModified()
                    isDirectory
                    size = source.length()
                }.let {
                    sink.putNextEntry(it)
                }
                sink.closeEntry()
            }
            source.listFiles()?.let {
                it.forEach { archiveFile(it, "${if (!name.isNullOrBlank())name + File.separator else ""}${it.name}", sink) }
            }
            return
        }
        ZipEntry(name).apply {
            time = source.lastModified()
            isDirectory
            size = source.length()
        }.let {
            sink.putNextEntry(it)
        }
        source.inputStream().buffered().use { file ->
            file.copyTo(sink)
        }
    }
}