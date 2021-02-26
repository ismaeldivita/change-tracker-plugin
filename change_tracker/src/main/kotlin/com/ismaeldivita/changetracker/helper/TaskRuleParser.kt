package com.ismaeldivita.changetracker.helper

import com.ismaeldivita.changetracker.model.TaskRule
import com.ismaeldivita.changetracker.util.getProjectsByName
import org.gradle.api.Project

object TaskRuleParser {

    fun parse(project: Project, taskRules: List<Map<String, Any>>): List<TaskRule> {
        return taskRules.map {
            TaskRule(
                task = it["task"] as String,
                alias = it["alias"] as? String,
                includeDependents = it["includeDependents"] as? Boolean ?: true,

                reevaluate = (it["reevaluate"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                whitelist = (it["whitelist"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                blacklist = (it["blacklist"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                targetPlugin = it["targetPlugin"] as? String
            )
        }
    }
}