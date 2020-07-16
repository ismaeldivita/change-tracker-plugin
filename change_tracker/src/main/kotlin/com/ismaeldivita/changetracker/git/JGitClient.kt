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
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Constants.R_HEADS
import java.io.File

class JGitClient(project: Project) {

    private val oldPathChangeTypes = listOf(MODIFY, DELETE, RENAME)
    private val newPathChangeTypes = listOf(RENAME, COPY, ADD)

    private val repo = RepositoryBuilder()
        .setGitDir(File(project.rootDir, "/.git"))
        .readEnvironment()
        .build()

    private val git = Git(repo)

    fun getChangedFiles(branch: String, remote: String?): Set<String> {
        val treeParser = CanonicalTreeParser(
            null,
            repo.newObjectReader(),
            getBranchTree(branch, remote)
        )

        val diffs = git
            .diff()
            .setOldTree(treeParser)
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

    private fun getBranchTree(branch: String, remote: String?): ObjectId {
        val reference = if (remote.isNullOrBlank()) {
            "$R_HEADS$branch^{$TYPE_TREE}"
        } else {
            "$R_REMOTES$remote/$branch^{$TYPE_TREE}"
        }

        return repo.resolve(reference) ?: throw GradleException("branch $remote $branch not found")
    }

    private fun DiffEntry.shouldIncludeOldPath() = oldPathChangeTypes.contains(changeType)

    private fun DiffEntry.shouldIncludeNewPath() = newPathChangeTypes.contains(changeType)
}
