package com.ismaeldivita.changetracker.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.gradle.api.Project
import java.io.File

class JGitClient(project: Project) {

    private val oldPathChangeTypes = listOf(MODIFY, DELETE, RENAME)
    private val newPathChangeTypes = listOf(RENAME, COPY, ADD)

    private val repo = RepositoryBuilder()
        .setGitDir(File(project.rootDir, "/.git"))
        .readEnvironment()
        .build()

    private val git = Git(repo)

    fun getChangedFiles(branchToCompare: String): Set<String> {
        val branch = repo.resolve("refs/heads/$branchToCompare^{tree}")
        val treeParser = CanonicalTreeParser(null, repo.newObjectReader(), branch)

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

    private fun DiffEntry.shouldIncludeOldPath() = oldPathChangeTypes.contains(changeType)

    private fun DiffEntry.shouldIncludeNewPath() = newPathChangeTypes.contains(changeType)

}
