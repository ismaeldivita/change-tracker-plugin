package com.ismaeldivita.changetracker.project

import org.gradle.api.Project
import java.io.File

class ProjectLocator(private val rootProject: Project) {

    private val projects = rootProject
        .subprojects
        .sortedByDescending { it.depth }
        .map { it to it.path.split(":").drop(1) }

    fun locateProject(filePath: String): Project? {
        val filePaths = filePath.split(File.separatorChar)

        return if (filePaths.size == 1) {
            rootProject
        } else {
            projects.find { (_, projectPaths) ->
                (projectPaths.indices).all { projectPaths[it] == filePaths[it] }
            }?.first
        }
    }

}