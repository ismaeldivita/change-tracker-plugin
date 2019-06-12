package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.git.JGitClient
import com.ismaeldivita.changetracker.project.ProjectDependents
import com.ismaeldivita.changetracker.project.ProjectLocator
import com.ismaeldivita.changetracker.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class ChangedModulesTask : DefaultTask() {

    companion object {
        private const val CONFIG_WHITELIST = "changedModules_whitelist"
        private const val CONFIG_BLACKLIST = "changedModules_blacklist"
        private const val CONFIG_DEFAULT_BRANCH = "changedModules_defaultBranch"

        private const val CMD_LINE_PROPERTY_BRANCH = "branch"
        private const val FALLBACK_BRANCH = "master"
    }

    private val whitelist by lazy { getProjectsByName(rootProject.getProperty(CONFIG_WHITELIST)) }
    private val blacklist by lazy { getProjectsByName(rootProject.getProperty(CONFIG_BLACKLIST)) }
    private val defaultBranch by lazy { getProperty(CONFIG_DEFAULT_BRANCH) ?: FALLBACK_BRANCH }
    private val cmdLinePropertyBranch by lazy { getProperty<String>(CMD_LINE_PROPERTY_BRANCH) }

    override fun getGroup(): String? = CHANGED_MODULES_GROUP_NAME

    @TaskAction
    fun taskAction() {
        val projectDependents = ProjectDependents(rootProject)
        val locator = ProjectLocator(rootProject)
        val changedFiles = JGitClient(rootProject)
            .getChangedFiles(cmdLinePropertyBranch ?: defaultBranch)

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

        rootProject.extensions.extraProperties.set(CHANGED_MODULES_OUTPUT, result)
    }

    private fun getProjectsByName(projectArgs: List<String>?): Set<Project> =
        projectArgs?.let { args ->
            rootProject.subprojects
                .filter { args.contains(it.path) }
                .toSet()
        } ?: emptySet()
}