package com.ismaeldivita.changetracker

import java.io.Serializable

open class ChangeTrackerExtension : Serializable {

    var tasks: Set<String> = emptySet()
    var whitelist: Set<String> = emptySet()
    var blacklist: Set<String> = emptySet()
    var reevaluate: Set<String> = emptySet()
    var branch: String = "master"
    var remote: String? = null
    var useMergeBaseDiff: Boolean = true
    var taskRules: List<LinkedHashMap<String, Any>> = listOf()
}
