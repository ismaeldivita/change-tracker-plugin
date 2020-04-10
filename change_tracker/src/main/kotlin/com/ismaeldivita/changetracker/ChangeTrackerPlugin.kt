package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ChangeTrackerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.isRoot) {
            throw GradleException("change-tracker plugin must be applied only on the root project")
        }

        project.extensions.create(CHANGE_TRACKER_EXTENSION, ChangeTrackerExtension::class.java)
        project.evaluationDependsOnChildrenRecursive()

        project.afterEvaluate {
            it.tasks.create(CHANGED_TRACKER_MODULES_TASK_NAME, ChangedModulesTask::class.java)
            configureTasks(it)
        }
    }

    private fun configureTasks(project: Project) {
        val tasksNames = project.changeTrackerExtension.tasks

        tasksNames.forEach {
            project.tasks.create("$it${CHANGED_TRACKER_MODULES_TASK_NAME.capitalize()}")
                .apply {
                    group = CHANGED_TRACKER_GROUP_NAME
                    dependsOn(project.tasks.findByName(CHANGED_TRACKER_MODULES_TASK_NAME))
                    finalizedBy(getTaskForSubProjects(it, project.subprojects))
                }
        }
    }

    private fun getTaskForSubProjects(taskName: String, subProjects: Set<Project>): List<Task> =
        subProjects.mapNotNull { subProject ->
            val task = subProject.tasks.findByName(taskName)
            if (task != null) {
                task.apply {
                    onlyIf {
                        subProject.rootProject
                            .getExtraProperty<Set<Project>>(CHANGED_TRACKER_OUTPUT)
                            ?.contains(subProject)
                            ?: false
                    }
                }
            } else {
                subProject.logger.quiet("ChangeTracker - Task [$taskName] not found for $subProject")
                null
            }
        }
}