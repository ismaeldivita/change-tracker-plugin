package com.ismaeldivita.changetracker.helper

import org.gradle.api.Project
import java.io.File

class ProjectSearch(private val rootProject: Project) {

    private val projectByPaths: List<Pair<Project, List<String>>> = rootProject
        .subprojects
        .sortedByDescending { it.depth }
        .map { subproject ->
            val relativePath = subproject.projectDir.path.removePrefix(subproject.rootDir.path)
            val paths = relativePath.split(File.separatorChar).drop(1)

            subproject to paths
        }

    fun search(filePath: String): Project? {
        val filePaths = filePath.split(File.separatorChar)

        return if (filePaths.size == 1) {
            rootProject
        } else {
            projectByPaths.find { (_, projectPaths) ->
                (projectPaths.indices).all { projectPaths[it] == filePaths[it] }
            }?.first
        }
    }
}