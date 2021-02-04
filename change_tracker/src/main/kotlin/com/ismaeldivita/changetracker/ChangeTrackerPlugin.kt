package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.helper.ProjectDependents
import com.ismaeldivita.changetracker.helper.TaskRuleParser
import com.ismaeldivita.changetracker.model.TaskRule
import com.ismaeldivita.changetracker.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ChangeTrackerPlugin : Plugin<Project> {

    private lateinit var projectDependents: ProjectDependents

    override fun apply(project: Project) {

        check(project.isRoot) { "change-tracker plugin must be applied only on the root project" }

        projectDependents = ProjectDependents(project)

        project.extensions.create(CHANGE_TRACKER_EXTENSION, ChangeTrackerExtension::class.java)

        project.afterEvaluate {
            it.tasks.create(CHANGED_TRACKER_MODULES_TASK_NAME, ChangedModulesTask::class.java)
            configureTasks(it)
            configureRules(it)
        }

    }

    private fun configureRules(project: Project) {
        val rules = TaskRuleParser.parse(project, project.changeTrackerExtension.taskRules)

        rules.forEach { rule ->
            val taskName =
                rule.alias ?: "${rule.task}${CHANGED_TRACKER_MODULES_TASK_NAME.capitalize()}"

            project.tasks.register(taskName) { task ->
                task.group = CHANGED_TRACKER_GROUP_NAME
                task.doFirst { logAffectedProjects(project, rule) }
                task.dependsOn(project.tasks.findByName(CHANGED_TRACKER_MODULES_TASK_NAME))
                task.finalizedBy(getTaskForSubProjects(project, rule))
            }
        }
    }

    private fun configureTasks(project: Project) {
        val extension = project.changeTrackerExtension
        val whitelist = project.getProjectsByName(extension.whitelist)
        val blacklist = project.getProjectsByName(extension.blacklist)
        val reevaluate = project.getProjectsByName(extension.reevaluate)

        extension.tasks.forEach { taskName ->
            project.tasks.register("$taskName${CHANGED_TRACKER_MODULES_TASK_NAME.capitalize()}") { task ->
                val rule = TaskRule(
                    task = taskName,
                    alias = null,
                    includeDependents = true,
                    reevaluate = reevaluate,
                    whitelist = whitelist,
                    blacklist = blacklist,
                    targetPlugins = setOf()
                )

                task.group = CHANGED_TRACKER_GROUP_NAME
                task.dependsOn(project.tasks.findByName(CHANGED_TRACKER_MODULES_TASK_NAME))
                task.finalizedBy(getTaskForSubProjects(project, rule))
            }
        }
    }

    private fun getTaskForSubProjects(project: Project, rule: TaskRule): List<Task> {
        return project.subprojects.mapNotNull { subProject ->
            val task = subProject.tasks.findByName(rule.task)
            if (task != null) {
                task.apply {
                    onlyIf {
                        getAffectedModulesForTask(project, rule)
                            ?.contains(subProject)
                            ?: true
                    }
                }
            } else {
                subProject.logger.info("ChangeTracker - Task [$rule.task] not found for $subProject")
                null
            }
        }
    }


    private fun getAffectedModulesForTask(project: Project, rule: TaskRule): Set<Project>? {
        val rootProject = project.rootProject

        val changedProjects = project.getExtraProperty<Set<Project?>>(CHANGED_TRACKER_OUTPUT)
            ?: return null

        val result: MutableSet<Project> = when {
            changedProjects.contains(rootProject) -> rootProject.subprojects
            rule.reevaluate.any(changedProjects::contains) -> rootProject.subprojects
            changedProjects.contains(null) -> rootProject.subprojects
            else -> changedProjects
                .filterNotNull()
                .map {
                    if (rule.includeDependents) {
                        projectDependents.getAllDependents(it)
                    } else {
                        setOf(it)
                    }
                }
                .flatten()
                .toMutableSet()
        }

        result.removeAll(rule.blacklist)
        result.addAll(rule.whitelist)

        rule.targetPlugins.forEach { plugin ->
            result.removeIf { !it.pluginManager.hasPlugin(plugin) }
        }

        return result
    }

    private fun logAffectedProjects(project: Project, rule: TaskRule) {
        val result = getAffectedModulesForTask(project, rule) ?: return

        if (result.isEmpty()) {
            project.logger.quiet("\nNo Affected Projects")
        } else {
            project.logger.quiet("\nAffected Projects")
            result.forEach { it.logger.quiet("- $it") }
        }
    }
}