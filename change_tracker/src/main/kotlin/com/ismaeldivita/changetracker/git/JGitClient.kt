package com.ismaeldivita.changetracker.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

class JGitClient(private val project: Project) {

    private val oldPathChangeTypes = listOf(MODIFY, DELETE, RENAME)
    private val newPathChangeTypes = listOf(RENAME, COPY, ADD)

    private val repo = RepositoryBuilder()
        .setGitDir(File(project.rootDir, "/.git"))
        .readEnvironment()
        .build()

    private val git = Git(repo)

    fun getChangedFiles(branch: String): Set<String> {
        val treeParser = CanonicalTreeParser(null, repo.newObjectReader(), getBranchTree(branch))

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

        if (project.logger.isInfoEnabled) {
            project.logger.info("Changed files")
            paths.forEach { project.logger.info(it) }
        }

        return paths
    }

    private fun getBranchTree(branch: String): ObjectId {
        val branchTree: ObjectId? = repo.resolve("refs/heads/$branch^{tree}")
        return branchTree ?: throw GradleException("branch $branch not found")
    }

    private fun DiffEntry.shouldIncludeOldPath() = oldPathChangeTypes.contains(changeType)

    private fun DiffEntry.shouldIncludeNewPath() = newPathChangeTypes.contains(changeType)

}
