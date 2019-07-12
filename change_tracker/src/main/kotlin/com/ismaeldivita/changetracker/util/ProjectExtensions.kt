package com.ismaeldivita.changetracker.util

import com.ismaeldivita.changetracker.ChangeTrackerExtension
import org.gradle.api.Project

val Project.isRoot get() = this == rootProject

@Suppress("UNCHECKED_CAST")
fun <T> Project.getExtraProperty(key: String): T? = extensions.extraProperties[key] as T?

val Project.changeTrackerExtension: ChangeTrackerExtension
    get() = extensions.getByName(CHANGE_TRACKER_EXTENSION) as ChangeTrackerExtension
