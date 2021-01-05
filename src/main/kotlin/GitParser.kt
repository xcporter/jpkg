package com.xcporter.jpkg

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

class GitParser(db: File) {
    val git = FileRepositoryBuilder()
        .setGitDir(db)
        .build()
        .let { Git(it) }

    val tagText : String = getLastTag()?.name?.split("/")?.lastOrNull() ?: ""
    val tagHash : String = getLastTag()?.objectId?.name ?: ""
    val head : String = findHead()

    fun findHead() : String = git.log().setMaxCount(1).call().iterator().next().name

    fun getLastTag() = git.tagList().call().firstOrNull()

    fun formatVersion() = "$tagText${ if (tagHash != head) "-${head.take(7)}" else ""}"

}