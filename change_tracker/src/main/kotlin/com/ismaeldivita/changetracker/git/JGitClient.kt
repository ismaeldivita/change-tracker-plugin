package com.ismaeldivita.changetracker.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD
import org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY
import org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE
import org.eclipse.jgit.diff.DiffEntry.ChangeType.RENAME
import org.eclipse.jgit.diff.DiffEntry.ChangeType.COPY
import org.eclipse.jgit.lib.Constants.R_REMOTES
import org.eclipse.jgit.lib.Constants.TYPE_TREE
import org.eclipse.jgit.lib.Constants.HEAD
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Constants.R_HEADS
import java.io.File
import java.lang.Exception

class JGitClient(project: Project) {

    private val oldPathChangeTypes = listOf(MODIFY, DELETE, RENAME)
    private val newPathChangeTypes = listOf(RENAME, COPY, ADD)

    private val repo = RepositoryBuilder()
        .setGitDir(File(project.rootDir, "/.git"))
        .readEnvironment()
        .build()

    private val git = Git(repo)

    fun getChangedFiles(branch: String, remote: String?, useMergeBaseDiff: Boolean): Set<String> {
        val oldTree = getOldTree(branch, remote, useMergeBaseDiff)

        val diffs = git
            .diff()
            .setOldTree(oldTree)
            .call()

        return mapPathFromDiffEntries(diffs)
    }

    private fun mapPathFromDiffEntries(diffs: List<DiffEntry>): Set<String> {
        val paths = mutableSetOf<String>()

        diffs.forEach {
            if (it.shouldIncludeOldPath()) paths.add(it.oldPath)
            if (it.shouldIncludeNewPath()) paths.add(it.newPath)
        }

        return paths
    }

    private fun getOldTree(
        branch: String,
        remote: String?,
        useMergeBaseDiff: Boolean
    ): AbstractTreeIterator = try {
        val reader = repo.newObjectReader()
        val rw = RevWalk(repo)

        val reference = if (remote.isNullOrBlank()) {
            "$R_HEADS$branch"
        } else {
            "$R_REMOTES$remote/$branch"
        }

        if (useMergeBaseDiff) {
            val commitA: RevCommit = rw.parseCommit(repo.resolve(reference))
            val commitB: RevCommit = rw.parseCommit(repo.resolve(HEAD))

            rw.markStart(commitA)
            rw.markStart(commitB)
            rw.revFilter = RevFilter.MERGE_BASE
            val base = rw.next()
            CanonicalTreeParser().apply { reset(reader, base.tree) }
        } else {
            val objId = repo.resolve("$reference^{$TYPE_TREE}")
            CanonicalTreeParser(null, reader, objId)
        }
    } catch (error: Exception) {
        throw GradleException("Branch ${remote.orEmpty()} $branch not found")
    }

    private fun DiffEntry.shouldIncludeOldPath() = oldPathChangeTypes.contains(changeType)

    private fun DiffEntry.shouldIncludeNewPath() = newPathChangeTypes.contains(changeType)
}
