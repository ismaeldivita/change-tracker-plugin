package com.ismaeldivita.changetracker.helper

import org.gradle.api.Project
import java.io.File

class ProjectLocator(private val rootProject: Project) {

    private val projects = rootProject
        .subprojects
        .sortedByDescending { it.depth }
        .map { subproject ->
            val relativePath = subproject.projectDir.path.removePrefix(subproject.rootDir.path)
            val paths = relativePath.split(File.separatorChar).drop(1)

            subproject to paths
        }

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