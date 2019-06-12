package com.ismaeldivita.changetracker.util

import org.gradle.api.Project

val Project.isRoot get() = this == rootProject

@Suppress("UNCHECKED_CAST")
fun <T> Project.getProperty(key: String): T? = properties[key] as T?

@Suppress("UNCHECKED_CAST")
fun <T> Project.getExtraProperty(key: String): T? = extensions.extraProperties[key] as T?