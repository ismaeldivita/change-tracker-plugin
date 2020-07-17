package com.ismaeldivita.changetracker

import java.io.Serializable

open class ChangeTrackerExtension : Serializable {

    var tasks: Set<String> = emptySet<String>()
    var whitelist: Set<String> = emptySet<String>()
    var blacklist: Set<String> = emptySet<String>()
    var reevaluate: Set<String> = emptySet<String>()
    var branch: String = "master"
    var remote: String? = null
    var useMergeBaseDiff: Boolean = true
}
