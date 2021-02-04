package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.git.JGitClient
import com.ismaeldivita.changetracker.helper.ProjectLocator
import com.ismaeldivita.changetracker.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ChangedModulesTask : DefaultTask() {

    override fun getGroup(): String? = CHANGED_TRACKER_GROUP_NAME

    @TaskAction
    fun taskAction() {
        val branch = getProperty<String>("branch") ?: changeTrackerExtension.branch
        val remote = getProperty<String>("remote") ?: changeTrackerExtension.remote
        val useMergeBranchDiff = changeTrackerExtension.useMergeBaseDiff

        val locator = ProjectLocator(rootProject)
        val changedFiles = JGitClient(rootProject).getChangedFiles(
            branch = branch,
            remote = remote,
            useMergeBaseDiff = useMergeBranchDiff
        )
        val changedProjects = changedFiles.map { locator.locateProject(it) }.toSet()

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