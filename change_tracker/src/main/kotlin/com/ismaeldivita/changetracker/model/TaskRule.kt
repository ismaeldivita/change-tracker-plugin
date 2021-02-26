package com.ismaeldivita.changetracker.model;

import org.gradle.api.Project

data class TaskRule(
    val task: String,
    val alias: String?,
    val includeDependents: Boolean,
    val reevaluate: Set<Project>,
    val whitelist: Set<Project>,
    val blacklist: Set<Project>,
    val targetPlugin: String?
)