package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.util.CHANGED_TRACKER_GROUP_NAME
import com.ismaeldivita.changetracker.util.isRoot
import com.ismaeldivita.changetracker.util.CHANGE_TRACKER_EXTENSION
import com.ismaeldivita.changetracker.util.CHANGED_TRACKER_MODULES_TASK_NAME
import com.ismaeldivita.changetracker.util.CHANGED_TRACKER_OUTPUT
import com.ismaeldivita.changetracker.util.changeTrackerExtension
import com.ismaeldivita.changetracker.util.getExtraProperty
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

        project.afterEvaluate {
            it.tasks.create(CHANGED_TRACKER_MODULES_TASK_NAME, ChangedModulesTask::class.java)
            configureTasks(it)
        }
    }

    private fun configureTasks(project: Project) {
        val tasksNames = project.changeTrackerExtension.tasks

        tasksNames.forEach {
            project.tasks.register("$it${CHANGED_TRACKER_MODULES_TASK_NAME.capitalize()}") { task ->
                task.group = CHANGED_TRACKER_GROUP_NAME
                task.dependsOn(project.tasks.findByName(CHANGED_TRACKER_MODULES_TASK_NAME))
                task.finalizedBy(getTaskForSubProjects(it, project.subprojects))
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
                            ?: true
                    }
                }
            } else {
                subProject.logger.info("ChangeTracker - Task [$taskName] not found for $subProject")
                null
            }
        }
}