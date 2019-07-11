package com.ismaeldivita.changetracker

import com.ismaeldivita.changetracker.util.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ChangeTrackerPlugin : Plugin<Project> {

    companion object {
        private const val CONFIG_TASKS = "changedModules_tasks"
    }

    override fun apply(project: Project) {
        if (!project.isRoot) {
            throw GradleException("change-tracker plugin must be applied only on the root project")
        }

        project.evaluationDependsOnChildren()

        project.afterEvaluate {
            project.tasks.create(CHANGED_MODULES_TASK_NAME, ChangedModulesTask::class.java)
            configureTasks(project)
        }
    }

    private fun configureTasks(project: Project) {
        val tasksNames = project.getProperty<List<String>>(CONFIG_TASKS) ?: emptyList()
        tasksNames.forEach {
            project.tasks.create("$it${CHANGED_MODULES_TASK_NAME.capitalize()}")
                .apply {
                    group = CHANGED_MODULES_TASK_NAME
                    dependsOn(project.tasks.findByName(CHANGED_MODULES_TASK_NAME))
                    finalizedBy(getTaskForSubProjects(it, project.subprojects))
                }
        }
    }

    private fun getTaskForSubProjects(taskName: String, subProjects: Set<Project>): List<Task> =
        subProjects.mapNotNull { subProject ->
            subProject.tasks.findByName(taskName)?.apply {
                onlyIf {
                    subProject.rootProject
                        .getExtraProperty<Set<Project>>(CHANGED_MODULES_OUTPUT)
                        ?.contains(subProject)
                        ?: false
                }
            }
        }
}