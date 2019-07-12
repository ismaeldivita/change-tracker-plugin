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
    private val branch by lazy { changeTrackerExtension.branch }

    override fun getGroup(): String? = CHANGED_TRACKER_GROUP_NAME

    @TaskAction
    fun taskAction() {
        val projectDependents = ProjectDependents(rootProject)
        val locator = ProjectLocator(rootProject)
        val changedFiles = JGitClient(rootProject).getChangedFiles(branch)

        val changedProjects = changedFiles.map { locator.locateProject(it) }

        val result: MutableSet<Project> = when {
            changedProjects.contains(rootProject) -> rootProject.subprojects
            changedProjects.contains(null) -> rootProject.subprojects
            else -> changedProjects
                .filterNotNull()
                .map { projectDependents.getAllDependents(it) }
                .flatten()
                .toMutableSet()
        }

        result.removeAll(blacklist)
        result.addAll(whitelist)
        rootProject.extensions.extraProperties.set(CHANGED_TRACKER_OUTPUT, result)
    }

    private fun getProjectsByName(projectArgs: Set<String>): Set<Project> =
        projectArgs.let { args ->
            rootProject.subprojects
                .filter { args.contains(it.path) }
                .toSet()
        }
}