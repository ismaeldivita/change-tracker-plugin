package com.ismaeldivita.changetracker.util

import org.gradle.api.Project
import org.gradle.api.Task

val Task.rootProject: Project get() = project.rootProject

fun <T> Task.getProperty(key: String): T? = project.getProperty<T>(key)
