package com.ismaeldivita.changetracker

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.Serializable

open class ChangeTrackerExtension : Serializable {

    var tasks = emptySet<String>()
    var whitelist = emptySet<Project>()
    var blacklist = emptySet<Project>()
    var branch = "master"

}