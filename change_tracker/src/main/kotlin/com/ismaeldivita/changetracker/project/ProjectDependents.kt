package com.ismaeldivita.changetracker.project

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class ProjectDependents(rootProject: Project) {

    private val dependents = rootProject
        .subprojects
        .map { it to mutableSetOf<Project>() }
        .toMap()
        .apply { traverseDependencies() }

    fun getAllDependents(project: Project): Set<Project> {
        val dependents = dependents[project] ?: emptySet<Project>()
        return setOf(project) + dependents + dependents.map { getAllDependents(it) }.flatten().toSet()
    }

    private fun Map<Project, MutableSet<Project>>.traverseDependencies() {
        forEach { subProject, _ ->
            subProject.configurations.forEach { config ->
                config
                    .dependencies
                    .filterIsInstance<ProjectDependency>()
                    .forEach { getValue(it.dependencyProject).add(subProject) }
            }
        }
    }

}