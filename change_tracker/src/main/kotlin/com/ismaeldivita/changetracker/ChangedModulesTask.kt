package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.git.JGitClient
import com.ismaeldivita.changetracker.project.ProjectDependents
import com.ismaeldivita.changetracker.project.ProjectLocator
import com.ismaeldivita.changetracker.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class ChangedModulesTask : DefaultTask() {

    private val whitelist by lazy { getProjectsByName(changeTrackerExtension.whitelist) }
    private val blacklist by lazy { getProjectsByName(changeTrackerExtension.blacklist) }
    private val reevaluate by lazy { getProjectsByName(changeTrackerExtension.reevaluate) }
    private val branch by lazy { getProperty<String>("branch") ?: changeTrackerExtension.branch }
    private val remote by lazy { getProperty<String>("remote") ?: changeTrackerExtension.remote }

    override fun getGroup(): String? = CHANGED_TRACKER_GROUP_NAME

    @TaskAction
    fun taskAction() {
        val projectDependents = ProjectDependents(rootProject)
        val locator = ProjectLocator(rootProject)
        val changedFiles = JGitClient(rootProject).getChangedFiles(branch, remote)
        val changedProjects = changedFiles.map { locator.locateProject(it) }.toSet()

        val result: MutableSet<Project> = when {
            changedProjects.contains(rootProject) -> rootProject.subprojects
            reevaluate.any(changedProjects::contains) -> rootProject.subprojects
            changedProjects.contains(null) -> rootProject.subprojects
            else -> changedProjects
                .filterNotNull()
                .map { projectDependents.getAllDependents(it) }
                .flatten()
                .toMutableSet()
        }

        result.removeAll(blacklist)
        result.addAll(whitelist)

        log(changedFiles, changedProjects, result)

        rootProject.extensions.extraProperties.set(CHANGED_TRACKER_OUTPUT, result)
    }

    private fun log(
        changedFiles: Set<String>,
        changedProjects: Set<Project?>,
        result: MutableSet<Project>
    ) {
        if (logger.isInfoEnabled) {
            if (changedFiles.isEmpty()) {
                logger.info("\nNo Changed Files")
            } else {
                logger.info("\nChanged Files")
                changedFiles.forEach { logger.info("- $it") }

                logger.info("\nChanged Projects")
                changedProjects.forEach { logger.info("- $it") }
            }
        }

        if (result.isEmpty()) {
            project.logger.quiet("\nNo Affected Projects")
        } else {
            project.logger.quiet("\nAffected Projects")
            result.forEach { it.logger.quiet("- $it") }
        }
    }

    private fun getProjectsByName(projectArgs: Set<String>): Set<Project> =
        rootProject
            .subprojects
            .filter { projectArgs.contains(it.path) }
            .toSet()
}