package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.git.JGitClient
import com.ismaeldivita.changetracker.helper.ProjectSearch
import com.ismaeldivita.changetracker.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ChangedModulesTask : DefaultTask() {

    @TaskAction
    fun taskAction() {
        val branch = getProperty("branch") ?: changeTrackerExtension.branch
        val remote = getProperty("remote") ?: changeTrackerExtension.remote
        val useMergeBranchDiff = changeTrackerExtension.useMergeBaseDiff

        val locator = ProjectSearch(rootProject)
        val changedFiles = JGitClient(rootProject).getChangedFiles(
            branch = branch,
            remote = remote,
            useMergeBaseDiff = useMergeBranchDiff
        )
        val changedProjects = changedFiles.map { locator.search(it) }.toSet()

        if (changedFiles.isEmpty()) {
            logger.info("\nNo Changed Files")
        } else {
            logger.info("\nChanged Files")
            changedFiles.forEach { logger.info("- $it") }
            logger.info("\nChanged Projects")
            changedProjects.forEach { logger.info("- $it") }
        }

        rootProject.extensions.extraProperties.set(CHANGED_TRACKER_OUTPUT, changedProjects)
    }
}