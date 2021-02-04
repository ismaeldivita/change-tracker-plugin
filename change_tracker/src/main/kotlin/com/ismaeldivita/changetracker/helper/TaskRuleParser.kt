package com.ismaeldivita.changetracker.helper

import com.ismaeldivita.changetracker.model.TaskRule
import com.ismaeldivita.changetracker.util.getProjectsByName
import org.gradle.api.Project

object TaskRuleParser {

    fun parse(project: Project, taskRules: List<LinkedHashMap<String, Any>>): List<TaskRule> {
        return taskRules.map {
            TaskRule(
                task = it["task"] as String,
                alias = it["alias"] as? String,
                includeDependents = it["includeDependents"] as? Boolean ?: true,

                reevaluate = (it["reevaluate"] as? ArrayList<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                whitelist = (it["whitelist"] as? ArrayList<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                blacklist = (it["blacklist"] as? ArrayList<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?.let(project::getProjectsByName)
                    ?: emptySet(),

                targetPlugins = (it["targetPlugins"] as? ArrayList<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?: emptySet()
            )
        }
    }
}